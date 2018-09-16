package de.zalando.zally.rule.api

import com.google.auto.service.AutoService
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic
import javax.tools.StandardLocation

@AutoService(Processor::class)
class RuleProcessor: AbstractProcessor() {

    val annotation = Rule::class.java

    val rules = mutableListOf<String>()

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Rule::class.java.name)
    }

    override fun process(annotations: MutableSet<out TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (roundEnv.processingOver()) {
            processComplete()
        } else {
            processRound(roundEnv)
        }
        return true
    }

    private fun processRound(roundEnv: RoundEnvironment) {
        val elements = roundEnv.getElementsAnnotatedWith(annotation)
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "PROCESSING ROUND: $elements")
        rules += elements.map { it.simpleName.toString() }
    }

    private fun processComplete() {
        val file = processingEnv.filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "META-INF/services/" + annotation.name)
        processingEnv.messager.printMessage(Diagnostic.Kind.NOTE, "PROCESSING COMPLETE: ${file.name}")
        file.openWriter().use { out ->
            rules.sorted().forEach { rule ->
                out.write(rule)
                out.write(System.lineSeparator())
            }
            out.flush()
        }
    }
}
