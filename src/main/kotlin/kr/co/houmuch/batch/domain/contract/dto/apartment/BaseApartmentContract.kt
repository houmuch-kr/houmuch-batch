package kr.co.houmuch.batch.domain.contract.dto.apartment

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id
import kr.co.houmuch.batch.domain.contract.dto.BaseContract
import kr.co.houmuch.core.domain.JsonSerializable
import kr.co.houmuch.core.domain.common.dto.CombinedAreaCode
import kr.co.houmuch.core.domain.contract.BuildingType
import kr.co.houmuch.core.domain.contract.ContractType
import java.time.Year

@JsonTypeInfo(use = Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "contractType")
@JsonSubTypes(
    JsonSubTypes.Type(name = "RENT", value = ApartmentContractRent::class),
    JsonSubTypes.Type(name = "TRADE", value = ApartmentContractTradeDetail::class))
abstract class BaseApartmentContract(
    val contractType: ContractType
) : BaseContract(BuildingType.APARTMENT), JsonSerializable {
    @JsonIgnore
    fun getOptionalName(): String? = when (this) {
        is ApartmentContractRent -> this.name
        is ApartmentContractTradeDetail -> this.name
        else -> throw IllegalArgumentException()
    }

    @JsonIgnore
    fun getOptionalBuiltYear(): Year? = when (this) {
        is ApartmentContractRent -> this.builtYear?.let { Year.of(it) }
        is ApartmentContractTradeDetail -> this.builtYear?.let { Year.of(it) }
        else -> throw IllegalArgumentException()
    }

    @JsonIgnore
    fun getOptionalSquareMeter(): Double? = when (this) {
        is ApartmentContractRent -> this.squareMeter
        is ApartmentContractTradeDetail -> this.squareMeter
        else -> throw IllegalArgumentException()
    }

    @JsonIgnore
    fun getOptionalAddressDetail(): String? = when (this) {
        is ApartmentContractRent -> this.addressDetail
        is ApartmentContractTradeDetail -> this.addressDetail
        else -> throw IllegalArgumentException()
    }

    @JsonIgnore
    fun getCombinedAreaCode(): CombinedAreaCode = CombinedAreaCode.of(getSidoCode(), getSggCode())

    @JsonIgnore
    fun getSidoCode(): Int = when (this) {
        is ApartmentContractRent -> this.regionCode!! / 1000
        is ApartmentContractTradeDetail -> this.regionCode!! / 1000
        else -> throw IllegalArgumentException()
    }

    @JsonIgnore
    fun getSggCode(): Int = when (this) {
        is ApartmentContractRent -> this.regionCode!! % 1000
        is ApartmentContractTradeDetail -> this.regionCode!! % 1000
        else -> throw IllegalArgumentException()
    }
}
