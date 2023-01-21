package kr.co.houmuch.batch.job.contract

import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.service.contract.ApartmentContractFetchService
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import org.springframework.batch.item.ItemReader
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.YearMonth

open class RestApartmentContractItemReader(
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val apartmentContractFetchService: ApartmentContractFetchService,
    private val date: String
) : ItemReader<MutableList<BaseApartmentContract>> {
    private var pageable: Pageable? = PageRequest.of(0, 100)

    override fun read(): MutableList<BaseApartmentContract>? {
        val areaCodeList: Page<AreaCodeJpo> = if (pageable == null) {
            return null
        } else {
            areaCodeJpaRepository.findSgg(pageable)
        }
        pageable = if (areaCodeList.isLast) {
            null;
        } else {
            pageable?.next()
        }
        val codeList: List<Long> = areaCodeList.content.map { it.getIdBy(0, 5) }
        val yearMonth: YearMonth = stringDateAsYearMonth(date)
        return apartmentContractFetchService.fetchAsync(codeList, yearMonthAsInt(yearMonth))
    }
}
