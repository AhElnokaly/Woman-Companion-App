package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.maonaty.*
import com.example.viewmodel.*

@Composable
fun MaonatySubScreen(viewModel: WomanCompanionViewModel) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Observe data live from database flows
    val inventory by viewModel.allInventoryItemsState.collectAsStateWithLifecycle()
    val shoppingList by viewModel.allShoppingItemsState.collectAsStateWithLifecycle()
    val tasks by viewModel.allHouseholdTasksState.collectAsStateWithLifecycle()
    val todayNutritionLogs by viewModel.todayNutritionLogsState.collectAsStateWithLifecycle()
    val pregnancyProgression = viewModel.getPregnancyProgression()

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

        // Trimester-Specific Pregnancy Nutrition Guidance Card (Only when pregnant)
        if (pregnancyProgression != null) {
            PregnancyNutritionGuidanceCard(
                pregnancyProgression = pregnancyProgression,
                todayLogs = todayNutritionLogs
            )
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

    // Modal Add Dialogues
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
