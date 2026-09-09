package org.futo.inputmethod.latin.uix.theme.presets

import android.content.Context
import android.content.res.Configuration
import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.KeyboardColorScheme
import org.futo.inputmethod.latin.uix.extendedDarkColorScheme
import org.futo.inputmethod.latin.uix.extendedLightColorScheme
import org.futo.inputmethod.latin.uix.theme.AdvancedThemeOptions
import org.futo.inputmethod.latin.uix.theme.ThemeOption

/**
 * Thèmes Plume — palettes du clavier iOS (mesurées via KeyboardKit), typographie Selawik
 * (police open source de Microsoft, métriquement compatible Segoe UI, licence OFL — le
 * parent de Segoe WP), étiquettes un peu plus grandes que FUTO, une seule couleur
 * d'accent (bleu iOS) pour la touche Retour et les suggestions.
 *
 *   clair : fond #D1D3D9, touche #FFFFFF, fonction #ABB1BA, texte noir, accent #007AFF
 *   sombre : fond #2C2C2C, touche #6B6B6B, fonction #474747, texte blanc, accent #0A84FF
 */
private object PlumeFont {
    @Volatile private var cached: Typeface? = null
    fun get(context: Context): Typeface? {
        cached?.let { return it }
        return try {
            Typeface.createFromAsset(context.assets, "fonts/Selawik-Semilight.ttf").also { cached = it }
        } catch (e: Exception) {
            null
        }
    }
}

private fun plumeAdvanced(context: Context) = AdvancedThemeOptions(
    keyRoundness = 1.0f,          // rayon 5 dp (BasicThemeProvider, base Plume)
    keyBorders = true,
    font = PlumeFont.get(context),
    textSizeMultiplier = 1.12f,   // Segoe WP / SF : lettres grandes et légères
    hintSizeMultiplier = 0.95f,
    themeName = "Plume",
    themeAuthor = "Plume",
)

private fun KeyboardColorScheme.withPlume(context: Context): KeyboardColorScheme =
    copy(extended = extended.copy(advancedThemeOptions = plumeAdvanced(context)))

private val plumeLight = extendedLightColorScheme(
    primary = Color(0xFF007AFF), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E6FF), onPrimaryContainer = Color(0xFF002A5C),
    secondary = Color(0xFFFFFFFF), onSecondary = Color(0xFF000000),   // Shift actif : blanc / flèche noire (iOS)
    secondaryContainer = Color(0xFFE5E5EA), onSecondaryContainer = Color(0xFF1C1C1E),
    tertiary = Color(0xFF34C759), onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD9F5DF), onTertiaryContainer = Color(0xFF0B3D1A),
    error = Color(0xFFFF3B30), onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
    outline = Color(0xFF8E8E93), outlineVariant = Color(0xFFC7C7CC),
    surface = Color(0xFFD1D3D9), onSurface = Color(0xFF000000), onSurfaceVariant = Color(0xFF3C3C43),
    surfaceContainerHighest = Color(0xFFC2C4CA),
    shadow = Color(0xFF000000).copy(alpha = 0.3f),
    keyboardSurface = Color(0xFFD1D3D9),
    keyboardSurfaceDim = Color(0xFFC6C8CE),
    keyboardContainer = Color(0xFFFFFFFF),
    keyboardContainerVariant = Color(0xFFABB1BA),
    onKeyboardContainer = Color(0xFF000000),
    keyboardPress = Color(0xFFABB1BA),
    keyboardFade0 = Color(0xFFD1D3D9), keyboardFade1 = Color(0xFFD1D3D9),
    primaryTransparent = Color(0xFF007AFF).copy(alpha = 0.25f),
    onSurfaceTransparent = Color(0xFF000000).copy(alpha = 0.08f),
)

