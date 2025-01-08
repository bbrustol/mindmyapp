package com.bbrustol.core.ui.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bbrustol.core.ui.R

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchTerm: String,
    onSearchTermChanged: (String) -> Unit,
    message: String,
) {
    TextField(
        value = searchTerm,
        onValueChange = onSearchTermChanged,
        label = { Text(stringResource(R.string.search_by_name_or_id)) },
        singleLine = true,
        supportingText = {
            if (searchTerm.isNotEmpty()) {
                Text(text = message)
            }
        },
        isError = message.isNotEmpty() && searchTerm.isNotEmpty(),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    )
}