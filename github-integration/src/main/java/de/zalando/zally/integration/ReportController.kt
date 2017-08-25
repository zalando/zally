package de.zalando.zally.integration

import de.zalando.zally.integration.config.logger
import de.zalando.zally.integration.reports.ReportService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView

@Controller
class ReportController(private val reportService: ReportService) {

    val log by logger()

    @GetMapping(path = arrayOf("/reports/{id}"))
    fun getValidation(@PathVariable("id") id: Long): ModelAndView {
        log.info("Requested validations with id: {}", id)

        val report = reportService.getReport(id)
        return ModelAndView("reports", "report", report)
    }

}