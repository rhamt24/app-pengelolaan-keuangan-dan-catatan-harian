package com.example.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.NoteEntity
import com.example.data.model.NoteMoods
import com.example.data.model.NoteTags

val NoteAccentColors = listOf(
    Color(0xFFE2E8F0),
    Color(0xFFFEF3C7),
    Color(0xFFDBEAFE),
    Color(0xFFEDE9FE),
    Color(0xFFD1FAE5),
    Color(0xFFFFE4E6)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditNoteDialog(
    initialNote: NoteEntity? = null,
    onDismiss: () -> Unit,
    onSave: (NoteEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var content by remember { mutableStateOf(initialNote?.content ?: "") }
    var tag by remember { mutableStateOf(initialNote?.tag ?: NoteTags.first()) }
    var mood by remember { mutableStateOf(initialNote?.mood ?: NoteMoods.first().first) }
    var isPinned by remember { mutableStateOf(initialNote?.isPinned ?: false) }
    var isFavorite by remember { mutableStateOf(initialNote?.isFavorite ?: false) }
    var colorIndex by remember {
        mutableStateOf(
            (initialNote?.colorIndex ?: 0).coerceIn(0, NoteAccentColors.size - 1)
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val wordCount = remember(content) {
        if (content.isBlank()) 0 else content.trim().split("\\s+".toRegex()).size
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("add_note_dialog"),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with Pin & Favorite Toggles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialNote == null) "Tulis Catatan" else "Edit Catatan",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pin Toggle
                        IconToggleButton(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            modifier = Modifier.testTag("toggle_pin_note_button")
                        ) {
                            Icon(
                                imageVector = if (isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                contentDescription = "Sematkan",
                                tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Favorite Toggle
                        IconToggleButton(
                            checked = isFavorite,
                            onCheckedChange = { isFavorite = it },
                            modifier = Modifier.testTag("toggle_favorite_note_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "Favorit",
                                tint = if (isFavorite) Color(0xFFEAB308) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_note_dialog")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Judul Catatan
                Text(
                    text = "Judul",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("note_title_input"),
                    placeholder = { Text("Judul catatan...") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Mood / Perasaan Hari Ini
                Text(
                    text = "Mood Hari Ini",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoteMoods.forEach { (emojiIcon, moodLabel) ->
                        val isSelected = mood == emojiIcon
                        FilterChip(
                            selected = isSelected,
                            onClick = { mood = emojiIcon },
                            label = { Text("$emojiIcon $moodLabel", fontSize = 12.sp) },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("mood_chip_$moodLabel")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Kategori / Tag
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NoteTags.forEach { tagName ->
                        val isSelected = tag == tagName
                        FilterChip(
                            selected = isSelected,
                            onClick = { tag = tagName },
                            label = { Text(tagName, fontSize = 12.sp) },
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
                            modifier = Modifier.testTag("tag_chip_$tagName")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Isi Catatan
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Isi Catatan",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$wordCount kata",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = {
                        content = it
                        errorMessage = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .testTag("note_content_input"),
                    placeholder = { Text("Tulis kegiatan harian, ide, catatan...") },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_note_button")
                    ) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank() && content.isBlank()) {
                                errorMessage = "Judul atau isi catatan tidak boleh kosong"
                                return@Button
                            }
                            val finalTitle = title.trim().ifEmpty {
                                content.lines().firstOrNull()?.take(30) ?: "Catatan Harian"
                            }
                            val newNote = NoteEntity(
                                id = initialNote?.id ?: 0L,
                                title = finalTitle,
                                content = content.trim(),
                                tag = tag,
                                mood = mood,
                                isPinned = isPinned,
                                isFavorite = isFavorite,
                                colorIndex = colorIndex,
                                createdAtMillis = initialNote?.createdAtMillis ?: System.currentTimeMillis(),
                                updatedAtMillis = System.currentTimeMillis()
                            )
                            onSave(newNote)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Text("Simpan", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
