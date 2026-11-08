package com.rk.gharstock.core.domain

enum class ValidationError {
    BLANK_NAME,
    DUPLICATE_NAME,
    INVALID_QUANTITY,
    INVALID_PRICE,
    INVALID_DOSE,
    INVALID_SCHEDULE,
    MISSING_MEMBER,
}

/** Thrown inside use cases and returned as `Result.failure` so the UI can show a message. */
class ValidationException(val error: ValidationError) : IllegalArgumentException(error.name)

internal fun validate(condition: Boolean, error: ValidationError) {
    if (!condition) throw ValidationException(error)
}
