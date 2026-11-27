package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AiResearchRepository
import com.example.data.repository.LiveGrantsRepository
import com.example.data.repository.TraceRepository
import com.example.data.worker.GrantsSyncScheduler
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceBackground
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTheme
import com.example.ui.viewmodel.AiAssistantViewModel
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.DataWorkspaceViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.OpportunitiesViewModel
import com.example.ui.viewmodel.ProblemsLabViewModel
import com.example.ui.viewmodel.TopicExplorerViewModel
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Step 1: Configure App Check before any UI or Firebase operations
        configureAppCheck(this, intent)

        // Schedule automatic periodic background sync for live Grants.gov opportunities
        GrantsSyncScheduler.schedulePeriodicSync(this)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            TraceTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = TraceBackground
                ) {
                    AppRoot()
                }
            }
        }
    }
}

@Composable
fun AppRoot() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val repository = remember { TraceRepository(context) }
    val aiRepository = remember { AiResearchRepository() }

    val authViewModel = remember { AuthViewModel(repository) }
    val authUiState by authViewModel.uiState.collectAsState()

    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
            authViewModel.checkCurrentAuth()
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    LaunchedEffect(Unit) {
        authViewModel.attemptAutoSignIn(context)
    }

    when (val state = authUiState) {
        is AuthUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(TraceAmbientBackgroundBrush),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = TraceTealPrimary)
            }
        }
        is AuthUiState.Unauthenticated -> {
            AuthScreen(viewModel = authViewModel)
        }
        is AuthUiState.Error -> {
            AuthScreen(viewModel = authViewModel)
        }
        is AuthUiState.Authenticated -> {
            val uid = state.user.uid
            key(uid) {
                val liveGrantsRepository = remember(context) { LiveGrantsRepository(context) }
                val labViewModel = remember(uid) { ProblemsLabViewModel(repository, aiRepository) }
                val topicViewModel = remember(uid) { TopicExplorerViewModel(repository) }
                val aiViewModel = remember(uid) { AiAssistantViewModel(aiRepository, repository) }
                val dataViewModel = remember(uid) { DataWorkspaceViewModel(repository) }
                val oppsViewModel = remember(uid) { OpportunitiesViewModel(repository, liveGrantsRepository) }
                val libraryViewModel = remember(uid) { LibraryViewModel(repository, aiRepository) }

                MainScreen(
                    currentUserId = uid,
                    authViewModel = authViewModel,
                    labViewModel = labViewModel,
                    topicViewModel = topicViewModel,
                    aiViewModel = aiViewModel,
                    dataViewModel = dataViewModel,
                    oppsViewModel = oppsViewModel,
                    libraryViewModel = libraryViewModel
                )
            }
        }
    }
}
