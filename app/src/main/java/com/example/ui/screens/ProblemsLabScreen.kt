package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FieldEvidence
import com.example.data.model.ProblemInvestigation
import com.example.ui.components.ResearchDashboard
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.OnPeach
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.TraceAmberTertiary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceEmerald
import com.example.ui.theme.TraceErrorContainer
import com.example.ui.theme.TraceOnErrorContainer
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceOutlineVariant
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfacePeach
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.AiAssistState
import com.example.ui.viewmodel.ProblemsLabViewModel

val SECTORS = listOf(
    "All",
    "Agriculture & Food",
    "Packaging & Materials",
    "Education & Books",
    "Waste & Sanitation",
    "Fintech & Blockchain",
    "Healthcare"
)

val LAB_STEPS = listOf(
    "1. Understand",
    "2. Research",
    "3. Gaps",
    "4. Evidence",
    "5. Solutions",
    "6. Document"
)

@Composable
fun ProblemsLabScreen(
    viewModel: ProblemsLabViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProblem by viewModel.selectedProblem.collectAsState()
    val problemsState by viewModel.problemsState.collectAsState()
    val sectorFilter by viewModel.selectedSectorFilter.collectAsState()
    val evidenceList by viewModel.evidenceList.collectAsState()
    val aiAssistState by viewModel.aiAssistState.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }

    if (selectedProblem != null) {
        BackHandler {
            viewModel.clearSelectedProblem()
        }
        ProblemDetailView(
            problem = selectedProblem!!,
            evidenceList = evidenceList,
            aiAssistState = aiAssistState,
            onBack = { viewModel.clearSelectedProblem() },
            viewModel = viewModel
        )
    } else {
        ResearchDashboard(
            problemsState = problemsState,
            sectors = SECTORS,
            selectedSector = sectorFilter,
            onSelectSector = { viewModel.setSectorFilter(it) },
            onProjectClick = { viewModel.selectProblem(it) },
            onDeleteProject = { viewModel.deleteProblem(it) },
            onStartNewEverydayProblem = { showCreateDialog = true },
            modifier = modifier
        )

        if (showCreateDialog) {
            CreateProblemDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { title, sector, location, statement, tags ->
                    viewModel.createNewProblem(title, sector, location, statement, tags)
                    showCreateDialog = false
                }
            )
        }
    }
}

// --- Detail View: The 6-Step Guided Methodology ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProblemDetailView(
    problem: ProblemInvestigation,
    evidenceList: List<FieldEvidence>,
    aiAssistState: AiAssistState,
    onBack: () -> Unit,
    viewModel: ProblemsLabViewModel
) {
    var selectedStepIndex by remember { mutableIntStateOf(0) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var showAddEvidenceDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TraceAmbientBackgroundBrush)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TraceTextPrimary
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = problem.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TraceTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${problem.sector} • ${problem.location.ifBlank { "Everyday Lab" }}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TraceTextSecondary
                )
            }
            IconButton(
                onClick = {
                    val summaryText = buildString {
                        append("TRACE Research Brief: ${problem.title}\n\n")
                        append("Sector: ${problem.sector} | Location: ${problem.location}\n\n")
                        append("Problem Statement:\n${problem.problemStatement}\n\n")
                        append("Existing Research:\n${problem.existingResearch}\n\n")
                        append("Gaps:\n${problem.unaddressedGaps}\n\n")
                        append("Evidence Count: ${evidenceList.size}\n")
                        evidenceList.forEach { append("- ${it.title}: ${it.content} (${it.dataValue})\n") }
                        append("\nProposed Solutions:\n${problem.proposedSolutions}\n\n")
                        append("Executive Summary & Pitch:\n${problem.opportunitySummary}\n")
                    }
                    clipboardManager.setText(AnnotatedString(summaryText))
                    Toast.makeText(context, "Full Research Brief copied to clipboard!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share / Copy Brief",
                    tint = TraceTealPrimary
                )
            }
        }

        // Stepper Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedStepIndex,
            containerColor = TraceSurfaceVariant,
            contentColor = TraceTealPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedStepIndex]),
                    color = TraceTealPrimary
                )
            }
        ) {
            LAB_STEPS.forEachIndexed { index, stepTitle ->
                Tab(
                    selected = selectedStepIndex == index,
                    onClick = { selectedStepIndex = index },
                    text = {
                        Text(
                            text = stepTitle,
                            fontWeight = if (selectedStepIndex == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedStepIndex == index) TraceTealPrimary else TraceTextSecondary
                        )
                    }
                )
            }
        }

        // AI Assistant Banner overlay if generating or output ready
        AnimatedVisibility(
            visible = aiAssistState !is AiAssistState.Idle,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AiAssistCard(
                state = aiAssistState,
                onApply = { field, text -> viewModel.applyAiResultToField(field, text) },
                onDismiss = { viewModel.dismissAiAssist() }
            )
        }

        // Step Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedStepIndex) {
                0 -> Step1Understand(problem, viewModel)
                1 -> Step2ExistingResearch(problem, viewModel)
                2 -> Step3Gaps(problem, viewModel)
                3 -> Step4Evidence(problem, evidenceList, onAddEvidence = { showAddEvidenceDialog = true }, viewModel = viewModel)
                4 -> Step5Solutions(problem, viewModel)
                5 -> Step6Document(problem, evidenceList, viewModel)
            }
        }
    }

    if (showAddEvidenceDialog) {
        AddEvidenceDialog(
            onDismiss = { showAddEvidenceDialog = false },
            onAdd = { title, type, content, dataValue, loc ->
                viewModel.addFieldEvidence(title, type, content, dataValue, loc)
                showAddEvidenceDialog = false
            }
        )
    }
}

