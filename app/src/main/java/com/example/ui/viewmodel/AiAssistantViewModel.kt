package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AiResearchRepository
import com.example.data.repository.AiResult
import com.example.data.repository.TraceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AssistantTool(val label: String, val promptHint: String) {
    SIMPLIFY("Simplify Paper", "Paste an abstract, dense academic paper excerpt, or technical report to translate into plain, actionable language..."),
    GAPS("Find Gaps", "Describe a community problem or sector challenge to find what researchers and policies have overlooked..."),
    HYPOTHESIS("Hypothesis & Plan", "State your research question or observation to generate a testable hypothesis and low-cost field methodology..."),
    SURVEY("Survey Questions", "Enter your target community and problem to generate an 8-question field survey..."),
    CONSULTANT("Free Research Chat", "Ask any research question, methodology doubt, or data collection strategy...")
}

data class ChatMessage(
    val id: String,
    val sender: String, // "user" or "model"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface ToolExecutionState {
    data object Idle : ToolExecutionState
    data class Streaming(val partial: String) : ToolExecutionState
    data class Finished(val output: String) : ToolExecutionState
    data class Error(val message: String) : ToolExecutionState
}

class AiAssistantViewModel(
    private val aiRepository: AiResearchRepository,
    private val traceRepository: TraceRepository
) : ViewModel() {

    private val _selectedTool = MutableStateFlow(AssistantTool.SIMPLIFY)
    val selectedTool: StateFlow<AssistantTool> = _selectedTool.asStateFlow()

    private val _toolInput = MutableStateFlow("")
    val toolInput: StateFlow<String> = _toolInput.asStateFlow()

    private val _toolExecutionState = MutableStateFlow<ToolExecutionState>(ToolExecutionState.Idle)
    val toolExecutionState: StateFlow<ToolExecutionState> = _toolExecutionState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome",
                sender = "model",
                content = "Welcome to the TRACE AI Research Assistant. I can help you decode complicated scientific papers into plain language, identify unaddressed research gaps, formulate testable field hypotheses, or design community survey templates. How can I help with your investigation today?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isChatGenerating = MutableStateFlow(false)
    val isChatGenerating: StateFlow<Boolean> = _isChatGenerating.asStateFlow()

    fun selectTool(tool: AssistantTool) {
        _selectedTool.value = tool
        _toolExecutionState.value = ToolExecutionState.Idle
    }

    fun onToolInputChanged(input: String) {
        _toolInput.value = input
    }

    fun onChatInputChanged(input: String) {
        _chatInput.value = input
    }

    fun setQuickSample(sampleText: String) {
        _toolInput.value = sampleText
    }

    fun runSelectedTool() {
        val input = _toolInput.value.trim()
        if (input.isBlank()) return

        _toolExecutionState.value = ToolExecutionState.Streaming("")

        viewModelScope.launch {
            when (_selectedTool.value) {
                AssistantTool.SIMPLIFY -> {
                    when (val res = aiRepository.simplifyResearchPaper(input)) {
                        is AiResult.Success -> _toolExecutionState.value = ToolExecutionState.Finished(res.data)
                        is AiResult.Error -> _toolExecutionState.value = ToolExecutionState.Error(res.message)
                    }
                }
                AssistantTool.GAPS -> {
                    when (val res = aiRepository.identifyResearchGaps("Community Problem", input, "Cross-sector")) {
                        is AiResult.Success -> _toolExecutionState.value = ToolExecutionState.Finished(res.data)
                        is AiResult.Error -> _toolExecutionState.value = ToolExecutionState.Error(res.message)
                    }
                }
                AssistantTool.HYPOTHESIS -> {
                    when (val res = aiRepository.generateHypothesisAndMethodology(input)) {
                        is AiResult.Success -> _toolExecutionState.value = ToolExecutionState.Finished(res.data)
                        is AiResult.Error -> _toolExecutionState.value = ToolExecutionState.Error(res.message)
                    }
                }
                AssistantTool.SURVEY -> {
                    when (val res = aiRepository.generateFieldSurveyQuestions(input, "Local community stakeholders")) {
                        is AiResult.Success -> _toolExecutionState.value = ToolExecutionState.Finished(res.data)
                        is AiResult.Error -> _toolExecutionState.value = ToolExecutionState.Error(res.message)
                    }
                }
                AssistantTool.CONSULTANT -> {
                    sendChatMessage(input)
                }
            }
        }
    }

    fun sendChatMessage(customPrompt: String? = null) {
        val text = customPrompt ?: _chatInput.value.trim()
        if (text.isBlank() || _isChatGenerating.value) return

        val userMsg = ChatMessage(
            id = "usr_${System.currentTimeMillis()}",
            sender = "user",
            content = text
        )
        val currentList = _chatMessages.value + userMsg
        _chatMessages.value = currentList
        if (customPrompt == null) _chatInput.value = ""

        _isChatGenerating.value = true

        val modelMsgId = "mod_${System.currentTimeMillis()}"
        val initialModelMsg = ChatMessage(id = modelMsgId, sender = "model", content = "")
        _chatMessages.value = currentList + initialModelMsg

        viewModelScope.launch {
            val contextSummary = "TRACE Research Engine. Assist independent researchers with scientific rigor in simple language."
            val res = aiRepository.streamResearchChat(contextSummary, text) { chunk ->
                _chatMessages.value = _chatMessages.value.map {
                    if (it.id == modelMsgId) it.copy(content = chunk) else it
                }
            }
            if (res is AiResult.Error) {
                _chatMessages.value = _chatMessages.value.map {
                    if (it.id == modelMsgId) it.copy(content = "Error: ${res.message}") else it
                }
            }
            _isChatGenerating.value = false
        }
    }

    fun saveOutputToLibrary(title: String, content: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            traceRepository.addLibraryItem(
                title = title.take(80),
                source = "TRACE AI Research Analysis",
                simplifiedExplanation = content,
                summary = content.take(300),
                domain = "Applied AI Research"
            )
            onComplete()
        }
    }
}
