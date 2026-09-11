package org.futo.inputmethod.latin.plume

import java.util.Locale

/**
 * Paires de confusion du français : des mots tous valides (a/à, ou/où, quelle/qu'elle…) que le
 * dictionnaire ne peut pas départager, puisqu'il ne corrige jamais un mot valide. Seul le contexte
 * tranche : le modèle de langue peut alors changer l'ordre des propositions et le meilleur candidat,
 * rien d'autre (aucune réécriture du texte déjà validé).
 *
 * - le modèle préfère un autre membre de l'ensemble → ce membre passe en tête des propositions ;
 * - il le préfère d'un facteur ≥ [autocorrectRatio] → il devient la correction appliquée à l'espace.
 *
 * Les facteurs viennent de ~/code/plume/tools/lm/09_calibrer_confusions.py (texte relu, fausses
 * alertes mesurées). Un mot sans facteur n'est jamais autocorrigé, seulement réordonné.
 */
object PlumeConfusions {
    private val SETS: List<List<String>> = listOf(
        listOf("a", "à"), listOf("ou", "où"), listOf("la", "là"), listOf("et", "est", "es"),
        listOf("on", "ont"), listOf("son", "sont"), listOf("ces", "ses", "c'est", "s'est"),
        listOf("ce", "se"), listOf("ça", "sa"), listOf("ma", "m'a"), listOf("ta", "t'a"),
        // quelle/qu'elle, quelles/qu'elles et quant/quand exclus : le modèle préfère la forme élidée
        // (ou « quand ») dans presque tous les contextes, même quand le mot tapé est juste.
        listOf("fais", "fait"),
        listOf("peu", "peut", "peux"), listOf("sais", "sait"), listOf("tout", "tous"),
        listOf("leur", "leurs"), listOf("quand", "qu'en"), listOf("dans", "d'en"),
        listOf("sans", "s'en"), listOf("ni", "n'y"), listOf("si", "s'y"), listOf("mais", "mes"),
    )

    /** Terminaisons du premier groupe confondues à l'oral (aller/allé/allez…). */
    private val ER_SUFFIXES = listOf("ées", "és", "ée", "er", "ez", "é")

    private val MEMBERS: Map<String, List<String>> =
        SETS.flatMap { set -> set.map { it to set } }.toMap()

    /** P(meilleur autre membre) / P(mot tapé) minimal pour autocorriger. Clé : mot tapé, ou « -er »,
     *  « -é »… pour la famille du premier groupe. */
    // Calibration 2026-09-11, modèle 4b, Leipzig fra_news_2024 (jamais vu) + sous-titres : chaque facteur
    // garde les fausses alertes à 0 (≤ 0,2 % pour « ma ») sur les deux textes. Exclus faute de facteur sûr :
    // ou→où (0,8 % à ×100 en sous-titres), la→là, sa→ça, fais→fait, famille -er/-é.
    private val AUTOCORRECT_RATIO: Map<String, Float> = mapOf(
        "son" to 2f,     // → sont : 17-24 % des erreurs corrigées
        "ses" to 2f,     // → s'est : 28-43 %
        "peut" to 3f,    // → peu : 34-39 %
        "peux" to 5f,    // → peu : 48-65 %
        "peu" to 10f,    // → peux 86-95 %, peut 6-16 %
        "ta" to 20f,     // → t'a : 35 %
        "on" to 50f,     // → ont : 23-31 %
        "et" to 50f,     // → est : 6-11 %
        "a" to 50f,      // → à : 2-4 % (le réordonnancement fait l'essentiel)
        "ma" to 100f,    // → m'a : 12-14 %
    )

    /** Facteur minimal pour réordonner (sinon [DEFAULT_REORDER_RATIO]) : le plus petit facteur où moins de
     *  10 % des usages corrects du mot verraient un autre membre passer devant (texte relu). */
    private const val DEFAULT_REORDER_RATIO = 2f
    private val REORDER_RATIO: Map<String, Float> = mapOf(
        "ou" to 10f, "là" to 5f, "es" to 5f, "sa" to 10f, "leurs" to 10f, "s'y" to 10f,
        "-é" to 3f, "-ée" to 20f, "-ées" to 3f, "-ez" to 10f,
    )

    @JvmStatic
    fun reorderRatio(typed: String): Float {
        val w = typed.lowercase(Locale.FRENCH).replace('’', '\'')
        REORDER_RATIO[w]?.let { return it }
        if (w in MEMBERS) return DEFAULT_REORDER_RATIO
        val suffix = ER_SUFFIXES.firstOrNull { w.endsWith(it) } ?: return DEFAULT_REORDER_RATIO
        return REORDER_RATIO["-$suffix"] ?: DEFAULT_REORDER_RATIO
    }

    /** Membres de l'ensemble du mot tapé (minuscules, apostrophe droite), mot tapé compris ; null sinon.
     *  Pour la famille -er/-é, les formes sont candidates : à filtrer par le dictionnaire. */
    @JvmStatic
    fun members(typed: String, locale: Locale?): List<String>? {
        if (locale?.language != "fr") return null
        val w = typed.lowercase(Locale.FRENCH).replace('’', '\'')
        MEMBERS[w]?.let { return it }
        val suffix = ER_SUFFIXES.firstOrNull { w.endsWith(it) } ?: return null
        val stem = w.dropLast(suffix.length)
        if (stem.length < 3 || stem.any { !it.isLetter() }) return null
        return ER_SUFFIXES.map { stem + it }
    }

    /** Ensembles fixes seulement (sans la famille -er/-é) : ceux que le modèle score directement. */
    @JvmStatic
    fun fixedMembers(typed: String, locale: Locale?): List<String>? {
        if (locale?.language != "fr") return null
        return MEMBERS[typed.lowercase(Locale.FRENCH).replace('’', '\'')]
    }

    @JvmStatic
    fun autocorrectRatio(typed: String): Float? {
        val w = typed.lowercase(Locale.FRENCH).replace('’', '\'')
        AUTOCORRECT_RATIO[w]?.let { return it }
        if (w in MEMBERS) return null
        val suffix = ER_SUFFIXES.firstOrNull { w.endsWith(it) } ?: return null
        return AUTOCORRECT_RATIO["-$suffix"]
    }

    /** Mot tapé dont Suggest peut lever la protection des mots-outils, le temps d'un appel synchrone. */
    private val allowed = ThreadLocal<String?>()

    @JvmStatic
    fun allowProtectedCorrection(typed: String?) = allowed.set(typed)

    @JvmStatic
    fun isProtectedCorrectionAllowed(typed: String?): Boolean = typed != null && allowed.get() == typed

    /**
     * Mot tapé que le dictionnaire tient pour valide, le temps du même appel synchrone. La liste fusionnée par le
     * modèle ne contient pas l'entrée du dictionnaire pour le mot tapé : sans cette information, Suggest le croit
     * inconnu et autocorrige vers le premier mot du modèle (« tu invites » → « invités »).
     */
    private val validTyped = ThreadLocal<String?>()

    @JvmStatic
    fun setValidTypedWord(typed: String?) = validTyped.set(typed)

    @JvmStatic
    fun isValidTypedWord(typed: String?): Boolean = typed != null && validTyped.get() == typed
}
