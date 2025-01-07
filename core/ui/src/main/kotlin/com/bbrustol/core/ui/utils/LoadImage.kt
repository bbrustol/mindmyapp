package com.bbrustol.core.ui.utils

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun LoadImage(
    modifier: Modifier = Modifier,
    imageUrl: String?,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    @DrawableRes placeholder: Int
) {
    if (LocalInspectionMode.current) {
        Image(
            painter = painterResource(id = placeholder),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
        return
    }

    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .memoryCacheKey(imageUrl)
            .diskCacheKey(imageUrl)
            .allowHardware(true)
            .placeholder(placeholder)
            .error(placeholder)
            .size(80)
            .crossfade(true)
            .build(),
        loading = {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        },
        onError = {
            Log.e("LoadImage", "LoadImage: ${it.result.throwable.message}")
        },

        modifier = modifier,
        contentScale = contentScale,
        contentDescription = contentDescription
    )
}