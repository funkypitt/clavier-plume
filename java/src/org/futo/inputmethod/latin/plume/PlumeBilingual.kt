package org.futo.inputmethod.latin.plume

import org.futo.inputmethod.latin.DictionaryFacilitator
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import java.text.Normalizer
import java.util.Locale

/**
 * Mode « les deux langues en même temps » (2.1.0) : les deux dictionnaires sont consultés, mais un mot de
 * l'autre langue ne doit pas bloquer une correction française quand on écrit en français. Mesuré le
 * 2026-09-15 : avec les deux dictionnaires, « lea » restait « Lea » (anglais : pré), « nest » restait
 * « Nest », « jai » « Jai » — le moteur préfère l'appariement exact, quelle que soit la langue.
 *
 * Règle : tant que la langue la plus confiante (trois derniers mots) n'est pas l'autre langue, les
 * candidats de l'autre dictionnaire dont le squelette (minuscules, sans accents) est celui de la frappe
 * sont écartés. Dès qu'on écrit vraiment dans l'autre langue (confiance), « boot » reste « boot ».
 */
object PlumeBilingual {
    @Volatile private var facilitator: DictionaryFacilitator? = null
    @JvmStatic fun setFacilitator(f: DictionaryFacilitator?) { facilitator = f }

    private fun skeleton(s: String): String =
        Normalizer.normalize(s.lowercase(Locale.ROOT), Normalizer.Form.NFD).filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }

    @JvmStatic
    fun filterOtherLanguageExactMatches(container: MutableList<SuggestedWordInfo>, typed: String, active: Locale?) {
        val facilitator = facilitator
        if (active == null || facilitator == null || typed.isEmpty()) return
        val locales = facilitator.locales
        if (locales.size < 2) return                                    // une langue à la fois : rien à faire
        val other = locales.firstOrNull { it.language != active.language } ?: return
        val confident = facilitator.mostConfidentLocale
        if (confident != null && confident.language == other.language) return   // on écrit dans l'autre langue
        val sk = skeleton(typed)
        container.removeAll { info ->
            val src = info.mSourceDict?.mLocale ?: return@removeAll false
            src.language == other.language && skeleton(info.mWord) == sk
        }
        // Les candidats de l'autre langue restent proposés mais passent après ceux de la langue active :
        // « lex » → « les » avant « led », « noe » → « nos » avant « now » (banc du 2026-09-15).
        val (others, actives) = container.partition { it.mSourceDict?.mLocale?.language == other.language }
        if (others.isNotEmpty() && actives.isNotEmpty()) { container.clear(); container.addAll(actives); container.addAll(others) }
    }
}
