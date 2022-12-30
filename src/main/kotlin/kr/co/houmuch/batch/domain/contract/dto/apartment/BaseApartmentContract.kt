package kr.co.houmuch.batch.domain.contract.dto.apartment

import kr.co.houmuch.batch.domain.contract.dto.BaseContract
import kr.co.houmuch.core.domain.JsonSerializable
import kr.co.houmuch.core.domain.contract.BuildingType
import kr.co.houmuch.core.domain.contract.ContractType

abstract class BaseApartmentContract(
    val contractType: ContractType
) : BaseContract(BuildingType.APARTMENT), JsonSerializable