private val plumeDark = extendedDarkColorScheme(
    primary = Color(0xFF0A84FF), onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF00397A), onPrimaryContainer = Color(0xFFD6E6FF),
    secondary = Color(0xFFFFFFFF), onSecondary = Color(0xFF000000),   // Shift actif : blanc / flèche noire (iOS)
    secondaryContainer = Color(0xFF3A3A3C), onSecondaryContainer = Color(0xFFF2F2F7),
    tertiary = Color(0xFF30D158), onTertiary = Color(0xFF00390F),
    tertiaryContainer = Color(0xFF0B3D1A), onTertiaryContainer = Color(0xFFD9F5DF),
    error = Color(0xFFFF453A), onError = Color(0xFF410002),
    errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
    outline = Color(0xFF8E8E93), outlineVariant = Color(0xFF48484A),
    surface = Color(0xFF2C2C2C), onSurface = Color(0xFFFFFFFF), onSurfaceVariant = Color(0xFFEBEBF5),
    surfaceContainerHighest = Color(0xFF3A3A3C),
    shadow = Color(0xFF000000).copy(alpha = 0.7f),
    keyboardSurface = Color(0xFF2C2C2C),
    keyboardSurfaceDim = Color(0xFF232323),
    keyboardContainer = Color(0xFF6B6B6B),
    keyboardContainerVariant = Color(0xFF474747),
    onKeyboardContainer = Color(0xFFFFFFFF),
    keyboardPress = Color(0xFF8E8E93),
    keyboardFade0 = Color(0xFF2C2C2C), keyboardFade1 = Color(0xFF2C2C2C),
    primaryTransparent = Color(0xFF0A84FF).copy(alpha = 0.3f),
    onSurfaceTransparent = Color(0xFFFFFFFF).copy(alpha = 0.1f),
)

// ---------------------------------------------------------------------------------------------
// Thèmes supplémentaires Plume (2026-09-08). Règle commune : lettres sur touche ≥ 8:1 (AAA),
// touches de fonction dans une variante du fond, un seul accent, Shift actif = inversion
// (fond de la couleur du texte, flèche de la couleur des touches).
// ---------------------------------------------------------------------------------------------
private data class PlumePalette(
    val light: Boolean,
    val surface: Long, val key: Long, val func: Long, val text: Long,
    val accent: Long, val onAccent: Long,
    val press: Long, val funcText: Long = text,
    val name: String,
)

private fun PlumePalette.scheme(): KeyboardColorScheme {
    val c = { v: Long -> Color(v) }
    val sfc = c(surface); val txt = c(text); val acc = c(accent); val onAcc = c(onAccent)
    val args = listOf(
        acc, onAcc, acc.copy(alpha = 0.25f), txt,           // primary, onPrimary, primaryContainer, onPrimaryContainer
        txt, c(key),                                         // secondary (Shift actif), onSecondary
        c(func), c(funcText),                                // secondaryContainer, onSecondaryContainer (glyphes des fonctions)
        acc, onAcc, acc.copy(alpha = 0.3f), txt,             // tertiary…
        Color(0xFFD64545), Color(0xFFFFFFFF), Color(0xFFFFDAD6), Color(0xFF410002),
        txt.copy(alpha = 0.55f), txt.copy(alpha = 0.3f),     // outline, outlineVariant
        sfc, txt, txt.copy(alpha = 0.8f), c(func),           // surface, onSurface, onSurfaceVariant, surfaceContainerHighest
        Color(0xFF000000).copy(alpha = if (light) 0.3f else 0.7f),
        sfc, c(func), c(key), c(func), txt, c(press),        // keyboardSurface, Dim, Container, ContainerVariant, onContainer, press
        sfc, sfc, acc.copy(alpha = 0.3f), txt.copy(alpha = 0.1f),
    )
    return if (light) extendedLightColorScheme(
        primary = args[0], onPrimary = args[1], primaryContainer = args[2], onPrimaryContainer = args[3],
        secondary = args[4], onSecondary = args[5], secondaryContainer = args[6], onSecondaryContainer = args[7],
        tertiary = args[8], onTertiary = args[9], tertiaryContainer = args[10], onTertiaryContainer = args[11],
        error = args[12], onError = args[13], errorContainer = args[14], onErrorContainer = args[15],
        outline = args[16], outlineVariant = args[17],
        surface = args[18], onSurface = args[19], onSurfaceVariant = args[20], surfaceContainerHighest = args[21],
        shadow = args[22],
        keyboardSurface = args[23], keyboardSurfaceDim = args[24], keyboardContainer = args[25],
        keyboardContainerVariant = args[26], onKeyboardContainer = args[27], keyboardPress = args[28],
        keyboardFade0 = args[29], keyboardFade1 = args[30], primaryTransparent = args[31], onSurfaceTransparent = args[32],
    ) else extendedDarkColorScheme(
        primary = args[0], onPrimary = args[1], primaryContainer = args[2], onPrimaryContainer = args[3],
        secondary = args[4], onSecondary = args[5], secondaryContainer = args[6], onSecondaryContainer = args[7],
        tertiary = args[8], onTertiary = args[9], tertiaryContainer = args[10], onTertiaryContainer = args[11],
        error = args[12], onError = args[13], errorContainer = args[14], onErrorContainer = args[15],
        outline = args[16], outlineVariant = args[17],
        surface = args[18], onSurface = args[19], onSurfaceVariant = args[20], surfaceContainerHighest = args[21],
        shadow = args[22],
        keyboardSurface = args[23], keyboardSurfaceDim = args[24], keyboardContainer = args[25],
        keyboardContainerVariant = args[26], onKeyboardContainer = args[27], keyboardPress = args[28],
        keyboardFade0 = args[29], keyboardFade1 = args[30], primaryTransparent = args[31], onSurfaceTransparent = args[32],
    )
}

