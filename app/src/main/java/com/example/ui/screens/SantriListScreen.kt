package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.example.data.Santri
import com.example.viewmodel.SantriViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SantriListScreen(
    viewModel: SantriViewModel,
    santriList: List<Santri>, // Total matching filters
    paginatedList: List<Santri>, // Currently visible on page
    currentPage: Int,
    totalCount: Int,
    isAdmin: Boolean,
    onAddClick: () -> Unit,
    onDetailClick: (Santri) -> Unit,
    onEditClick: (Santri) -> Unit,
    onDeleteClick: (Santri) -> Unit,
    onImportExcelClick: () -> Unit,
    onExportPdfClick: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val genderFilter by viewModel.genderFilter.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()
    val itemsPerPage = viewModel.itemsPerPage

    // Filter status details
    val statusOptions = listOf("Semua", "Aktif", "Alumni")
    val genderOptions = listOf("Semua", "Laki-laki", "Perempuan")
    val sortOptions = listOf("Baru Ditambahkan", "Nama A-Z", "Nama Z-A", "Nomor Induk")

    // Sort Dropdown expansion state
    var sortExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Add Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Cari Nomer Induk, Nama & kota...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear query")
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .testTag("santri_search_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Trigger action to Add New Santri
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onAddClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(4.dp, CircleShape)
                        .testTag("add_santri_fab"),
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Tambah Santri")
                }
            }
        }

        // Import & Export Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PDF Export button - Available to everyone or at least admin, usually helpful to anyone
            Button(
                onClick = onExportPdfClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(42.dp)
                    .testTag("export_pdf_button"),
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Ekspor PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            if (isAdmin) {
                // Excel Import button - admin only
                Button(
                    onClick = onImportExcelClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("import_excel_button"),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.FileUpload, contentDescription = "Import Excel", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Excel/CSV", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Expanded Filters Panel
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Status Filters pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Status: ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(60.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4,
                ) {
                    statusOptions.forEach { option ->
                        val selected = statusFilter == option
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setStatusFilter(option) },
                            label = { Text(option, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("filter_status_$option")
                        )
                    }
                }
            }

            // Gender Filters pills
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Gender: ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.width(60.dp)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4,
                ) {
                    genderOptions.forEach { option ->
                        val selected = genderFilter == option
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setGenderFilter(option) },
                            label = { Text(option, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                selectedLabelColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier.testTag("filter_gender_$option")
                        )
                    }
                }
            }

            // Sort Selector dropdown inside form
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Sort,
                        contentDescription = "Sorting tool",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Urutkan: ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = sortBy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { sortExpanded = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("sort_filter_trigger")
                    )
                    DropdownMenu(
                        expanded = sortExpanded,
                        onDismissRequest = { sortExpanded = false }
                    ) {
                        sortOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    viewModel.setSortBy(option)
                                    sortExpanded = false
                                },
                                modifier = Modifier.testTag("sort_option_$option")
                            )
                        }
                    }
                }

                // Show total record size indicator
                Text(
                    text = "Ditemukan: $totalCount data",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        // Active List content space
        if (paginatedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "No data icon",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f),
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Data santri tidak ditemukan",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = "Silakan ubah kueri pencarian atau filter Anda.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).testTag("santri_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(paginatedList, key = { it.id }) { santri ->
                    SantriItemCard(
                        santri = santri,
                        isAdmin = isAdmin,
                        onCardClick = { onDetailClick(santri) },
                        onEditClick = { onEditClick(santri) },
                        onDeleteClick = { onDeleteClick(santri) }
                    )
                }
            }

            // High craft Pagination Controller Footer
            val totalPages = kotlin.math.ceil(totalCount.toDouble() / itemsPerPage).toInt()
            if (totalPages > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.setPage(currentPage - 1) },
                        enabled = currentPage > 0,
                        modifier = Modifier.testTag("prev_page_button")
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Sebelumnya")
                    }

                    Text(
                        text = "Halaman ${currentPage + 1} dari $totalPages",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    IconButton(
                        onClick = { viewModel.setPage(currentPage + 1) },
                        enabled = currentPage + 1 < totalPages,
                        modifier = Modifier.testTag("next_page_button")
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Selanjutnya")
                    }
                }
            }
        }
    }
}

@Composable
fun SantriItemCard(
    santri: Santri,
    isAdmin: Boolean,
    onCardClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("santri_card_${santri.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // vertical 4:5 photo layout aspect ratio
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(santri.foto)
                        .crossfade(true)
                        .error(android.R.drawable.ic_menu_gallery)
                        .build(),
                    contentDescription = "Foto ${santri.namaLengkap}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Data Santri on Right
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "NI: ${santri.nomorInduk}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )

                    // Colored status badge index
                    StatusBadge(status = santri.statusSantri)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = santri.namaLengkap,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = "Wali: ${santri.namaWali}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Bottom location identifier row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = santri.tempatLahir,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Actions toolbar indicators
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lihat Detail",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable(onClick = onCardClick)
                            .padding(vertical = 4.dp)
                    )

                    if (isAdmin) {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = onEditClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("card_edit_${santri.id}")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit data",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            IconButton(
                                onClick = onDeleteClick,
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("card_delete_${santri.id}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete record",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val containerColor = if (status == "Aktif") Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
    val contentColor = if (status == "Aktif") Color(0xFF15803D) else Color(0xFF475569)

    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status.uppercase(),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
