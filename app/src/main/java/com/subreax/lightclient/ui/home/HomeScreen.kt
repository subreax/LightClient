package com.subreax.lightclient.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.subreax.lightclient.ui.*
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlin.text.Typography

@Composable
fun HomeScreen() {
    val greeting = "Добрых вечеров!"
    val connectedDeviceInfo = buildAnnotatedString {
        withStyle(SpanStyle(color = LocalContentColorMediumAlpha)) {
            append("Выполнено подключение к контроллеру ")
        }
        append("ESP32-Home")
    }

    val propertyModifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)

    Column(Modifier.fillMaxSize()) {
        TopBar(
            title = greeting,
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        )

        Text(
            text = connectedDeviceInfo,
            modifier = Modifier
                .edgePadding()
                .padding(bottom = 24.dp)
        )

        PropertiesSection(
            name = "Глобальные параметры",
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        ) {
            StringEnumProperty(
                name = "Сцена",
                values = listOf("Smoke"),
                pickedValue = 0,
                onClick = { /*TODO*/ },
                modifier = propertyModifier
            )

            IntRangeProperty(
                name = "Яркость",
                min = 0,
                max = 100,
                value = 63,
                onValueChanged = {},
                modifier = propertyModifier
            )

            ToggleProperty(
                name = "Датчик движения",
                checked = true,
                onCheckedChange = {},
                modifier = propertyModifier
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        PropertiesSection(
            name = "Параметры сцены",
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp, top = 16.dp)
                .fillMaxWidth()
        ) {
            ColorProperty(
                name = "Основной цвет",
                color = Color(0xFF0099EF),
                onClick = { /*TODO*/ },
                modifier = propertyModifier
            )

            FloatRangeProperty(
                name = "Скорость",
                min = 0.0f,
                max = 5.0f,
                value = 1.0f,
                onValueChanged = { /*TODO*/ },
                modifier = propertyModifier
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun PropertiesSection(
    name: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp)),
        elevation = 2.dp
    ) {
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.primary,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            )
            content()
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 320,
    heightDp = 640,
    showBackground = true
)
@Composable
fun HomeScreenPreview() {
    LightClientTheme {
        HomeScreen()
    }
}