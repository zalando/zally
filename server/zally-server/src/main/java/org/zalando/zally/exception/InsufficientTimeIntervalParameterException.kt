package org.zalando.zally.exception

class InsufficientTimeIntervalParameterException :
    RuntimeException("TO parameter was supplied without corresponding FROM parameter")
