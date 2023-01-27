package kr.co.houmuch.batch.domain.area.cache

import kr.co.houmuch.core.domain.code.AreaCodeJpaRepository
import kr.co.houmuch.core.domain.code.AreaCodeJpo
import kr.co.houmuch.core.domain.common.jpa.CombinedAreaCodeJpo
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
@StepScope
class AreaCodeCacheRepository(
    private val areaCodeJpaRepository: AreaCodeJpaRepository
) {
    private lateinit var areaCodeList: List<AreaCodeJpo>

    @PostConstruct
    fun postConstruct() {
        areaCodeList = areaCodeJpaRepository.findAllFetchJoin()
    }

    fun findByCodeSidoAndCodeSgg(sido: Int, sgg: Int): List<AreaCodeJpo> {
        return areaCodeList
            .filter { areaCodeJpo -> filterCode(areaCodeJpo, sido, sgg) }
    }

    fun filterCode(areaCodeJpo: AreaCodeJpo, sido: Int, sgg: Int): Boolean {
        val code: CombinedAreaCodeJpo = areaCodeJpo.code
        return code.sido == sido && code.sgg == sgg
    }
}
