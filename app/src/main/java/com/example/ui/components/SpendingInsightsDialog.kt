package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WeekComparisonData
import java.util.Locale

@Composable
fun SpendingInsightsDialog(
    data: WeekComparisonData,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Insights,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = "📊 Spending Insights",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${data.currentWeekDateRange} vs ${data.previousWeekDateRange}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Overview Banner: Potential Savings / Change
                val isNetSaved = data.totalDiff < 0
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNetSaved) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isNetSaved) Color(0xFF2E7D32) else Color(0xFFE65100),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isNetSaved) Icons.Default.Savings else Icons.Default.CompareArrows,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isNetSaved)
                                    "Saved ₹${(-data.totalDiff).toInt()} this week!"
                                else if (data.totalDiff > 0)
                                    "Spent ₹${data.totalDiff.toInt()} more than last week"
                                else
                                    "Spending matches last week exactly",
                                fontWeight = FontWeight.Bold,
                                color = if (isNetSaved) Color(0xFF1B5E20) else Color(0xFFBF360C),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (isNetSaved)
                                    "Your weekly total dropped from ₹${data.previousWeekTotal.toInt()} to ₹${data.currentWeekTotal.toInt()} (${String.format(Locale.getDefault(), "%.1f", -data.totalPercentChange)}% reduction)."
                                else
                                    "Current Week Total: ₹${data.currentWeekTotal.toInt()} | Previous Week: ₹${data.previousWeekTotal.toInt()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Breakdown 1: Grocery Expenses
                ComparisonCategoryCard(
                    title = "Grocery & Food Provisions",
                    icon = Icons.Default.ShoppingCart,
                    iconTint = Color(0xFF00897B),
                    currentAmount = data.currentWeekGrocery,
                    previousAmount = data.previousWeekGrocery,
                    diff = data.groceryDiff,
                    percentChange = data.groceryPercentChange
                )

                // Breakdown 2: Business Expenses
                ComparisonCategoryCard(
                    title = "Goli Soda Business",
                    icon = Icons.Default.LocalDrink,
                    iconTint = Color(0xFF1565C0),
                    currentAmount = data.currentWeekBusiness,
                    previousAmount = data.previousWeekBusiness,
                    diff = data.businessDiff,
                    percentChange = data.businessPercentChange
                )

                // Savings Tips & Actionable Insights
                if (data.savingsTips.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lightbulb,
                                    contentDescription = null,
                                    tint = Color(0xFFF57C00),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "💡 Actionable Savings Tips",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            data.savingsTips.forEach { tip ->
                                Text(
                                    text = "• $tip",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Got It")
            }
        }
    )
}

@Composable
private fun ComparisonCategoryCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    currentAmount: Double,
    previousAmount: Double,
    diff: Double,
    percentChange: Double
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
                    Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                }

                val isSaved = diff <= 0
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isSaved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (isSaved) Color(0xFF2E7D32) else Color(0xFFC62828),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${if (diff >= 0) "+" else ""}₹${diff.toInt()} (${String.format(Locale.getDefault(), "%.0f", percentChange)}%)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSaved) Color(0xFF2E7D32) else Color(0xFFC62828)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current vs Previous amounts side by side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("This Week", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${currentAmount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = iconTint)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Last Week", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹${previousAmount.toInt()}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val maxVal = maxOf(currentAmount, previousAmount, 100.0)
            val progress = (currentAmount / maxVal).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = iconTint,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
