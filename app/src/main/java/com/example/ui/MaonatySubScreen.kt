package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.MaonatyHouseholdTask
import com.example.data.MaonatyInventoryItem
import com.example.data.MaonatyShoppingItem
import com.example.viewmodel.WomanCompanionViewModel
import java.text.SimpleDateFormat
import java.util.*

// Recipe definitions inside kotlin
data class MaonatyRecipe(
    val name: String,
    val timeMinutes: Int,
    val difficulty: String,
    val instructions: String,
    val ingredients: List<RecipeIngredient>
)

data class RecipeIngredient(
    val name: String,
    val quantity: Double,
    val unit: String
)

data class IngredientMatchStatus(
    val name: String,
    val requiredQty: Double,
    val availableQty: Double,
    val unit: String,
    val isMatched: Boolean,
    val missingQty: Double
)

@Composable
fun MaonatySubScreen(viewModel: WomanCompanionViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe data live from database flows
    val inventory by viewModel.allInventoryItemsState.collectAsStateWithLifecycle()
    val shoppingList by viewModel.allShoppingItemsState.collectAsStateWithLifecycle()
    val tasks by viewModel.allHouseholdTasksState.collectAsStateWithLifecycle()

    // Sub-tab navigation
    var activeTab by remember { mutableStateOf(0) } // 0: Inventory, 1: Shopping, 2: Recipes, 3: Tasks, 4: Backup
    val tabs = listOf("📦 المخزون", "🛒 المشتريات", "🍳 وصفات ذكية", "📝 المهام", "🗄️ نسخ احتياطي")

    // Seed sample data if empty
    LaunchedEffect(Unit) {
        viewModel.populateMaonatySampleData()
    }

    // Modal forms states
    var showAddInventoryDialog by remember { mutableStateOf(false) }
    var showAddShoppingDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftTheme.DeepSlate)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(SoftTheme.SoftPink.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "مؤونتي",
                        tint = SoftTheme.SoftPink,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "مؤونتي الذكية لإدارة المنزل 📦🍳",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SoftTheme.TextWhite
                    )
                    Text(
                        text = "نظام عائلي متكامل لإدارة مطبخك، مخزونك، ومهامك اليومية بذكاء تام 100% محلي",
                        fontSize = 12.sp,
                        color = SoftTheme.SoftGray
                    )
                }
            }
        }

        // Custom Scrollable Row for Sub-Tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = Color.Transparent,
            contentColor = SoftTheme.SoftPink,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                if (activeTab < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[activeTab]),
                        color = SoftTheme.SoftPink
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("maonaty_tabs")
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    selectedContentColor = SoftTheme.SoftPink,
                    unselectedContentColor = SoftTheme.SoftGray
                )
            }
        }

        // Display current tab content
        Box(modifier = Modifier.weight(1f)) {
            when (activeTab) {
                0 -> InventoryTab(
                    inventory = inventory,
                    onAddClick = { showAddInventoryDialog = true },
                    onDelete = { viewModel.deleteInventoryItem(it) },
                    onAdjustQty = { item, delta ->
                        val newQty = (item.quantity + delta).coerceAtLeast(0.0)
                        viewModel.addInventoryItem(
                            id = item.id,
                            name = item.name,
                            category = item.category,
                            quantity = newQty,
                            minQuantity = item.minQuantity,
                            unit = item.unit,
                            priceEstimate = item.priceEstimate
                        )
                    }
                )
                1 -> ShoppingTab(
                    shoppingList = shoppingList,
                    onAddClick = { showAddShoppingDialog = true },
                    onDelete = { viewModel.deleteShoppingItem(it) },
                    onToggleBought = { viewModel.toggleShoppingItemBought(it) },
                    onAutoGenerate = { viewModel.generateAutoShoppingList() },
                    onClearAll = { viewModel.clearShoppingList() }
                )
                2 -> SmartRecipesTab(
                    inventory = inventory,
                    onAddMissingToShopping = { name, category, qty, unit, price ->
                        viewModel.addShoppingItem(name, category, qty, unit, price, autoGenerated = true)
                    }
                )
                3 -> HouseholdTasksTab(
                    tasks = tasks,
                    onAddClick = { showAddTaskDialog = true },
                    onDelete = { viewModel.deleteHouseholdTask(it) },
                    onToggleComplete = { viewModel.toggleTaskCompleted(it) },
                    onClearAll = { viewModel.clearHouseholdTasks() }
                )
                4 -> BackupTab(
                    viewModel = viewModel,
                    clipboardManager = clipboardManager,
                    context = context
                )
            }
        }
    }

    // --- Modal Add Dialogues ---

    if (showAddInventoryDialog) {
        AddInventoryDialog(
            onDismiss = { showAddInventoryDialog = false },
            onConfirm = { name, category, qty, minQty, unit, price ->
                viewModel.addInventoryItem(name, category, qty, minQty, unit, price)
                showAddInventoryDialog = false
            }
        )
    }

    if (showAddShoppingDialog) {
        AddShoppingDialog(
            onDismiss = { showAddShoppingDialog = false },
            onConfirm = { name, category, qty, unit, price ->
                viewModel.addShoppingItem(name, category, qty, unit, price)
                showAddShoppingDialog = false
            }
        )
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, category, priority, dueDate ->
                viewModel.addHouseholdTask(title, category, priority, dueDate)
                showAddTaskDialog = false
            }
        )
    }
}


