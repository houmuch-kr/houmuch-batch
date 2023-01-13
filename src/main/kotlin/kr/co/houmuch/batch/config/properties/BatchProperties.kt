package kr.co.houmuch.batch.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("batch")
@ConstructorBinding
class BatchProperties(
    val jsonOutputDirectory: String
)
