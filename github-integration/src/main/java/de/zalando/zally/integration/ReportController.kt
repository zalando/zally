package de.zalando.zally.integration

import de.zalando.zally.integration.reports.ReportService
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView

@Controller
class ReportController(private val reportService: ReportService) {

    private val log = KotlinLogging.logger {}

    @GetMapping(path = ["/reports/{id}"])
    fun getValidation(@PathVariable("id") id: Long): ModelAndView {
        log.info("Requested validations with id: {}", id)

        val report = reportService.getReport(id)
        return ModelAndView("reports", "report", report)
    }
}