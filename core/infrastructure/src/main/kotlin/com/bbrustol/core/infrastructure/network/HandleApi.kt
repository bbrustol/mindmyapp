package com.bbrustol.core.infrastructure.network

import com.bbrustol.core.infrastructure.BuildConfig
import com.bbrustol.core.infrastructure.network.ServerStatusType.*
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.io.IOException
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object ApiHandler : KoinComponent {
    suspend inline fun <reified T : Any> handleApi(
        crossinline response: suspend () -> HttpResponse
    ): ApiResult<T> {

        val networkChecker: NetworkChecker = get()
        if (!networkChecker.isNetworkAvailable()) return ApiException(serviceStatusType = InternetConnectionProblems)

        if (BuildConfig.API_TOKEN.isEmpty()) return ApiException(serviceStatusType = NoToken)

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
}