package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.text.format.DateUtils
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CatalogOpportunity
import com.example.data.model.TrackedOpportunity
import com.example.ui.theme.GreenContainer
import com.example.ui.theme.GreenDark
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.OnPeach
import com.example.ui.theme.OnPeachContainer
import com.example.ui.theme.PeachContainer
import com.example.ui.theme.PeachPrimary
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
import com.example.ui.viewmodel.OpportunitiesViewModel

val OPP_CATEGORIES = listOf("All", "Grant", "Fellowship", "Innovation Challenge", "Research Residency")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpportunitiesScreen(
    viewModel: OpportunitiesViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Catalog, 1: Tracked Pipeline
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val trackedList by viewModel.trackedOpportunities.collectAsStateWithLifecycle()
    val catalogList by viewModel.catalogOpportunities.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val lastSyncMillis by viewModel.lastSyncMillis.collectAsStateWithLifecycle()
    val syncError by viewModel.syncError.collectAsStateWithLifecycle()
    val liveGrantCount by viewModel.liveGrantCount.collectAsStateWithLifecycle()
    val activeLiveKeyword by viewModel.activeLiveKeyword.collectAsStateWithLifecycle()
    val selectedOpportunity by viewModel.selectedOpportunity.collectAsStateWithLifecycle()
    val isLoadingDetail by viewModel.isLoadingDetail.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Research Opportunity Finder",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TraceTextPrimary
                    )
                    Text(
                        text = "Live Grants.gov feed + curated fellowships & funding calls",
                        style = MaterialTheme.typography.bodySmall,
                        color = TraceTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PeachContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = null,
                        tint = PeachPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Auto-Sync Status Strip
            LiveFeedStatusBar(
                isSyncing = isSyncing,
                lastSyncMillis = lastSyncMillis,
                liveCount = liveGrantCount,
                activeKeyword = activeLiveKeyword,
                onSyncNow = { viewModel.refreshLiveGrants(searchQuery) }
            )

            if (syncError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(TraceErrorContainer)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = syncError ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = TraceOnErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = { viewModel.dismissSyncError() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Text("Dismiss", fontSize = 11.sp, color = TraceOnErrorContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Tab Switcher
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = TraceSurfaceVariant,
                contentColor = GreenPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = GreenPrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "Live & Curated (${catalogList.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "My Tracked (${trackedList.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                Spacer(modifier = Modifier.height(10.dp))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    placeholder = { Text("Search grants, agencies, or topics...", color = TraceTextMuted) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = GreenPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.onSearchQueryChanged("")
                                if (activeLiveKeyword.isNotBlank()) {
                                    viewModel.refreshLiveGrants("")
                                }
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = TraceTextMuted)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = customTextFieldColors()
                )

                if (searchQuery.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Want fresh federal grants matching \"$searchQuery\"?",
                            style = MaterialTheme.typography.labelSmall,
                            color = TraceTextSecondary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = { viewModel.refreshLiveGrants(searchQuery) },
                            enabled = !isSyncing,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isSyncing) "Searching..." else "Query Live API",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = PeachPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Filter chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 12.dp)
                ) {
                    items(OPP_CATEGORIES) { cat ->
                        val isSelected = categoryFilter == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) GreenPrimary else TraceSurfaceVariant)
                                .clickable { viewModel.setCategoryFilter(cat) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) OnGreen else TraceTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // List Content
        if (selectedTab == 0) {
            if (catalogList.isEmpty() && !isSyncing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No matching grants in current view",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TraceTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing filters or querying the live Grants.gov feed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.refreshLiveGrants(searchQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = PeachPrimary, contentColor = OnPeach)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Query Live Grants.gov API", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(catalogList, key = { it.id }) { opp ->
                        val isTracked = trackedList.any { it.opportunityId == opp.id }
                        OpportunityCatalogCard(
                            opp = opp,
                            isTracked = isTracked,
                            onCardClick = { viewModel.selectOpportunity(opp) },
                            onTrack = {
                                viewModel.trackOpportunity(opp, "interested", "Tracked from catalog")
                                Toast.makeText(context, "Added '${opp.title}' to tracked pipeline!", Toast.LENGTH_SHORT).show()
                            },
                            onOpenUrl = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(opp.applicationUrl))
                                context.startActivity(intent)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        } else {
            // Tracked Pipeline Tab
            if (trackedList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CardGiftcard,
                            contentDescription = null,
                            tint = GreenPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Grants in Your Pipeline Yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TraceTextPrimary
                        )
                        Text(
                            text = "Browse the live catalog and tap 'Track Grant' to monitor proposal deadlines and application stages.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(trackedList, key = { it.id }) { tracked ->
                        TrackedPipelineCard(
                            tracked = tracked,
                            onStageChange = { newStage ->
                                viewModel.updateTrackedStage(tracked.id, newStage, null)
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(64.dp))
                    }
                }
            }
        }
    }

    // Live Opportunity Detail Sheet
    if (selectedOpportunity != null) {
        val opp = selectedOpportunity!!
        val isTracked = trackedList.any { it.opportunityId == opp.id }
        ModalBottomSheet(
            onDismissRequest = { viewModel.clearSelectedOpportunity() },
            sheetState = sheetState,
            containerColor = TraceSurface,
            dragHandle = null
        ) {
            OpportunityDetailSheet(
                opp = opp,
                isLoadingDetail = isLoadingDetail,
                isTracked = isTracked,
                onDismiss = { viewModel.clearSelectedOpportunity() },
                onTrack = {
                    viewModel.trackOpportunity(opp, "interested", "Tracked from live feed")
                    Toast.makeText(context, "Added '${opp.title}' to tracked pipeline!", Toast.LENGTH_SHORT).show()
                },
                onOpenUrl = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(opp.applicationUrl))
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
private fun LiveFeedStatusBar(
    isSyncing: Boolean,
    lastSyncMillis: Long?,
    liveCount: Int,
    activeKeyword: String,
    onSyncNow: () -> Unit
) {
    val syncTimeText = remember(lastSyncMillis, isSyncing, liveCount, activeKeyword) {
        when {
            isSyncing -> "Syncing live feed from Grants.gov..."
            lastSyncMillis != null -> {
                val rel = DateUtils.getRelativeTimeSpanString(
                    lastSyncMillis,
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
                )
                val filterNote = if (activeKeyword.isNotBlank()) " • \"$activeKeyword\"" else ""
                "Auto-Sync On • $liveCount live calls • Updated $rel$filterNote"
            }
            else -> "Auto-Sync On (12h) • Tap Sync for live federal grants"
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(GreenContainer.copy(alpha = 0.65f))
            .border(1.dp, GreenPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (isSyncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = GreenDark,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = GreenDark,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = syncTimeText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = GreenDark,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(GreenPrimary)
                .clickable(enabled = !isSyncing) { onSyncNow() }
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .testTag("sync_grants_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Sync Live Grants",
                    tint = OnGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = OnGreen
                )
            }
        }
    }
}

@Composable
private fun OpportunityCatalogCard(
    opp: CatalogOpportunity,
    isTracked: Boolean,
    onCardClick: () -> Unit,
    onTrack: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurface),
        border = BorderStroke(1.dp, TraceOutline)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (opp.isLiveFeed) GreenContainer else PeachContainer)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (opp.isLiveFeed) "LIVE • GRANTS.GOV" else "CURATED GLOBAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (opp.isLiveFeed) GreenDark else OnPeachContainer
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(TraceSurfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = opp.category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TraceTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = opp.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = opp.fundingAmount,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = GreenPrimary
            )

            Text(
                text = "${opp.organization} • Deadline: ${opp.deadline}",
                style = MaterialTheme.typography.labelMedium,
                color = PeachPrimary,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = opp.description,
                style = MaterialTheme.typography.bodySmall,
                color = TraceTextSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenUrl,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceTextSecondary),
                    border = BorderStroke(1.dp, TraceOutline)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Official Portal", fontSize = 12.sp)
                }

                Button(
                    onClick = onTrack,
                    enabled = !isTracked,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isTracked) TraceSurfaceHighlight else GreenPrimary,
                        contentColor = if (isTracked) TraceTextSecondary else OnGreen
                    )
                ) {
                    Icon(
                        imageVector = if (isTracked) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isTracked) "Tracked" else "Track Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun OpportunityDetailSheet(
    opp: CatalogOpportunity,
    isLoadingDetail: Boolean,
    isTracked: Boolean,
    onDismiss: () -> Unit,
    onTrack: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
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
                    .background(if (opp.isLiveFeed) GreenContainer else PeachContainer)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (opp.isLiveFeed) "LIVE • GRANTS.GOV" else opp.category.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = if (opp.isLiveFeed) GreenDark else OnPeachContainer
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TraceTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = opp.title,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = TraceTextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = opp.organization,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = PeachPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DetailMetaBox(
                label = "Award / Status",
                value = opp.fundingAmount,
                modifier = Modifier.weight(1f)
            )
            DetailMetaBox(
                label = "Deadline",
                value = opp.deadline,
                modifier = Modifier.weight(1f)
            )
        }

        if (opp.opportunityNumber.isNotBlank() || opp.openDate.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (opp.opportunityNumber.isNotBlank()) {
                    DetailMetaBox(
                        label = "Opportunity #",
                        value = opp.opportunityNumber,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (opp.openDate.isNotBlank()) {
                    DetailMetaBox(
                        label = "Posted Date",
                        value = opp.openDate,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Synopsis & Focus",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )
            if (isLoadingDetail) {
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    color = GreenPrimary,
                    strokeWidth = 2.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = opp.description,
            style = MaterialTheme.typography.bodyMedium,
            color = TraceTextSecondary,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Eligibility & Regions: ${opp.eligibleRegions}",
            style = MaterialTheme.typography.labelMedium,
            color = GreenDark
        )

        Spacer(modifier = Modifier.height(22.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOpenUrl,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TraceTextPrimary),
                border = BorderStroke(1.dp, TraceOutline)
            ) {
                Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Official Portal", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onTrack,
                enabled = !isTracked,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isTracked) TraceSurfaceHighlight else GreenPrimary,
                    contentColor = if (isTracked) TraceTextSecondary else OnGreen
                )
            ) {
                Icon(
                    imageVector = if (isTracked) Icons.Default.CheckCircle else Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isTracked) "Tracked" else "Track Grant", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun DetailMetaBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(TraceSurfacePeach)
            .padding(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TraceTextMuted
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = TraceTextPrimary
        )
    }
}

@Composable
private fun TrackedPipelineCard(
    tracked: TrackedOpportunity,
    onStageChange: (String) -> Unit
) {
    val stages = listOf("interested", "preparing_proposal", "submitted", "awarded")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurface),
        border = BorderStroke(1.dp, TraceTealPrimary.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tracked.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TraceTextPrimary
                    )
                    Text(
                        text = "${tracked.organization} • ${tracked.fundingAmount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TraceTealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage Pipeline Pills
            Text(
                text = "Application Stage:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = TraceTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stages.forEach { stage ->
                    val isCurrent = tracked.stage == stage
                    val displayLabel = when (stage) {
                        "interested" -> "Interested"
                        "preparing_proposal" -> "Preparing"
                        "submitted" -> "Submitted"
                        "awarded" -> "Awarded"
                        else -> stage
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isCurrent) {
                                    if (stage == "awarded") TraceEmerald else TraceTealPrimary
                                } else TraceSurfaceVariant
                            )
                            .clickable { onStageChange(stage) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isCurrent) OnGreen else TraceTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
