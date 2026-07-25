package com.rudra.defineeasy.core.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A shimmer animation value that oscillates between 0f and 1f indefinitely.
 */
@Composable
fun rememberShimmerProgress(): Float {
    val transition = rememberInfiniteTransition(label = "shimmer")
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "shimmer_progress"
    ).value
}

/**
 * Returns a shimmer brush that creates a moving highlight effect.
 */
@Composable
private fun shimmerBrush(progress: Float, shapeSize: androidx.compose.ui.geometry.Size): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )
    val width = shapeSize.width.coerceAtLeast(1f)
    val xOffset = progress * (width * 1.5f)
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(xOffset - width * 0.75f, 0f),
        end = Offset(xOffset + width * 0.75f, 0f)
    )
}

/**
 * A shimmer placeholder that animates with a moving highlight.
 * Used as a building block for skeleton screens.
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val progress = rememberShimmerProgress()
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    )
                )
            )
    )
}

// ─── Skeleton Layouts ───────────────────────────────────────────────────────

/**
 * Skeleton for a favorite/review word list item: icon + two text lines.
 */
@Composable
fun SkeletonWordItem(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            ShimmerPlaceholder(
                modifier = Modifier.fillMaxWidth(0.5f).height(18.dp),
                shape = RoundedCornerShape(4.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(
                modifier = Modifier.fillMaxWidth(0.85f).height(14.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        ShimmerPlaceholder(
            modifier = Modifier.size(24.dp),
            shape = CircleShape
        )
    }
}

/**
 * Skeleton for a stat card in ProgressScreen.
 */
@Composable
fun SkeletonStatCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShimmerPlaceholder(
            modifier = Modifier.size(36.dp, 32.dp),
            shape = RoundedCornerShape(6.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        ShimmerPlaceholder(
            modifier = Modifier.fillMaxWidth(0.65f).height(12.dp),
            shape = RoundedCornerShape(4.dp)
        )
    }
}

/**
 * Skeleton for a collection card with gradient and progress bar.
 */
@Composable
fun SkeletonCollectionCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ShimmerPlaceholder(
                        modifier = Modifier.width(120.dp).height(22.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                    ShimmerPlaceholder(
                        modifier = Modifier.width(80.dp).height(14.dp),
                        shape = RoundedCornerShape(4.dp)
                    )
                }
                ShimmerPlaceholder(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            ShimmerPlaceholder(
                modifier = Modifier.width(80.dp).height(16.dp),
                shape = RoundedCornerShape(4.dp)
            )
            ShimmerPlaceholder(
                modifier = Modifier.fillMaxWidth().height(8.dp),
                shape = RoundedCornerShape(4.dp)
            )
            ShimmerPlaceholder(
                modifier = Modifier.width(60.dp).height(12.dp),
                shape = RoundedCornerShape(4.dp)
            )
        }
    }
}

/**
 * Skeleton for quiz/review card with question area.
 */
@Composable
fun SkeletonCard(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerPlaceholder(
            modifier = Modifier.fillMaxWidth(0.6f).height(28.dp),
            shape = RoundedCornerShape(6.dp)
        )
        ShimmerPlaceholder(
            modifier = Modifier.fillMaxWidth(0.4f).height(16.dp),
            shape = RoundedCornerShape(4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        repeat(3) {
            ShimmerPlaceholder(
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}
