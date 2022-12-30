package kr.co.houmuch.batch.domain.contract.dto

import com.fasterxml.jackson.annotation.JsonProperty
import kr.co.houmuch.core.domain.JsonSerializable

data class RoadAddress(
    @JsonProperty("도로명")
    val name: String,

    @JsonProperty("도로명건물본번호코드")
    val buildingMainNumberCode: String,

    @JsonProperty("도로명건물부번호코드")
    val buildingSubNumberCode: String,

    @JsonProperty("도로명시군구코드")
    val sggCode: String,

    @JsonProperty("도로명일련번호코드")
    val serialNumberCode: String,

    @JsonProperty("도로명지상지하코드")
    val underOverCode: String,

    @JsonProperty("도로명코드")
    val code: String,
) : JsonSerializable
