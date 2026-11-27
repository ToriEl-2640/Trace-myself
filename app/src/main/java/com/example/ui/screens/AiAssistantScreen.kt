package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.OnPeachContainer
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.TraceAmberTertiary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceEmerald
import com.example.ui.theme.TraceErrorContainer
import com.example.ui.theme.TraceOnErrorContainer
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfaceHighlight
import com.example.ui.theme.TraceSurfacePeach
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.AssistantTool
import com.example.ui.viewmodel.ChatMessage
import com.example.ui.viewmodel.ToolExecutionState

@Composable
fun AiAssistantScreen(
    viewModel: AiAssistantViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Specialized Toolkits, 1: Consultant Chat

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TraceAmbientBackgroundBrush)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TraceTealPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = TraceTealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "AI Research Assistant",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TraceTextPrimary
                        )
                        Text(
                            text = "Powered by Gemini 3.8 Flash & Firebase AI Logic",
                            style = MaterialTheme.typography.labelSmall,
                            color = TraceCyanSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub Mode Tab
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TraceSurfaceVariant,
                contentColor = TraceTealPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = TraceTealPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Research Toolkits",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Research Consultant Chat",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        if (selectedTab == 0) {
            ToolkitsTabContent(viewModel)
        } else {
            ConsultantChatTabContent(viewModel)
        }
    }
}

@Composable
private fun ToolkitsTabContent(viewModel: AiAssistantViewModel) {
    val selectedTool by viewModel.selectedTool.collectAsState()
    val toolInput by viewModel.toolInput.collectAsState()
    val executionState by viewModel.toolExecutionState.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Tool Selector Pills
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(AssistantTool.entries.filter { it != AssistantTool.CONSULTANT }) { tool ->
                val isSelected = selectedTool == tool
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) TraceTealPrimary else TraceSurface)
                        .clickable { viewModel.selectTool(tool) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = tool.label,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) OnGreen else TraceTextSecondary
                    )
                }
            }
        }

        // Quick Sample Fillers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TRY SAMPLE CONTEXT",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TraceTextMuted
            )
            Text(
                text = "Tap to paste sample",
                style = MaterialTheme.typography.labelSmall,
                color = TraceTealPrimary,
                modifier = Modifier.clickable {
                    when (selectedTool) {
                        AssistantTool.SIMPLIFY -> viewModel.setQuickSample(
                            "\"The efficacy of decentralized zeolitic evaporative cooling matrixes in suppressing post-harvest ethylene ripening pathways was quantified across horticultural cultivars in semi-arid zones. Results demonstrated statistically significant reduction in respiration coefficients (p < 0.01) under diurnal temperature differentials exceeding 14K.\""
                        )
                        AssistantTool.GAPS -> viewModel.setQuickSample(
                            "Small food producers in West Africa spend up to 35% of total cost on imported multilayer plastic packaging pouches. Local bans on single-use plastics lack accessible biodegradable substitutes for dry food shelf-life."
                        )
                        AssistantTool.HYPOTHESIS -> viewModel.setQuickSample(
                            "How can farmer cooperatives reduce tomato spoilage on 12-hour transport trucks between Northern farms and coastal urban markets without multimillion-dollar refrigerated trucks?"
                        )
                        AssistantTool.SURVEY -> viewModel.setQuickSample(
                            "Investigating why tomato farmers in Kano reject standard plastic collapsible crates and continue using wooden raffia baskets."
                        )
                        else -> {}
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = toolInput,
            onValueChange = { viewModel.onToolInputChanged(it) },
            placeholder = { Text(selectedTool.promptHint, color = TraceTextMuted) },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .testTag("ai_tool_input"),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = { viewModel.runSelectedTool() },
            enabled = toolInput.isNotBlank() && executionState !is ToolExecutionState.Streaming,
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("run_ai_tool_button")
        ) {
            if (executionState is ToolExecutionState.Streaming) {
                CircularProgressIndicator(
                    color = OnGreen,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Analyzing with Gemini...", fontWeight = FontWeight.Bold)
            } else {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Scientific Breakdown", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Output Result Card
        when (val state = executionState) {
            is ToolExecutionState.Finished -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TraceSurface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, TraceCyanSecondary)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TRACE Synthesis",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TraceCyanSecondary
                            )
                            Row {
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(state.output))
                                    Toast.makeText(context, "Copied analysis to clipboard!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TraceTextSecondary)
                                }
                                IconButton(onClick = {
                                    viewModel.saveOutputToLibrary(
                                        title = "${selectedTool.label}: ${toolInput.take(40)}",
                                        content = state.output,
                                        onComplete = {
                                            Toast.makeText(context, "Saved to Research Library!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Library", tint = TraceTealPrimary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = state.output,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TraceTextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
            is ToolExecutionState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TraceErrorContainer)
                ) {
                    Text(
                        text = "Error: ${state.message}",
                        color = TraceOnErrorContainer,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun ConsultantChatTabContent(viewModel: AiAssistantViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val chatInput by viewModel.chatInput.collectAsState()
    val isGenerating by viewModel.isChatGenerating.collectAsState()

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }
        }

        // Suggested Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                listOf(
                    "How to calculate sample size for a market survey?",
                    "Explain smart contract escrow for agriculture in simple terms",
                    "What makes a winning Gates Foundation grant proposal?",
                    "How to validate packaging moisture barrier with low-cost tools?"
                )
            ) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(TraceSurfacePeach)
                        .border(1.dp, PeachSoft, RoundedCornerShape(16.dp))
                        .clickable { viewModel.sendChatMessage(prompt) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnPeachContainer
                    )
                }
            }
        }

        // Input Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = chatInput,
                onValueChange = { viewModel.onChatInputChanged(it) },
                placeholder = { Text("Ask any research question...", color = TraceTextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_field"),
                maxLines = 4,
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { viewModel.sendChatMessage() },
                enabled = chatInput.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (chatInput.isNotBlank() && !isGenerating) TraceTealPrimary else TraceSurfaceVariant)
                    .testTag("send_chat_message_button")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        color = TraceTealPrimary,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (chatInput.isNotBlank()) OnGreen else TraceTextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(TraceTealPrimary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = TraceTealPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) GreenPrimary else TraceSurface
            ),
            border = if (isUser) null else androidx.compose.foundation.BorderStroke(1.dp, TraceOutline),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Text(
                text = message.content.ifBlank { "..." },
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) OnGreen else TraceTextPrimary,
                modifier = Modifier.padding(14.dp)
            )
        }
    }
}
