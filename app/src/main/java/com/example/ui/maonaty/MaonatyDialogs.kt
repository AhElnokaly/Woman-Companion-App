package com.example.ui.maonaty

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.SoftTheme

@Composable
fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, Double, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("معلبات وحبوب") }
    var quantityText by remember { mutableStateOf("") }
    var minQuantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("كيلوجرام") }
    var priceText by remember { mutableStateOf("") }

    val categories = listOf("معلبات وحبوب", "منتجات ألبان", "خضار وفواكه", "لحوم ودواجن", "بهارات وتوابل", "أدوات تنظيف", "أخرى")
    val units = listOf("كيلوجرام", "جرام", "لتر", "علبة", "كيس", "حبة")

    var expandedCat by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("إضافة مادة للمخزون 📦", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SoftTheme.TextWhite)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم السلعة (مثال: أرز بسمتي)", color = SoftTheme.SoftGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    )
                )

                // Category Selection
                Box {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("القسم: $category", color = SoftTheme.TextWhite)
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false },
                        modifier = Modifier.background(SoftTheme.CardSlate)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = SoftTheme.TextWhite) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("الكمية الحالية", color = SoftTheme.SoftGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )

                    OutlinedTextField(
                        value = minQuantityText,
                        onValueChange = { minQuantityText = it },
                        label = { Text("الحد الحرج", color = SoftTheme.SoftGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { expandedUnit = true },
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, SoftTheme.SoftGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("الوحدة: $unit", color = SoftTheme.TextWhite, fontSize = 11.sp)
                        }
                        DropdownMenu(
                            expanded = expandedUnit,
                            onDismissRequest = { expandedUnit = false },
                            modifier = Modifier.background(SoftTheme.CardSlate)
                        ) {
                            units.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text(u, color = SoftTheme.TextWhite) },
                                    onClick = {
                                        unit = u
                                        expandedUnit = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("سعر تقريبي (ج.م)", color = SoftTheme.SoftGray, fontSize = 10.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull() ?: 1.0
                            val minQty = minQuantityText.toDoubleOrNull() ?: 1.0
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty()) {
                                onConfirm(name, category, qty, minQty, unit, price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حفظ")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SoftTheme.RedDanger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
fun AddShoppingDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Double, String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("معلبات وحبوب") }
    var quantityText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("حبة") }
    var priceText by remember { mutableStateOf("") }

    val categories = listOf("معلبات وحبوب", "منتجات ألبان", "خضار وفواكه", "لحوم ودواجن", "بهارات وتوابل", "أدوات تنظيف", "أخرى")
    val units = listOf("كيلوجرام", "جرام", "لتر", "علبة", "كيس", "حبة")

    var expandedCat by remember { mutableStateOf(false) }
    var expandedUnit by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("إضافة للمشتريات 🛒", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SoftTheme.TextWhite)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم السلعة للتسوق", color = SoftTheme.SoftGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    )
                )

                Box {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("القسم: $category", color = SoftTheme.TextWhite)
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false },
                        modifier = Modifier.background(SoftTheme.CardSlate)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = SoftTheme.TextWhite) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("الكمية المطلوبة", color = SoftTheme.SoftGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("سعر تقريبي", color = SoftTheme.SoftGray) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SoftTheme.SoftPink,
                            focusedTextColor = SoftTheme.TextWhite,
                            unfocusedTextColor = SoftTheme.TextWhite
                        )
                    )
                }

                Box {
                    OutlinedButton(
                        onClick = { expandedUnit = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("الوحدة: $unit", color = SoftTheme.TextWhite)
                    }
                    DropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false },
                        modifier = Modifier.background(SoftTheme.CardSlate)
                    ) {
                        units.forEach { u ->
                            DropdownMenuItem(
                                text = { Text(u, color = SoftTheme.TextWhite) },
                                onClick = {
                                    unit = u
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val qty = quantityText.toDoubleOrNull() ?: 1.0
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            if (name.isNotEmpty()) {
                                onConfirm(name, category, qty, unit, price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إضافة")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SoftTheme.RedDanger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("🧼 تنظيف وترتيب") }
    var priority by remember { mutableStateOf("⚡ متوسط") }

    val categories = listOf("🧼 تنظيف وترتيب", "🛠️ صيانة وأعطال", "📦 جرد وتخزين", "📅 شؤون منزلية")
    val priorities = listOf("🔴 عاجل", "⚡ متوسط", "🟢 عادي")

    var expandedCat by remember { mutableStateOf(false) }
    var expandedPri by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("إضافة مهمة منزلية جديدة 📝", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SoftTheme.TextWhite)

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان المهمة (مثال: تنظيف وتصفية الشفاط)", color = SoftTheme.SoftGray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    )
                )

                Box {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("التصنيف: $category", color = SoftTheme.TextWhite)
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false },
                        modifier = Modifier.background(SoftTheme.CardSlate)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat, color = SoftTheme.TextWhite) },
                                onClick = {
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                Box {
                    OutlinedButton(
                        onClick = { expandedPri = true },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, SoftTheme.SoftGray),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("الأهمية: $priority", color = SoftTheme.TextWhite)
                    }
                    DropdownMenu(
                        expanded = expandedPri,
                        onDismissRequest = { expandedPri = false },
                        modifier = Modifier.background(SoftTheme.CardSlate)
                    ) {
                        priorities.forEach { pri ->
                            DropdownMenuItem(
                                text = { Text(pri, color = SoftTheme.TextWhite) },
                                onClick = {
                                    priority = pri
                                    expandedPri = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (title.isNotEmpty()) {
                                val tomorrow = System.currentTimeMillis() + 24 * 60 * 60 * 1000
                                onConfirm(title, category, priority, tomorrow)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حفظ")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        border = BorderStroke(1.dp, SoftTheme.RedDanger),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("إلغاء")
                    }
                }
            }
        }
    }
}
