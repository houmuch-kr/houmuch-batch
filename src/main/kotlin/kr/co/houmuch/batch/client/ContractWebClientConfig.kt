package kr.co.houmuch.batch.client

import kr.co.houmuch.batch.config.properties.ContractOpenApiProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory


@Configuration
class ContractWebClientConfig(
    private val contractOpenApiProperties: ContractOpenApiProperties
) {
    @Bean
    fun contractWebClient(): WebClient {
        return WebClient.builder()
            .uriBuilderFactory(defaultUriBuilderFactory(contractOpenApiProperties.getBaseFull()))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .build()
    }

    @Bean
    fun contractWebClientWithPort(): WebClient {
        return WebClient.builder()
            .uriBuilderFactory(defaultUriBuilderFactory(contractOpenApiProperties.getBaseFullWithPort()))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .build()
    }

    private fun defaultUriBuilderFactory(baseUrl: String): DefaultUriBuilderFactory {
        val factory = DefaultUriBuilderFactory(baseUrl)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        return factory
    }
}
