package com.wenha.cefrenglish.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavGraphTest {
    @Test
    fun startDestination_isPlacement() {
        assertEquals(AppRoute.Placement.route, startDestination)
    }
}