// --- Step 1: Understand ---
@Composable
private fun Step1Understand(problem: ProblemInvestigation, viewModel: ProblemsLabViewModel) {
    var text by remember(problem.problemStatement) { mutableStateOf(problem.problemStatement) }
    var location by remember(problem.location) { mutableStateOf(problem.location) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        InstructionBox(
            number = "STEP 1",
            title = "Understand the Problem",
            body = "Describe what you observed on the ground. Who suffers from this problem? What is the estimated scale, frequency, and root causes?"
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Specific Location / Community") },
            placeholder = { Text("e.g. Kano rural farming belts, Lagos market alleys") },
            modifier = Modifier.fillMaxWidth(),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Problem Root Cause & Scope Statement") },
            placeholder = { Text("Farmers lose 40% of harvest because wooden crates crush bottom layers during 12-hour hot transit...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateCurrentProblemStep(
                    problemStatement = text,
                    status = "investigating"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Problem Statement", fontWeight = FontWeight.Bold)
        }
    }
}

// --- Step 2: Existing Research ---
@Composable
private fun Step2ExistingResearch(problem: ProblemInvestigation, viewModel: ProblemsLabViewModel) {
    var text by remember(problem.existingResearch) { mutableStateOf(problem.existingResearch) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        InstructionBox(
            number = "STEP 2",
            title = "Find Existing Research",
            body = "What do academic studies, universities, and institutional reports already say about this? Have past government or NGO interventions succeeded or failed?"
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Existing Literature & Interventions") },
            placeholder = { Text("FAO and CGIAR reports show $1.8B loss in Nigeria. Past attempts introduced refrigerated trucks, but smallholders could not afford rental fees...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateCurrentProblemStep(existingResearch = text)
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Research Notes", fontWeight = FontWeight.Bold)
        }
    }
}

// --- Step 3: Unaddressed Gaps ---
@Composable
private fun Step3Gaps(problem: ProblemInvestigation, viewModel: ProblemsLabViewModel) {
    var text by remember(problem.unaddressedGaps) { mutableStateOf(problem.unaddressedGaps) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        InstructionBox(
            number = "STEP 3",
            title = "Identify Unaddressed Gaps",
            body = "This is what makes your research unique! What has not been adequately addressed? What niche or grassroots condition did big studies overlook?"
        )

        Spacer(modifier = Modifier.height(14.dp))

        // AI Accelerator Button
        Button(
            onClick = { viewModel.requestAiResearchGaps() },
            colors = ButtonDefaults.buttonColors(containerColor = PeachPrimary, contentColor = OnPeach),
            modifier = Modifier.fillMaxWidth().testTag("ai_find_gaps_button")
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Ask AI to Analyze Research Gaps", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("What Hasn't Been Addressed?") },
            placeholder = { Text("1. No one explored zero-electricity evaporative cooling with local clay.\n2. Crate pooling logistics haven't been tested with USSD phone booking...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.updateCurrentProblemStep(unaddressedGaps = text) },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Research Gaps", fontWeight = FontWeight.Bold)
        }
    }
}

// --- Step 4: Evidence Collection ---
@Composable
private fun Step4Evidence(
    problem: ProblemInvestigation,
    evidenceList: List<FieldEvidence>,
    onAddEvidence: () -> Unit,
    viewModel: ProblemsLabViewModel
) {
    var notes by remember(problem.evidenceNotes) { mutableStateOf(problem.evidenceNotes) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        InstructionBox(
            number = "STEP 4",
            title = "Collect Relevant Evidence",
            body = "Log local metrics, interviews with affected individuals, field measurements, and survey samples. Real evidence turns an opinion into research."
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddEvidence,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
                modifier = Modifier.weight(1f).testTag("add_field_evidence_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Log Field Data", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { viewModel.requestAiFieldSurvey() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceCyanSecondary),
                border = androidx.compose.foundation.BorderStroke(1.dp, TraceCyanSecondary),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("AI Survey Plan", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Logged Evidence Points (${evidenceList.size})",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TraceTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (evidenceList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TraceSurface)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No field evidence points logged yet. Tap 'Log Field Data' above to record prices, spoilage counts, or interview quotes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TraceTextMuted,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                evidenceList.forEach { ev ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = TraceSurface),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TraceTealPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = ev.evidenceType.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TraceTealPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = ev.title,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TraceTextPrimary
                                )
                                if (ev.content.isNotBlank()) {
                                    Text(
                                        text = ev.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TraceTextSecondary
                                    )
                                }
                                if (ev.dataValue.isNotBlank()) {
                                    Text(
                                        text = "Metric: ${ev.dataValue}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TraceAmberTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Field Notes & Methodology Log") },
            placeholder = { Text("Detailed observations from interviews with farmers, transport drivers, and market chairmen...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateCurrentProblemStep(
                    evidenceNotes = notes,
                    status = "evidence_collected"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Evidence Log", fontWeight = FontWeight.Bold)
        }
    }
}

// --- Step 5: Develop Solutions ---
@Composable
private fun Step5Solutions(problem: ProblemInvestigation, viewModel: ProblemsLabViewModel) {
    var text by remember(problem.proposedSolutions) { mutableStateOf(problem.proposedSolutions) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        InstructionBox(
            number = "STEP 5",
            title = "Develop Possible Solutions",
            body = "Based on the evidence you collected, what pragmatic intervention solves this? Think low-cost engineering, business model shift, or software."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // AI Accelerator Button
        Button(
            onClick = { viewModel.requestAiHypothesisAndMethodology() },
            colors = ButtonDefaults.buttonColors(containerColor = PeachPrimary, contentColor = OnPeach),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI: Generate Scientific Hypothesis & Solution Plan", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Proposed Solutions & Interventions") },
            placeholder = { Text("1. Village evaporative cooling chambers using sand and clay.\n2. Standardized reusable plastic crates pooled via local micro-credit...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.updateCurrentProblemStep(
                    proposedSolutions = text,
                    status = "solution_drafted"
                )
            },
            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Solutions", fontWeight = FontWeight.Bold)
        }
    }
}

// --- Step 6: Document & Pitch ---
@Composable
private fun Step6Document(
    problem: ProblemInvestigation,
    evidenceList: List<FieldEvidence>,
    viewModel: ProblemsLabViewModel
) {
    var summary by remember(problem.opportunitySummary) { mutableStateOf(problem.opportunitySummary) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        InstructionBox(
            number = "STEP 6",
            title = "Document Findings & Opportunity",
            body = "Turn your findings into a structured research report and grant/venture pitch. This is your portfolio proof as an independent researcher."
        )

        Spacer(modifier = Modifier.height(14.dp))

        // AI Accelerator Button
        Button(
            onClick = { viewModel.requestAiExecutiveReport() },
            colors = ButtonDefaults.buttonColors(containerColor = PeachPrimary, contentColor = OnPeach),
            modifier = Modifier.fillMaxWidth().testTag("compile_brief_button")
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("AI: Compile Publication-Grade Research Brief", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = summary,
            onValueChange = { summary = it },
            label = { Text("Research Brief & Opportunity Pitch") },
            placeholder = { Text("Executive summary, market viability, potential addressable market, and grant pitch...") },
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            colors = customTextFieldColors()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    val fullReport = buildString {
                        append("=== TRACE RESEARCH REPORT ===\n\n")
                        append("Title: ${problem.title}\n")
                        append("Sector: ${problem.sector} | Location: ${problem.location}\n\n")
                        append("1. PROBLEM STATEMENT:\n${problem.problemStatement}\n\n")
                        append("2. EXISTING RESEARCH:\n${problem.existingResearch}\n\n")
                        append("3. UNADDRESSED GAPS:\n${problem.unaddressedGaps}\n\n")
                        append("4. FIELD EVIDENCE:\n")
                        evidenceList.forEach { append("- [${it.evidenceType}] ${it.title}: ${it.content} (${it.dataValue})\n") }
                        append("\n5. PROPOSED SOLUTIONS:\n${problem.proposedSolutions}\n\n")
                        append("6. OPPORTUNITY BRIEF & PITCH:\n$summary\n")
                    }
                    clipboardManager.setText(AnnotatedString(fullReport))
                    Toast.makeText(context, "Full Report copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceTealPrimary)
            ) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Full Report")
            }

            Button(
                onClick = {
                    viewModel.updateCurrentProblemStep(
                        opportunitySummary = summary,
                        status = "completed"
                    )
                    Toast.makeText(context, "Research investigation marked completed!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mark as Completed", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun InstructionBox(number: String, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurfaceVariant),
        border = androidx.compose.foundation.BorderStroke(1.dp, TraceTealPrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TraceTealPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = number,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnGreen
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TraceTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall,
                color = TraceTextSecondary
            )
        }
    }
}

@Composable
private fun AiAssistCard(
    state: AiAssistState,
    onApply: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurfacePeach),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PeachPrimary)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = TraceCyanSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "TRACE AI Research Intelligence",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TraceCyanSecondary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TraceTextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (state) {
                is AiAssistState.Generating -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = TraceCyanSecondary,
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Synthesizing research data with Gemini...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceTextSecondary
                        )
                    }
                }
                is AiAssistState.Completed -> {
                    Text(
                        text = state.result,
                        style = MaterialTheme.typography.bodySmall,
                        color = TraceTextPrimary,
                        maxLines = 8,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Discard", color = TraceTextMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onApply(state.targetField, state.result) },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apply to Step", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                is AiAssistState.Error -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TraceErrorContainer),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "AI Generation Notice: ${state.message}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceOnErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
                else -> {}
            }
        }
    }
}

// --- Create Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateProblemDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, sector: String, location: String, statement: String, tags: List<String>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var sector by remember { mutableStateOf(SECTORS[1]) }
    var location by remember { mutableStateOf("") }
    var statement by remember { mutableStateOf("") }
    var sectorExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "New Everyday Investigation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Problem Observed") },
                    placeholder = { Text("e.g. Small bakeries paying 35% on packaging films") },
                    modifier = Modifier.fillMaxWidth().testTag("new_problem_title_input"),
                    colors = customTextFieldColors()
                )

                ExposedDropdownMenuBox(
                    expanded = sectorExpanded,
                    onExpandedChange = { sectorExpanded = !sectorExpanded }
                ) {
                    OutlinedTextField(
                        value = sector,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sector") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectorExpanded) },
                        modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = customTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = sectorExpanded,
                        onDismissRequest = { sectorExpanded = false }
                    ) {
                        SECTORS.filter { it != "All" }.forEach { sec ->
                            DropdownMenuItem(
                                text = { Text(sec) },
                                onClick = {
                                    sector = sec
                                    sectorExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location / Community") },
                    placeholder = { Text("e.g. Kano, Nigeria or Nairobi outskirts") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("Initial Observation & What You Noticed") },
                    placeholder = { Text("Describe what caught your attention and why this problem matters...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    colors = customTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreate(title, sector, location, statement, listOf(sector))
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen),
                modifier = Modifier.testTag("submit_new_problem_button")
            ) {
                Text("Start Investigation", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TraceTextMuted)
            }
        },
        containerColor = TraceSurface
    )
}

// --- Add Evidence Dialog ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEvidenceDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, type: String, content: String, dataValue: String, locationTag: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("metric") }
    var content by remember { mutableStateOf("") }
    var dataValue by remember { mutableStateOf("") }
    var locationTag by remember { mutableStateOf("") }
    var typeExpanded by remember { mutableStateOf(false) }

    val types = listOf("metric", "observation", "interview", "cost_data", "photo_note")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Field Evidence Point",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Evidence Headline") },
                    placeholder = { Text("e.g. Crate spoil rate at market arrival") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = type.uppercase(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Evidence Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = customTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t.uppercase()) },
                                onClick = {
                                    type = t
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = dataValue,
                    onValueChange = { dataValue = it },
                    label = { Text("Quantifiable Metric / Data Value (optional)") },
                    placeholder = { Text("e.g. 38% loss, N4,500/crate, 6 hrs delay") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = locationTag,
                    onValueChange = { locationTag = it },
                    label = { Text("Specific Venue / Market") },
                    placeholder = { Text("e.g. Mile 12 Market, Lagos") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Details & Qualitative Notes") },
                    placeholder = { Text("Counted 42 bad crates out of 110 unloaded. Truck had no ventilation...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = customTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onAdd(title, type, content, dataValue, locationTag)
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen)
            ) {
                Text("Log Evidence", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TraceTextMuted)
            }
        },
        containerColor = TraceSurface
    )
}

@Composable
fun customTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TraceTextPrimary,
    unfocusedTextColor = TraceTextPrimary,
    focusedContainerColor = TraceSurface,
    unfocusedContainerColor = TraceSurface,
    focusedBorderColor = GreenPrimary,
    unfocusedBorderColor = TraceOutline,
    focusedLabelColor = GreenPrimary,
    unfocusedLabelColor = TraceTextSecondary,
    cursorColor = GreenPrimary
)
