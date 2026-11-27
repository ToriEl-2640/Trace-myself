package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.OnPeachContainer
import com.example.ui.theme.PeachPrimary
import com.example.ui.theme.PeachSoft
import com.example.ui.theme.TraceAmberTertiary
import com.example.ui.theme.TraceAmbientBackgroundBrush
import com.example.ui.theme.TraceCyanSecondary
import com.example.ui.theme.TraceEmerald
import com.example.ui.theme.TraceErrorBorder
import com.example.ui.theme.TraceErrorContainer
import com.example.ui.theme.TraceOnErrorContainer
import com.example.ui.theme.TraceOutline
import com.example.ui.theme.TraceSurface
import com.example.ui.theme.TraceSurfacePeach
import com.example.ui.theme.TraceSurfaceVariant
import com.example.ui.theme.TraceTealPrimary
import com.example.ui.theme.TraceTextMuted
import com.example.ui.theme.TraceTextPrimary
import com.example.ui.theme.TraceTextSecondary
import com.example.ui.viewmodel.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

val RESEARCH_TRACKS = listOf(
    "Agtech & Rural Logistics",
    "Finance, Fintech & Blockchain",
    "Circular Economy & Bio-materials",
    "Education & Digital Inclusion",
    "Urban Waste & Sanitation"
)

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    problemCount: Int,
    libraryCount: Int,
    grantCount: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var selectedTrack by remember { mutableStateOf(RESEARCH_TRACKS[0]) }

    val user = com.google.firebase.Firebase.auth.currentUser

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TraceAmbientBackgroundBrush)
    ) {
        // Header
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
            Text(
                text = "Researcher Profile & Portfolio",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // User Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = TraceSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, TraceOutline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(TraceTealPrimary.copy(alpha = 0.2f))
                            .border(2.dp, TraceTealPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TraceTealPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = user?.displayName ?: "Independent Researcher",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TraceTextPrimary
                        )
                        Text(
                            text = user?.email ?: "Signed in via Google",
                            style = MaterialTheme.typography.bodySmall,
                            color = TraceTextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(TraceTealPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "COMMUNITY RESEARCH FELLOW",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TraceTealPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Portfolio Stats Grid
            Text(
                text = "Your Investigation Portfolio",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Problems",
                    count = problemCount.toString(),
                    subtitle = "Investigated",
                    icon = Icons.Default.Biotech,
                    color = TraceTealPrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Citations",
                    count = libraryCount.toString(),
                    subtitle = "In Library",
                    icon = Icons.Default.Bookmark,
                    color = TraceCyanSecondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Grants",
                    count = grantCount.toString(),
                    subtitle = "Tracked",
                    icon = Icons.Default.CardGiftcard,
                    color = TraceAmberTertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Research Track Selector
            Text(
                text = "Primary Research Track",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TraceTextPrimary
            )
            Text(
                text = "Select your core area to tailor AI methodology suggestions and grant matching",
                style = MaterialTheme.typography.bodySmall,
                color = TraceTextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RESEARCH_TRACKS.forEach { track ->
                    val isSelected = track == selectedTrack
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) TraceTealPrimary.copy(alpha = 0.15f) else TraceSurface)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) TraceTealPrimary else TraceOutline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedTrack = track }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = track,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isSelected) TraceTealPrimary else TraceTextPrimary
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = TraceTealPrimary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // TRACE Vision Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TraceSurfacePeach),
                border = androidx.compose.foundation.BorderStroke(1.dp, PeachSoft)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "THE TRACE VISION",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PeachPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "\"You could actually use the app to conduct research about the problems it is designed to solve. You are building a product while developing your own research career.\"",
                        style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                        color = OnPeachContainer,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Sign Out Button
            OutlinedButton(
                onClick = { authViewModel.signOut(context) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = TraceErrorContainer.copy(alpha = 0.45f),
                    contentColor = TraceOnErrorContainer
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, TraceErrorBorder)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out of TRACE", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TraceSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = TraceTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TraceTextMuted
            )
        }
    }
}
