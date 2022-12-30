package kr.co.houmuch.batch.domain.contract.dto

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.houmuch.core.domain.JsonSerializable

data class LegalAddress(
    @JsonProperty("도로명")
    val name: String,

    @JsonProperty("법정동본번코드")
    val buildingMainNumberCode: String,

    @JsonProperty("법정동부번코드")
    val buildingSubNumberCode: String,

    @JsonProperty("법정동시군구코드")
    val sggCode: String,

    @JsonProperty("법정동읍면동코드")
    val umdCode: String,

    @JsonProperty("법정동지번코드")
    val code: String,
) : JsonSerializable
