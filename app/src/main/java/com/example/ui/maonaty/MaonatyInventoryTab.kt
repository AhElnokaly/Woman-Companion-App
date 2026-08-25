package com.example.ui.maonaty

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaonatyInventoryItem
import com.example.ui.SoftTheme

@Composable
fun InventoryTab(
    inventory: List<MaonatyInventoryItem>,
    onAddClick: () -> Unit,
    onDelete: (MaonatyInventoryItem) -> Unit,
    onAdjustQty: (MaonatyInventoryItem, Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("الكل") }

    val categories = listOf("الكل", "معلبات وحبوب", "منتجات ألبان", "خضار وفواكه", "لحوم ودواجن", "بهارات وتوابل", "أدوات تنظيف", "أخرى")

    val filteredInventory = remember(inventory, searchQuery, selectedCategory) {
        inventory.filter {
            val matchesSearch = it.name.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "الكل" || it.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Actions and Search bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("ابحث عن سلعة...", color = SoftTheme.SoftGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftTheme.SoftPink) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SoftTheme.SoftPink,
                    unfocusedBorderColor = SoftTheme.SoftGray,
                    focusedTextColor = SoftTheme.TextWhite,
                    unfocusedTextColor = SoftTheme.TextWhite
                ),
                singleLine = true
            )

            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("add_inventory_btn"),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة")
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة", fontWeight = FontWeight.Bold)
            }
        }

        // Horizontal Category Row
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
            containerColor = Color.Transparent,
            contentColor = SoftTheme.SoftPink,
            edgePadding = 0.dp,
            indicator = {},
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            categories.forEach { cat ->
                val isSel = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSel) SoftTheme.SoftPink else SoftTheme.CardSlate)
                        .clickable { selectedCategory = cat }
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

        if (filteredInventory.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SoftTheme.SoftGray, modifier = Modifier.size(48.dp))
                    Text("لا توجد مواد في مخزن مطبخك تتطابق مع البحث الحالي.", color = SoftTheme.SoftGray, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredInventory, key = { it.id }) { item ->
                    val isCritical = item.quantity <= item.minQuantity
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.dp,
                                color = if (isCritical) SoftTheme.RedDanger.copy(alpha = 0.6f) else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCritical) SoftTheme.RedDanger.copy(alpha = 0.05f) else SoftTheme.CardSlate
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = item.name,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftTheme.TextWhite,
                                        fontSize = 15.sp
                                    )
                                    if (isCritical) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SoftTheme.RedDanger.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("ناقص ⚠️", color = SoftTheme.RedDanger, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${item.category}",
                                        fontSize = 11.sp,
                                        color = SoftTheme.SoftGray
                                    )
                                    Text(
                                        text = "الحد الحرج: ${item.minQuantity} ${item.unit}",
                                        fontSize = 11.sp,
                                        color = SoftTheme.SoftGray
                                    )
                                }
                            }

                            // Stock adjustment controls
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledIconButton(
                                    onClick = { onAdjustQty(item, -0.5) },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.DeepSlate),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "تقليل الكمية", tint = SoftTheme.SoftPink)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCritical) SoftTheme.RedDanger else SoftTheme.MintTeal,
                                        fontSize = 16.sp
                                    )
                                    Text(text = item.unit, fontSize = 10.sp, color = SoftTheme.SoftGray)
                                }

                                FilledIconButton(
                                    onClick = { onAdjustQty(item, 0.5) },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.DeepSlate),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "زيادة الكمية", tint = SoftTheme.SoftPink)
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                IconButton(
                                    onClick = { onDelete(item) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف المادة", tint = SoftTheme.RedDanger)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
