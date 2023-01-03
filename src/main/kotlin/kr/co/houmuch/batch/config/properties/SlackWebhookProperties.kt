package kr.co.houmuch.batch.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("slack.webhook")
@ConstructorBinding
class SlackWebhookProperties(
    val url: String
)
