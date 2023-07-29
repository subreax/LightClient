package com.subreax.lightclient.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.R
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    navBack: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .padding(top = 32.dp, bottom = 16.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .fillMaxWidth()
    ) {
        if (navBack != null) {
            IconButton(onClick = navBack, modifier = Modifier.padding(end = 8.dp)) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = stringResource(R.string.go_back),
                    modifier = Modifier.size(32.dp)
                )
            }
        } else {
            Spacer(Modifier.width(edgePaddingValue))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h4
            )
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                subtitle()
            }
        }

        actions()

        Spacer(Modifier.width(edgePaddingValue))
    }
}


@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun TopBarPreview() {
    LightClientTheme {
        TopBar(
            title = "Hello!",
            subtitle = {
                Text(
                    buildAnnotatedString {
                        append("This is a ")
                        withStyle(
                            SpanStyle(
                                color = LocalContentColor.current.copy(alpha = ContentAlpha.high)
                            )
                        ) {
                            append("top bar")
                        }
                    },
                )
            },
            navBack = {},
            actions = {
                IconButton(onClick = {  }) {
                    Icon(Icons.Filled.Settings, contentDescription = "")
                }
            })
    }
}