package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.OnGreen
import com.example.ui.theme.OnPeach
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceBackground
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DataWorkspaceViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.OpportunitiesViewModel
import com.example.ui.viewmodel.ProblemsLabViewModel
import com.example.ui.viewmodel.TopicExplorerViewModel

enum class MainNavTab(val label: String, val icon: ImageVector, val tag: String) {
    LAB("Problems Lab", Icons.Default.Biotech, "tab_lab"),
    EXPLORE("Explore", Icons.Default.Explore, "tab_explore"),
    AI_ASSIST("AI Assist", Icons.Default.AutoAwesome, "tab_ai"),
    DATA("Data", Icons.Default.BarChart, "tab_data"),
    GRANTS("Grants", Icons.Default.CardGiftcard, "tab_grants")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentUserId: String,
    authViewModel: AuthViewModel,
    labViewModel: ProblemsLabViewModel,
    topicViewModel: TopicExplorerViewModel,
    aiViewModel: AiAssistantViewModel,
    dataViewModel: DataWorkspaceViewModel,
    oppsViewModel: OpportunitiesViewModel,
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainNavTab.LAB) }
    var currentSubScreen by remember { mutableStateOf<String?>(null) } // "library" or "profile" or null

    val problemsState by labViewModel.problemsState.collectAsState()
    val libraryItems by libraryViewModel.libraryItems.collectAsState()
    val trackedOpps by oppsViewModel.trackedOpportunities.collectAsState()

    val problemCount = (problemsState as? com.example.ui.viewmodel.ProblemsUiState.Success)?.problems?.size ?: 0

    if (currentSubScreen == "library") {
        LibraryScreen(
            viewModel = libraryViewModel,
            onBack = { currentSubScreen = null }
        )
    } else if (currentSubScreen == "profile") {
        ProfileScreen(
            authViewModel = authViewModel,
            problemCount = problemCount,
            libraryCount = libraryItems.size,
            grantCount = trackedOpps.size,
            onBack = { currentSubScreen = null }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(TraceSurfaceVariant)
                                    .border(1.dp, TraceTealPrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_trace_icon_1794501733783),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "TRACE",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = 2.sp
                                    ),
                                    color = TraceTextPrimary
                                )
                                Text(
                                    text = "RESEARCH & DISCOVERY",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = TraceTealPrimary,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    },
                    actions = {
                        // Library Icon
                        IconButton(
                            onClick = { currentSubScreen = "library" },
                            modifier = Modifier.testTag("open_library_button")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (libraryItems.isNotEmpty()) {
                                        Badge(containerColor = PeachPrimary, contentColor = OnPeach) {
                                            Text(libraryItems.size.toString())
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmarks,
                                    contentDescription = "Library",
                                    tint = TraceTextSecondary
                                )
                            }
                        }

                        // Profile Icon
                        IconButton(
                            onClick = { currentSubScreen = "profile" },
                            modifier = Modifier.testTag("open_profile_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(TraceTealPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = TraceTealPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TraceSurface,
                        titleContentColor = TraceTextPrimary
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = TraceSurface,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    MainNavTab.entries.forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = OnGreen,
                                selectedTextColor = GreenPrimary,
                                indicatorColor = GreenPrimary,
                                unselectedIconColor = TraceTextMuted,
                                unselectedTextColor = TraceTextMuted
                            ),
                            modifier = Modifier.testTag(tab.tag)
                        )
                    }
                }
            },
            containerColor = TraceBackground
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TraceAmbientBackgroundBrush)
                    .padding(paddingValues)
            ) {
                when (selectedTab) {
                    MainNavTab.LAB -> {
                        ProblemsLabScreen(viewModel = labViewModel)
                    }
                    MainNavTab.EXPLORE -> {
                        TopicExplorerScreen(
                            viewModel = topicViewModel,
                            onNavigateToLab = {
                                selectedTab = MainNavTab.LAB
                            },
                            onConsultAi = { prompt ->
                                aiViewModel.setQuickSample(prompt)
                                selectedTab = MainNavTab.AI_ASSIST
                            }
                        )
                    }
                    MainNavTab.AI_ASSIST -> {
                        AiAssistantScreen(viewModel = aiViewModel)
                    }
                    MainNavTab.DATA -> {
                        DataWorkspaceScreen(viewModel = dataViewModel)
                    }
                    MainNavTab.GRANTS -> {
                        OpportunitiesScreen(viewModel = oppsViewModel)
                    }
                }
            }
        }
    }
}
