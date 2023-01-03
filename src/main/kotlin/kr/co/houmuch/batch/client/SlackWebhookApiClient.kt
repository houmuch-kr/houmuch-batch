package kr.co.houmuch.batch.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class SlackWebhookApiClient(
    private val slackWebhookWebClient: WebClient,
) {
    fun post(template: String) {
        slackWebhookWebClient.post()
            .bodyValue(template)
            .exchangeToMono { it.toBodilessEntity() }
            .doOnError { it.printStackTrace() }
            .block()
    }
}
