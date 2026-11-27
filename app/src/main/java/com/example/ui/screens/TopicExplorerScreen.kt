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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ResearchTopic
import com.example.data.model.TopicCategory
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.OnPeach
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.TraceAmberTertiary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceEmerald
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfaceHighlight
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.TopicExplorerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicExplorerScreen(
    viewModel: TopicExplorerViewModel,
    onNavigateToLab: (String?) -> Unit,
    onConsultAi: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredTopics by viewModel.filteredTopics.collectAsState()
    val selectedTopic by viewModel.selectedTopic.collectAsState()
    val context = LocalContext.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TraceAmbientBackgroundBrush)
    ) {
        // Top Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Research Topic Explorer",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )
            Text(
                text = "Discover high-impact questions, community challenges & emerging frontiers",
                style = MaterialTheme.typography.bodySmall,
                color = TraceTextSecondary
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Search Box
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search topics, sectors, or questions...", color = TraceTextMuted) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TraceTealPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TraceTextMuted)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("topic_search_input"),
                singleLine = true,
                colors = customTextFieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 12.dp)
            ) {
                item {
                    val isAll = selectedCategory == null
                    CategoryChip(
                        title = "All Topics",
                        isSelected = isAll,
                        onClick = { viewModel.selectCategory(null) }
                    )
                }
                items(TopicCategory.entries) { cat ->
                    val isSelected = selectedCategory == cat
                    CategoryChip(
                        title = cat.displayName,
                        isSelected = isSelected,
                        onClick = { viewModel.selectCategory(cat) }
                    )
                }
            }
        }

        // List of Topics
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(filteredTopics, key = { it.id }) { topic ->
                TopicCard(
                    topic = topic,
                    onViewDetails = { viewModel.selectTopic(topic) },
                    onImportToLab = {
                        viewModel.importTopicToLab(topic) { newProblemId ->
                            Toast.makeText(context, "Imported '${topic.title}' to Problems Lab!", Toast.LENGTH_SHORT).show()
                            onNavigateToLab(newProblemId)
                        }
                    },
                    onConsultAi = {
                        onConsultAi("I am investigating the research topic '${topic.title}' in ${topic.region}. What are the primary methodologies and baseline questions to consider?")
                    }
                )
            }
            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Detail Bottom Sheet
    if (selectedTopic != null) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedTopic() },
            sheetState = sheetState,
            containerColor = TraceSurface,
            dragHandle = null
        ) {
            TopicDetailSheet(
                topic = selectedTopic!!,
                onDismiss = { viewModel.clearSelectedTopic() },
                onImportToLab = {
                    viewModel.importTopicToLab(selectedTopic!!) { newProblemId ->
                        Toast.makeText(context, "Imported to Everyday Problems Lab!", Toast.LENGTH_SHORT).show()
                        viewModel.clearSelectedTopic()
                        onNavigateToLab(newProblemId)
                    }
                },
                onConsultAi = {
                    val prompt = "Provide a deep research breakdown of '${selectedTopic!!.title}' in ${selectedTopic!!.sector}. Explain key empirical methods and potential breakthroughs."
                    viewModel.clearSelectedTopic()
                    onConsultAi(prompt)
                }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) GreenPrimary else TraceSurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (isSelected) OnGreen else TraceTextSecondary
        )
    }
}

@Composable
private fun TopicCard(
    topic: ResearchTopic,
    onViewDetails: () -> Unit,
    onImportToLab: () -> Unit,
    onConsultAi: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TraceOutline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TraceTealPrimary.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = topic.category.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TraceTealPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (topic.urgencyLevel.contains("High")) TraceAmberTertiary.copy(alpha = 0.2f)
                            else TraceSurfaceHighlight
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = topic.urgencyLevel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (topic.urgencyLevel.contains("High")) TraceAmberTertiary else TraceCyanSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = topic.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )

            Text(
                text = "${topic.sector} • ${topic.region}",
                style = MaterialTheme.typography.labelMedium,
                color = TraceTextSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = topic.overview,
                style = MaterialTheme.typography.bodySmall,
                color = TraceTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onImportToLab,
                    modifier = Modifier.weight(1f).testTag("import_to_lab_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceTealPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TraceTealPrimary)
                ) {
                    Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Investigate in Lab", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onConsultAi,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PeachPrimary, contentColor = OnPeach)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask TRACE AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TopicDetailSheet(
    topic: ResearchTopic,
    onDismiss: () -> Unit,
    onImportToLab: () -> Unit,
    onConsultAi: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(TraceTealPrimary.copy(alpha = 0.2f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = topic.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TraceTealPrimary
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TraceTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = topic.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TraceTextPrimary
        )

        Text(
            text = "${topic.sector} | Region: ${topic.region}",
            style = MaterialTheme.typography.bodySmall,
            color = TraceAmberTertiary
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = topic.overview,
            style = MaterialTheme.typography.bodyMedium,
            color = TraceTextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Unanswered Questions Worth Investigating:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TraceCyanSecondary
        )

        Spacer(modifier = Modifier.height(6.dp))

        topic.keyQuestions.forEachIndexed { i, q ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "${i + 1}. ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = TraceTealPrimary
                )
                Text(
                    text = q,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TraceTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Potential Real-World Impact:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = TraceEmerald
        )
        Text(
            text = topic.potentialImpact,
            style = MaterialTheme.typography.bodySmall,
            color = TraceTextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onImportToLab,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceTealPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, TraceTealPrimary)
            ) {
                Icon(Icons.Default.Biotech, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Open in Lab", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onConsultAi,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Deep Dive AI", fontWeight = FontWeight.Bold)
            }
        }
    }
}
