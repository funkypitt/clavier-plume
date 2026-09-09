package org.futo.inputmethod.latin.plume

import org.futo.inputmethod.latin.SuggestedWords

/**
 * Version publique : pas de pondération probabiliste des touches. Le clavier garde le
 * comportement d'origine de FUTO (touches élargies de façon binaire pour les lettres
 * valides). Cette fonction est un point d'extension inactif.
 */
object PlumeKeyBoost {
    @JvmStatic
    fun computeWeights(typedPrefix: String, suggestions: SuggestedWords): Map<Int, Float>? = null
}
