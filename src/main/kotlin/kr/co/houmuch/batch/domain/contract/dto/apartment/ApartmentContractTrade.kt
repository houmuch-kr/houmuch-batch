package kr.co.houmuch.batch.domain.contract.dto.apartment

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonUnwrapped
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import kr.co.houmuch.batch.config.convert.NumberCommaParseDeserializer
import kr.co.houmuch.batch.config.convert.StringTrimDeserializer
import kr.co.houmuch.batch.domain.contract.dto.ContractDate
import kr.co.houmuch.core.domain.JsonSerializable

/*
<item>
    <거래금액> 82,500</거래금액>
    <거래유형> </거래유형>
    <건축년도>2008</건축년도>
    <년>2015</년>
    <법정동> 사직동</법정동>
    <아파트>광화문스페이스본(101동~105동)</아파트>
    <월>12</월>
    <일>10</일>
    <전용면적>94.51</전용면적>
    <중개사소재지> </중개사소재지>
    <지번>9</지번>
    <지역코드>11110</지역코드>
    <층>11</층>
    <해제사유발생일> </해제사유발생일>
    <해제여부> </해제여부>
</item>
 */
data class ApartmentContractTrade(
    @JsonDeserialize(using = NumberCommaParseDeserializer::class)
    @JsonProperty("거래금액")
    var price: Int?,

    @JsonDeserialize(using = StringTrimDeserializer::class)
    @JsonProperty("거래유형")
    var type: String?,

    @JsonProperty("건축년도")
    var builtYear: Int?,

    @JsonProperty("법정동")
    var dong: String?,

    @JsonProperty("아파트")
    var name: String?,

    @JsonProperty("전용면적")
    var squareMeter: Double?,

    @JsonProperty("중개사소재지")
    var broker: String?,

    @JsonProperty("지번")
    var addressDetail: String?,

    @JsonProperty("지역코드")
    var regionCode: Int?,

    @JsonProperty("층")
    var floor: Int?,

    @JsonProperty("해제사유발생일")
    var releasedAt: String?,

    @JsonProperty("해제여부")
    var release: String?,

) : BaseApartmentContract(), JsonSerializable
