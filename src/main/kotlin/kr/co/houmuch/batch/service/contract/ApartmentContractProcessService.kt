package kr.co.houmuch.batch.service.contract

import kr.co.houmuch.batch.domain.area.cache.AreaCodeCacheRepository
import kr.co.houmuch.batch.domain.area.cache.BuildingCacheRepository
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractRent
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.batch.logger
import kr.co.houmuch.core.domain.building.jpa.BuildingJpo
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.common.dto.CombinedAreaCode
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@StepScope
class ApartmentContractProcessService(
    private val areaCodeCacheRepository: AreaCodeCacheRepository,
    private val buildingCacheRepository: BuildingCacheRepository
) {
    val log = logger<ApartmentContractProcessService>()

    @Transactional(readOnly = true)
    fun processAreaCode(contract: BaseApartmentContract): AreaCodeJpo? {
        val combinedAreaCode: CombinedAreaCode = contract.getCombinedAreaCode()
        val areaCodeList = areaCodeCacheRepository.findByCodeSidoAndCodeSgg(combinedAreaCode.sido, combinedAreaCode.sgg)
        val areaCode: AreaCodeJpo? = when (contract) {
            is ApartmentContractRent -> {
                val areaCodeJpo = areaCodeList.stream()
                    .filter { areaCodeJpo -> areaCodeJpo.address.contains(contract.dong!!) }
                    .findFirst()
                    .orElse(areaCodeList.findLast { areaCodeJpo -> areaCodeJpo.address.contains(contract.dong!!.substring(0, contract.dong.length - 1))})
                if (areaCodeJpo == null) {
                    log.info("지역코드 찾을 수 없음 --> sido : ${combinedAreaCode.sido}, sgg : ${combinedAreaCode.sgg}, dong : ${contract.dong}")
                }
                areaCodeJpo
            }
            is ApartmentContractTradeDetail -> {
                val code = "${contract.legalAddress.sggCode}${contract.legalAddress.umdCode}".toLong()
                areaCodeList.findLast { areaCodeJpo -> areaCodeJpo.id.equals(code) }
            }
            else -> throw IllegalArgumentException()
        }
        return areaCode
    }

    @Transactional
    fun processBuilding(contract: BaseApartmentContract, areaCode: AreaCodeJpo?): BuildingJpo? {
        return buildingCacheRepository.findByNameAndTypeAndAreaCode(contract.getOptionalName(), contract.buildingType, areaCode)
            .orElseGet {
                log.info("건물정보 찾을 수 없음 --> name : ${contract.getOptionalName()}, type : ${contract.buildingType}, areaCode : $areaCode")
                null
            }
    }
}
