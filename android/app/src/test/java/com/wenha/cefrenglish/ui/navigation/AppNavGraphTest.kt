package com.wenha.cefrenglish.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class AppNavGraphTest {
    @Test
    fun startDestination_isHome() {
        assertEquals(AppRoute.Home.route, startDestination)
    }
}