// ============================================================================
// 1. INVENTORY TAB
// ============================================================================
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
                                // Minus Button (Touch Target 48dp)
                                FilledIconButton(
                                    onClick = { onAdjustQty(item, -0.5) },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.DeepSlate),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "تقليل الكمية", tint = SoftTheme.SoftPink)
                                }

                                // Stock readout
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${item.quantity}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCritical) SoftTheme.RedDanger else SoftTheme.MintTeal,
                                        fontSize = 16.sp
                                    )
                                    Text(text = item.unit, fontSize = 10.sp, color = SoftTheme.SoftGray)
                                }

                                // Plus Button (Touch Target 48dp)
                                FilledIconButton(
                                    onClick = { onAdjustQty(item, 0.5) },
                                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = SoftTheme.DeepSlate),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "زيادة الكمية", tint = SoftTheme.SoftPink)
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                // Delete
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

// ============================================================================
// 2. SHOPPING TAB
// ============================================================================
@Composable
fun ShoppingTab(
    shoppingList: List<MaonatyShoppingItem>,
    onAddClick: () -> Unit,
    onDelete: (MaonatyShoppingItem) -> Unit,
    onToggleBought: (MaonatyShoppingItem) -> Unit,
    onAutoGenerate: () -> Unit,
    onClearAll: () -> Unit
) {
    val unbought = shoppingList.filter { !it.isBought }
    val bought = shoppingList.filter { it.isBought }

    val totalBudget = shoppingList.sumOf { it.price * it.quantity }
    val boughtBudget = bought.sumOf { it.price * it.quantity }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick Actions & Budget estimation header
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

                    OutlinedButton(
                        onClick = onClearAll,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftTheme.RedDanger),
                        modifier = Modifier.weight(0.5f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, SoftTheme.RedDanger),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text("مسح الكل", fontSize = 11.sp)
                    }
                }
            }
        }

        // Add custom shopping row
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
                // Unbought section
                if (unbought.isNotEmpty()) {
                    item {
                        Text("سلع للشراء (${unbought.size})", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                    }
                    items(unbought, key = { it.id }) { item ->
                        ShoppingListItemRow(item, onToggleBought, onDelete)
                    }
                }

                // Bought section
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
                // Checkbox (touch target 48dp)
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

// ============================================================================
// 3. SMART RECIPES TAB (RECIPE ENGINE)
// ============================================================================
@Composable
fun SmartRecipesTab(
    inventory: List<MaonatyInventoryItem>,
    onAddMissingToShopping: (String, String, Double, String, Double) -> Unit
) {
    val recipes = remember(inventory) {
        standardRecipes.map { recipe ->
            val matchStatuses = recipe.ingredients.map { getIngredientMatchStatus(it, inventory) }
            val matchedCount = matchStatuses.count { it.isMatched }
            val matchPercentage = if (recipe.ingredients.isEmpty()) 0 else (matchedCount * 100) / recipe.ingredients.size
            RecipeWithMatchStatus(recipe, matchStatuses, matchPercentage)
        }.sortedByDescending { it.matchPercentage }
    }

    var expandedRecipeName by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftTeal.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = SoftTheme.SoftTeal)
                    Text(
                        text = "محرك المطبخ يطابق الوصفات بنشاط مع خزائنك ومخزونك المسجل حالياً في علامة التبويب الأولى live!",
                        fontSize = 12.sp,
                        color = SoftTheme.TextWhite,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        items(recipes) { item ->
            val recipe = item.recipe
            val isExpanded = expandedRecipeName == recipe.name

            Card(
                colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedRecipeName = if (isExpanded) null else recipe.name }
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = recipe.name, fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 15.sp)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text("⏱️ ${recipe.timeMinutes} دقيقة", fontSize = 11.sp, color = SoftTheme.SoftGray)
                                Text("•", fontSize = 11.sp, color = SoftTheme.SoftGray)
                                Text("📊 ${recipe.difficulty}", fontSize = 11.sp, color = SoftTheme.SoftGray)
                            }
                        }

                        // Match percentage badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    when {
                                        item.matchPercentage >= 100 -> SoftTheme.SoftTeal.copy(alpha = 0.2f)
                                        item.matchPercentage >= 50 -> SoftTheme.GoldFasting.copy(alpha = 0.2f)
                                        else -> SoftTheme.RedDanger.copy(alpha = 0.2f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "تطابق ${item.matchPercentage}%",
                                color = when {
                                    item.matchPercentage >= 100 -> SoftTheme.SoftTeal
                                    item.matchPercentage >= 50 -> SoftTheme.GoldFasting
                                    else -> SoftTheme.RedDanger
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)

                            // Ingredients checklist
                            Text("المكونات والمطابقة:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 13.sp)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                item.matchStatuses.forEach { ing ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = if (ing.isMatched) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (ing.isMatched) SoftTheme.SoftTeal else SoftTheme.RedDanger,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = ing.name,
                                                color = if (ing.isMatched) SoftTheme.TextWhite else SoftTheme.SoftGray,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Text(
                                            text = if (ing.isMatched) "متوفر (${ing.availableQty} ${ing.unit})" else "ناقص ${ing.missingQty} ${ing.unit}",
                                            color = if (ing.isMatched) SoftTheme.SoftTeal else SoftTheme.RedDanger,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = SoftTheme.DeepSlate, thickness = 1.dp)

                            // Cooking instructions
                            Text("طريقة التحضير:", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 13.sp)
                            Text(
                                text = recipe.instructions,
                                color = SoftTheme.TextWhite,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )

                            // Add missing ingredients to shopping list button
                            val missing = item.matchStatuses.filter { !it.isMatched }
                            if (missing.isNotEmpty()) {
                                Button(
                                    onClick = {
                                        missing.forEach { ing ->
                                            onAddMissingToShopping(
                                                ing.name,
                                                "خضار وفواكه", // Default sensible category
                                                ing.missingQty,
                                                ing.unit,
                                                15.0 // Sensible default estimate price
                                            )
                                        }
                                        expandedRecipeName = null
                                        ScaffoldMessengerHelper.showToast(context, "تم إضافة ${missing.size} سلع ناقصة لقائمة المشتريات!")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("إضافة النواقص (${missing.size}) لقائمة التسوق 🛒", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            } else {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SoftTheme.SoftTeal.copy(alpha = 0.15f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "كل المكونات متوفرة في مطبخك! ابدئي الطهي فوراً وبالهناء والشفاء! 😍🍳",
                                        color = SoftTheme.SoftTeal,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(10.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class RecipeWithMatchStatus(
    val recipe: MaonatyRecipe,
    val matchStatuses: List<IngredientMatchStatus>,
    val matchPercentage: Int
)

// List of recipes defined globally
val standardRecipes = listOf(
    MaonatyRecipe(
        name = "ملوخية مصرية بالدجاج 🍲",
        timeMinutes = 45,
        difficulty = "متوسط",
        instructions = "1. اسلقي صدور الدجاج مع البصل والبهارات لصنع مرقة دافئة.\n2. خرطي الملوخية جيداً حتى تصبح ناعمة.\n3. أضيفي الملوخية إلى المرقة الساخنة مع التقليب المستمر.\n4. حمري الثوم والكزبرة في السمنة لعمل الطشة، ثم اسكبيها فوق الملوخية وقدميها ساخنة.",
        ingredients = listOf(
            RecipeIngredient("صدور فراخ بانيه", 0.5, "كيلوجرام"),
            RecipeIngredient("بهارات لحمة مشكلة", 20.0, "جرام"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "صينية بطاطس بالفراخ في الفرن 🥘",
        timeMinutes = 60,
        difficulty = "سهل",
        instructions = "1. قطعي البطاطس والبصل والطماطم لشرائح دائرية.\n2. تبلي صدور الدجاج بالملح والبهارات.\n3. رصي الخضار في صينية وأضيفي فوقه الدجاج والزيت والقليل من الماء.\n4. غطي الصينية بورق القصدير وضعيها في الفرن حتى تنضج تماماً ثم حمري وجهها.",
        ingredients = listOf(
            RecipeIngredient("طماطم طازجة", 1.0, "كيلوجرام"),
            RecipeIngredient("صدور فراخ بانيه", 1.0, "كيلوجرام"),
            RecipeIngredient("بهارات لحمة مشكلة", 20.0, "جرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "أرز أبيض مصري مفلفل 🍚",
        timeMinutes = 25,
        difficulty = "سهل",
        instructions = "1. اغسلي الأرز جيداً وصفيه من الماء.\n2. في حلة، سخني زيت عباد الشمس ثم أضيفي الأرز وشوحيه قليلاً.\n3. أضيفي الماء المغلي والملح واتركيه يغلي ثم هدي النار تماماً وغطيه حتى ينضج.",
        ingredients = listOf(
            RecipeIngredient("أرز مصري فاخر", 1.0, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "سلطة خضراء متكاملة ومنعشة 🥗",
        timeMinutes = 10,
        difficulty = "سهل جداً",
        instructions = "1. قطعي الطماطم والخيار والخضار الورقي لقطع متوسطة.\n2. اعصري ليمونة طازجة فوق المكونات.\n3. أضيفي ملعقة زيت عباد الشمس ورشة ملح خفيفة ثم قلبيها جيداً وقدميها فوراً.",
        ingredients = listOf(
            RecipeIngredient("طماطم طازجة", 0.5, "كيلوجرام"),
            RecipeIngredient("ليمون أصفر", 0.5, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "مكرونة قلم بالصلصة الحمراء 🍝",
        timeMinutes = 30,
        difficulty = "سهل",
        instructions = "1. اسلقي المكرونة في ماء مغلي مملح وصفيها.\n2. في حلة أخرى، شوحي الثوم المفروم مع زيت عباد الشمس.\n3. أضيفي عصير الطماطم والبهارات واتركيها لتتسبك.\n4. اخلطي المكرونة المسلوقة بالصلصة الدافئة وقدميها.",
        ingredients = listOf(
            RecipeIngredient("مكرونة قلم", 1.0, "كيس"),
            RecipeIngredient("طماطم طازجة", 1.0, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    ),
    MaonatyRecipe(
        name = "عشاء خفيف: جبنة فيتا بالطماطم والزيت 🧀🍅",
        timeMinutes = 5,
        difficulty = "سهل جداً",
        instructions = "1. في طبق واسع، قطعي الطماطم لقطع صغيرة جداً.\n2. أضيفي الجبنة البيضاء الفيتا واهرسيها بالشوكة.\n3. رشي زيت عباد الشمس واعصري القليل من الليمون لمذاق منعش ومغذي.",
        ingredients = listOf(
            RecipeIngredient("جبنة بيضاء فيتا", 0.5, "كيلوجرام"),
            RecipeIngredient("طماطم طازجة", 0.5, "كيلوجرام"),
            RecipeIngredient("زيت عباد الشمس", 1.0, "لتر"),
            RecipeIngredient("ليمون أصفر", 0.5, "كيلوجرام")
        )
    ),
    MaonatyRecipe(
        name = "شوربة خضار صحية دافئة 🍲🥦",
        timeMinutes = 35,
        difficulty = "سهل",
        instructions = "1. قطعي الخضار لقطع متوسطة الحجم.\n2. شوحي البصل والثوم بملعقة زيت ثم أضيفي الخضار وشوحيه قليلاً.\n3. أضيفي الماء الساخن والبهارات والملح ودعيه يغلي على نار هادئة حتى تمام النضج.",
        ingredients = listOf(
            RecipeIngredient("طماطم طازجة", 0.5, "كيلوجرام"),
            RecipeIngredient("ليمون أصفر", 0.5, "كيلوجرام"),
            RecipeIngredient("بهارات لحمة مشكلة", 20.0, "جرام"),
            RecipeIngredient("ملح طعام ناعم", 1.0, "كيس")
        )
    )
)

fun getIngredientMatchStatus(ingredient: RecipeIngredient, inventory: List<MaonatyInventoryItem>): IngredientMatchStatus {
    val matchedItem = inventory.find {
        it.name.trim().equals(ingredient.name.trim(), ignoreCase = true) ||
                it.name.trim().contains(ingredient.name.trim(), ignoreCase = true) ||
                ingredient.name.trim().contains(it.name.trim(), ignoreCase = true)
    }

    if (matchedItem == null) {
        return IngredientMatchStatus(
            name = ingredient.name,
            requiredQty = ingredient.quantity,
            availableQty = 0.0,
            unit = ingredient.unit,
            isMatched = false,
            missingQty = ingredient.quantity
        )
    }

    val available = matchedItem.quantity
    val isMatched = available >= ingredient.quantity
    val missing = if (isMatched) 0.0 else ingredient.quantity - available

    return IngredientMatchStatus(
        name = ingredient.name,
        requiredQty = ingredient.quantity,
        availableQty = available,
        unit = ingredient.unit,
        isMatched = isMatched,
        missingQty = missing
    )
}

// ============================================================================
// 4. HOUSEHOLD TASKS TAB
// ============================================================================
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
        // Top filters & buttons row
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

        // Horizontal Category Tab Filter
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
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SoftTheme.SoftTeal, modifier = Modifier.size(56.dp))
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
        else -> SoftTheme.SoftTeal
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
                // Large touch checkbox 48dp
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

// ============================================================================
// 5. BACKUP & SPREADSHEET COMPANION TAB
// ============================================================================
@Composable
fun BackupTab(
    viewModel: WomanCompanionViewModel,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    context: android.content.Context
) {
    var backupText by remember { mutableStateOf("") }
    var restoreText by remember { mutableStateOf("") }

    // Read current backup string
    LaunchedEffect(Unit) {
        backupText = viewModel.exportMaonatyBackup()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("تصدير النسخة الاحتياطية لمؤونتي 🗄️", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                Text(
                    "انسخي الكود البرمجي أدناه واحتفظي به في أي ملف نصي أو جدول Excel/Sheets لاسترجاعه في أي وقت لاحق بالكامل:",
                    fontSize = 11.sp,
                    color = SoftTheme.SoftGray
                )

                OutlinedTextField(
                    value = backupText,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.SoftGray,
                        unfocusedTextColor = SoftTheme.SoftGray
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )

                Button(
                    onClick = {
                        if (backupText.isNotEmpty()) {
                            clipboardManager.setText(AnnotatedString(backupText))
                            ScaffoldMessengerHelper.showToast(context, "تم نسخ كود النسخة الاحتياطية للحافظة بنجاح! 📋")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.SoftPink),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("نسخ كود النسخ الاحتياطي 📋", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = SoftTheme.CardSlate),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("استرجاع كود النسخ الاحتياطي 📥", fontWeight = FontWeight.Bold, color = SoftTheme.TextWhite, fontSize = 14.sp)
                Text(
                    "ألصقي الكود الاحتياطي الخاص بك هنا لاسترداد مخزون المطبخ والمشتريات والمهام المنزلية كاملة دفعة واحدة:",
                    fontSize = 11.sp,
                    color = SoftTheme.SoftGray
                )

                OutlinedTextField(
                    value = restoreText,
                    onValueChange = { restoreText = it },
                    placeholder = { Text("ألصقي الكود البرمجي النصي هنا...", color = SoftTheme.SoftGray, fontSize = 11.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SoftTheme.SoftPink,
                        unfocusedBorderColor = SoftTheme.SoftGray,
                        focusedTextColor = SoftTheme.TextWhite,
                        unfocusedTextColor = SoftTheme.TextWhite
                    ),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = TextStyle(fontSize = 10.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                )

                Button(
                    onClick = {
                        if (restoreText.isNotEmpty()) {
                            val success = viewModel.importMaonatyBackup(restoreText)
                            if (success) {
                                ScaffoldMessengerHelper.showToast(context, "تم استرجاع نسخة مؤونتي بالكامل بنجاح! 🎉📦")
                                restoreText = ""
                                backupText = viewModel.exportMaonatyBackup()
                            } else {
                                ScaffoldMessengerHelper.showToast(context, "فشل استرجاع الكود. تأكدي من سلامة كود التصدير الملصق! ⚠️")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SoftTheme.MintTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp), tint = SoftTheme.DeepSlate)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تأكيد استرجاع البيانات 📥", fontWeight = FontWeight.Bold, color = SoftTheme.DeepSlate)
                }
            }
        }
    }
}

// ============================================================================
// helper object to display clean toast alerts on Android UI context
// ============================================================================
object ScaffoldMessengerHelper {
    fun showToast(context: android.content.Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
    }
}

// ============================================================================
// DIALOGUES & FORMS
// ============================================================================

@Composable
fun AddInventoryDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, Double, String, Double) -> Unit) {
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Unit selection
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
fun AddShoppingDialog(onDismiss: () -> Unit, onConfirm: (String, String, Double, String, Double) -> Unit) {
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

                // Category selection
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

                // Unit selection
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
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, String, Long) -> Unit) {
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

                // Category select
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

                // Priority select
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
                                // Default due date is tomorrow (24 hours from now)
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
