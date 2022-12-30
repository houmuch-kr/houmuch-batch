package kr.co.houmuch.batch.client

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.function.Consumer

@SpringBootTest
@DisplayName("실거래 API")
class ContractApiClientTests {
    @Autowired
    private lateinit var contractApiClient: ContractApiClient

    @DisplayName("아파트 매매 정보 조회")
    @ParameterizedTest
    @CsvSource(value = [
        "11110,202211"
    ])
    fun fetchApartmentTradeTest(regionCode: Int, yearMonth: Int) {
        val apartmentTrade = contractApiClient.fetchApartmentTrade(regionCode, yearMonth).block()
        with(apartmentTrade) {
            assertTrue(this!!.isNotEmpty())
            forEach(Consumer { println(it.asPrettyJson()) })
        }
    }

    @DisplayName("아파트 매매 상세 정보 조회")
    @ParameterizedTest
    @CsvSource(value = [
        "11110,202211"
    ])
    fun fetchApartmentTradeDetailTest(regionCode: Int, yearMonth: Int) {
        val apartmentTradeDetail = contractApiClient.fetchApartmentTradeDetail(regionCode, yearMonth).block()
        with(apartmentTradeDetail) {
            assertTrue(this!!.isNotEmpty())
            forEach(Consumer { println(it.asPrettyJson()) })
        }
    }

    @DisplayName("아파트 전/월세 정보 조회")
    @ParameterizedTest
    @CsvSource(value = [
        "11110,202211"
    ])
    fun fetchApartmentTradeRentTest(regionCode: Int, yearMonth: Int) {
        val apartmentRent = contractApiClient.fetchApartmentRent(regionCode, yearMonth).block()
        with(apartmentRent) {
            assertTrue(this!!.isNotEmpty())
            forEach(Consumer { println(it.asPrettyJson()) })
        }
    }
}
