package org.zalando.zally.core

import com.google.auto.service.AutoService
import org.zalando.zally.rule.api.Rule
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedSourceVersion
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@AutoService(Processor::class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class RuleProcessor : AbstractProcessor() {

    val annotation = Rule::class.java

    val rules = mutableListOf<String>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> =
        mutableSetOf(Rule::class.java.name)

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()) {
            processComplete()
        } else {
            processRound(roundEnv)
        }
        return true
    }

    private fun processRound(roundEnv: RoundEnvironment) {
        rules += roundEnv
            .getElementsAnnotatedWith(annotation)
            .filterIsInstance(TypeElement::class.java)
            .map { it.qualifiedName.toString() }
    }

    private fun processComplete() {
        val file = processingEnv.filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + annotation.name)

        file.openWriter().use { out ->
            rules.sorted().forEach { rule ->
                out.write(rule)
                out.write(System.lineSeparator())
            }
            out.flush()
        }
    }
}
