package kr.co.houmuch.batch.service.contract

import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractRent
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.core.domain.building.jpa.BuildingJpaRepository
import kr.co.houmuch.core.domain.building.jpa.BuildingJpo
import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.common.dto.CombinedAreaCode
import kr.co.houmuch.core.util.RandomGenerator
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ApartmentContractProcessService(
    private val areaCodeJpaRepository: AreaCodeJpaRepository,
    private val buildingJpaRepository: BuildingJpaRepository
) {
    @Transactional(readOnly = true)
    fun processAreaCode(contract: BaseApartmentContract): AreaCodeJpo? {
        val combinedAreaCode: CombinedAreaCode = contract.getCombinedAreaCode()
        val areaCodeList = areaCodeJpaRepository.findByCodeSidoAndCodeSgg(combinedAreaCode.sido, combinedAreaCode.sgg, Pageable.unpaged())
        val areaCode: AreaCodeJpo? = when (contract) {
            is ApartmentContractRent -> {
                val areaCodeJpo = areaCodeList.findLast { areaCodeJpo -> areaCodeJpo.address.contains(contract.dong!!) }
                if (areaCodeJpo == null) {
                    println("지역코드 찾을 수 없음 --> sido : ${combinedAreaCode.sido}, sgg : ${combinedAreaCode.sgg}, dong : ${contract.dong}")
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
    fun processBuilding(contract: BaseApartmentContract, areaCode: AreaCodeJpo?): BuildingJpo {
        return buildingJpaRepository.findByNameAndTypeAndAreaCode(contract.getOptionalName(), contract.buildingType, areaCode)
            .orElseGet {
                buildingJpaRepository.saveAndFlush(BuildingJpo.builder()
                    .id(RandomGenerator.generator(10))
                    .name(contract.getOptionalName())
                    .areaCode(areaCode)
                    .type(contract.buildingType)
                    .squareMeter(contract.getOptionalSquareMeter())
                    .builtAt(contract.getOptionalBuiltYear()?.value)
                    .addressDetail(contract.getOptionalAddressDetail())
                    .build())
            }
    }
}
