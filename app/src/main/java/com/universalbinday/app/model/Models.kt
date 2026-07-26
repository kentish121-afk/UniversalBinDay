package com.universalbinday.app.model

import androidx.compose.ui.graphics.Color

enum class ContainerType(val displayName: String) {
    BIN("Bin"),
    BOX("Box"),
    CADDY("Caddy"),
    BAG("Bag")
}

enum class Frequency(val displayName: String, val weeks: Int) {
    WEEKLY("Weekly", 1),
    FORTNIGHTLY("Fortnightly", 2)
}

data class Council(
    val id: String,
    val name: String,
    val reportEmail: String? = null,
    val reportSubject: String = "Missed Bin Collection Report"
)

data class BinDefinition(
    val id: String,
    val name: String,
    val defaultColor: Long, // ARGB
    val defaultContainer: ContainerType
)

data class UserBinConfig(
    val binId: String,
    val enabled: Boolean = true,
    val daysOfWeek: Set<Int> = emptySet(), // Calendar.SUNDAY = 1 ... SATURDAY = 7
    val frequency: Frequency = Frequency.WEEKLY,
    val color: Long = 0xFF000000, // ARGB
    val containerType: ContainerType = ContainerType.BIN,
    val fortnightlyIsThisWeek: Boolean = true // for alternating
)

data class AppSettings(
    val selectedCouncilId: String? = null,
    val bins: List<UserBinConfig> = emptyList(),
    val notificationHour: Int = 19,
    val notificationMinute: Int = 0
)

object Defaults {
    val councils = listOf(
        Council("birmingham", "Birmingham City Council", "contact@birmingham.gov.uk"),
        Council("coventry", "Coventry City Council", "customer.services@coventry.gov.uk"),
        Council("dudley", "Dudley Council", "dudleycouncilplus@dudley.gov.uk"),
        Council("sandwell", "Sandwell Council", "contact@sandwell.gov.uk"),
        Council("solihull", "Solihull Council", "connect@solihull.gov.uk"),
        Council("walsall", "Walsall Council", "customer.services@walsall.gov.uk"),
        Council("wolverhampton", "City of Wolverhampton Council", "contactus@wolverhampton.gov.uk"),
        Council("other", "Other / Manual", null)
    )

    val binDefinitions = listOf(
        BinDefinition("residual", "Residual / General Waste", 0xFF212121, ContainerType.BIN),
        BinDefinition("recycling", "Mixed Recycling", 0xFF1565C0, ContainerType.BIN),
        BinDefinition("garden", "Garden Waste", 0xFF2E7D32, ContainerType.BIN),
        BinDefinition("food", "Food Waste", 0xFF5D4037, ContainerType.CADDY),
        BinDefinition("glass", "Glass", 0xFF00838F, ContainerType.BOX),
        BinDefinition("paper", "Paper / Card", 0xFF1565C0, ContainerType.BOX),
        BinDefinition("cardboard", "Cardboard", 0xFF6D4C41, ContainerType.BAG),
        BinDefinition("plastics", "Plastics", 0xFF0277BD, ContainerType.BIN)
    )

    fun defaultUserBins(): List<UserBinConfig> = binDefinitions.map {
        UserBinConfig(
            binId = it.id,
            enabled = false, // user enables what they have
            color = it.defaultColor,
            containerType = it.defaultContainer
        )
    }
}

fun Long.toComposeColor(): Color = Color(this)
