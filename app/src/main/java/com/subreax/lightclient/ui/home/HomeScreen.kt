package com.subreax.lightclient.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.ui.*
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navToColorPicker: (propId: Int) -> Unit
) {
    val uiState = homeViewModel.uiState

    val propertyCallback = PropertyCallback(
        colorPropertyClicked = { prop ->
            navToColorPicker(prop.id)
        },
        stringEnumClicked = { prop ->
            homeViewModel.setNextStringEnumValue(prop)
        },
        floatRangeChanged = { prop, value ->
            homeViewModel.setPropertyValue(prop, value)
        },
        toggleChanged = { prop, value ->
            homeViewModel.setPropertyValue(prop, value)
        }
    )

    HomeScreen(
        appState = uiState.appState,
        deviceName = uiState.deviceName,
        globalProperties = uiState.globalProperties,
        sceneProperties = uiState.sceneProperties,
        propertyCallback = propertyCallback
    )
}


@Composable
fun HomeScreen(
    appState: AppStateId,
    deviceName: String,
    globalProperties: List<Property>,
    sceneProperties: List<Property>,
    propertyCallback: PropertyCallback
) {
    val greeting = "Добрых вечеров!"
    val connectedDeviceInfo = buildAnnotatedString {
        when (appState) {
            AppStateId.Ready -> {
                append("Выполнено подключение к контроллеру ")
            }
            AppStateId.Connecting -> {
                append("Переподключение к контроллеру ")
            }
            AppStateId.Syncing -> {
                append("Синхронизация с контроллером ")
            }
            else -> {
                append("$appState ")
            }
        }

        withStyle(SpanStyle(color = LocalContentColor.current.copy(alpha = ContentAlpha.high))) {
            append(deviceName)
        }
    }

    val propertyModifier = Modifier.padding(horizontal = 8.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(
            title = greeting,
            subtitle = { Text(text = connectedDeviceInfo) },
            actions = {
                IconButton(onClick = { /*TODO*/ }) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PropertiesSection(
            name = "Глобальные параметры",
            modifier = Modifier
                .fillMaxWidth(),
            spacing = 8.dp
        ) {
            globalProperties.forEach {
                Property(it, propertyCallback, propertyModifier)
            }
        }

        PropertiesSection(
            name = "Параметры сцены",
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            spacing = 8.dp
        ) {
            sceneProperties.forEach {
                Property(it, propertyCallback, propertyModifier)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun Property(
    property: Property,
    callback: PropertyCallback,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    val contentPadding = PaddingValues(vertical = 12.dp, horizontal = 12.dp)

    when (property) {
        is Property.StringEnumProperty -> {
            StringEnumProperty(
                name = property.name,
                values = property.values,
                pickedValue = property.currentValue.collectAsState().value,
                onClick = { callback.stringEnumClicked(property) },
                modifier = modifier,
                shape = shape,
                contentPadding = contentPadding
            )
        }

        is Property.ColorProperty -> {
            ColorProperty(
                name = property.name,
                color = Color(property.color.collectAsState().value),
                onClick = { callback.colorPropertyClicked(property) },
                modifier = modifier,
                shape = shape,
                contentPadding = contentPadding
            )
        }

        is Property.FloatRangeProperty -> {
            FloatRangeProperty(
                name = property.name,
                min = property.min,
                max = property.max,
                value = property.current.collectAsState().value,
                onValueChanged = { callback.floatRangeChanged(property, it) },
                modifier = modifier,
                shape = shape,
                contentPadding = contentPadding
            )
        }

        is Property.ToggleProperty -> {
            ToggleProperty(
                name = property.name,
                checked = property.toggled.collectAsState().value,
                onCheckedChange = { callback.toggleChanged(property, it) },
                modifier = modifier,
                shape = shape,
                contentPadding = contentPadding
            )
        }
    }
}

@Composable
fun PropertiesSection(
    name: String,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            content()
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 780,
    showBackground = true
)
@Composable
fun HomeScreenPreview() {
    LightClientTheme {
        HomeScreen(
            appState = AppStateId.Ready,
            deviceName = "ESP32-Home",
            globalProperties = listOf(
                Property.StringEnumProperty(0, "Сцена", listOf("Smoke"), 0),
                Property.FloatRangeProperty(1, "Яркость", 0.0f, 100.0f, 42.0f),
                Property.ToggleProperty(2, "Датчик движения", true)
            ),
            sceneProperties = listOf(
                Property.ColorProperty(3, "Цвет", -16738049),
                Property.FloatRangeProperty(4, "Скорость", 0.0f, 5.0f, 1.0f)
            ),
            propertyCallback = PropertyCallback({}, {}, { _, _ -> }, { _, _ -> })
        )
    }
}