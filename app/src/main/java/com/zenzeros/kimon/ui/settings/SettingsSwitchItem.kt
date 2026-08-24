package com.zenzeros.kimon.ui.settings

data class SettingsSwitchItem(
    val checked: Boolean,
    val enabled: Boolean = true,
    val collapsible: Boolean = false,
    val icon: Int,
    val label: Int,
    val description: Int,
    val onClick: (Boolean) -> Unit
)
