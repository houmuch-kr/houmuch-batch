package kr.co.houmuch.batch.client

import kr.co.houmuch.batch.config.properties.SlackWebhookProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class SlackWebhookClientConfig(
    private val slackWebhookProperties: SlackWebhookProperties
) : WebClientConfigHelper() {
    @Bean(name = ["slackWebhookWebClient"])
    fun webClient(): WebClient {
        return WebClient.builder()
            .baseUrl(slackWebhookProperties.url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .exchangeStrategies(defaultExchangeStrategies())
            .build()
    }
}
