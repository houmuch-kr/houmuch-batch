package kr.co.houmuch.batch.service.contract

import kr.co.houmuch.batch.client.ContractApiClient
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractRent
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.logger
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.concurrent.CompletableFuture

@Service
class ApartmentContractFetchService(
    private val contractApiClient: ContractApiClient,
    private val threadPoolTaskExecutor: ThreadPoolTaskExecutor
) {
    private val log = logger<ApartmentContractFetchService>()

    fun fetchAsync(regionCodeList: List<Long>, yearMonth: Int): MutableList<BaseApartmentContract>? {
        return regionCodeList
            .map { listOf(fetchTradeAsync(it, yearMonth), fetchRentAsync(it, yearMonth)) }
            .flatMap { it.stream().toList() }
            .toList()
            .stream()
            .flatMap { it.join().stream() }
            .toList()
    }

    fun fetchAsync(regionCode: Long, yearMonth: Int): List<BaseApartmentContract>? = CompletableFuture
        .allOf()
        .thenApply {
            listOf(fetchTradeAsync(regionCode, yearMonth), fetchRentAsync(regionCode, yearMonth))
                .stream()
                .flatMap { it.join().stream() }
                .toList()
        }
        .join()

    fun fetchTradeAsync(regionCode: Long, yearMonth: Int):
            CompletableFuture<List<BaseApartmentContract>> =
        CompletableFuture.supplyAsync({ fetchTrade(regionCode, yearMonth) }, threadPoolTaskExecutor)

    fun fetchRentAsync(regionCode: Long, yearMonth: Int):
            CompletableFuture<List<BaseApartmentContract>> =
        CompletableFuture.supplyAsync({ fetchRent(regionCode, yearMonth) }, threadPoolTaskExecutor)

    fun fetchTrade(regionCode: Long, yearMonth: Int): List<ApartmentContractTradeDetail>? {
        return contractApiClient.fetchApartmentTradeDetail(regionCode.toInt(), yearMonth)
            .doOnSuccess { log.info("????????? ?????? ????????? --> ???????????? : {}, ???????????? : {}, {}", regionCode, yearMonth, it.size) }
            .block()
    }

    fun fetchRent(regionCode: Long, yearMonth: Int): List<ApartmentContractRent>? {
        return contractApiClient.fetchApartmentRent(regionCode.toInt(), yearMonth)
            .doOnSuccess { log.info("????????? ????????? ????????? --> ???????????? : {}, ???????????? : {}, {}", regionCode, yearMonth, it.size) }
            .block()
    }

    private fun parseYearMonthAsString(yearMonth: YearMonth): Int =
        yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")).toInt()
}
