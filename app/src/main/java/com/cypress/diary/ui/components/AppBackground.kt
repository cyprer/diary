package com.cypress.diary.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun AppBackground(
    backgroundUri: String?,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit,
) {
    val context = LocalContext.current
    val bitmapState by produceState<android.graphics.Bitmap?>(initialValue = null, key1 = backgroundUri) {
        value = withContext(Dispatchers.IO) {
            backgroundUri?.let { uriString ->
                runCatching {
                    context.contentResolver.openInputStream(Uri.parse(uriString))?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                }.getOrNull()
            }
        }
    }
    val hasImage = bitmapState != null

    Box(modifier = Modifier.fillMaxSize()) {
        if (hasImage) {
            Image(
                bitmap = bitmapState!!.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = appBackgroundScrimAlpha(hasImage))),
        )

        content()
    }
}

fun appBackgroundScrimAlpha(hasImage: Boolean): Float {
    return if (hasImage) 0.56f else 1f
}
