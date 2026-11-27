package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FieldEvidence
import com.example.data.model.ProblemInvestigation
import com.example.data.repository.AiResearchRepository
import com.example.data.repository.AiResult
import com.example.data.repository.TraceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.flow.map

sealed interface ProblemsUiState {
    data object Loading : ProblemsUiState
    data class Success(val problems: List<ProblemInvestigation>) : ProblemsUiState
    data class Error(val message: String) : ProblemsUiState
}

sealed interface AiAssistState {
    data object Idle : AiAssistState
    data object Generating : AiAssistState
    data class Completed(val result: String, val targetField: String) : AiAssistState
    data class Error(val message: String) : AiAssistState
}

class ProblemsLabViewModel(
    private val repository: TraceRepository,
    private val aiRepository: AiResearchRepository
) : ViewModel() {

    val problemsState: StateFlow<ProblemsUiState> = repository.observeProblems()
        .map<List<ProblemInvestigation>, ProblemsUiState> { ProblemsUiState.Success(it) }
        .catch { e -> emit(ProblemsUiState.Error(e.message ?: "Failed to load investigations")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ProblemsUiState.Loading
        )

    private val _selectedProblem = MutableStateFlow<ProblemInvestigation?>(null)
    val selectedProblem: StateFlow<ProblemInvestigation?> = _selectedProblem.asStateFlow()

    private val _evidenceList = MutableStateFlow<List<FieldEvidence>>(emptyList())
    val evidenceList: StateFlow<List<FieldEvidence>> = _evidenceList.asStateFlow()

    private val _aiAssistState = MutableStateFlow<AiAssistState>(AiAssistState.Idle)
    val aiAssistState: StateFlow<AiAssistState> = _aiAssistState.asStateFlow()

    private val _selectedSectorFilter = MutableStateFlow("All")
    val selectedSectorFilter: StateFlow<String> = _selectedSectorFilter.asStateFlow()

    fun setSectorFilter(sector: String) {
        _selectedSectorFilter.value = sector
    }

    fun selectProblem(problem: ProblemInvestigation) {
        _selectedProblem.value = problem
        observeEvidenceForProblem(problem.id)
    }

    fun clearSelectedProblem() {
        _selectedProblem.value = null
        _evidenceList.value = emptyList()
        _aiAssistState.value = AiAssistState.Idle
    }

    private fun observeEvidenceForProblem(problemId: String) {
        viewModelScope.launch {
            repository.observeEvidence(problemId).collect { list ->
                _evidenceList.value = list
            }
        }
    }

    fun createNewProblem(
        title: String,
        sector: String,
        location: String,
        problemStatement: String,
        tags: List<String>
    ) {
        viewModelScope.launch {
            val newId = repository.createProblem(title, sector, location, problemStatement, tags)
            val created = repository.getProblemById(newId)
            created?.let { selectProblem(it) }
        }
    }

    fun updateCurrentProblemStep(
        status: String? = null,
        problemStatement: String? = null,
        existingResearch: String? = null,
        unaddressedGaps: String? = null,
        evidenceNotes: String? = null,
        proposedSolutions: String? = null,
        opportunitySummary: String? = null
    ) {
        val current = _selectedProblem.value ?: return
        viewModelScope.launch {
            repository.updateProblemStep(
                problemId = current.id,
                status = status,
                problemStatement = problemStatement,
                existingResearch = existingResearch,
                unaddressedGaps = unaddressedGaps,
                evidenceNotes = evidenceNotes,
                proposedSolutions = proposedSolutions,
                opportunitySummary = opportunitySummary
            )
            // Reload updated problem
            _selectedProblem.value = repository.getProblemById(current.id)
        }
    }

    fun addFieldEvidence(
        title: String,
        evidenceType: String,
        content: String,
        dataValue: String,
        locationTag: String
    ) {
        val current = _selectedProblem.value ?: return
        viewModelScope.launch {
            repository.addEvidence(
                problemId = current.id,
                title = title,
                evidenceType = evidenceType,
                content = content,
                dataValue = dataValue,
                locationTag = locationTag
            )
        }
    }

    fun deleteProblem(problemId: String) {
        viewModelScope.launch {
            repository.deleteProblem(problemId)
            if (_selectedProblem.value?.id == problemId) {
                clearSelectedProblem()
            }
        }
    }

    // --- AI Research Accelerators ---

    fun requestAiResearchGaps() {
        val current = _selectedProblem.value ?: return
        _aiAssistState.value = AiAssistState.Generating
        viewModelScope.launch {
            when (val result = aiRepository.identifyResearchGaps(current.title, current.problemStatement, current.sector)) {
                is AiResult.Success -> {
                    _aiAssistState.value = AiAssistState.Completed(result.data, "unaddressedGaps")
                }
                is AiResult.Error -> {
                    _aiAssistState.value = AiAssistState.Error(result.message)
                }
            }
        }
    }

    fun requestAiHypothesisAndMethodology() {
        val current = _selectedProblem.value ?: return
        _aiAssistState.value = AiAssistState.Generating
        viewModelScope.launch {
            when (val result = aiRepository.generateHypothesisAndMethodology(current.problemStatement.ifBlank { current.title })) {
                is AiResult.Success -> {
                    _aiAssistState.value = AiAssistState.Completed(result.data, "methodology")
                }
                is AiResult.Error -> {
                    _aiAssistState.value = AiAssistState.Error(result.message)
                }
            }
        }
    }

    fun requestAiFieldSurvey() {
        val current = _selectedProblem.value ?: return
        _aiAssistState.value = AiAssistState.Generating
        viewModelScope.launch {
            val audience = "Local community members, producers, and small business owners in ${current.location.ifBlank { "rural markets" }}"
            when (val result = aiRepository.generateFieldSurveyQuestions(current.title, audience)) {
                is AiResult.Success -> {
                    _aiAssistState.value = AiAssistState.Completed(result.data, "survey")
                }
                is AiResult.Error -> {
                    _aiAssistState.value = AiAssistState.Error(result.message)
                }
            }
        }
    }

    fun requestAiExecutiveReport() {
        val current = _selectedProblem.value ?: return
        _aiAssistState.value = AiAssistState.Generating
        viewModelScope.launch {
            when (val result = aiRepository.draftResearchExecutiveSummary(current, _evidenceList.value)) {
                is AiResult.Success -> {
                    _aiAssistState.value = AiAssistState.Completed(result.data, "opportunitySummary")
                }
                is AiResult.Error -> {
                    _aiAssistState.value = AiAssistState.Error(result.message)
                }
            }
        }
    }

    fun applyAiResultToField(field: String, text: String) {
        when (field) {
            "unaddressedGaps" -> updateCurrentProblemStep(unaddressedGaps = text)
            "evidenceNotes" -> updateCurrentProblemStep(evidenceNotes = text)
            "proposedSolutions" -> updateCurrentProblemStep(proposedSolutions = text)
            "opportunitySummary" -> updateCurrentProblemStep(opportunitySummary = text, status = "completed")
            "methodology" -> {
                val current = _selectedProblem.value
                val updatedNotes = if (current?.evidenceNotes.isNullOrBlank()) text else "${current?.evidenceNotes}\n\n--- Methodology Plan ---\n$text"
                updateCurrentProblemStep(evidenceNotes = updatedNotes)
            }
            "survey" -> {
                val current = _selectedProblem.value
                val updatedNotes = if (current?.evidenceNotes.isNullOrBlank()) text else "${current?.evidenceNotes}\n\n--- Field Survey Template ---\n$text"
                updateCurrentProblemStep(evidenceNotes = updatedNotes)
            }
        }
        _aiAssistState.value = AiAssistState.Idle
    }

    fun dismissAiAssist() {
        _aiAssistState.value = AiAssistState.Idle
    }
}
