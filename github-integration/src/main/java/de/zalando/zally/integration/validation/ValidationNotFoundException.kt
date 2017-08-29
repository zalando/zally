package de.zalando.zally.integration.validation

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ValidationNotFoundException : RuntimeException {

    constructor()

    constructor(message: String) : super(message)

}
