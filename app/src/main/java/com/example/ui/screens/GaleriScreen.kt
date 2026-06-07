package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.Galeri
import com.example.viewmodel.SantriViewModel

@Composable
fun GaleriScreen(
    viewModel: SantriViewModel,
    galeriList: List<Galeri>,
    isAdmin: Boolean,
    onAddClick: () -> Unit,
    onDeleteClick: (Galeri) -> Unit
) {
    var selectedFullPhoto by remember { mutableStateOf<Galeri?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with action upload button if Admin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Galeri Dokumentasi Pesantren",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kegiatan dan aktivitas santri selama di pondok",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }

                if (isAdmin) {
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("upload_gallery_trigger")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Upload", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Grid Layout of images
            if (galeriList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Empty",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Galeri masih kosong",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.weight(1f).testTag("gallery_grid"),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(galeriList, key = { it.id }) { item ->
                        GaleriCardItem(
                            item = item,
                            isAdmin = isAdmin,
                            onCardClick = { selectedFullPhoto = item },
                            onDeleteClick = { onDeleteClick(item) }
                        )
                    }
                }
            }
        }

        // Fullscreen Lightbox image Viewer Overlay
        if (selectedFullPhoto != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { selectedFullPhoto = null }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(selectedFullPhoto!!.foto)
                                .crossfade(true)
                                .build(),
                            contentDescription = selectedFullPhoto!!.judul,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = selectedFullPhoto!!.judul,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(48.dp))
                }

                // Close button in top RHS
                IconButton(
                    onClick = { selectedFullPhoto = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun GaleriCardItem(
    item: Galeri,
    isAdmin: Boolean,
    onCardClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onCardClick)
            .testTag("gallery_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background image
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.foto)
                    .crossfade(true)
                    .error(android.R.drawable.ic_menu_gallery)
                    .build(),
                contentDescription = item.judul,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim overlay at the bottom for title legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )

            // Title
            Text(
                text = item.judul,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            )

            // Trash edit controls if Admin
            if (isAdmin) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.85f), CircleShape)
                        .testTag("delete_gallery_button_${item.id}")
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Hapus foto",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
