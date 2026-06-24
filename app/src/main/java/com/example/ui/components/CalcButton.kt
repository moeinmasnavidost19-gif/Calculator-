package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalcButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary,
    isSecondaryOperator: Boolean = false,
    isEqualButton: Boolean = false,
    aspectRatio: Float = 1f,
    border: BorderStroke? = null
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth scaling effect when clicked
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        label = "button_scale"
    )

    Card(
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(aspectRatio)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Custom visual scaling instead of loud ripple
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            )
            .testTag("btn_$text"),
        shape = CircleShape, // Full circular shape as per "rounded-full" in HTML Design
        border = border,
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) backgroundColor.copy(alpha = 0.88f) else backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 0.dp else if (isEqualButton) 4.dp else 0.dp
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = if (text.length > 2) 16.sp else 24.sp,
                fontWeight = if (isEqualButton || isSecondaryOperator) FontWeight.Bold else FontWeight.Medium,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
