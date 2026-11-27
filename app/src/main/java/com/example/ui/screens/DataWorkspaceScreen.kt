package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatasetAnalysis
import com.example.data.model.MetricDataPoint
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.TraceAmberTertiary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceEmerald
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceRose
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfaceHighlight
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.DataWorkspaceViewModel

@Composable
fun DataWorkspaceScreen(
    viewModel: DataWorkspaceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedIndex by viewModel.selectedDatasetIndex.collectAsState()
    val customPoints by viewModel.customDataPoints.collectAsState()
    val datasets = viewModel.getDatasets()
    val current = viewModel.currentDataset()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showAddPointDialog by remember { mutableStateOf(false) }

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
                Column {
                    Text(
                        text = "Data Analysis Workspace",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TraceTextPrimary
                    )
                    Text(
                        text = "Visual charts, field metrics & empirical evidence comparison",
                        style = MaterialTheme.typography.bodySmall,
                        color = TraceTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(TraceCyanSecondary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = TraceCyanSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dataset Selector Tabs
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 12.dp)
            ) {
                itemsIndexed(datasets) { index, set ->
                    val isSelected = selectedIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) TraceTealPrimary else TraceSurfaceVariant)
                            .clickable { viewModel.selectDataset(index) }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = set.title.take(24) + "...",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) OnGreen else TraceTextSecondary
                        )
                    }
                }
            }
        }

        // Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Active Dataset Overview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TraceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TraceOutline.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = current.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TraceTextPrimary
                    )
                    Text(
                        text = current.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = TraceCyanSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = current.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TraceTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Visual Data Bar Chart
                    DataVisualizer(dataPoints = current.dataPoints)

                    Spacer(modifier = Modifier.height(16.dp))

                    // Key Insight Box
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = TraceSurfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Insights,
                                contentDescription = null,
                                tint = TraceAmberTertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Empirical Finding",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TraceAmberTertiary
                                )
                                Text(
                                    text = current.insightSummary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TraceTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Actionable Takeaway: ${current.keyTakeaway}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TraceTealPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Custom Field Metrics Logger Card
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                        Column {
                            Text(
                                text = "Your Field Metric Logger",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TraceTextPrimary
                            )
                            Text(
                                text = "Enter local prices, counts, or measurements to compute spreads",
                                style = MaterialTheme.typography.bodySmall,
                                color = TraceTextSecondary
                            )
                        }
                        IconButton(onClick = { showAddPointDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Point", tint = TraceTealPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (customPoints.isEmpty()) {
                        Text(
                            text = "No custom metric points logged. Tap '+' above to add your own field measurements.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceTextMuted
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            customPoints.forEachIndexed { idx, point ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TraceSurfaceVariant)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = point.label,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = TraceTextPrimary
                                        )
                                        Text(
                                            text = "${point.category} • ${point.unit}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TraceTextMuted
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${point.value.toInt()} ${point.unit}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TraceTealPrimary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = { viewModel.removeCustomDataPoint(idx) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = TraceTextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Spread computation
                        if (customPoints.size >= 2) {
                            val minVal = customPoints.minOf { it.value }
                            val maxVal = customPoints.maxOf { it.value }
                            val spread = maxVal - minVal
                            val spreadPercent = if (minVal > 0) ((spread / minVal) * 100).toInt() else 0

                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TraceTealPrimary.copy(alpha = 0.15f))
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Calculated Spread Gap:",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TraceTealPrimary
                                    )
                                    Text(
                                        text = "+${spread.toInt()} (${spreadPercent}% spread)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = TraceTealPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Export Summary Button
            OutlinedButton(
                onClick = {
                    val summary = buildString {
                        append("TRACE DATA ANALYSIS REPORT\n\n")
                        append("Dataset: ${current.title}\n")
                        append("${current.description}\n\n")
                        append("Breakdown:\n")
                        current.dataPoints.forEach {
                            append("- ${it.label}: ${it.value}${it.unit}\n")
                        }
                        append("\nKey Finding: ${current.insightSummary}\n")
                        append("Recommendation: ${current.keyTakeaway}\n\n")
                        if (customPoints.isNotEmpty()) {
                            append("Field Logged Points:\n")
                            customPoints.forEach { append("- ${it.label}: ${it.value} ${it.unit}\n") }
                        }
                    }
                    clipboardManager.setText(AnnotatedString(summary))
                    Toast.makeText(context, "Data analysis report copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceTealPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, TraceTealPrimary)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Data Summary Card", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }

    if (showAddPointDialog) {
        AddPointDialog(
            onDismiss = { showAddPointDialog = false },
            onAdd = { label, value, category, unit ->
                viewModel.addCustomDataPoint(label, value, category, unit)
                showAddPointDialog = false
            }
        )
    }
}

@Composable
private fun DataVisualizer(dataPoints: List<MetricDataPoint>) {
    val maxValue = (dataPoints.maxOfOrNull { it.value } ?: 100f).coerceAtLeast(1f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        dataPoints.forEach { point ->
            val ratio = (point.value / maxValue).coerceIn(0f, 1f)
            val animatedRatio by animateFloatAsState(
                targetValue = ratio,
                animationSpec = tween(600),
                label = "bar_ratio"
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = point.label,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = TraceTextPrimary
                    )
                    Text(
                        text = "${point.value}${point.unit}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = TraceCyanSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(TraceSurfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedRatio)
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                if (point.value > (maxValue * 0.7f)) TraceRose
                                else if (point.value > (maxValue * 0.4f)) TraceAmberTertiary
                                else TraceTealPrimary
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPointDialog(
    onDismiss: () -> Unit,
    onAdd: (label: String, value: Float, category: String, unit: String) -> Unit
) {
    var label by remember { mutableStateOf("") }
    var valueStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Field Metric") }
    var unit by remember { mutableStateOf("%") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Custom Field Metric",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Metric Label / Observation") },
                    placeholder = { Text("e.g. Kano farmgate tomato price") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = valueStr,
                    onValueChange = { valueStr = it },
                    label = { Text("Numerical Value") },
                    placeholder = { Text("e.g. 4500 or 38") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Unit of Measurement") },
                    placeholder = { Text("e.g. NGN, %, kg, hours") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g. Pricing, Spoilage, Transit") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsedVal = valueStr.toFloatOrNull() ?: 0f
                    if (label.isNotBlank() && parsedVal > 0f) {
                        onAdd(label, parsedVal, category, unit)
                    }
                },
                enabled = label.isNotBlank() && valueStr.toFloatOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen)
            ) {
                Text("Add Metric", fontWeight = FontWeight.Bold)
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
