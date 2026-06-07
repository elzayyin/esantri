package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SettingsEntity

@Composable
fun KontakScreen(
    settings: SettingsEntity?
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val namaPondok = settings?.namaPondok ?: "Pondok Pesantren Al-Hidayah"
    val alamat = settings?.alamat ?: "Jl. Raya Pesantren No. 45, Kel. Sukamaju, Kec. Cibeunying, Bandung, Jawa Barat 40123"
    val whatsapp = settings?.whatsapp ?: "08123456780"
    val email = settings?.email ?: "info@alhidayah.or.id"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top visual banner of Pondok Pesantren
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = "https://images.unsplash.com/photo-1541339907198-e08756dedf3f?w=800",
                    contentDescription = "Pondok Pesantren",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Ambient Dark visual scrim overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = namaPondok,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Kontak Resmi & Alamat Lembaga",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Contact Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("contact_info_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Informasi Kontak",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Divider()

                // Address
                ContactRowItem(
                    icon = Icons.Default.LocationOn,
                    title = "Alamat Lengkap",
                    value = alamat,
                    actionText = "Salin Alamat",
                    onAction = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Alamat Pesantren", alamat)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Alamat disalin!", Toast.LENGTH_SHORT).show()
                    }
                )

                // Whatsapp click (Direct link triggers)
                ContactRowItem(
                    icon = Icons.Default.Chat,
                    title = "Nomor WhatsApp",
                    value = whatsapp,
                    actionText = "Kirim Chat",
                    onAction = {
                        try {
                            val cleanPhone = whatsapp.replace("+", "").replace(" ", "").replace("-", "")
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=Assalamualaikum%20admin%20$namaPondok...")
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Tidak bisa membuka WhatsApp", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // Email
                ContactRowItem(
                    icon = Icons.Default.Email,
                    title = "E-mail Resmi",
                    value = email,
                    actionText = "Kirim Email",
                    onAction = {
                        try {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:")
                                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                                putExtra(Intent.EXTRA_SUBJECT, "Pertanyaan Terkait Pusat Data Santri")
                            }
                            context.startActivity(Intent.createChooser(emailIntent, "Kirim email..."))
                        } catch (e: Exception) {
                            Toast.makeText(context, "Aplikasi email tidak ditemukan", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        }

        // Google Maps Card Embed representational layout
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("maps_embed_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Google Maps Lokasi",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful Stylized Static Map graphics
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    // Modern Vector Grid map graphics representational look
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val stroke = Stroke(width = 1.dp.toPx())
                        for (i in 0..size.width.toInt() step 50) {
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(i.toFloat(), 0f), Offset(i.toFloat(), size.height), strokeWidth = 1.dp.toPx())
                        }
                        for (i in 0..size.height.toInt() step 50) {
                            drawLine(Color.White.copy(alpha = 0.5f), Offset(0f, i.toFloat()), Offset(size.width, i.toFloat()), strokeWidth = 1.dp.toPx())
                        }
                    }

                    // Simulated navigation path curves
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = "Pin lokasi",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Presensi Koordinat Terdaftar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Kota Bandung, Jawa Barat, Indonesia",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Interactive trigger button to run Geo Navigation on Google Maps client App
                Button(
                    onClick = {
                        try {
                            val rawMapQuery = "geo:0,0?q=" + Uri.encode(alamat)
                            val mapUri = Uri.parse(rawMapQuery)
                            val mapIntent = Intent(Intent.ACTION_VIEW, mapUri)
                            mapIntent.setPackage("com.google.android.apps.maps")
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // Fallback to web browser maps search
                            val webMapUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(alamat))
                            val webIntent = Intent(Intent.ACTION_VIEW, webMapUri)
                            context.startActivity(webIntent)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_maps_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = "Buka maps")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buka Navigasi Rute Maps", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ContactRowItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    actionText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = actionText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .clickable(onClick = onAction)
                    .padding(vertical = 4.dp)
            )
        }
    }
}
