package de.zalando.zally.integration.config

import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class StringJsonUserType : UserType {
    override fun sqlTypes(): IntArray = intArrayOf(Types.JAVA_OBJECT)
    override fun returnedClass(): Class<*> = String::class.java
    override fun equals(x: Any?, y: Any?): Boolean = if (x == null) y == null else x == y
    override fun hashCode(x: Any): Int = x.hashCode()
    override fun nullSafeGet(rs: ResultSet, names: Array<String>, session: SharedSessionContractImplementor, owner: Any): Any? =
            if (rs.getString(names[0]) == null) null else rs.getString(names[0])
    override fun nullSafeSet(st: PreparedStatement, value: Any?, index: Int, session: SharedSessionContractImplementor) =
            if (value == null) { st.setNull(index, Types.OTHER) } else { st.setObject(index, value, Types.OTHER) }
    override fun deepCopy(value: Any): Any = value
    override fun isMutable(): Boolean = true
    override fun disassemble(value: Any): Serializable = this.deepCopy(value) as String
    override fun assemble(cached: Serializable, owner: Any): Any = this.deepCopy(cached)
    override fun replace(original: Any, target: Any, owner: Any): Any = original
}