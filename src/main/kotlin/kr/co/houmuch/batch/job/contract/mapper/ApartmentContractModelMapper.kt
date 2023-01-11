package kr.co.houmuch.batch.job.contract.mapper

import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractRent
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.contract.ContractAdditionalJpo
import kr.co.houmuch.core.domain.contract.ContractDetailJpo
import kr.co.houmuch.core.domain.contract.ContractJpo
import kr.co.houmuch.core.util.RandomGenerator

class ApartmentContractModelMapper {
    companion object {
        fun mapping(contract: BaseApartmentContract, areaCode: AreaCodeJpo, childrenAreaCode: List<AreaCodeJpo>): ContractJpo {
            val contractJpo: ContractJpo = ContractJpo.builder()
                .id(RandomGenerator.generator(10))
                .type(contract.contractType)
                .buildingType(contract.buildingType)
                .contractedAt(contract.contractedAt.asLocalDate())
                .build()
            mapping(contract, contractJpo, childrenAreaCode)
            return contractJpo
        }

        private fun mapping(contract: BaseApartmentContract, contractJpo: ContractJpo, childrenAreaCode: List<AreaCodeJpo>) {
            when (contract) {
                is ApartmentContractRent -> mappingRent(contractJpo, contract, childrenAreaCode)
                is ApartmentContractTradeDetail -> mappingTradeDetail(contractJpo, contract, childrenAreaCode)
            }
        }

        private fun mappingTradeDetail(contractJpo: ContractJpo, contract: ApartmentContractTradeDetail, childrenAreaCode: List<AreaCodeJpo>) {
            val areaCode = "${contract.legalAddress.sggCode}${contract.legalAddress.umdCode}".toLong()
            contractJpo.serialNumber = contract.serialNumber
            contractJpo.name = contract.name
            contractJpo.areaCode = childrenAreaCode.findLast { areaCodeJpo -> areaCodeJpo.id.equals(areaCode) }
            contractJpo.detail = ContractDetailJpo.builder()
                .id(contractJpo.id)
                .price(contract.price)
                .squareMeter(contract.squareMeter)
                .builtAt(contract.builtYear)
                .floor(contract.floor)
                .addressDetail(contract.addressDetail)
                .build()
            contractJpo.additional = ContractAdditionalJpo.builder()
                .id(contractJpo.id)
                .contractType(contract.type)
                .release(contract.release)
                .releaseAt(contract.releasedAt)
                .build()
        }

        private fun mappingRent(contractJpo: ContractJpo, contract: ApartmentContractRent, childrenAreaCode: List<AreaCodeJpo>) {
            contractJpo.areaCode = childrenAreaCode.findLast { areaCodeJpo -> areaCodeJpo.address.contains(contract.dong!!) }
            contractJpo.name = contract.name
            contractJpo.detail = ContractDetailJpo.builder()
                .id(contractJpo.id)
                .price(contract.price)
                .monthlyPrice(contract.monthlyPrice)
                .squareMeter(contract.squareMeter)
                .builtAt(contract.builtYear)
                .floor(contract.floor)
                .addressDetail(contract.addressDetail)
                .build()
            contractJpo.additional = ContractAdditionalJpo.builder()
                .id(contractJpo.id)
                .useRefreshClaim(contract.useRefreshClaim)
                .contractType(contract.type)
                .term(contract.term)
                .previousPrice(contract.previousPrice)
                .previousMonthlyPrice(contract.previousMonthlyPrice)
                .build()
        }
    }
}
