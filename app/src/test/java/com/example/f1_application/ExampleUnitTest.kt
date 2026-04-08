package com.example.f1_application

import org.junit.Assert.*
import org.junit.Test

class F1UnitTests {



    private fun toTitleCase(text: String?): String {
        if (text.isNullOrBlank()) return "Ismeretlen"
        return text.split(" ", "_", "-").filter { it.isNotEmpty() }.joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }

    @Test
    fun `toTitleCase - normál névből helyes formátum`() {
        val result = toTitleCase("max verstappen")
        assertEquals("Max Verstappen", result)
    }

    @Test
    fun `toTitleCase - null bemenetre Ismeretlen visszatérési érték`() {
        val result = toTitleCase(null)
        assertEquals("Ismeretlen", result)
    }

    @Test
    fun `toTitleCase - üres stringre Ismeretlen visszatérési érték`() {
        val result = toTitleCase("")
        assertEquals("Ismeretlen", result)
    }

    @Test
    fun `toTitleCase - aláhúzással elválasztott szavak`() {
        val result = toTitleCase("red_bull_racing")
        assertEquals("Red Bull Racing", result)
    }


    private fun formatPointsValue(points: Double): String =
        if (points % 1.0 == 0.0) points.toInt().toString() else points.toString()

    @Test
    fun `formatPointsValue - egész pontszám esetén nincs tizedes`() {
        val result = formatPointsValue(275.0)
        assertEquals("275", result)
    }

    @Test
    fun `formatPointsValue - tizedes pontszám megmarad`() {
        val result = formatPointsValue(18.5)
        assertEquals("18.5", result)
    }


    private fun formatF1Date(rawDate: String): String =
        if (rawDate.length >= 10) rawDate.substring(0, 10).replace("-", ". ") else rawDate

    @Test
    fun `formatF1Date - ISO dátumból pontokkal elválasztott formátum`() {
        val result = formatF1Date("2026-05-10T13:00:00Z")
        assertEquals("2026. 05. 10", result)
    }

    @Test
    fun `formatF1Date - túl rövid string változatlanul marad`() {
        val result = formatF1Date("2026")
        assertEquals("2026", result)
    }
}