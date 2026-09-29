package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WeeklyDayTrend
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun WeeklySpendingTrendChart(
    trends: List<WeeklyDayTrend>,
    modifier: Modifier = Modifier
) {
    if (trends.isEmpty()) return

    val incomeColor = Color(0xFF00897B)   // Goli Soda Emerald
    val expenseColor = Color(0xFFE53935)  // Spending Coral Red

    // Find the max amount to scale the bars nicely
    val maxVal = remember(trends) {
        val highest = trends.maxOfOrNull { maxOf(it.sodaIncomeAmount, it.expenseAmount) } ?: 100.0
        if (highest <= 0.0) 100.0 else highest * 1.15
    }

    // Default select today or the day with activity
    var selectedDayIndex by remember(trends) {
        val todayIdx = trends.indexOfFirst { it.isToday }
        mutableIntStateOf(if (todayIdx >= 0) todayIdx else trends.lastIndex)
    }

    val selectedDay = trends.getOrNull(selectedDayIndex) ?: trends.first()

    // Smooth fade-in & update animation trigger whenever trends changes
    var animationTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(trends) {
        animationTrigger = false
        delay(25)
        animationTrigger = true
    }

    val animatedAlpha by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0.15f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "chart_fade_alpha"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0.05f,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "chart_bars_anim"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0.97f,
        animationSpec = tween(durationMillis = 550, easing = FastOutSlowInEasing),
        label = "chart_scale_anim"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = animatedAlpha
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .testTag("weekly_spending_trend_chart")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title and legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "📈 Weekly Trends & Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Daily Spending vs Goli Soda Income",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(incomeColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Soda",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(expenseColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Spent",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val bottomPadding = 24.dp.toPx()
                    val chartHeight = height - bottomPadding
                    val count = trends.size
                    val slotWidth = width / count
                    val barWidth = 12.dp.toPx()
                    val barSpacing = 3.dp.toPx()

                    // Horizontal guideline at 50%
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.35f),
                        start = Offset(0f, chartHeight * 0.5f),
                        end = Offset(width, chartHeight * 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Baseline
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.5f),
                        start = Offset(0f, chartHeight),
                        end = Offset(width, chartHeight),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    trends.forEachIndexed { index, day ->
                        val centerX = slotWidth * index + (slotWidth / 2f)

                        // Highlight background for selected day
                        if (index == selectedDayIndex) {
                            drawRoundRect(
                                color = Color(0xFF006D5B).copy(alpha = 0.08f),
                                topLeft = Offset(centerX - slotWidth / 2f + 2.dp.toPx(), 0f),
                                size = Size(slotWidth - 4.dp.toPx(), chartHeight),
                                cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                            )
                        }

                        // Soda Income Bar (Left)
                        val incomeFraction = (day.sodaIncomeAmount / maxVal).toFloat() * animatedProgress
                        val incomeBarHeight = (chartHeight * incomeFraction).coerceAtLeast(0f)
                        val incomeLeft = centerX - barWidth - (barSpacing / 2f)
                        val incomeTop = chartHeight - incomeBarHeight

                        if (incomeBarHeight > 0f) {
                            drawRoundRect(
                                color = incomeColor,
                                topLeft = Offset(incomeLeft, incomeTop),
                                size = Size(barWidth, incomeBarHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        } else {
                            // Subtle placeholder dot
                            drawCircle(
                                color = incomeColor.copy(alpha = 0.3f),
                                radius = 2.dp.toPx(),
                                center = Offset(incomeLeft + barWidth / 2f, chartHeight - 2.dp.toPx())
                            )
                        }

                        // Expense Bar (Right)
                        val expenseFraction = (day.expenseAmount / maxVal).toFloat() * animatedProgress
                        val expenseBarHeight = (chartHeight * expenseFraction).coerceAtLeast(0f)
                        val expenseLeft = centerX + (barSpacing / 2f)
                        val expenseTop = chartHeight - expenseBarHeight

                        if (expenseBarHeight > 0f) {
                            drawRoundRect(
                                color = expenseColor,
                                topLeft = Offset(expenseLeft, expenseTop),
                                size = Size(barWidth, expenseBarHeight),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                            )
                        } else {
                            drawCircle(
                                color = expenseColor.copy(alpha = 0.3f),
                                radius = 2.dp.toPx(),
                                center = Offset(expenseLeft + barWidth / 2f, chartHeight - 2.dp.toPx())
                            )
                        }
                    }
                }

                // Clickable overlay buttons for each day column
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    trends.forEachIndexed { index, day ->
                        val isSelected = index == selectedDayIndex
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(160.dp)
                                .clickable { selectedDayIndex = index },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(bottom = 2.dp)
                            ) {
                                Text(
                                    text = day.dayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected || day.isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else if (day.isToday) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (day.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Selected Day Inspection Card (Interactive Tooltip / Detail)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${selectedDay.dayName} (${selectedDay.dateDisplay})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (selectedDay.isToday) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "Today",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))
                        val net = selectedDay.sodaIncomeAmount - selectedDay.expenseAmount
                        Text(
                            text = if (net >= 0) "Daily Profit: +₹${net.toInt()}" else "Deficit: -₹${(-net).toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (net >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "🍾 Soda Income",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "+₹${selectedDay.sodaIncomeAmount.toInt()}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = incomeColor
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "💸 Spent",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "-₹${selectedDay.expenseAmount.toInt()}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = expenseColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weekly Cumulative Progress Footer
            val weekTotalIncome = trends.sumOf { it.sodaIncomeAmount }
            val weekTotalSpent = trends.sumOf { it.expenseAmount }
            val weekNet = weekTotalIncome - weekTotalSpent

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Weekly Total: Soda +₹${weekTotalIncome.toInt()}  |  Spent -₹${weekTotalSpent.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = if (weekNet >= 0) "Net: +₹${weekNet.toInt()}" else "Net: -₹${(-weekNet).toInt()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (weekNet >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }
    }
}
