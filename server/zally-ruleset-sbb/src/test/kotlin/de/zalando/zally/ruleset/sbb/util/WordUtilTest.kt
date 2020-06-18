package de.zalando.zally.ruleset.zalando.util

import de.zalando.zally.ruleset.zalando.util.WordUtil.isPlural
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WordUtilTest {
    @Test
    fun positiveCasePluralized() {
        assertTrue(isPlural("dogs"))
        assertTrue(isPlural("resources"))
        assertTrue(isPlural("payments"))
        assertTrue(isPlural("orders"))
        assertTrue(isPlural("parcels"))
        assertTrue(isPlural("commissions"))
        assertTrue(isPlural("commission_groups"))
        assertTrue(isPlural("articles"))
        assertTrue(isPlural("merchants"))
        assertTrue(isPlural("warehouse-locations"))
        assertTrue(isPlural("sales-channels"))
        assertTrue(isPlural("domains"))
        assertTrue(isPlural("addresses"))
        assertTrue(isPlural("bank-accounts"))
    }

    @Test
    fun negativeCasePluralized() {
        assertFalse(isPlural("cat"))
        assertFalse(isPlural("resource"))
        assertFalse(isPlural("payment"))
        assertFalse(isPlural("order"))
        assertFalse(isPlural("parcel"))
        assertFalse(isPlural("item"))
        assertFalse(isPlural("commission"))
        assertFalse(isPlural("commission_group"))
        assertFalse(isPlural("article"))
        assertFalse(isPlural("merchant"))
        assertFalse(isPlural("warehouse-location"))
        assertFalse(isPlural("sales-channel"))
        assertFalse(isPlural("domain"))
        assertFalse(isPlural("address"))
        assertFalse(isPlural("bank-account"))
    }

    @Test
    fun specialCasePluralized() {
        assertTrue(isPlural("vat")) // whitelisted
        assertTrue(isPlural("apis")) // whitelisted
    }
}
