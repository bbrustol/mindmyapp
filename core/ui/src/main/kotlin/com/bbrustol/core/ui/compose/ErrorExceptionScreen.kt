package com.bbrustol.core.ui.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bbrustol.ui.R
import com.bbrustol.ui.utils.LoadLottie


@Composable
fun ErrorScreen(errorMessage: String, code: Int,  onRetryAction:() -> Unit) {
    ErrorAndException("$code - $errorMessage", onRetryAction)
}

@Composable
fun ExceptionScreen(errorMessage: String,  onRetryAction:() -> Unit) {
    ErrorAndException(errorMessage, onRetryAction)
}

@Composable
fun WithoutInternetScreen( onRetryAction:() -> Unit) {
    WithoutInternet(onRetryAction)
}

@Composable
private fun WithoutInternet(onRetryAction: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            LoadLottie(R.raw.lottie_without_internet)

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = stringResource(R.string.internet_connection_not_available),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { onRetryAction() }) {
                Text(text = stringResource(R.string.button_try_again))
            }
        }
    }
}

@Composable
private fun ErrorAndException(errorMessage: String,  onRetryAction:() -> Unit) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            LoadLottie(R.raw.lottie_exclamation)

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                text = errorMessage,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.error
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                onClick = { onRetryAction() }) {
                Text(text = stringResource(R.string.button_try_again))
            }
        }
    }
}

@Preview
@Composable
fun ErrorScreenPreview() {
    ErrorScreen("Error: Lorem ipsum dolor it ", 100) {}
}

@Preview
@Composable
fun ExceptionScreenPreview() {
    ExceptionScreen("Exception: Lorem ipsum dolor it ") {}
}
@Preview
@Composable
fun WithoutInternetScreenPreview() {
    WithoutInternetScreen {}
}
