package kr.co.houmuch.batch.domain.contract.dto

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.houmuch.core.domain.JsonSerializable
import java.time.LocalDate

data class ContractDate(
    @JsonProperty("년")
    var year: Int,

    @JsonProperty("월")
    var month: Int,

    @JsonProperty("일")
    var day: Int,
) : JsonSerializable {
    fun asLocalDate(): LocalDate {
        return LocalDate.of(year, month, day)
    }
}
