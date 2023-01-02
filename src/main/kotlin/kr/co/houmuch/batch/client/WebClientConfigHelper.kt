package kr.co.houmuch.batch.client

import org.springframework.web.reactive.function.client.ExchangeStrategies

abstract class WebClientConfigHelper {
    fun defaultExchangeStrategies(): ExchangeStrategies = ExchangeStrategies.builder()
        .codecs { it
            .defaultCodecs()
            .maxInMemorySize(-1)
        }
        .build()
}
