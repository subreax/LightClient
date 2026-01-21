package com.subreax.lightclient.ui.enumscreen

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.subreax.lightclient.R
import com.subreax.lightclient.ui.TopBar
import com.subreax.lightclient.ui.theme.LightClientTheme


@Composable
fun EnumScreen(
    navBack: () -> Unit,
    enumViewModel: EnumPickerViewModel = hiltViewModel()
) {
    val selected by enumViewModel.selected.collectAsState(0)

    EnumScreen(
        propertyName = enumViewModel.propertyName,
        enumerators = enumViewModel.enumerators,
        selected = selected,
        onSelect = enumViewModel::select,
        navBack = navBack
    )
}

@Composable
fun EnumScreen(
    propertyName: String,
    enumerators: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    navBack: () -> Unit
) {
    Column(Modifier.fillMaxSize().navigationBarsPadding()) {
        TopBar(
            title = propertyName,
            subtitle = { Text(stringResource(R.string.select_an_item)) },
            navBack = navBack
        )

        LazyColumn {
            itemsIndexed(enumerators) { i, item ->
                EnumItem(
                    text = item,
                    selected = i == selected,
                    onClick = { onSelect(i) }
                )
            }
        }
    }
}


@Composable
private fun EnumItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor = MaterialTheme.colors.primary
    val unselectedColor = MaterialTheme.colors.onSurface.copy(alpha = 0.1f)

    val color = if (selected) selectedColor else unselectedColor

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(48.dp)
            .clickable { onClick() }
            .then(modifier)
    ) {
        Box(
            modifier = Modifier
                .padding(start = 8.dp)
                .background(color)
                .fillMaxHeight(0.66f)
                .width(4.dp)
        )
        Text(
            text, modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1.0f)
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES, showBackground = true, widthDp = 300)
@Composable
fun EnumItemPreview() {
    LightClientTheme {
        EnumItem(text = "Item 1", selected = true, onClick = {})
    }
}