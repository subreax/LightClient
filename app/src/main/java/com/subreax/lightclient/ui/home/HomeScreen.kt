package com.subreax.lightclient.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.state.AppStateId
import com.subreax.lightclient.ui.*
import com.subreax.lightclient.ui.theme.LightClientTheme

@Composable
fun HomeScreen(
    navToColorPicker: (propId: Int) -> Unit,
    navToEnumPicker: (propId: Int) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val uiState = homeViewModel.uiState

    val propertyCallback = PropertyCallback(
        colorPropertyClicked = { prop ->
            navToColorPicker(prop.id)
        },
        enumClicked = { prop ->
            navToEnumPicker(prop.id)
        },
        floatChanged = { prop, value ->
            homeViewModel.setPropertyValue(prop, value)
        },
        floatClicked = { prop ->
            homeViewModel.showEditDialog(prop)
        },
        toggleChanged = { prop, value ->
            homeViewModel.setPropertyValue(prop, value)
        },
        intChanged = { prop, value ->
            homeViewModel.setPropertyValue(prop, value)
        },
        intClicked = { prop ->
            homeViewModel.showEditDialog(prop)
        },
    )

    HomeScreen(
        appState = uiState.appState,
        deviceName = uiState.deviceName,
        globalProperties = uiState.globalProperties,
        sceneProperties = uiState.sceneProperties,
        propertyCallback = propertyCallback
    )

    if (uiState.dialogEditProperty != null) {
        PropertyEditorDialog(
            property = uiState.dialogEditProperty,
            callback = propertyCallback,
            onClose = homeViewModel::closeDialog
        )
    }
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
                .padding(horizontal = 8.dp)
                .fillMaxWidth(),
            spacing = 8.dp,
            properties = globalProperties,
            callback = propertyCallback
        )

        PropertiesSection(
            name = "Параметры сцены",
            modifier = Modifier
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .fillMaxWidth(),
            spacing = 8.dp,
            properties = sceneProperties,
            callback = propertyCallback
        )

        Spacer(Modifier.height(16.dp))
    }
}


typealias PropertyComposableFactory = @Composable (prop: Property, callback: PropertyCallback) -> Unit

val PCF = Array<PropertyComposableFactory>(PropertyType.Count.ordinal) {
    when (it) {
        PropertyType.FloatNumber.ordinal -> { prop, callback ->
            FloatProperty(prop, callback)
        }

        PropertyType.FloatSlider.ordinal -> { prop, callback ->
            FloatSliderProperty(prop, callback)
        }

        PropertyType.Color.ordinal -> { prop, callback ->
            ColorProperty(prop, callback)
        }

        PropertyType.Enum.ordinal -> { prop, callback ->
            EnumProperty(prop, callback)
        }

        PropertyType.IntNumber.ordinal -> { prop, callback ->
            IntProperty(prop, callback)
        }

        PropertyType.IntSlider.ordinal -> { prop, callback ->
            IntSliderProperty(prop, callback)
        }

        PropertyType.Bool.ordinal -> { prop, callback ->
            ToggleProperty(prop, callback)
        }

        PropertyType.Special.ordinal -> { prop, callback ->
            SpecLoadingProperty(prop, callback)
        }

        else -> { _, _ -> }
    }
}

@Composable
fun Property(
    property: Property,
    callback: PropertyCallback
) {
    PCF[property.type.ordinal](property, callback)
}

@Composable
fun PropertiesSection(
    name: String,
    modifier: Modifier = Modifier,
    spacing: Dp = 8.dp,
    properties: List<Property>,
    callback: PropertyCallback
) {
    Column(modifier) {
        Text(
            text = name,
            style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colors.primary,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(spacing)) {
            for (prop in properties) {
                Property(property = prop, callback = callback)
            }
        }
    }
}

@Composable
fun PropertyEditorDialog(
    property: Property,
    callback: PropertyCallback,
    onClose: () -> Unit
) {
    when (property) {
        is Property.BaseInt -> {
            IntPropertyEditorDialog(
                propertyName = property.name,
                value = property.current.collectAsState().value,
                min = property.min,
                max = property.max,
                onSubmit = { callback.intChanged(property, it) },
                onClose = onClose
            )
        }

        is Property.BaseFloat -> {
            FloatPropertyEditorDialog(
                propertyName = property.name,
                value = property.current.collectAsState().value,
                min = property.min,
                max = property.max,
                onSubmit = { callback.floatChanged(property, it) },
                onClose = onClose
            )
        }

        else -> {
            Log.d("HomeScreen", "PropertyEditorDialog unsupported property type: ${property.type}")
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
                Property.Enum(0, "Сцена", listOf("Smoke"), 0),
                Property.FloatSlider(1, "Яркость", 0.0f, 100.0f, 42.0f),
                Property.Bool(2, "Датчик движения", true)
            ),
            sceneProperties = listOf(
                Property.Color(3, "Цвет", -16738049),
                Property.FloatSlider(4, "Скорость", 0.0f, 5.0f, 1.0f)
            ),
            propertyCallback = PropertyCallback(
                {},
                {},
                { _, _ -> },
                {},
                { _, _ -> },
                { _, _ -> },
                {},
            )
        )
    }
}