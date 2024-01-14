package com.dongun.crop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.dongun.crop.crop.CropAgent
import com.dongun.crop.draw.DrawingOverlay
import com.dongun.crop.draw.ImageDrawCanvas
import com.dongun.crop.image.ImageWithConstraints
import com.dongun.crop.image.getScaledImageBitmap
import com.dongun.crop.model.CropOutline
import com.dongun.crop.settings.CropDefaults
import com.dongun.crop.settings.CropProperties
import com.dongun.crop.settings.CropStyle
import com.dongun.crop.state.rememberCropState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

@Composable
fun ImageCropper(
    modifier: Modifier = Modifier,
    imageBitmap: ImageBitmap,
    contentDescription: String?,
    cropStyle: CropStyle = CropDefaults.style(),
    cropProperties: CropProperties,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
    crop: Boolean = false,
    backgroundColor: Color = Color.Black,
    onCropStart: () -> Unit,
    onCropSuccess: (ImageBitmap) -> Unit,
) {
    ImageWithConstraints(
        modifier = modifier.clipToBounds(),
        contentScale = cropProperties.contentScale,
        contentDescription = contentDescription,
        filterQuality = filterQuality,
        imageBitmap = imageBitmap,
        drawImage = false
    ) {

        // No crop operation is applied by ScalableImage so rect points to bounds of original
        // bitmap
        val scaledImageBitmap = getScaledImageBitmap(
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            rect = rect,
            bitmap = imageBitmap,
            contentScale = cropProperties.contentScale,
        )

        // Container Dimensions
        val containerWidthPx = constraints.maxWidth
        val containerHeightPx = constraints.maxHeight

        val containerWidth: Dp
        val containerHeight: Dp

        // Bitmap Dimensions
        val bitmapWidth = scaledImageBitmap.width
        val bitmapHeight = scaledImageBitmap.height

        // Dimensions of Composable that displays Bitmap
        val imageWidthPx: Int
        val imageHeightPx: Int

        with(LocalDensity.current) {
            imageWidthPx = imageWidth.roundToPx()
            imageHeightPx = imageHeight.roundToPx()
            containerWidth = containerWidthPx.toDp()
            containerHeight = containerHeightPx.toDp()
        }

        val contentScale = cropProperties.contentScale
        val fixedAspectRatio = cropProperties.fixedAspectRatio
        val cropOutline = cropProperties.cropOutlineProperty.cropOutline

        // these keys are for resetting cropper when image width/height, contentScale or
        // overlay aspect ratio changes
        val resetKeys =
            getResetKeys(
                scaledImageBitmap,
                imageWidthPx,
                imageHeightPx,
                contentScale,
                fixedAspectRatio
            )

        val cropState = rememberCropState(
            imageSize = IntSize(bitmapWidth, bitmapHeight),
            containerSize = IntSize(containerWidthPx, containerHeightPx),
            drawAreaSize = IntSize(imageWidthPx, imageHeightPx),
            cropProperties = cropProperties,
            keys = resetKeys
        )

        val isHandleTouched by remember(cropState) {
            derivedStateOf {
                handlesTouched(cropState.touchRegion)
            }
        }

        val pressedStateColor = remember(cropStyle.backgroundColor){
            cropStyle.backgroundColor
                .copy(cropStyle.backgroundColor.alpha * .7f)
        }

        val transparentColor by animateColorAsState(
            animationSpec = tween(300, easing = LinearEasing),
            targetValue = if (isHandleTouched) pressedStateColor else cropStyle.backgroundColor
        )

        // Crops image when user invokes crop operation
        Crop(
            crop,
            scaledImageBitmap,
            cropState.cropRect,
            cropOutline,
            onCropStart,
            onCropSuccess,
            cropProperties.requiredSize
        )

        val imageModifier = Modifier
            .size(containerWidth, containerHeight)
            .crop(
                keys = resetKeys,
                cropState = cropState
            )

        LaunchedEffect(key1 = cropProperties) {
            cropState.updateProperties(cropProperties)
        }

        /// Create a MutableTransitionState<Boolean> for the AnimatedVisibility.
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(100)
            visible = true
        }

        ImageCropper(
            modifier = imageModifier,
            visible = visible,
            imageBitmap = imageBitmap,
            containerWidth = containerWidth,
            containerHeight = containerHeight,
            imageWidthPx = imageWidthPx,
            imageHeightPx = imageHeightPx,
            handleSize = cropProperties.handleSize,
            overlayRect = cropState.overlayRect,
            cropOutline = cropOutline,
            cropStyle = cropStyle,
            transparentColor = transparentColor,
            backgroundColor = backgroundColor,
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ImageCropper(
    modifier: Modifier,
    visible: Boolean,
    imageBitmap: ImageBitmap,
    containerWidth: Dp,
    containerHeight: Dp,
    imageWidthPx: Int,
    imageHeightPx: Int,
    handleSize: Float,
    cropOutline: CropOutline,
    cropStyle: CropStyle,
    overlayRect: Rect,
    transparentColor: Color,
    backgroundColor: Color,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {

        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(tween(500))
        ) {

            ImageCropperImpl(
                modifier = modifier,
                imageBitmap = imageBitmap,
                containerWidth = containerWidth,
                containerHeight = containerHeight,
                imageWidthPx = imageWidthPx,
                imageHeightPx = imageHeightPx,
                cropOutline = cropOutline,
                handleSize = handleSize,
                cropStyle = cropStyle,
                rectOverlay = overlayRect,
                transparentColor = transparentColor,
            )
        }

        // TODO Remove this text when cropper is complete. This is for debugging
//            val rectCrop = cropState.cropRect
//            val drawAreaRect = cropState.drawAreaRect
//            val pan = cropState.pan
//            val zoom = cropState.zoom
//            Text(
//                modifier = Modifier.align(Alignment.TopStart),
//                color = Color.White,
//                fontSize = 10.sp,
//                text = "imageWidthInPx: $imageWidthPx, imageHeightInPx: $imageHeightPx\n" +
//                        "bitmapWidth: $bitmapWidth, bitmapHeight: $bitmapHeight\n" +
//                        "zoom: $zoom, pan: $pan\n" +
//                        "drawAreaRect: $drawAreaRect, size: ${drawAreaRect.size}\n" +
//                        "overlayRect: ${cropState.overlayRect}, size: ${cropState.overlayRect.size}\n" +
//                        "cropRect: $rectCrop, size: ${rectCrop.size}"
//            )
    }
}

@Composable
private fun ImageCropperImpl(
    modifier: Modifier,
    imageBitmap: ImageBitmap,
    containerWidth: Dp,
    containerHeight: Dp,
    imageWidthPx: Int,
    imageHeightPx: Int,
    cropOutline: CropOutline,
    handleSize: Float,
    cropStyle: CropStyle,
    transparentColor: Color,
    rectOverlay: Rect,
) {

    Box(contentAlignment = Alignment.Center) {

        // Draw Image
        ImageDrawCanvas(
            modifier = modifier,
            imageBitmap = imageBitmap,
            imageWidth = imageWidthPx,
            imageHeight = imageHeightPx
        )

        val drawOverlay = cropStyle.drawOverlay

        val drawGrid = cropStyle.drawGrid
        val overlayColor = cropStyle.overlayColor
        val handleColor = cropStyle.handleColor
        val strokeWidth = cropStyle.strokeWidth

        DrawingOverlay(
            modifier = Modifier.size(containerWidth, containerHeight),
            drawOverlay = drawOverlay,
            rect = rectOverlay,
            cropOutline = cropOutline,
            drawGrid = drawGrid,
            overlayColor = overlayColor,
            handleColor = handleColor,
            strokeWidth = strokeWidth,
            handleSize = handleSize,
            transparentColor = transparentColor,
        )

    }
}

@Composable
private fun Crop(
    crop: Boolean,
    scaledImageBitmap: ImageBitmap,
    cropRect: Rect,
    cropOutline: CropOutline,
    onCropStart: () -> Unit,
    onCropSuccess: (ImageBitmap) -> Unit,
    requiredSize: IntSize?,
) {

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    // Crop Agent is responsible for cropping image
    val cropAgent = remember { CropAgent() }

    LaunchedEffect(crop) {
        if (crop) {
            flow {
                val croppedImageBitmap = cropAgent.crop(
                    scaledImageBitmap,
                    cropRect,
                    cropOutline,
                    layoutDirection,
                    density
                )
                if (requiredSize != null) {
                    emit(
                        cropAgent.resize(
                            croppedImageBitmap,
                            requiredSize.width,
                            requiredSize.height,
                        )
                    )
                } else {
                    emit(croppedImageBitmap)
                }
            }
                .flowOn(Dispatchers.Default)
                .onStart {
                    onCropStart()
                    delay(400)
                }
                .onEach {
                    onCropSuccess(it)
                }
                .launchIn(this)
        }
    }
}

@Composable
private fun getResetKeys(
    scaledImageBitmap: ImageBitmap,
    imageWidthPx: Int,
    imageHeightPx: Int,
    contentScale: ContentScale,
    fixedAspectRatio: Boolean,
) = remember(
    scaledImageBitmap,
    imageWidthPx,
    imageHeightPx,
    contentScale,
    fixedAspectRatio,
) {
    arrayOf(
        scaledImageBitmap,
        imageWidthPx,
        imageHeightPx,
        contentScale,
        fixedAspectRatio,
    )
}