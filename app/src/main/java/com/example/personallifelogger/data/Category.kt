package com.example.personallifelogger.data

sealed class Category(val title: String, val icon: String) {
    object All : Category("All", "📋")
    object DailyLife : Category("Daily Life", "🏠")
    object HealthFitness : Category("Health & Fitness", "💪")
    object Education : Category("Education", "📚")
    object WorkProductivity : Category("Work & Productivity", "💼")
    object SocialFun : Category("Social & Fun", "🎉")
    object MentalReflection : Category("Mental & Reflection", "🧠")
    object Finance : Category("Finance", "💰")
    object Other : Category("Other", "📌")

    companion object {
        val values = listOf(
            All, DailyLife, HealthFitness, Education,
            WorkProductivity, SocialFun, MentalReflection,
            Finance, Other
        )

        fun fromString(title: String): Category {
            return values.find { it.title == title } ?: Other
        }
    }
}