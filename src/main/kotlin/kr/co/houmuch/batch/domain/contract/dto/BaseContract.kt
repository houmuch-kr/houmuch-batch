package kr.co.houmuch.batch.domain.contract.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import kr.co.houmuch.core.domain.JsonSerializable
import kr.co.houmuch.core.domain.contract.BuildingType

abstract class BaseContract(
    val buildingType: BuildingType
) : JsonSerializable {
    @JsonUnwrapped
    lateinit var contractedAt: ContractDate
}
