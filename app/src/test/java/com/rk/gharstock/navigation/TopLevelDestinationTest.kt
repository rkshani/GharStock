package com.rk.gharstock.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class TopLevelDestinationTest {

    @Test
    fun `bottom bar tabs are in the expected order`() {
        assertEquals(
            listOf("HOME", "SHOPPING", "STOCK", "MEDICINE", "MORE"),
            TopLevelDestination.entries.map { it.name },
        )
    }

    @Test
    fun `every tab has a distinct route`() {
        val routes = TopLevelDestination.entries.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }
}
