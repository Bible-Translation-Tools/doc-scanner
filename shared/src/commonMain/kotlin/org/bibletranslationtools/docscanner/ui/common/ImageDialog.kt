package org.bibletranslationtools.docscanner.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.size.Size
import kotlin.math.max

@Composable
fun ImageDialog(
    image: String,
    onDismiss: () -> Unit
) {
    // State for managing zoom and pan
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    // Define zoom levels
    val minScale = 1f
    val maxScale = 3f
    val doubleTabZoomScale = 2f

    val context = LocalContext.current

    // State to track image loading
    var imageState by remember {
        mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty)
    }

    // Create a painter with size tracking
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(image)
            .size(Size.ORIGINAL)
            .build(),
        onState = { state ->
            imageState = state
            // When image is successfully loaded, try to get its dimensions
            if (state is AsyncImagePainter.State.Success) {
                // Use Coil's built-in size information
                val width = state.painter.intrinsicSize.width.toInt()
                val height = state.painter.intrinsicSize.height.toInt()

                // Update image size if valid dimensions are found
                if (width > 0 && height > 0) {
                    imageSize = IntSize(width, height)
                }
            }
        }
    )

    LaunchedEffect(image) {
        scale = 1f
        offset = Offset.Zero
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    containerSize = coordinates.size
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                // If already zoomed in, zoom out
                                if (scale > minScale) {
                                    scale = minScale
                                    offset = Offset.Zero
                                } else {
                                    scale = doubleTabZoomScale

                                    // Calculate offset to center zoom at tap point
                                    val screenWidth = containerSize.width.toFloat()
                                    val screenHeight = containerSize.height.toFloat()

                                    val translateX = -(tapOffset.x - screenWidth / 2) * (doubleTabZoomScale - 1)
                                    val translateY = -(tapOffset.y - screenHeight / 2) * (doubleTabZoomScale - 1)

                                    offset = limitPan(
                                        offset = Offset(translateX, translateY),
                                        scale = doubleTabZoomScale,
                                        containerSize = containerSize,
                                        imageSize = imageSize
                                    )
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val panFactor = 3f
                            val adjustedPan = pan.times(panFactor)

                            // Update scale with pinch-to-zoom, limiting zoom range
                            val newScale = scale.times(zoom).coerceIn(minScale, maxScale)

                            // Update offset with panning, considering current scale
                            val newOffset = limitPan(
                                offset = offset + adjustedPan.times(1 / scale),
                                scale = newScale,
                                containerSize = containerSize,
                                imageSize = imageSize
                            )

                            scale = newScale
                            offset = newOffset
                        }
                    }
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .zIndex(10f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Cancel,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Image(
                    painter = painter,
                    contentDescription = "Zoomable Image",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                )
            }
        }
    }
}

/**
 * Limits panning based on image and container sizes
 */
private fun limitPan(
    offset: Offset,
    scale: Float,
    containerSize: IntSize,
    imageSize: IntSize
): Offset {
    // If image or container size is not determined, or scale is 1 (or less),
    // panning should be reset as there's nothing to pan.
    if (imageSize == IntSize.Zero || containerSize == IntSize.Zero || scale <= 1f) {
        return Offset.Zero
    }

    // Calculate the size of the image as it's initially rendered by ContentScale.Fit
    // within the container. This is the size before our custom 'scale' is applied.
    val containerAspectRatio = containerSize.width.toFloat() / containerSize.height
    val imageAspectRatio = imageSize.width.toFloat() / imageSize.height

    val initialRenderedWidth: Float
    val initialRenderedHeight: Float

    if (imageAspectRatio > containerAspectRatio) {
        // Image is relatively wider than the container,
        // so it's scaled to fit the container's width.
        // Height will be constrained by the aspect ratio.
        initialRenderedWidth = containerSize.width.toFloat()
        initialRenderedHeight = containerSize.width / imageAspectRatio
    } else {
        // Image is relatively taller than the container,
        // so it's scaled to fit the container's height.
        // Width will be constrained by the aspect ratio.
        initialRenderedWidth = containerSize.height * imageAspectRatio
        initialRenderedHeight = containerSize.height.toFloat()
    }

    // Now, calculate the dimensions of the image after our custom 'scale' is applied
    // to the 'initialRenderedWidth' and 'initialRenderedHeight'.
    val scaledWidth = initialRenderedWidth * scale
    val scaledHeight = initialRenderedHeight * scale

    // Calculate maximum allowed translation. This is half of the excess size beyond the container.
    // Panning occurs to move the image edge to the container edge.
    val maxTranslateX = max(0f, (scaledWidth - containerSize.width) / 2f)
    val maxTranslateY = max(0f, (scaledHeight - containerSize.height) / 2f)

    // Determine if the scaled image actually exceeds the container dimensions in width/height.
    // If it doesn't, panning in that direction should not be allowed.
    val fillsWidth = scaledWidth >= containerSize.width
    val fillsHeight = scaledHeight >= containerSize.height

    // Limit horizontal translation
    val limitedX = when {
        !fillsWidth -> 0f // If the scaled image doesn't exceed container width, no horizontal pan.
        offset.x > maxTranslateX -> maxTranslateX
        offset.x < -maxTranslateX -> -maxTranslateX
        else -> offset.x
    }

    // Limit vertical translation
    val limitedY = when {
        !fillsHeight -> 0f // If the scaled image doesn't exceed container height, no vertical pan.
        offset.y > maxTranslateY -> maxTranslateY
        offset.y < -maxTranslateY -> -maxTranslateY
        else -> offset.y
    }

    return Offset(limitedX, limitedY)
}