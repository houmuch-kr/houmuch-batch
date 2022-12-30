package kr.co.houmuch.batch.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kr.co.houmuch.batch.config.properties.ContractOpenApiProperties
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractRent
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTrade
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.logger
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono

@Component
class ContractApiClient(
    private val contractWebClient: WebClient,
    private val contractWebClientWithPort: WebClient,
    private val contractOpenApiProperties: ContractOpenApiProperties,
    private val xmlMapper: ObjectMapper,
    private val objectMapper: ObjectMapper
) {
    val log = logger<ContractApiClient>()

    /**
     * 아파트 매매 목록 조회
     */
    fun fetchApartmentTrade(regionCode: Int, yearMonth: Int): Mono<List<ApartmentContractTrade>> {
        return contractWebClientWithPort.get()
            .uri { createUriBuilder(it.path("/getRTMSDataSvcAptTrade"), regionCode, yearMonth).build() }
            .retrieve()
            .bodyToMono(String::class.java)
            .map { xmlMapper.readTree(it).findPath("item") }
            .doOnNext { if (log.isDebugEnabled) println(it) }
            .map { objectMapper.convertValue(it, object: TypeReference<List<ApartmentContractTrade>>() {}) }
            .doOnError { it.printStackTrace() }
            .onErrorReturn(emptyList())
    }

    /**
     * 아파트 매매 상세 조회
     */
    fun fetchApartmentTradeDetail(regionCode: Int, yearMonth: Int): Mono<List<ApartmentContractTradeDetail>> {
        return contractWebClient.get()
            .uri { createUriBuilder(it.path("/getRTMSDataSvcAptTradeDev"), regionCode, yearMonth).build() }
            .retrieve()
            .bodyToMono(String::class.java)
            .map { xmlMapper.readTree(it).findPath("item") }
            .doOnNext { if (log.isDebugEnabled) println(it) }
            .map { objectMapper.convertValue(it, object: TypeReference<List<ApartmentContractTradeDetail>>() {}) }
            .doOnError { it.printStackTrace() }
            .onErrorReturn(emptyList())
    }

    /**
     * 아파트 전/월세 목록 조회
     */
    fun fetchApartmentRent(regionCode: Int, yearMonth: Int): Mono<List<ApartmentContractRent>> {
        return contractWebClientWithPort.get()
            .uri { createUriBuilder(it.path("/getRTMSDataSvcAptRent"), regionCode, yearMonth).build() }
            .retrieve()
            .bodyToMono(String::class.java)
            .map { xmlMapper.readTree(it).findPath("item") }
            .doOnNext { if (log.isDebugEnabled) println(it) }
            .map { objectMapper.convertValue(it, object: TypeReference<List<ApartmentContractRent>>() {}) }
            .doOnError { it.printStackTrace() }
            .onErrorReturn(emptyList())
    }

    fun createUriBuilder(uriBuilder: UriBuilder, regionCode: Int, yearMonth: Int): UriBuilder {
        return uriBuilder
            .queryParam("LAWD_CD", regionCode)
            .queryParam("DEAL_YMD", yearMonth)
            .queryParam("serviceKey", contractOpenApiProperties.serviceKey)
    }
}
