package com.example.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SoftTheme

@Composable
fun JouriChatMessageList(
    messages: List<Pair<String, Boolean>>,
    isTyping: Boolean,
    companionName: String,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(messages, key = { index, message -> "${index}_${message.first.hashCode()}" }) { _, (text, isUser) ->
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isUser) SoftTheme.SoftPink else SoftTheme.DeepSlate
                    ),
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 0.dp,
                        bottomEnd = if (isUser) 0.dp else 16.dp
                    ),
                    modifier = Modifier.widthIn(max = 260.dp)
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftTheme.TextWhite,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        if (isTyping) {
            item {
                JouriChatSkeletonResponse(companionName = companionName)
            }
        }
    }
}
