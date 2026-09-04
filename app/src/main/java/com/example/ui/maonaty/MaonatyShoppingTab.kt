package com.example.ui.maonaty

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MaonatyShoppingItem
import com.example.ui.SoftTheme

@Composable
fun ShoppingTab(
    shoppingList: List<MaonatyShoppingItem>,
    onAddClick: () -> Unit,
    onDelete: (MaonatyShoppingItem) -> Unit,
    onToggleBought: (MaonatyShoppingItem) -> Unit,
    onAutoGenerate: () -> Unit,
    onClearAll: () -> Unit
) {
    val context = LocalContext.current
    val unbought = shoppingList.filter { !it.isBought }
    val bought = shoppingList.filter { it.isBought }

    val totalBudget = shoppingList.sumOf { it.price * it.quantity }
    val boughtBudget = bought.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftPink.copy(alpha = 0.08f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ميزانية التسوق المقدرة 🛒", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                        Text(
                            text = "تم شراء ${boughtBudget} ج.م من أصل ${totalBudget} ج.م",
                            color = SoftTheme.SoftGray,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "${totalBudget - boughtBudget} ج.م متبقي",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.SoftPink
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAutoGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("توليد من النواقص ⚡", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SoftTheme.DeepSlate)
                    }

                    if (unbought.isNotEmpty()) {
                        Button(
                            onClick = {
                                val textBuilder = StringBuilder()
                                textBuilder.append("🛒 قائمة مشتريات مؤونتي:\n")
                                unbought.forEachIndexed { i, item ->
                                    textBuilder.append("${i + 1}. ${item.name} (${item.quantity} ${item.unit})\n")
                                }
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, textBuilder.toString())
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة قائمة المشتريات")
                                context.startActivity(shareIntent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.DeepSlate),
                            modifier = Modifier.weight(0.7f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = SoftTheme.MintTeal, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("مشاركة 📤", fontSize = 11.sp, color = SoftTheme.TextWhite)
                        }
                    }

                    OutlinedButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SoftTheme.RedDanger),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("مسح", fontSize = 11.sp)
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("add_custom_shopping_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة سلعة تسوق مخصصة 🛒", fontWeight = FontWeight.Bold)
            }
        }

        if (shoppingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, tint = SoftTheme.SoftGray, modifier = Modifier.size(56.dp))
                    Text(
                        text = "سلة المشتريات فارغة.\nانقري على 'توليد من النواقص ⚡' لسحب النواقص تلقائياً!",
                        color = SoftTheme.SoftGray,
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (unbought.isNotEmpty()) {
                    item {
                        Text("سلع للشراء (${unbought.size})", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                    }
                    items(unbought, key = { it.id }) { item ->
                        ShoppingListItemRow(item, onToggleBought, onDelete)
                    }
                }

                if (bought.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("تم شراؤها (${bought.size})", fontWeight = FontWeight.Bold, color = SoftTheme.SoftGray, fontSize = 14.sp)
                    }
                    items(bought, key = { it.id }) { item ->
                        ShoppingListItemRow(item, onToggleBought, onDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListItemRow(
    item: MaonatyShoppingItem,
    onToggleBought: (MaonatyShoppingItem) -> Unit,
    onDelete: (MaonatyShoppingItem) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isBought) SoftTheme.CardSlate.copy(alpha = 0.5f) else SoftTheme.CardSlate
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleBought(item) }
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
                    checked = item.isBought,
                    onCheckedChange = { onToggleBought(item) },
                    colors = CheckboxDefaults.colors(checkedColor = SoftTheme.SoftPink, checkmarkColor = SoftTheme.DeepSlate)
                )

                Column {
                    Text(
                        text = item.name,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isBought) SoftTheme.SoftGray else SoftTheme.TextWhite,
                        textDecoration = if (item.isBought) TextDecoration.LineThrough else TextDecoration.None,
                        fontSize = 14.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${item.quantity} ${item.unit}",
                            fontSize = 11.sp,
                            color = SoftTheme.SoftGray
                        )
                        if (item.price > 0) {
                            Text(
                                text = "•  ${item.price} ج.م / وحدة",
                                fontSize = 11.sp,
                                color = SoftTheme.SoftGray
                            )
                        }
                        if (item.autoGenerated) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(SoftTheme.SoftPink.copy(alpha = 0.15f))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("تلقائي ⚡", color = SoftTheme.SoftPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (item.price > 0) {
                    Text(
                        text = "${item.price * item.quantity} ج.م",
                        fontWeight = FontWeight.Bold,
                        color = if (item.isBought) SoftTheme.SoftGray else SoftTheme.SoftPink,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = { onDelete(item) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = SoftTheme.RedDanger)
                }
            }
        }
    }
}
