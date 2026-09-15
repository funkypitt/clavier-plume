package org.futo.inputmethod.latin.plume

import android.content.Context
import org.futo.inputmethod.latin.Dictionary
import org.futo.inputmethod.latin.DictionaryFacilitator
import org.futo.inputmethod.latin.DictionaryFactory
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.SuggestedWords
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.futo.inputmethod.latin.Subtypes
import java.util.Locale

/**
 * Indice de langue (2.1.0, décision de l'utilisateur du 2026-09-15) : après trois mots consécutifs
 * connus de l'autre langue et inconnus de la langue active, la barre propose « Passer en anglais ? » ;
 * un tap bascule la langue (même geste que la touche EN/FR). Pas de clignotement, pas de bascule
 * automatique. Après un refus (on continue d'écrire), silence pendant vingt mots.
 *
 * En mode « une langue à la fois » l'autre dictionnaire n'est pas chargé par le moteur : on en ouvre
 * une copie en lecture seule, uniquement pour dire si un mot lui appartient.
 */
object PlumeLanguageHint {
    private const val WORDS_NEEDED = 3
    private const val COOLDOWN_AFTER_DISMISS = 20
    private const val COOLDOWN_AFTER_SWITCH = 5

    private var run = 0
    private var cooldown = 0
    @Volatile private var armed = false
    private var otherLocale: Locale? = null
    @Volatile private var otherDict: Dictionary? = null
    private var loading = false

    /** L'autre langue installée (exactement deux langues), sinon null. */
    private fun otherLanguage(context: Context, active: Locale): Locale? {
        val keys = Subtypes.installedLanguageKeys(context)
        if (keys.size != 2) return null
        return keys.map { Subtypes.getLocale(it) }.firstOrNull { it.language != active.language }
    }

    private fun ensureOtherDict(context: Context, locale: Locale) {
        if (otherLocale?.language == locale.language && (otherDict != null || loading)) return
        otherLocale = locale; otherDict = null; loading = true
        Thread {
            val d = try { DictionaryFactory.createMainDictionaryFromManager(context.applicationContext, locale) } catch (e: Exception) { null }
            synchronized(this) { if (otherLocale?.language == locale.language) otherDict = d; loading = false }
        }.start()
    }

    /** À chaque mot validé (hors URL, hors champ sensible). */
    @JvmStatic
    fun onWordCommitted(context: Context, word: String, active: Locale, facilitator: DictionaryFacilitator) {
        if (word.isEmpty() || !word.any { it.isLetter() } || word.any { it.isDigit() }) return
        val other = otherLanguage(context, active) ?: run { reset(); return }
        if (armed) { armed = false; cooldown = COOLDOWN_AFTER_DISMISS; run = 0 }   // proposé, pas pris
        if (cooldown > 0) { cooldown--; return }
        // « The » en début de phrase : la majuscule automatique ne doit pas rendre le mot inconnu
        val forms = listOf(word, word.lowercase(active)).distinct()
        val inActive = forms.any { facilitator.isValidWordInLocale(it, active) }
        val inOther = forms.any { facilitator.isValidWordInLocale(it, other) } || run {
            ensureOtherDict(context, other); forms.any { otherDict?.isValidWord(it) == true }
        }
        when {
            inActive -> run = 0                 // un mot de la langue active remet le compteur à zéro
            inOther -> run++                    // connu de l'autre langue seulement
            else -> Unit                        // inconnu des deux : neutre (faute de frappe, nom…)
        }
        if (run >= WORDS_NEEDED) armed = true
    }

    /** Le chip en tête de barre quand l'indice est armé. */
    @JvmStatic
    fun inject(words: SuggestedWords?, context: Context, active: Locale): SuggestedWords? {
        if (!armed) return words
        val other = otherLanguage(context, active) ?: return words
        val ui = context.resources.configuration.locales[0]          // nom de la langue dans la langue de l'interface
        val label = context.getString(R.string.plume_hint_switch_language, other.getDisplayLanguage(ui))
        val chip = SuggestedWordInfo(label, "", Int.MAX_VALUE, SuggestedWordInfo.KIND_PLUME_LANG_HINT,
            null, SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)
        val list = ArrayList<SuggestedWordInfo>(); list.add(chip)
        if (words != null && !words.isPunctuationSuggestions) for (i in 0 until words.size()) list.add(words.getInfo(i))
        return SuggestedWords(list, words?.mRawSuggestions, null, false, false, false,
            words?.mInputStyle ?: SuggestedWords.INPUT_STYLE_PREDICTION,
            words?.mSequenceNumber ?: SuggestedWords.NOT_A_SEQUENCE_NUMBER)
    }

    /** Tap sur le chip : bascule de langue (touche EN/FR). */
    @JvmStatic
    fun onPicked(context: Context) {
        Subtypes.switchToNextLanguage(context, 1)
        armed = false; run = 0; cooldown = COOLDOWN_AFTER_SWITCH
    }

    @JvmStatic
    fun onStartInput() { armed = false }

    private fun reset() { run = 0; armed = false }
}
