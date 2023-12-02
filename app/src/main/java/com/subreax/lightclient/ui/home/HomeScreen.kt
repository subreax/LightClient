package com.subreax.lightclient.ui.home

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NetworkPing
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.data.Property
import com.subreax.lightclient.data.PropertyType
import com.subreax.lightclient.data.device.Device
import com.subreax.lightclient.ui.*
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Calendar

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
    navToColorPicker: (propId: Int) -> Unit,
    navToEnumPicker: (propId: Int) -> Unit,
    navToPingScreen: () -> Unit,
    navBack: () -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val density = LocalDensity.current
    val bottomSheet = remember {
        ModalBottomSheetState(ModalBottomSheetValue.Hidden, density)
    }
    val coroutineScope = rememberCoroutineScope()
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

    if (uiState.dialogEditProperty != null) {
        PropertyEditorDialog(
            property = uiState.dialogEditProperty,
            callback = propertyCallback,
            onClose = homeViewModel::closeDialog
        )
    }

    ModalBottomSheetLayout(
        sheetState = bottomSheet,
        sheetContent = {
            HomeBottomSheetContent(
                pingClicked = navToPingScreen,
                disconnectClicked = homeViewModel::disconnect
            )
        },
        scrimColor = MaterialTheme.colors.background.copy(0.6f),
        sheetElevation = 0.dp,
    ) {
        HomeScreen(
            deviceState = uiState.deviceState,
            deviceName = uiState.deviceName,
            globalProperties = uiState.globalProperties,
            sceneProperties = uiState.sceneProperties,
            propertyCallback = propertyCallback,
            onOptionsClicked = {
                coroutineScope.launch {
                    bottomSheet.show()
                }
            }
        )
    }

    LaunchedEffect(key1 = Unit) {
        homeViewModel.navBack.collect {
            if (it) {
                navBack()
                homeViewModel.navBackHandled()
            }
        }
    }
}


@Composable
fun HomeScreen(
    deviceState: Device.State,
    deviceName: String,
    globalProperties: List<Property>,
    sceneProperties: List<Property>,
    propertyCallback: PropertyCallback,
    onOptionsClicked: () -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(300.dp),
        modifier = Modifier.fillMaxSize().navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            TopBar(
                title = getGreeting(),
                subtitle = {
                    val text = formatDeviceInfo(deviceState, deviceName)
                    Text(text = text)
                },
                actions = {
                    IconButton(onClick = onOptionsClicked) {
                        Icon(Icons.Filled.MoreVert, "More options")
                    }
                }
            )
        }

        item {
            PropertiesSection(
                name = stringResource(R.string.global_props),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(0.95f),
                spacing = 8.dp,
                properties = globalProperties,
                callback = propertyCallback
            )
        }

        item {
            PropertiesSection(
                name = stringResource(R.string.scene_props),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(0.95f),
                spacing = 8.dp,
                properties = sceneProperties,
                callback = propertyCallback
            )
        }
    }
}

@Composable
private fun formatDeviceInfo(deviceState: Device.State, deviceName: String): AnnotatedString {
    return buildAnnotatedString {
        when (deviceState) {
            Device.State.Ready -> {
                append(stringResource(R.string.connected_to))
            }

            Device.State.Connecting,
            Device.State.NoConnectivity -> {
                append(stringResource(R.string.reconnecting_to))
            }

            Device.State.Fetching -> {
                append(stringResource(R.string.syncing_with))
            }

            else -> {
                append("$deviceState ")
            }
        }

        append("\n")

        withStyle(SpanStyle(color = LocalContentColor.current.copy(alpha = ContentAlpha.high))) {
            append(deviceName)
        }
    }
}

@Composable
private fun getGreeting(): String {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    return when (hour / 6) {
        0 -> stringResource(R.string.greeing_at_night)
        1 -> stringResource(R.string.greeting_at_morning)
        2 -> stringResource(R.string.greeting_at_noon)
        3 -> stringResource(R.string.greeting_at_evening)
        else -> stringResource(R.string.greeting_unknown)
    }
}


typealias PropertyComposableFactory = @Composable (prop: Property, callback: PropertyCallback) -> Unit

val PCF = Array<PropertyComposableFactory>(PropertyType.values().size) {
    when (it) {
        PropertyType.FloatNumber.ordinal -> { prop, callback ->
            FloatProperty(prop, callback)
        }

        PropertyType.FloatSlider.ordinal -> { prop, callback ->
            FloatSliderProperty(prop, callback)
        }

        PropertyType.FloatSmallHSlider.ordinal -> { prop, callback ->
            FloatSmallHSliderProperty(prop, callback)
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

        PropertyType.IntSmallHSlider.ordinal -> { prop, callback ->
            IntSmallHSliderProperty(prop, callback)
        }

        PropertyType.Bool.ordinal -> { prop, callback ->
            BoolProperty(prop, callback)
        }

        PropertyType.Special.ordinal -> { prop, callback ->
            LoadingProperty(prop, callback)
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
    if (properties.isNotEmpty()) {
        Column(modifier) {
            Text(
                text = name,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = LocalContentColorMediumAlpha,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            UniformGrid(
                columns = ColumnsCount.Constant(6),
                spacing = spacing
            ) {
                properties.forEach {
                    Property(property = it, callback = callback)
                }
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
            Timber.d("PropertyEditorDialog unsupported property type: ${property.type}")
        }
    }
}


@Composable
private fun HomeBottomSheetContent(
    pingClicked: () -> Unit,
    disconnectClicked: () -> Unit
) {
    BottomSheetItem(
        icon = Icons.Filled.NetworkPing,
        text = stringResource(R.string.ping_measurement),
        onClick = pingClicked
    )
    BottomSheetItem(
        icon = Icons.Filled.Close,
        text = stringResource(R.string.disconnect),
        onClick = disconnectClicked
    )
}

@Composable
private fun BottomSheetItem(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = text)
        Text(text)
    }
}


@SuppressLint("UnrememberedMutableState")
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 400,
    heightDp = 780,
    showBackground = true
)
@Composable
fun HomeScreenPreview() {
    LightClientTheme {
        HomeScreen(
            deviceState = Device.State.Ready,
            deviceName = "ESP32-Home",
            globalProperties = listOf(
                Property.FloatSlider(1, "Яркость", 0.0f, 100.0f, 42.0f),
                Property.Enum(0, "Сцена", listOf("Smoke"), 0),
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
            ),
            {}
        )
    }
}