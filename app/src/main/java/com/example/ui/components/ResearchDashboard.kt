package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProblemInvestigation
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.OnPeach
import com.example.ui.theme.OnPeachContainer
import com.example.ui.theme.PeachContainer
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceErrorContainer
import com.example.ui.theme.TraceHeroGradientBrush
import com.example.ui.theme.TraceOnErrorContainer
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfaceHighlight
import com.example.ui.theme.TraceSurfacePeach
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.ProblemsUiState

@Composable
fun ResearchDashboard(
    problemsState: ProblemsUiState,
    sectors: List<String>,
    selectedSector: String,
    onSelectSector: (String) -> Unit,
    onProjectClick: (ProblemInvestigation) -> Unit,
    onDeleteProject: (String) -> Unit,
    onStartNewEverydayProblem: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProblems = (problemsState as? ProblemsUiState.Success)?.problems ?: emptyList()
    val totalEvidence = allProblems.sumOf { it.evidenceCount }
    val completedCount = allProblems.count { it.status == "completed" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TraceAmbientBackgroundBrush)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Motivating Peach & Green Hero Dashboard Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = TraceSurface),
                border = BorderStroke(1.dp, TraceOutline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TraceHeroGradientBrush)
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PeachPrimary.copy(alpha = 0.16f))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "EVERYDAY PROBLEMS LAB",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = OnPeachContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Turn Curiosity Into Evidence",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = TraceTextPrimary
                            )
                            Text(
                                text = "Document local challenges, build field proof, and unlock grants.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TraceTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Progress Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DashboardMetricPill(
                            value = allProblems.size.toString(),
                            label = "Projects",
                            containerColor = GreenContainer,
                            accentColor = GreenDark,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricPill(
                            value = totalEvidence.toString(),
                            label = "Evidence",
                            containerColor = PeachContainer,
                            accentColor = OnPeachContainer,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardMetricPill(
                            value = completedCount.toString(),
                            label = "Completed",
                            containerColor = TraceSurface,
                            accentColor = GreenPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Prominent Button to start a new 'Everyday Problems' entry
                    Button(
                        onClick = onStartNewEverydayProblem,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dashboard_new_problem_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeachPrimary,
                            contentColor = OnPeach
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Start New 'Everyday Problem'",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Sector Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(sectors) { sector ->
                    val isSelected = sector == selectedSector
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) GreenPrimary else TraceSurface)
                            .then(
                                if (isSelected) Modifier
                                else Modifier.border(1.dp, TraceOutline, RoundedCornerShape(20.dp))
                            )
                            .clickable { onSelectSector(sector) }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = sector,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) OnGreen else TraceTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Research Projects List
            when (problemsState) {
                is ProblemsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
                is ProblemsUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = TraceErrorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = problemsState.message,
                                color = TraceOnErrorContainer,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                    }
                }
                is ProblemsUiState.Success -> {
                    val filteredList = problemsState.problems.filter {
                        selectedSector == "All" || it.sector.equals(selectedSector, ignoreCase = true)
                    }

                    if (filteredList.isEmpty()) {
                        EmptyDashboardView(
                            onStartNew = onStartNewEverydayProblem,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Active Research Projects (${filteredList.size})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TraceTextPrimary
                                    )
                                    Text(
                                        text = "Tap any card to continue",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TraceTextMuted
                                    )
                                }
                            }

                            items(filteredList, key = { it.id }) { problem ->
                                ResearchProjectCard(
                                    problem = problem,
                                    onClick = { onProjectClick(problem) },
                                    onDelete = { onDeleteProject(problem.id) }
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(76.dp))
                            }
                        }
                    }
                }
            }
        }

        // Quick-access FAB in Botanical Green
        FloatingActionButton(
            onClick = onStartNewEverydayProblem,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("new_problem_fab"),
            containerColor = GreenPrimary,
            contentColor = OnGreen
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "New Investigation")
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "New Entry", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun DashboardMetricPill(
    value: String,
    label: String,
    containerColor: Color,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(vertical = 8.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = accentColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TraceTextSecondary
        )
    }
}

@Composable
private fun ResearchProjectCard(
    problem: ProblemInvestigation,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val stepNum = when (problem.status) {
        "completed" -> 6
        "solution_drafted" -> 5
        "evidence_collected" -> 4
        "investigating" -> 3
        else -> 1
    }
    val progress = stepNum / 6f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("problem_card_${problem.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurface),
        border = BorderStroke(1.dp, TraceOutline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PeachContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = problem.sector.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = OnPeachContainer
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProjectStatusBadge(status = problem.status)
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete project",
                            tint = TraceTextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = problem.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )

            if (problem.location.isNotBlank()) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PeachPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = problem.location,
                        style = MaterialTheme.typography.labelMedium,
                        color = TraceTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = problem.problemStatement.ifBlank { "Investigation initialized. Tap to begin Step 1." },
                style = MaterialTheme.typography.bodySmall,
                color = TraceTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Visual 6-step Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (stepNum == 6) GreenPrimary else PeachPrimary,
                trackColor = TraceSurfaceHighlight
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $stepNum of 6 • ${problem.evidenceCount} evidence logs",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = GreenDark
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Open Lab",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PeachPrimary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = PeachPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectStatusBadge(status: String) {
    val (label, bg, fg) = when (status) {
        "completed" -> Triple("Completed", GreenContainer, GreenDark)
        "solution_drafted" -> Triple("Solutions Drafted", GreenContainer, GreenPrimary)
        "evidence_collected" -> Triple("Evidence Logged", PeachContainer, OnPeachContainer)
        "investigating" -> Triple("Investigating", TraceSurfacePeach, PeachPrimary)
        else -> Triple("Draft", TraceSurfaceVariant, TraceTextSecondary)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = fg
        )
    }
}

@Composable
private fun EmptyDashboardView(
    onStartNew: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(PeachContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Biotech,
                contentDescription = null,
                tint = PeachPrimary,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Start Your First Discovery",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = TraceTextPrimary
        )
        Text(
            text = "Spotted an everyday problem in your neighborhood, farm, or business? Log it here and investigate with AI.",
            style = MaterialTheme.typography.bodySmall,
            color = TraceTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        Button(
            onClick = onStartNew,
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = OnGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Start Everyday Problem", fontWeight = FontWeight.Bold)
        }
    }
}
