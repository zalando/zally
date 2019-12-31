package de.zalando.zally.rule

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import de.zalando.zally.core.ContentParseResult
import de.zalando.zally.core.ObjectTreeReader
import de.zalando.zally.core.RulesManager
import de.zalando.zally.core.ast.ReverseAst
import io.swagger.util.Json

class JsonRulesValidator(rules: RulesManager) : RulesValidator<JsonNode>(rules) {
    private var ast: ReverseAst? = null

    override fun parse(content: String, authorization: String?): ContentParseResult<JsonNode> {
        return try {
            val json = ObjectTreeReader().read(content)
            ast = createAst(json)
            ContentParseResult.ParsedSuccessfully(json)
        } catch (e: Exception) {
            ContentParseResult.NotApplicable()
        }
    }

    private fun createAst(json: JsonNode) = try {
        Json.mapper().convertValue(json, Map::class.java)
    } catch (e: Exception) {
        null
    }?.let {
        ReverseAst.fromObject(it).build()
    }

    override fun ignore(root: JsonNode, pointer: JsonPointer, ruleId: String) = ast?.isIgnored(pointer, ruleId) ?: false
}