private fun PlumePalette.withPlume(context: Context): KeyboardColorScheme =
    scheme().copy(extended = scheme().extended.copy(advancedThemeOptions = plumeAdvanced(context).copy(themeName = name)))

/** Papier électronique : noir sur blanc. Touches blanches cerclées de noir (le contour est
 *  dessiné par BasicThemeProvider pour les thèmes « Plume Encre »), fonctions en gris clair. */
private val encre = PlumePalette(true, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFD9D9D9, 0xFF000000, 0xFF000000, 0xFFFFFFFF, 0xFFBFBFBF, name = "Plume Encre")
/** Papier électronique inversé : blanc sur noir, touches noires cerclées de blanc, fonctions blanches. */
private val encreInv = PlumePalette(false, 0xFF000000, 0xFF000000, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFF000000, 0xFF666666, funcText = 0xFF000000, name = "Plume Encre inversée")
/** Violet : aubergine profonde, touches prune, accent lavande (Very Peri / lilas 2022-2026). Lettres 10,5:1. */
private val lavande = PlumePalette(false, 0xFF1F1A2E, 0xFF3B3352, 0xFF2B2540, 0xFFF4EFFF, 0xFFB8A4FF, 0xFF1F1A2E, 0xFF5A4F78, name = "Plume Lavande")
/** Sauge : vert-gris doux (tendance « earthy » 2024-2026), touches crème, accent vert forêt. Lettres 14:1. */
private val sauge = PlumePalette(true, 0xFFD9E0D4, 0xFFF7F9F4, 0xFFB5C2B2, 0xFF1E2A21, 0xFF4C7A5B, 0xFFFFFFFF, 0xFFB5C2B2, name = "Plume Sauge")
/** Nordique : palette Nord (bleu-gris arctique, favorite des développeurs), accent bleu glacier. Lettres 8,7:1. */
private val nordique = PlumePalette(false, 0xFF2E3440, 0xFF3B4252, 0xFF353B4A, 0xFFECEFF4, 0xFF88C0D0, 0xFF2E3440, 0xFF4C566A, name = "Plume Nordique")
/** Terracotta : neutres chauds (Mocha Mousse 2025), touches ivoire, accent terre cuite. Lettres 14,8:1. */
private val terracotta = PlumePalette(true, 0xFFE6D9CC, 0xFFFCF7F1, 0xFFC8B6A5, 0xFF2B211B, 0xFFC25E38, 0xFFFFFFFF, 0xFFC8B6A5, name = "Plume Terracotta")

val PlumeEncreTheme = ThemeOption(false, "PlumeEncre", R.string.theme_plume_encre, { true }) { encre.withPlume(it) }
val PlumeEncreInvTheme = ThemeOption(false, "PlumeEncreInv", R.string.theme_plume_encre_inv, { true }) { encreInv.withPlume(it) }
val PlumeLavandeTheme = ThemeOption(false, "PlumeLavande", R.string.theme_plume_lavande, { true }) { lavande.withPlume(it) }
val PlumeSaugeTheme = ThemeOption(false, "PlumeSauge", R.string.theme_plume_sauge, { true }) { sauge.withPlume(it) }
val PlumeNordiqueTheme = ThemeOption(false, "PlumeNordique", R.string.theme_plume_nordique, { true }) { nordique.withPlume(it) }
val PlumeTerracottaTheme = ThemeOption(false, "PlumeTerracotta", R.string.theme_plume_terracotta, { true }) { terracotta.withPlume(it) }

val PlumeLightTheme = ThemeOption(
    dynamic = false, key = "PlumeLight", name = R.string.theme_plume_light, available = { true }
) { plumeLight.withPlume(it) }

val PlumeDarkTheme = ThemeOption(
    dynamic = false, key = "PlumeDark", name = R.string.theme_plume_dark, available = { true }
) { plumeDark.withPlume(it) }

/** Suit le mode clair/sombre du système, comme le clavier iOS. */
val PlumeSystemTheme = ThemeOption(
    dynamic = true, key = "PlumeSystem", name = R.string.theme_plume_system, available = { true }
) {
    val isLight = (it.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_NO
    (if (isLight) plumeLight else plumeDark).withPlume(it)
}
