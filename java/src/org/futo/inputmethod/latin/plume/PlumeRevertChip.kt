package org.futo.inputmethod.latin.plume

import org.futo.inputmethod.latin.SuggestedWords
import org.futo.inputmethod.latin.SuggestedWords.SuggestedWordInfo
import org.futo.inputmethod.latin.inputlogic.InputLogic

/**
 * « Tap pour annuler » (iOS 17) : juste après une autocorrection, la barre de
 * suggestions commence par le mot tel qu'il avait été tapé, précédé de ↶. Un tap
 * le restaure (même mécanique que le retour arrière qui annule). Le chip disparaît
 * dès que l'utilisateur continue à écrire.
 */
object PlumeRevertChip {
    private const val ARROW = "↶ "

    @JvmStatic
    fun inject(words: SuggestedWords?, logic: InputLogic): SuggestedWords? {
        val last = logic.mLastComposedWord
        if (logic.mWordComposer.isComposingWord()) return words
        if (!last.canRevertCommit()) return words
        val typed = last.mTypedWord ?: return words
        if (typed.isBlank() || typed.length > 24) return words

        val chip = SuggestedWordInfo(typed, "", Int.MAX_VALUE, SuggestedWordInfo.KIND_PLUME_REVERT,
            null, SuggestedWordInfo.NOT_AN_INDEX, SuggestedWordInfo.NOT_A_CONFIDENCE)
        val list = ArrayList<SuggestedWordInfo>()
        list.add(chip)
        if (words != null && !words.isPunctuationSuggestions) {
            for (i in 0 until words.size()) {
                val info = words.getInfo(i)
                if (info.mWord == typed) continue
                list.add(info)
            }
        }
        return SuggestedWords(
            list, words?.mRawSuggestions, null, false, false, false,
            words?.mInputStyle ?: SuggestedWords.INPUT_STYLE_PREDICTION,
            words?.mSequenceNumber ?: SuggestedWords.NOT_A_SEQUENCE_NUMBER
        )
    }

    /** Libellé affiché dans la barre pour un chip d'annulation. */
    @JvmStatic
    fun label(info: SuggestedWordInfo): String =
        if (info.mKindAndFlags == SuggestedWordInfo.KIND_PLUME_REVERT) ARROW + info.mWord else info.mWord
}
