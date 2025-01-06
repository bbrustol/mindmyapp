package com.bbrustol.core.infrastructure.network

import com.bbrustol.core.infrastructure.network.ServerStatusType.*
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.io.IOException

suspend inline fun <reified T : Any> handleApi(
    networkChecker: NetworkChecker,
    crossinline response: suspend () -> HttpResponse
): ApiResult<T> {

    if (!networkChecker.isNetworkAvailable()) {
        return WithoutInternet(ServiceUnavailable)
    }

    val result = try {
        response()
    } catch (e: IOException) {
        return ApiException(throwable = e, serviceStatusType = ServiceUnavailable)
    } catch (e: Throwable) {
        return ApiException(throwable = e, serviceStatusType = UnknownError)
    }

    return when (result.status.value) {
        in Success.range -> ApiSuccess(result.body())

        in ClientError.range -> ApiError(
            code = result.status.value,
            message = result.status.description,
            serviceStatusType = ClientError
        )

        in ServerError.range -> ApiError(
            code = result.status.value,
            message = result.status.description,
            serviceStatusType = ServerError
        )

        else -> ApiException(serviceStatusType = UnknownError)
    }
}
