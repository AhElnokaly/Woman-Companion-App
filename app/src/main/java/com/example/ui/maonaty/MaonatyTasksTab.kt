package com.example.ui.maonaty

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaonatyHouseholdTask
import com.example.ui.SoftTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HouseholdTasksTab(
    tasks: List<MaonatyHouseholdTask>,
    onAddClick: () -> Unit,
    onDelete: (MaonatyHouseholdTask) -> Unit,
    onToggleComplete: (MaonatyHouseholdTask) -> Unit,
    onClearAll: () -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    val categories = listOf("الكل", "🧼 تنظيف وترتيب", "🛠️ صيانة وأعطال", "📦 جرد وتخزين", "📅 شؤون منزلية")

    val filteredTasks = remember(tasks, selectedCategoryFilter) {
        tasks.filter {
            selectedCategoryFilter == "الكل" || it.category == selectedCategoryFilter
        }
    }

    val incomplete = filteredTasks.filter { !it.isCompleted }
    val completed = filteredTasks.filter { it.isCompleted }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("add_task_btn")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة مهمة منزلية جديدة", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onClearAll,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SoftTheme.RedDanger)
            ) {
                Text("مسح الكل")
            }
        }

        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategoryFilter).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = SoftTheme.SoftPink,
            edgePadding = 0.dp,
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                val isSel = selectedCategoryFilter == cat
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) SoftTheme.SoftPink else SoftTheme.CardSlate)
                        .clickable { selectedCategoryFilter = cat }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSel) SoftTheme.DeepSlate else SoftTheme.TextWhite,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SoftTheme.MintTeal, modifier = Modifier.size(56.dp))
                    Text("أحسنتِ! لا توجد مهام منزلية متبقية في هذا القسم! 🎉", color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (incomplete.isNotEmpty()) {
                    item {
                        Text("المهام الجارية (${incomplete.size})", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                    }
                    items(incomplete, key = { it.id }) { task ->
                        TaskRowItem(task, onToggleComplete, onDelete)
                    }
                }

                if (completed.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("المهام المكتملة (${completed.size})", fontWeight = FontWeight.Bold, color = SoftTheme.SoftGray, fontSize = 14.sp)
                    }
                    items(completed, key = { it.id }) { task ->
                        TaskRowItem(task, onToggleComplete, onDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun TaskRowItem(
    task: MaonatyHouseholdTask,
    onToggleComplete: (MaonatyHouseholdTask) -> Unit,
    onDelete: (MaonatyHouseholdTask) -> Unit
) {
    val formatter = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val formattedDate = remember(task.dueDate) { formatter.format(Date(task.dueDate)) }

    val priorityColor = when (task.priority) {
        "🔴 عاجل" -> SoftTheme.RedDanger
        "⚡ متوسط" -> SoftTheme.GoldFasting
        else -> SoftTheme.MintTeal
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) SoftTheme.CardSlate.copy(alpha = 0.5f) else SoftTheme.CardSlate
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleComplete(task) }
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { onToggleComplete(task) },
                    colors = CheckboxDefaults.colors(checkedColor = SoftTheme.SoftPink, checkmarkColor = SoftTheme.DeepSlate)
                )

                Column {
                    Text(
                        text = task.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (task.isCompleted) SoftTheme.SoftGray else SoftTheme.TextWhite,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(priorityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(task.priority, color = priorityColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Text("تاريخ الاستحقاق: $formattedDate", fontSize = 11.sp, color = SoftTheme.SoftGray)
                    }
                }
            }

            IconButton(onClick = { onDelete(task) }, modifier = Modifier.size(36.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "حذف المهمة", tint = SoftTheme.RedDanger)
            }
        }
    }
}
