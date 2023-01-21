package kr.co.houmuch.batch.job.contract.mapper

import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractRent
import kr.co.houmuch.batch.domain.contract.dto.apartment.ApartmentContractTradeDetail
import kr.co.houmuch.batch.domain.contract.dto.apartment.BaseApartmentContract
import kr.co.houmuch.core.domain.contract.jpa.ContractAdditionalJpo
import kr.co.houmuch.core.domain.contract.jpa.ContractDetailJpo
import kr.co.houmuch.core.domain.contract.jpa.ContractJpo
import kr.co.houmuch.core.util.RandomGenerator

class ApartmentContractModelMapper

fun mapping(contract: BaseApartmentContract): ContractJpo {
    val contractJpo: ContractJpo = ContractJpo.builder()
        .id(RandomGenerator.generator(10))
        .type(contract.contractType)
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
    val areaCode = "${contract.legalAddress.sggCode}${contract.legalAddress.umdCode}".toLong()
    contractJpo.serialNumber = contract.serialNumber
    contractJpo.detail = ContractDetailJpo.builder()
        .id(contractJpo.id)
        .price(contract.price)
        .floor(contract.floor)
        .build()
    contractJpo.additional = ContractAdditionalJpo.builder()
        .id(contractJpo.id)
        .contractType(contract.type)
        .release(contract.release)
        .releaseAt(contract.releasedAt)
        .build()
}

private fun mappingRent(contractJpo: ContractJpo, contract: ApartmentContractRent) {
    contractJpo.detail = ContractDetailJpo.builder()
        .id(contractJpo.id)
        .price(contract.price)
        .monthlyPrice(contract.monthlyPrice)
        .floor(contract.floor)
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
