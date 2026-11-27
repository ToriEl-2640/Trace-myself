package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LibraryItem
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.TraceAmberTertiary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.LibraryViewModel

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val items by viewModel.libraryItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSimplifying by viewModel.isSimplifying.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TraceAmbientBackgroundBrush)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TraceTextPrimary)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Research Library & Citations",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TraceTextPrimary
                )
                Text(
                    text = "${items.size} curated publications, case studies & papers",
                    style = MaterialTheme.typography.labelSmall,
                    color = TraceCyanSecondary
                )
            }
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Publication", tint = TraceTealPrimary)
            }
        }

        // Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TraceSurface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search papers, authors, or sources...", color = TraceTextMuted) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TraceTealPrimary)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = customTextFieldColors()
            )
        }

        val filteredItems = items.filter { item ->
            val q = searchQuery.trim().lowercase()
            q.isBlank() ||
                    item.title.lowercase().contains(q) ||
                    item.source.lowercase().contains(q) ||
                    item.authors.lowercase().contains(q) ||
                    item.domain.lowercase().contains(q)
        }

        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null, tint = TraceTealPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No Papers in Library",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TraceTextPrimary
                    )
                    Text(
                        text = "Save research papers, AI summaries, or add external citations to your personal notebook.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TraceTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredItems, key = { it.id }) { item ->
                    LibraryCard(
                        item = item,
                        isSimplifying = isSimplifying == item.id,
                        onBookmarkToggle = { viewModel.toggleBookmark(item) },
                        onSimplify = {
                            viewModel.requestAiSimplification(item)
                            Toast.makeText(context, "AI is creating a plain-English explanation...", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddLibraryItemDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title, source, authors, year, domain, summary, notes ->
                viewModel.addCustomItem(title, source, authors, year, domain, summary, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun LibraryCard(
    item: LibraryItem,
    isSimplifying: Boolean,
    onBookmarkToggle: () -> Unit,
    onSimplify: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, TraceOutline.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TraceTealPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = (if (item.domain.isNotBlank()) item.domain else "RESEARCH").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TraceTealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TraceTextPrimary
                    )

                    Text(
                        text = "${item.authors.ifBlank { "Authors" }} • ${item.source} (${item.publicationYear})",
                        style = MaterialTheme.typography.labelSmall,
                        color = TraceCyanSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                IconButton(onClick = onBookmarkToggle) {
                    Icon(
                        imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (item.isBookmarked) TraceAmberTertiary else TraceTextMuted
                    )
                }
            }

            if (item.summary.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = TraceTextSecondary
                )
            }

            if (item.simplifiedExplanation.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = TraceSurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = TraceTealPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "TRACE Plain-English Translation",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TraceTealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.simplifiedExplanation,
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceTextPrimary
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = onSimplify,
                    enabled = !isSimplifying,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceCyanSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TraceCyanSecondary)
                ) {
                    if (isSimplifying) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = TraceCyanSecondary, strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Translating to Plain English...")
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Explain Paper in Plain English with AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddLibraryItemDialog(
    onDismiss: () -> Unit,
    onAdd: (title: String, source: String, authors: String, year: String, domain: String, summary: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("") }
    var authors by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("2026") }
    var domain by remember { mutableStateOf("Agriculture") }
    var summary by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Research Publication", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = TraceTextPrimary)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Paper / Report Title") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Publisher / Journal / Institution") },
                    placeholder = { Text("e.g. CGIAR, MIT, AfDB, World Bank") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )
                OutlinedTextField(
                    value = authors,
                    onValueChange = { authors = it },
                    label = { Text("Authors") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )
                OutlinedTextField(
                    value = year,
                    onValueChange = { year = it },
                    label = { Text("Publication Year") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = customTextFieldColors()
                )
                OutlinedTextField(
                    value = summary,
                    onValueChange = { summary = it },
                    label = { Text("Abstract / Summary Notes") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = customTextFieldColors()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && source.isNotBlank()) {
                        onAdd(title, source, authors, year, domain, summary, "")
                    }
                },
                enabled = title.isNotBlank() && source.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary, contentColor = OnGreen)
            ) {
                Text("Add to Library", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TraceTextMuted) }
        },
        containerColor = TraceSurface
    )
}
