package kr.co.houmuch.batch.service.contract

import kr.co.houmuch.batch.client.ContractApiClient
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.logger
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class ApartmentTradeFetchService(
    private val contractApiClient: ContractApiClient
) {
    private val log = logger<ApartmentTradeFetchService>()

    fun fetch(regionCode: Long, yearMonth: Int): List<ApartmentContractTradeDetail>? {
        return contractApiClient.fetchApartmentTradeDetail(regionCode.toInt(), yearMonth)
            .doOnSuccess { log.info("아파트 매매 실거래 --> 지역코드 : {}, 조회년월 : {}, {}", regionCode, yearMonth, it.size) }
            .block()
    }

    private fun parseYearMonthAsString(yearMonth: YearMonth): Int =
        yearMonth.format(DateTimeFormatter.ofPattern("yyyyMM")).toInt()
}
