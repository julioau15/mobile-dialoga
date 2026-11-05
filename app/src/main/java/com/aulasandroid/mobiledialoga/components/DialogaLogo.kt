package com.aulasandroid.mobiledialoga.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.aulasandroid.mobiledialoga.R

// TODO: logo_dialoga.png foi recortado do protótipo; trocar pelo arquivo original em alta resolução
@Composable
fun DialogaLogo(
    modifier: Modifier = Modifier,
    altura: Dp = 72.dp
) {
    Image(
        painter = painterResource(R.drawable.logo_dialoga),
        contentDescription = "Dialoga",
        contentScale = ContentScale.Fit,
        modifier = modifier.height(altura)
    )
}
