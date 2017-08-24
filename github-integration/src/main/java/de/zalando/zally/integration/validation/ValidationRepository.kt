package de.zalando.zally.integration.validation

import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ValidationRepository : JpaRepository<Validation, Long> {

    fun findById(id: Long): Optional<Validation>

}
