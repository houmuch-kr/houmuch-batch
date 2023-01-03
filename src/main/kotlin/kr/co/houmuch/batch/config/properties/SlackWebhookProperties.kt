package kr.co.houmuch.batch.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("slack.webhook")
@ConstructorBinding
class SlackWebhookProperties(
    private val baseUrl: String,
    private val token: String
) {
    fun getBaseFullUrl(): String {
        return "${baseUrl}/${token}"
    }
}
