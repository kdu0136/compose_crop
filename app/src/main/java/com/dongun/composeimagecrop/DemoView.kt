package com.dongun.composeimagecrop

import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.dongun.crop.ImageCropper
import com.dongun.crop.PrintLog
import com.dongun.crop.model.OutlineType
import com.dongun.crop.model.OvalCropShape
import com.dongun.crop.settings.CropDefaults
import com.dongun.crop.settings.CropOutlineProperty

@Composable
fun DemoView(
) {
    val context = LocalContext.current
    val defaultImage: MutableState<ImageBitmap?> = remember { mutableStateOf(null) }

    LaunchedEffect(key1 = true, block =  {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data("https://service.klaatoo.com/files/nft-example/newjeans-0003.png")
            .allowHardware(false) // Disable hardware bitmaps.
            .build()

        val result = (loader.execute(request) as SuccessResult).drawable
        defaultImage.value = (result as BitmapDrawable).bitmap.asImageBitmap()
    })

    val handleSize: Float = LocalDensity.current.run { 20.dp.toPx() }
//    val defaultImage = ImageBitmap.imageResource(id = R.drawable.update_name_splash)
    var cropProperties by remember {
        mutableStateOf(
            CropDefaults.properties(
                cropOutlineProperty = CropOutlineProperty(
                    OutlineType.Oval,
                    OvalCropShape(0, "Oval")
                ),
                maxZoom = 3f,
                handleSize = handleSize,
                rotatable = true,
            )
        )
    }
    var cropStyle by remember {
        mutableStateOf(
            CropDefaults.style(
//                drawOverlay = false,
//                drawGrid = false,
//                handleColor = Color.Transparent,
//                overlayColor = Color.Transparent,
//                backgroundColor = Color.Black.copy(alpha = 0.4f)
            )
        )
    }

    var crop by remember {
        mutableStateOf(false)
    }

    var showDialog by remember { mutableStateOf(false) }
    var croppedImage by remember { mutableStateOf<ImageBitmap?>(null) }

    if (defaultImage.value != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            ImageCropper(
                modifier = Modifier.align(Alignment.Center),
                imageBitmap = defaultImage.value!!,
                contentDescription = "",
                cropProperties = cropProperties,
                cropStyle = cropStyle,
                onCropStart = {
                },
                onCropSuccess = {
                    croppedImage = it
                    showDialog = true
                    crop = false
                },
                crop = crop,
            )

            Button(onClick = { crop = true }) {

            }
        }
    }

    if (showDialog) {
        croppedImage?.let {
            ShowCroppedImageDialog(imageBitmap = it) {
                showDialog = !showDialog
                croppedImage = null
            }
        }
    }
}
@Composable
private fun ShowCroppedImageDialog(imageBitmap: ImageBitmap, onDismissRequest: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Image(
                modifier = Modifier
//                    .drawChecker(RoundedCornerShape(8.dp))
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentScale = ContentScale.Fit,
                bitmap = imageBitmap,
                contentDescription = "result"
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
}
