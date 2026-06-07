package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Galeri
import com.example.data.Santri
import com.example.viewmodel.SantriViewModel

@Composable
fun DashboardScreen(
    viewModel: SantriViewModel,
    allSantri: List<Santri>,
    allGaleri: List<Galeri>,
    onNavigate: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    // Calculated metrics
    val totalSantri = allSantri.size
    val santriAktif = allSantri.filter { it.statusSantri == "Aktif" }.size
    val santriAlumni = allSantri.filter { it.statusSantri == "Alumni" }.size
    val totalGallery = allGaleri.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcoming Hero banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        // Subtle circles in background
                        drawCircle(
                            color = Color.White.copy(alpha = 0.08f),
                            radius = size.width / 3f,
                            center = Offset(size.width * 0.9f, size.height * 0.2f)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = size.width / 4f,
                            center = Offset(size.width * 0.1f, size.height * 0.8f)
                        )
                    }
                    .padding(24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pusat Data Santri",
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Selamat datang kembali di sistem informasi manajemen pondok pesantren. Kelola database secara praktis dan modern.",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Kartu Statistik Utama",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Statistics Grid layout (responsive flow using Rows/Columns)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Total Santri",
                value = totalSantri.toString(),
                icon = Icons.Default.Groups,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f).testTag("stat_total_santri")
            )
            StatCard(
                label = "Santri Aktif",
                value = santriAktif.toString(),
                icon = Icons.Default.CheckCircle,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f).testTag("stat_santri_aktif")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Santri Alumni",
                value = santriAlumni.toString(),
                icon = Icons.Default.School,
                tint = Color(0xFFD97706), // Cool warm amber
                modifier = Modifier.weight(1f).testTag("stat_santri_alumni")
            )
            StatCard(
                label = "Foto Galeri",
                value = totalGallery.toString(),
                icon = Icons.Outlined.PhotoLibrary,
                tint = Color(0xFF4F46E5), // Cool Indigo
                modifier = Modifier.weight(1f).testTag("stat_total_foto")
            )
        }

        // Statistik chart segment
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_chart_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Statistik Perbandingan Status",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Beautiful interactive Canvas Bar Graph
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(horizontal = 8.dp)
                ) {
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val accentColor = Color(0xFFD97706)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val yAxisLineX = 40.dp.toPx()
                        val graphBottomY = canvasHeight - 30.dp.toPx()

                        // Calculate max value for scaling
                        val maxValue = maxOf(totalSantri, santriAktif, santriAlumni, 1).toFloat()
                        val unitHeight = (graphBottomY - 20.dp.toPx()) / maxValue

                        // Draw Grid/Y-Axis lines
                        val gridLineCount = 4
                        for (i in 0..gridLineCount) {
                            val lineY = graphBottomY - (i * ((graphBottomY - 20.dp.toPx()) / gridLineCount))
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(yAxisLineX, lineY),
                                end = Offset(canvasWidth, lineY),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Coordinates for drawing bars
                        val barWidth = 44.dp.toPx()
                        val labels = listOf("Total", "Aktif", "Alumni")
                        val colors = listOf(primaryColor, secondaryColor, accentColor)
                        val values = listOf(totalSantri, santriAktif, santriAlumni)

                        val usableWidth = canvasWidth - yAxisLineX
                        val spaceBetween = (usableWidth - (barWidth * labels.size)) / (labels.size + 1)

                        for (i in labels.indices) {
                            val barX = yAxisLineX + spaceBetween + i * (barWidth + spaceBetween)
                            val barHeightValue = values[i] * unitHeight
                            val barTopY = graphBottomY - barHeightValue

                            // Draw Rounded Rect/Bar
                            drawRect(
                                color = colors[i],
                                size = Size(barWidth, barHeightValue),
                                topLeft = Offset(barX, barTopY)
                            )

                            // Light upper cap highlight
                            drawLine(
                                color = Color.White.copy(alpha = 0.4f),
                                start = Offset(barX, barTopY),
                                end = Offset(barX + barWidth, barTopY),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                    }
                }

                // Custom legends underneath
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem("Semua Santri", MaterialTheme.colorScheme.primary, totalSantri)
                    LegendItem("Aktif", MaterialTheme.colorScheme.secondary, santriAktif)
                    LegendItem("Alumni", Color(0xFFD97706), santriAlumni)
                }
            }
        }

        // Quick Navigation Section
        Text(
            text = "Navigasi Cepat",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionButton(
                label = "Lihat Santri",
                icon = Icons.Default.FormatListBulleted,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("santri_list") }
            )
            QuickActionButton(
                label = "Lihat Galeri",
                icon = Icons.Outlined.PhotoLibrary,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("galeri") }
            )
            QuickActionButton(
                label = "Info Kontak",
                icon = Icons.Default.AlternateEmail,
                color = Color(0xFF4F46E5),
                modifier = Modifier.weight(1f),
                onClick = { onNavigate("kontak") }
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val containerColor = tint.copy(alpha = 0.05f)
    val borderColor = tint.copy(alpha = 0.15f)
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black
                    ),
                    color = tint
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = tint.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$label: $count",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun QuickActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
