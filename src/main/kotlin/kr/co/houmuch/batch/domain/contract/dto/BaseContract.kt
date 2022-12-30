package kr.co.houmuch.batch.domain.contract.dto

import com.fasterxml.jackson.annotation.JsonUnwrapped
import kr.co.houmuch.core.domain.JsonSerializable

abstract class BaseContract : JsonSerializable {
    @JsonUnwrapped
    lateinit var contractedAt: ContractDate
}
