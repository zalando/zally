package org.zalando.zally.core

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

val rulesConfig: Config by lazy {
    ConfigFactory.load("rules-config.conf")
}
