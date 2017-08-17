package de.zalando.zally.integration.validation

import org.springframework.data.jpa.repository.JpaRepository

interface ValidationRepository : JpaRepository<Validation, Long>
