package kr.co.houmuch.batch.client

import kr.co.houmuch.batch.config.properties.ContractOpenApiProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.DefaultUriBuilderFactory
import reactor.netty.http.client.HttpClient
import java.time.Duration


@Configuration
class ContractWebClientConfig(
    private val contractOpenApiProperties: ContractOpenApiProperties,
) : WebClientConfigHelper() {
    @Bean
    fun contractWebClient(): WebClient {
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .uriBuilderFactory(defaultUriBuilderFactory(contractOpenApiProperties.getBaseFull()))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .exchangeStrategies(defaultExchangeStrategies())
            .build()
    }

    @Bean
    fun contractWebClientWithPort(): WebClient {
        return WebClient.builder()
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .uriBuilderFactory(defaultUriBuilderFactory(contractOpenApiProperties.getBaseFullWithPort()))
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
            .exchangeStrategies(defaultExchangeStrategies())
            .build()
    }

    private fun httpClient(): HttpClient {
        return HttpClient.create()
            .keepAlive(true)
            .responseTimeout(Duration.ofSeconds(10))
    }

    private fun defaultUriBuilderFactory(baseUrl: String): DefaultUriBuilderFactory {
        val factory = DefaultUriBuilderFactory(baseUrl)
        factory.encodingMode = DefaultUriBuilderFactory.EncodingMode.NONE
        return factory
    }
}
