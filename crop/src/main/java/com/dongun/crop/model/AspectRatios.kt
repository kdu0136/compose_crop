package com.dongun.crop.model

import com.dongun.crop.util.createRectShape

/**
 * Aspect ratio list with pre-defined aspect ratios
 */
val aspectRatios = listOf(
    CropAspectRatio(
        title = "9:16",
        shape = createRectShape(9 / 16f),
        aspectRatio = 9 / 16f
    ),
    CropAspectRatio(
        title = "2:3",
        shape = createRectShape(2 / 3f),
        aspectRatio = 2 / 3f
    ),
    CropAspectRatio(
        title = "1:1",
        shape = createRectShape(1 / 1f),
        aspectRatio = 1 / 1f
    ),
    CropAspectRatio(
        title = "16:9",
        shape = createRectShape(16 / 9f),
        aspectRatio = 16 / 9f
    ),
    CropAspectRatio(
        title = "1.91:1",
        shape = createRectShape(1.91f / 1f),
        aspectRatio = 1.91f / 1f
    ),
    CropAspectRatio(
        title = "3:2",
        shape = createRectShape(3 / 2f),
        aspectRatio = 3 / 2f
    ),
    CropAspectRatio(
        title = "3:4",
        shape = createRectShape(3 / 4f),
        aspectRatio = 3 / 4f
    ),
    CropAspectRatio(
        title = "3:5",
        shape = createRectShape(3 / 5f),
        aspectRatio = 3 / 5f
    )
)