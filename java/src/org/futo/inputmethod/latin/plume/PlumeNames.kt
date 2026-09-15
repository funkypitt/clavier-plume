package org.futo.inputmethod.latin.plume

import org.futo.inputmethod.latin.DictionaryFacilitator
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import java.text.Normalizer
import java.util.Locale

/**
 * Nom propre ou élision qui n'a que ses lettres pour lui (2.1.1, signalé par l'utilisateur le 2026-09-15 :
 * « moses » → « Moses » au lieu de « modes »). Le moteur apparie sans casse ni accents, et l'apostrophe s'omet à
 * coût nul : « Moses » (nom au plancher, f = 24) et « m'oses » (élision rare, f = 25) sont des appariements exacts
 * de la frappe « moses » et passent devant « modes » (f = 47), à une touche. Règle : frappe tout en minuscules,
 * sans apostrophe, qui n'est pas un mot ; premier candidat au même squelette, capitalisé ou élidé, venu d'un
 * dictionnaire LIVRÉ ; s'il existe un candidat en minuscules sans apostrophe, de même longueur, d'un autre squelette
 * et STRICTEMENT plus fréquent, il passe devant. « j'ai », « c'est », « n'est » (f ≥ 170) restent devant tout ;
 * un nom appris ou du dictionnaire personnel garde la main.
 */
object PlumeNames {
    @Volatile private var facilitator: DictionaryFacilitator? = null
    @JvmStatic fun setFacilitator(f: DictionaryFacilitator?) { facilitator = f }

    private fun skeleton(s: String): String =
        Normalizer.normalize(s.lowercase(Locale.ROOT), Normalizer.Form.NFD).filter { Character.getType(it) != Character.NON_SPACING_MARK.toInt() }

    @JvmStatic
    fun preferCommonWord(container: MutableList<SuggestedWordInfo>, typed: String) {
        val fac = facilitator ?: return
        if (container.size < 2 || typed.isEmpty()) return
        if (!typed.all { it.isLetter() && it.isLowerCase() }) return
        val first = container[0]
        val src = first.mSourceDict ?: return
        if (src.isUserSpecific) return
        val t = first.mWord
        val byLettersOnly = (t[0].isUpperCase() || t.contains('\'') || t.contains('’')) && skeleton(t) == skeleton(typed)
        if (!byLettersOnly) return
        val fTop = fac.getWordFrequency(t)
        val i = container.indexOfFirst { c ->
            val w = c.mWord
            w.length == typed.length && w.all { it.isLetter() && it.isLowerCase() } && skeleton(w) != skeleton(typed)
                    && (c.mSourceDict?.isUserSpecific == true || fac.getWordFrequency(w) > fTop)
        }
        if (i <= 0) return
        val common = container.removeAt(i)
        container.add(0, common)
    }
}
