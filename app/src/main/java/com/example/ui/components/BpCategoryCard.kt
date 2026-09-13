package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BpCategoryInfo
import com.example.data.BpCategoryLevel

@Composable
fun BpCategoryCard(
    categoryInfo: BpCategoryInfo,
    systolic: Int,
    diastolic: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.5.dp, categoryInfo.color.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .testTag("bp_category_card"),
        color = categoryInfo.containerColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val icon = when (categoryInfo.level) {
                        BpCategoryLevel.NORMAL -> Icons.Default.CheckCircle
                        BpCategoryLevel.ELEVATED -> Icons.Default.Info
                        BpCategoryLevel.STAGE_1, BpCategoryLevel.STAGE_2 -> Icons.Default.Warning
                        BpCategoryLevel.CRISIS -> Icons.Default.Warning
                        BpCategoryLevel.LOW -> Icons.Default.Info
                        BpCategoryLevel.UNKNOWN -> Icons.Default.Favorite
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(categoryInfo.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = categoryInfo.color,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = categoryInfo.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = categoryInfo.color
                            )
                        )
                        Text(
                            text = categoryInfo.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                color = categoryInfo.color.copy(alpha = 0.85f)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Comforting advice for Nani
            Text(
                text = categoryInfo.advice,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2B2B2B)
                )
            )

            // Visual BP range bar
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                // Low (<90)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (categoryInfo.level == BpCategoryLevel.LOW) Color(0xFF0277BD) else Color(0xFF0277BD).copy(alpha = 0.25f))
                )
                // Normal (<120)
                Box(
                    modifier = Modifier
                        .weight(1.5f)
                        .height(6.dp)
                        .background(if (categoryInfo.level == BpCategoryLevel.NORMAL) Color(0xFF2E7D32) else Color(0xFF2E7D32).copy(alpha = 0.25f))
                )
                // Elevated (120-129)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (categoryInfo.level == BpCategoryLevel.ELEVATED) Color(0xFFEF6C00) else Color(0xFFEF6C00).copy(alpha = 0.25f))
                )
                // Stage 1 (130-139)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (categoryInfo.level == BpCategoryLevel.STAGE_1) Color(0xFFE65100) else Color(0xFFE65100).copy(alpha = 0.25f))
                )
                // Stage 2 (140+)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(if (categoryInfo.level == BpCategoryLevel.STAGE_2 || categoryInfo.level == BpCategoryLevel.CRISIS) Color(0xFFD32F2F) else Color(0xFFD32F2F).copy(alpha = 0.25f))
                )
            }
        }
    }
}
