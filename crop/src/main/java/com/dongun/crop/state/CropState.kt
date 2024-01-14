package com.dongun.crop.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntSize
import com.dongun.crop.settings.CropProperties


/**
 * Create and [remember] the [CropState] based on the currently appropriate transform
 * configuration to allow changing pan, zoom, and rotation.
 * @param imageSize size of the **Bitmap**
 * @param containerSize size of the Composable that draws **Bitmap**
 * @param cropProperties wrapper class that contains crop state properties such as
 * crop type,
 * @param keys are used to reset remember block to initial calculations. This can be used
 * when image, contentScale or any property changes which requires values to be reset to initial
 * values
 */
@Composable
fun rememberCropState(
    imageSize: IntSize,
    containerSize: IntSize,
    drawAreaSize: IntSize,
    cropProperties: CropProperties,
    vararg keys: Any?
): CropState {

    // Properties of crop state
    val aspectRatio = cropProperties.aspectRatio
    val overlayRatio = cropProperties.overlayRatio
    val maxZoom = cropProperties.maxZoom
    val zoomable = cropProperties.zoomable
    val rotatable = cropProperties.rotatable

    return remember(*keys) {
        StaticCropState(
            imageSize = imageSize,
            containerSize = containerSize,
            drawAreaSize = drawAreaSize,
            aspectRatio = aspectRatio,
            overlayRatio = overlayRatio,
            maxZoom = maxZoom,
            zoomable = zoomable,
            rotatable = rotatable,
            limitPan = false
        )
    }
}
