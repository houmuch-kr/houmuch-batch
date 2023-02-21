package kr.co.houmuch.batch.domain.area.cache

import kr.co.houmuch.core.domain.building.jpa.BuildingJpaRepository
import kr.co.houmuch.core.domain.building.jpa.BuildingJpo
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.contract.BuildingType
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.stereotype.Component
import java.util.*
import javax.annotation.PostConstruct

@Component
@StepScope
class BuildingCacheRepository(
    private val buildingJpaRepository: BuildingJpaRepository
) {
    private lateinit var buildingList: List<BuildingJpo>

    @PostConstruct
    fun postConstruct() {
        buildingList = buildingJpaRepository.findAllFetchJoin()
    }

    fun findByNameAndTypeAndAreaCode(name: String?, type: BuildingType, areaCodeJpo: AreaCodeJpo?): Optional<BuildingJpo> {
        return buildingList
            .stream()
            .filter { buildingJpo -> filter(buildingJpo, name, type, areaCodeJpo) }
            .findFirst()
    }

    fun filter(buildingJpo: BuildingJpo, name: String?, type: BuildingType, areaCodeJpo: AreaCodeJpo?): Boolean {
        val isEqualsName = buildingJpo.name.lowercase() == name?.lowercase()
        val isEqualsType = type == buildingJpo.type
        val isEqualsAreaCode = buildingJpo.areaCode.id.equals(areaCodeJpo?.id)
        return isEqualsName && isEqualsType && isEqualsAreaCode
    }
}
