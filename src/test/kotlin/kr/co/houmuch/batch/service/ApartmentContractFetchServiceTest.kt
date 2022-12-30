package kr.co.houmuch.batch.service

import kr.co.houmuch.batch.service.contract.ApartmentContractFetchService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
@DisplayName("아파트 실거래 매매, 전월세 조회")
class ApartmentContractFetchServiceTest {
    @Autowired
    lateinit var apartmentContractFetchService: ApartmentContractFetchService

    @ParameterizedTest
    @DisplayName("비동기 요청")
    @CsvSource(value = [
        "11110,202211"
    ])
    fun fetchAsyncTest(regionCode: Long, yearMonth: Int) {
        val apartmentContractList = apartmentContractFetchService.fetchAsync(regionCode, yearMonth)
        with(apartmentContractList) {
            Assertions.assertTrue(this!!.isNotEmpty())
            forEach { println(it.asPrettyJson()) }
        }
    }
}
