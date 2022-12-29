package kr.co.houmuch.batch.config.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@ConfigurationProperties("open-api.contract")
@ConstructorBinding
class ContractOpenApiProperties(
    val baseUrl: String,
    val baseUrlWithPort: String,
    val baseUri: String,
    val serviceKey: String
) {
    fun getBaseFull(): String {
        return "${baseUrl}/${baseUri}"
    }

    fun getBaseFullWithPort(): String {
        return "${baseUrlWithPort}/${baseUri}"
    }

    fun getEncodedServiceKey(): String {
        return URLEncoder.encode(serviceKey, StandardCharsets.UTF_8)
    }
}
