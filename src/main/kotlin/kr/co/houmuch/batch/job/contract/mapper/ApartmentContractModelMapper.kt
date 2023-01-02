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
        fun mapping(contract: BaseApartmentContract, areaCode: AreaCodeJpo): ContractJpo {
            val contractJpo: ContractJpo = ContractJpo.builder()
                .id(RandomGenerator.generatorLong(10))
                .type(contract.contractType)
                .buildingType(contract.buildingType)
                .areaCode(areaCode)
                .contractedAt(contract.contractedAt.asLocalDate())
                .build()
            mapping(contract, contractJpo)
            return contractJpo
        }

        private fun mapping(contract: BaseApartmentContract, contractJpo: ContractJpo) {
            when (contract) {
                is ApartmentContractRent -> mappingRent(contractJpo, contract)
                is ApartmentContractTradeDetail -> mappingTradeDetail(contractJpo, contract)
            }
        }

        private fun mappingTradeDetail(contractJpo: ContractJpo, contract: ApartmentContractTradeDetail) {
            contractJpo.serialNumber = contract.serialNumber
            contractJpo.name = contract.name
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

        private fun mappingRent(contractJpo: ContractJpo, contract: ApartmentContractRent) {
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
