package com.zenzeros.kimon.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface KimonNavKey : NavKey {
    @Serializable
    data object Focus : KimonNavKey

    @Serializable
    data object Plan : KimonNavKey

    @Serializable
    data object Analyze : KimonNavKey

    @Serializable
    data object SettingsMain : KimonNavKey

    @Serializable
    data object TimerSettings : KimonNavKey

    @Serializable
    data object AlarmSettings : KimonNavKey

    @Serializable
    data object AppearanceSettings : KimonNavKey

    @Serializable
    data object AboutSettings : KimonNavKey
}
