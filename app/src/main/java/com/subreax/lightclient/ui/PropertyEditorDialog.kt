package com.subreax.lightclient.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.subreax.lightclient.R
import com.subreax.lightclient.round4
import com.subreax.lightclient.ui.theme.LightClientTheme
import kotlinx.coroutines.delay

@Composable
fun BasePropertyEditorDialog(
    propertyName: String,
    initialValue: String,
    onValueChanged: (String) -> String,
    onSubmit: (String) -> Unit,
    onClose: () -> Unit,
    hint: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var tfvKey by remember { mutableStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    var tfv by remember(tfvKey) {
        val selection = TextRange(0, initialValue.length)
        val textFieldValue = TextFieldValue(text = initialValue, selection = selection)
        mutableStateOf(textFieldValue)
    }

    val submitAction = {
        onSubmit(tfv.text)
        ++tfvKey
    }

    Dialog(onDismissRequest = onClose) {
        Surface(shape = RoundedCornerShape(16.dp)) {
            PropertyEditorDialogLayout(
                title = propertyName,
                subtitle = stringResource(R.string.edit_value),
                submitButton = {
                    TextButton(
                        onClick = {
                            submitAction()
                        }
                    ) {
                        Text(stringResource(R.string.set))
                    }
                },
                onClose = onClose
            ) {
                OutlinedTextField(
                    value = tfv,
                    onValueChange = {
                        tfv = it.copy(text = onValueChanged(it.text))
                    },
                    modifier = Modifier
                        .focusRequester(focusRequester)
                        .fillMaxWidth(),
                    label = {
                        if (hint != null)
                            Text(hint)
                    },
                    keyboardOptions = keyboardOptions.copy(imeAction = ImeAction.Go),
                    maxLines = 1,
                    keyboardActions = KeyboardActions(onGo = { submitAction() })
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
            Icon(Icons.Filled.Close, stringResource(R.string.close))
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
    var value1 by remember { mutableStateOf(value.toString()) }

    BasePropertyEditorDialog(
        propertyName = propertyName,
        initialValue = value1,
        onValueChanged = {
            it.filterInt()
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

private fun String.filterInt(): String {
    return filterIndexed { index, ch ->
        ch.isDigit() || (index == 0 && ch == '-')
    }
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
        initialValue = value1,
        onValueChanged = {
            it.filterFloat()
        },
        onSubmit = {
            val i = (it.replace(',', '.').toFloatOrNull() ?: min1)
                .coerceIn(min1, max1)
            value1 = i.toString()
            onSubmit(i)
        },
        onClose = onClose,
        hint = "${round4(min)} .. ${round4(max)}",
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
    )
}


private fun String.filterFloat(): String {
    var pointsCount = 0
    return filterIndexed { index, ch ->
        val isPoint = ch == ',' || ch == '.'
        if (isPoint) {
            ++pointsCount
        }

        ch.isDigit() || (index == 0 && ch == '-') || (isPoint && pointsCount < 2)
    }
}

@Preview
@Composable
fun PropertyEditorDialogPreview() {
    LightClientTheme {
        BasePropertyEditorDialog(
            propertyName = "Speed",
            initialValue = "0.4",
            onValueChanged = { "" },
            onSubmit = { },
            onClose = { }
        )
    }
}