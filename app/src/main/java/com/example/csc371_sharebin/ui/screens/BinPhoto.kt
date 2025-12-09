package com.example.csc371_sharebin.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.csc371_sharebin.R
import com.example.csc371_sharebin.data.local.BinEntity

/**
 * Displays the appropriate photo for a bin. Supports both bundled demo images
 * ("b1"–"b10") and user-selected gallery images via URI.
 */
@Composable
fun BinPhoto(
    bin: BinEntity,
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
) {
    val key = bin.photoUri ?: return


    val drawableRes = when (key) {
        "b1" -> R.drawable.b1
        "b2" -> R.drawable.b2
        "b3" -> R.drawable.b3
        "b4" -> R.drawable.b4
        "b5" -> R.drawable.b5
        "b6" -> R.drawable.b6
        "b7" -> R.drawable.b7
        "b8" -> R.drawable.b8
        "b9" -> R.drawable.b9
        "b10" -> R.drawable.b10
        else -> null
    }

    if (drawableRes != null) {

        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = "Bin photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {

        Image(
            painter = rememberAsyncImagePainter(model = key),
            contentDescription = "Bin photo",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
