package org.zalando.zally.apireview

import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner
import org.zalando.zally.core.ApiValidator
import org.zalando.zally.core.RulesPolicy
import org.zalando.zally.util.readApiDefinition

@RunWith(SpringRunner::class)
@SpringBootTest
@ActiveProfiles("test", "limited-rules")
class ApiReviewRepositoryTest {

    @Autowired
    private lateinit var apiReviewRepository: ApiReviewRepository

    @Autowired
    private lateinit var rulesValidator: ApiValidator

    @Before
    fun setUp() {
        apiReviewRepository.deleteAll()
    }

    @Test
    fun shouldOnlyReturnOneResultWhenMultipleReviewsExistWithSameName() {
        val givenRequest = readApiDefinition("fixtures/openapi3_petstore.json")
        val givenApiReview = ApiReview(
            givenRequest,
            apiDefinition = givenRequest.apiDefinition!!,
            violations = rulesValidator.validate(givenRequest.apiDefinition!!, RulesPolicy(listOf()))
        )
        apiReviewRepository.saveAll(listOf(givenApiReview, givenApiReview))

        val actual = apiReviewRepository.findLatestApiReviews()

        assertThat(actual.size).isEqualTo(1)
    }
}
