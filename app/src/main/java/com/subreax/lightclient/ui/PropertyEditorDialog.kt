package com.subreax.lightclient.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlinx.coroutines.delay

@Composable
fun BasePropertyEditorDialog(
    propertyName: String,
    value: String,
    onValueChanged: (String) -> Unit,
    onSubmit: (String) -> Unit,
    onClose: () -> Unit,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Dialog(onDismissRequest = onClose) {
        Surface(elevation = 2.dp, shape = RoundedCornerShape(16.dp)) {
            PropertyEditorDialogLayout(
                title = propertyName,
                subtitle = "Редактирование значения",
                submitButton = {
                    TextButton(onClick = { onSubmit(value) }) {
                        Text("Установить")
                    }
                },
                onClose = onClose
            ) {
                val focusRequester = remember { FocusRequester() }
                val direction = LocalLayoutDirection.current
                var tfv by remember {
                    val selection =
                        if (direction == LayoutDirection.Ltr)
                            TextRange(value.length)
                        else
                            TextRange.Zero

                    val textFieldValue = TextFieldValue(text = value, selection = selection)
                    mutableStateOf(textFieldValue)
                }

                OutlinedTextField(
                    value = tfv,
                    onValueChange = {
                        tfv = it
                        onValueChanged(it.text)
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    label = {
                        if (hint != null)
                            Text(hint)
                    },
                    keyboardOptions = keyboardOptions
                )

                LaunchedEffect(Unit) {
                    // todo: somehow remove the delay
                    delay(100)
                    focusRequester.requestFocus()
                }
            }
        }
    }
}

@Composable
fun PropertyEditorDialogLayout(
    title: String,
    subtitle: String,
    submitButton: @Composable () -> Unit,
    onClose: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(top = 8.dp, start = 8.dp, end = 8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .padding(end = 48.dp)
                    .fillMaxWidth()
            )
            Text(subtitle, color = LocalContentColorMediumAlpha)
            Spacer(Modifier.height(16.dp))
            content()
            Spacer(Modifier.height(8.dp))
            Box(Modifier.align(Alignment.End)) {
                submitButton()
            }
        }

        IconButton(onClick = onClose, Modifier.align(Alignment.TopEnd)) {
            Icon(Icons.Filled.Close, "Close")
        }
    }
}


@Composable
fun IntPropertyEditorDialog(
    propertyName: String,
    value: Int,
    min: Int,
    max: Int,
    onSubmit: (Int) -> Unit,
    onClose: () -> Unit,
) {
    val min1 = minOf(min, max)
    val max1 = maxOf(min, max)
    var value1 by remember(Unit) { mutableStateOf(value.toString()) }

    BasePropertyEditorDialog(
        propertyName = propertyName,
        value = value1,
        onValueChanged = {
            value1 = it.filter { ch -> ch.isDigit() }
        },
        onSubmit = {
            val i = (it.toIntOrNull() ?: min1).coerceIn(min1, max1)
            value1 = i.toString()
            onSubmit(i)
        },
        onClose = onClose,
        hint = "$min1 .. $max1",
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    )
}


@Composable
fun FloatPropertyEditorDialog(
    propertyName: String,
    value: Float,
    min: Float,
    max: Float,
    onSubmit: (Float) -> Unit,
    onClose: () -> Unit,
) {
    val min1 = minOf(min, max)
    val max1 = maxOf(min, max)
    var value1 by remember(Unit) {
        mutableStateOf(value.toString()) // todo: remove exponent notation
    }

    BasePropertyEditorDialog(
        propertyName = propertyName,
        value = value1,
        onValueChanged = {
            value1 = it.filterFloat()
        },
        onSubmit = {
            val i = (it.replace(',', '.').toFloatOrNull() ?: min1)
                .coerceIn(min1, max1)
            value1 = i.toString()
            onSubmit(i)
        },
        onClose = onClose,
        hint = "$min .. $max",
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
    )
}


private fun String.filterFloat(): String {
    var pointsCount = 0
    return filter { ch ->
        val isPoint = ch == ',' || ch == '.'
        if (isPoint) {
            ++pointsCount
        }

        ch.isDigit() || (isPoint && pointsCount < 2)
    }
}

@Preview
@Composable
fun PropertyEditorDialogPreview() {
    LightClientTheme {
        BasePropertyEditorDialog(
            propertyName = "Speed",
            value = "0.4",
            onValueChanged = { },
            onSubmit = { },
            onClose = { }
        )
    }
}