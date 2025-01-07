package com.bbrustol.core.infrastructure.network

enum class ServerStatusType(val range: IntRange) {
    Success(200..299),
    ClientError(400..499),
    ServerError(500..599),
    ServiceUnavailable(0..0),
    UnknownError(0..0),
    Catch(0..0)
}