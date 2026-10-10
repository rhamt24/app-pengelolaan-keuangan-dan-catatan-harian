package com.example.ui.notes

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.NoteEntity
import com.example.data.model.NoteTags
import com.example.ui.components.AppSearchBar
import com.example.ui.components.ConfirmDeleteDialog
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.Formatters
import com.example.ui.viewmodel.AppViewModel

@Composable
fun NotesScreen(
    viewModel: AppViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val searchQuery by viewModel.notesSearchQuery.collectAsStateWithLifecycle()
    val selectedTag by viewModel.selectedNoteTag.collectAsStateWithLifecycle()
    val showFavoritesOnly by viewModel.showOnlyFavorites.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<NoteEntity?>(null) }
    var noteToDelete by remember { mutableStateOf<NoteEntity?>(null) }

    val pinnedNotes = remember(notes) { notes.filter { it.isPinned } }
    val regularNotes = remember(notes) { notes.filter { !it.isPinned } }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Search Bar
            item {
                AppSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.setNotesSearchQuery(it) },
                    placeholder = "Cari judul atau isi catatan..."
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Tag & Favorite Filter Row Minimalist
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedTag == null && !showFavoritesOnly,
                            onClick = {
                                viewModel.setNoteTagFilter(null)
                                if (showFavoritesOnly) viewModel.toggleFavoriteFilter()
                            },
                            label = { Text("Semua", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedTag == null && !showFavoritesOnly,
                                borderColor = if (selectedTag == null && !showFavoritesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("filter_all_notes")
                        )
                    }

                    item {
                        FilterChip(
                            selected = showFavoritesOnly,
                            onClick = { viewModel.toggleFavoriteFilter() },
                            label = { Text("⭐ Favorit", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFEF3C7),
                                selectedLabelColor = Color(0xFF92400E)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = showFavoritesOnly,
                                borderColor = if (showFavoritesOnly) Color(0xFFF59E0B) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("filter_favorite_notes")
                        )
                    }

                    items(NoteTags) { tag ->
                        val isSelected = selectedTag == tag
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                viewModel.setNoteTagFilter(if (isSelected) null else tag)
                            },
                            label = { Text(tag, fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("filter_tag_$tag")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            if (notes.isEmpty()) {
                item {
                    EmptyStateCard(
                        emoji = "📝",
                        title = "Belum Ada Catatan",
                        message = if (searchQuery.isNotEmpty()) {
                            "Tidak ada catatan dengan kata kunci \"$searchQuery\"."
                        } else {
                            "Tekan tombol (+) untuk menulis catatan harian baru."
                        }
                    )
                }
            } else {
                // Pinned Section
                if (pinnedNotes.isNotEmpty()) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Disematkan",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    items(pinnedNotes, key = { "${it.id}_${it.updatedAtMillis}" }) { note ->
                        NoteCard(
                            note = note,
                            onCardClick = { noteToEdit = note },
                            onTogglePin = { viewModel.togglePinNote(note) },
                            onToggleFavorite = { viewModel.toggleFavoriteNote(note) },
                            onEdit = { noteToEdit = note },
                            onDelete = { noteToDelete = note }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (regularNotes.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Lainnya",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }
                    }
                }

                // Regular Notes
                items(regularNotes, key = { "${it.id}_${it.updatedAtMillis}" }) { note ->
                    NoteCard(
                        note = note,
                        onCardClick = { noteToEdit = note },
                        onTogglePin = { viewModel.togglePinNote(note) },
                        onToggleFavorite = { viewModel.toggleFavoriteNote(note) },
                        onEdit = { noteToEdit = note },
                        onDelete = { noteToDelete = note }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Minimalist FAB
        ExtendedFloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("add_note_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Tulis Catatan") },
            text = { Text("Tulis Catatan", fontWeight = FontWeight.Bold) }
        )
    }

    // Add Note Dialog
    if (showAddDialog) {
        AddEditNoteDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newNote ->
                viewModel.addNote(newNote)
                showAddDialog = false
            }
        )
    }

    // Edit Note Dialog
    if (noteToEdit != null) {
        AddEditNoteDialog(
            initialNote = noteToEdit,
            onDismiss = { noteToEdit = null },
            onSave = { updatedNote ->
                viewModel.updateNote(updatedNote)
                noteToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (noteToDelete != null) {
        val note = noteToDelete!!
        ConfirmDeleteDialog(
            title = "Hapus Catatan?",
            message = "Apakah Anda yakin ingin menghapus \"${note.title}\"?",
            onConfirm = {
                viewModel.deleteNote(note)
                noteToDelete = null
            },
            onDismiss = { noteToDelete = null }
        )
    }
}

@Composable
fun NoteCard(
    note: NoteEntity,
    onCardClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("note_card_${note.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Mood, Title, Badges & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.mood,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(end = 8.dp, top = 1.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = note.title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                        ) {
                            Text(
                                text = note.tag,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatRelativeTime(note.updatedAtMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Favorite
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("card_favorite_button_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = "Favorit",
                        tint = if (note.isFavorite) Color(0xFFF59E0B) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Pin
                IconButton(
                    onClick = onTogglePin,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("card_pin_button_${note.id}")
                ) {
                    Icon(
                        imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Sematkan",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Menu
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("card_menu_button_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opsi",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onEdit()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                showMenu = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            // Note Content Snippet
            if (note.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
