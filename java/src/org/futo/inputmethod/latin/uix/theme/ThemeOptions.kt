package org.futo.inputmethod.latin.uix.theme

import android.content.Context
import androidx.annotation.StringRes
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.KeyboardColorScheme
import org.futo.inputmethod.latin.uix.actions.BugInfo
import org.futo.inputmethod.latin.uix.actions.BugViewerState
import org.futo.inputmethod.latin.uix.theme.presets.AMOLEDDarkPurple
import org.futo.inputmethod.latin.uix.theme.presets.CatppuccinMocha
import org.futo.inputmethod.latin.uix.theme.presets.ClassicMaterialDark
import org.futo.inputmethod.latin.uix.theme.presets.ClassicMaterialLight
import org.futo.inputmethod.latin.uix.theme.presets.CottonCandy
import org.futo.inputmethod.latin.uix.theme.presets.DeepSeaDark
import org.futo.inputmethod.latin.uix.theme.presets.DeepSeaLight
import org.futo.inputmethod.latin.uix.theme.presets.DefaultDarkScheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeDarkTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeLightTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeSystemTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeEncreTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeEncreInvTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeLavandeTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeSaugeTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeCoquetteTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeMilitanteTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeTorrideTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeNatureTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeNordiqueTheme
import org.futo.inputmethod.latin.uix.theme.presets.PlumeTerracottaTheme
import org.futo.inputmethod.latin.uix.theme.presets.DefaultLightScheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicDarkTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicLightTheme
import org.futo.inputmethod.latin.uix.theme.presets.DynamicSystemTheme
import org.futo.inputmethod.latin.uix.theme.presets.Emerald
import org.futo.inputmethod.latin.uix.theme.presets.Gradient1
import org.futo.inputmethod.latin.uix.theme.presets.HotDog
import org.futo.inputmethod.latin.uix.theme.presets.Snowfall
import org.futo.inputmethod.latin.uix.theme.presets.SteelGray
import org.futo.inputmethod.latin.uix.theme.presets.Sunflower
import org.futo.inputmethod.latin.uix.theme.presets.VoiceInputTheme
import org.futo.inputmethod.latin.uix.theme.presets.DevTheme
import org.futo.inputmethod.latin.uix.theme.presets.HighContrastYellow

data class ThemeOption(
    val dynamic: Boolean,
    val key: String,
    @StringRes val name: Int,
    val available: (Context) -> Boolean,
    val obtainColors: (Context) -> KeyboardColorScheme,
)

// Plume : un seul design, trois entrées (auto / clair / sombre). Les presets FUTO restent
// dans le code (références internes) mais ne sont plus proposés ; une clé inconnue → Plume auto.
val ThemeOptions = mapOf(
    PlumeSystemTheme.key to PlumeSystemTheme,
    PlumeLightTheme.key to PlumeLightTheme,
    PlumeDarkTheme.key to PlumeDarkTheme,
    PlumeEncreTheme.key to PlumeEncreTheme,
    PlumeEncreInvTheme.key to PlumeEncreInvTheme,
    PlumeLavandeTheme.key to PlumeLavandeTheme,
    PlumeSaugeTheme.key to PlumeSaugeTheme,
    PlumeNordiqueTheme.key to PlumeNordiqueTheme,
    PlumeTerracottaTheme.key to PlumeTerracottaTheme,
    PlumeCoquetteTheme.key to PlumeCoquetteTheme,
    PlumeMilitanteTheme.key to PlumeMilitanteTheme,
    PlumeTorrideTheme.key to PlumeTorrideTheme,
    PlumeNatureTheme.key to PlumeNatureTheme,
)

val ThemeOptionKeys = ThemeOptions.keys

// Plume : thème par défaut = Plume (auto), y compris sur les builds unstable (plus de thème Dev rayé)
fun defaultThemeOption(context: Context): ThemeOption =
    if (org.futo.inputmethod.latin.BuildConfig.PLUME_PUBLIC) PlumeLavandeTheme else PlumeSystemTheme

fun getThemeOption(context: Context, key: String): ThemeOption? {
    return ThemeOptions[key] ?: run {
        return ZipThemes.ThemeFileName.fromSetting(key)?.let { name ->
            ThemeOption(
                dynamic = false,
                key = key,
                name = 0,
                available = { true },
                obtainColors = {
                    try {
                        ZipThemes.loadScheme(context, name)
                    } catch(e: Exception) {
                        BugViewerState.pushBug(BugInfo(
                            name = "Theme $name",
                            details = e.toString(),
                        ))
                        defaultThemeOption(context).obtainColors(it)
                    }
                }
            )
        }
    }
}

fun ThemeOption?.orDefault(context: Context): ThemeOption {
    val themeOptionFromSettings = this
    val themeOption = when {
        themeOptionFromSettings == null -> defaultThemeOption(context)
        !themeOptionFromSettings.available(context) -> defaultThemeOption(context)
        else -> themeOptionFromSettings
    }

    return themeOption
}