package com.example.math

sealed class ConverterCategory(val nameEn: String, val nameFa: String, val units: List<UnitType>) {
    object Length : ConverterCategory("Length", "طول", listOf(
        UnitType("Meter", "متر", "m", 1.0),
        UnitType("Kilometer", "کیلومتر", "km", 1000.0),
        UnitType("Mile", "مایل", "mi", 1609.344),
        UnitType("Foot", "فوت", "ft", 0.3048),
        UnitType("Inch", "اینچ", "in", 0.0254),
        UnitType("Centimeter", "سانتی‌متر", "cm", 0.01)
    ))

    object Weight : ConverterCategory("Weight", "وزن", listOf(
        UnitType("Gram", "گرم", "g", 1.0),
        UnitType("Kilogram", "کیلوگرم", "kg", 1000.0),
        UnitType("Pound", "پوند", "lb", 453.59237),
        UnitType("Ounce", "اونس", "oz", 28.349523)
    ))

    object Temperature : ConverterCategory("Temperature", "دما", listOf(
        UnitType("Celsius", "سلسیوس", "°C", 1.0),
        UnitType("Fahrenheit", "فارنهایت", "°F", 1.0),
        UnitType("Kelvin", "کلوین", "K", 1.0)
    ))

    object Area : ConverterCategory("Area", "مساحت", listOf(
        UnitType("Square Meter", "متر مربع", "m²", 1.0),
        UnitType("Square Kilometer", "کیلومتر مربع", "km²", 1000000.0),
        UnitType("Acre", "آکر", "ac", 4046.8564),
        UnitType("Hectare", "هکتار", "ha", 10000.0)
    ))

    companion object {
        fun values() = listOf(Length, Weight, Temperature, Area)
    }
}

data class UnitType(
    val nameEn: String,
    val nameFa: String,
    val symbol: String,
    val baseFactor: Double
)

object UnitConverter {
    fun convert(value: Double, from: UnitType, to: UnitType, category: ConverterCategory): Double {
        if (category is ConverterCategory.Temperature) {
            val tempInCelsius = when (from.nameEn) {
                "Celsius" -> value
                "Fahrenheit" -> (value - 32.0) * 5.0 / 9.0
                "Kelvin" -> value - 273.15
                else -> value
            }
            return when (to.nameEn) {
                "Celsius" -> tempInCelsius
                "Fahrenheit" -> tempInCelsius * 9.0 / 5.0 + 32.0
                "Kelvin" -> tempInCelsius + 273.15
                else -> tempInCelsius
            }
        } else {
            val valueInBase = value * from.baseFactor
            return valueInBase / to.baseFactor
        }
    }
}
