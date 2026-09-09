package org.futo.inputmethod.latin.plume

import java.util.Locale

/**
 * Règles Plume (fork privé) — voir ~/code/plume/docs/03-plaintes-francophones.md.
 *
 * Regroupe les décisions linguistiques ajoutées au moteur FUTO/LatinIME pour le
 * français et l'anglais, afin qu'elles restent lisibles et faciles à ajuster.
 */
object PlumeRules {

    /** Espace fine insécable (U+202F) : avant « ; ! ? » en typographie française. */
    const val NARROW_NBSP = '\u202F'

    /** Espace insécable classique (U+00A0) : avant « : » et à l'intérieur de « ». */
    const val NBSP = '\u00A0'

    /**
     * Mots-outils courts que le clavier ne doit JAMAIS autocorriger en un autre mot,
     * seulement suggérer. Plainte n°1 des francophones : « mes » → « les »,
     * « mon » → « ton », « un » → « uniquement », « à » → « y », « sur » → « sir ».
     * Ces mots sont tous valides ; les corriger n'est jamais une faute de frappe.
     */
    private val PROTECTED_FR = setOf(
        "le", "la", "les", "un", "une", "des", "du", "de", "d",
        "me", "te", "se", "ne", "ce", "je", "tu", "il", "on", "y", "en",
        "ma", "ta", "sa", "mes", "tes", "ses", "mon", "ton", "son",
        "nos", "vos", "leur", "leurs", "notre", "votre",
        "ou", "où", "et", "est", "es", "ai", "as", "a", "à", "au", "aux",
        "sur", "sous", "par", "pour", "pas", "plus", "peu", "que", "qui", "quoi",
        "ça", "sa", "ces", "cet", "cette", "si", "tout", "tous", "toute", "toutes",
        "oui", "non", "ok", "vu", "va", "vas", "dès", "des", "car", "or", "ni",
    )

    private val PROTECTED_EN = setOf(
        "a", "an", "the", "i", "it", "is", "as", "at", "in", "on", "of", "or", "to",
        "be", "by", "do", "go", "he", "if", "me", "my", "no", "so", "up", "us", "we",
        "am", "are", "was", "his", "her", "him", "its", "our", "you", "your", "yes",
        "not", "for", "and", "but", "can", "had", "has", "how", "may", "now", "out",
        "own", "say", "she", "the", "too", "two", "use", "way", "who", "why", "yet",
        "ok", "hi", "oh", "ma", "co",
    )

    /** Vrai si [typedWord] (tel que tapé) est un mot-outil protégé pour cette langue. */
    /**
     * Abréviations suivies d'un point qui ne terminent pas la phrase : pas de majuscule
     * automatique après « etc. », « p. ex. », « M. », « cf. » (liste LibreOffice acor_fr
     * SentenceExceptList + usages courants FR/EN). Comparaison en minuscules.
     */
    private val ABBREVIATIONS: Set<String> = hashSetOf(
        // français
        "etc", "cf", "ex", "p", "pp", "fig", "chap", "vol", "env", "apr", "av", "ibid", "id", "op", "cit",
        "éd", "ed", "trad", "réf", "ref", "art", "al", "n°", "no", "nos", "tél", "tel", "bd", "boul", "av",
        "fbg", "sq", "dép", "dept", "min", "max", "num", "resp", "dipl", "vs", "svp", "stp", "approx",
        "m", "mm", "mme", "mmes", "mlle", "mlles", "dr", "drs", "pr", "me", "mgr", "st", "ste", "sts",
        "janv", "févr", "fév", "sept", "oct", "nov", "déc", "dec", "lun", "mar", "mer", "jeu", "ven", "sam", "dim",
        // anglais
        "mr", "mrs", "ms", "jr", "sr", "inc", "ltd", "co", "corp", "dept", "est", "vs", "approx", "misc",
        "e.g", "i.e", "jan", "feb", "aug", "sep", "dec", "mon", "tue", "wed", "thu", "fri", "sat", "sun",
        // lettres seules d'énumération (a. b. c.) sauf mots d'une lettre
        "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "n", "q", "r", "s", "t", "u", "v", "w", "x", "z",
    )

    private val ORDINAL = Regex("^(\\d*)(e|è|em|èm|ème|eme|er|re|ere|ère)$")

    /**
     * Ordinaux : « 3e » (norme), « 3ème » (usage), « 1er » / « 1re ». Le chiffre est soit dans le
     * mot composé (« 3e », rangée de chiffres), soit juste avant lui dans le texte (« 3|ème »,
     * chiffre tapé sur le clavier des symboles) : dans ce cas on ne propose que le suffixe,
     * qui remplace le mot composé. Vide hors français.
     */
    @JvmStatic
    fun ordinalSuggestions(typed: String?, digitsBefore: String?, locale: Locale?): List<String> {
        if (typed == null || locale?.language != "fr") return emptyList()
        val m = ORDINAL.find(typed) ?: return emptyList()
        val inWord = m.groupValues[1]
        val suffix = m.groupValues[2]
        val before = digitsBefore ?: ""
        if (inWord.isEmpty() && before.isEmpty()) return emptyList()
        val n = if (inWord.isNotEmpty()) inWord else before
        val prefix = inWord                                   // "" si le chiffre est déjà dans le texte
        val first = n == "1" || (n.endsWith("1") && !n.endsWith("11"))
        return if (suffix.startsWith("er") || suffix.startsWith("re") || suffix.startsWith("ère") || suffix.startsWith("ere")) {
            if (first) listOf("${prefix}er", "${prefix}re") else listOf("${prefix}e", "${prefix}ème")
        } else {
            listOf("${prefix}e", "${prefix}ème")
        }
    }

    /** Le jeton (lettres, points internes) juste avant le point est-il une abréviation ? */
    @JvmStatic
    fun isAbbreviation(token: CharSequence?): Boolean {
        if (token == null || token.isEmpty()) return false
        return token.toString().lowercase(Locale.ROOT) in ABBREVIATIONS
    }

    @JvmStatic
    fun isProtectedWord(typedWord: String?, locale: Locale?): Boolean {
        if (typedWord.isNullOrEmpty() || typedWord.length > 7) return false
        val w = typedWord.lowercase(Locale.ROOT).trimEnd('\'')
        return when (locale?.language) {
            "fr" -> w in PROTECTED_FR || w in PROTECTED_EN   // bilingue : on protège les deux
            "en" -> w in PROTECTED_EN || w in PROTECTED_FR
            else -> false
        }
    }

    /** Vrai si la typographie française (espace avant ; : ! ?) s'applique à cette locale. */
    @JvmStatic
    fun usesFrenchPunctuationSpacing(locale: Locale?): Boolean {
        if (locale?.language != "fr") return false
        return locale.country != "CA"   // au Québec : pas d'espace avant ; ! ?
    }

    /**
     * Caractère d'espace à insérer avant une ponctuation haute en français :
     * espace fine insécable avant « ; ! ? », espace insécable avant « : ».
     * Retourne l'espace ordinaire pour les autres cas.
     */
    @JvmStatic
    fun spaceBeforePunctuation(codePoint: Int, locale: Locale?): Int {
        if (!usesFrenchPunctuationSpacing(locale)) return ' '.code
        return when (codePoint) {
            ';'.code, '!'.code, '?'.code -> NARROW_NBSP.code
            ':'.code -> NBSP.code
            else -> ' '.code
        }
    }

    /** Apostrophe typographique ’ (U+2019) — iOS « ponctuation intelligente ». */
    const val APOSTROPHE = '\u2019'

    /**
     * Texte tel qu'il sera écrit dans le champ : apostrophes droites → typographiques
     * en français et en anglais. Le dictionnaire, lui, reste en apostrophes droites.
     */
    @JvmStatic
    fun typographic(word: CharSequence, locale: Locale?, sensitiveField: Boolean): CharSequence {
        if (sensitiveField) return word
        val lang = locale?.language
        if (lang != "fr" && lang != "en") return word
        if (word.indexOf('\'') < 0) return word
        return word.toString().replace('\'', APOSTROPHE)
    }

    /**
     * Guillemets français : « … » avec espaces insécables, décidés par l'équilibre des
     * guillemets déjà présents avant le curseur. Retourne null si pas de conversion.
     */
    @JvmStatic
    fun frenchQuote(textBefore: CharSequence?, locale: Locale?, sensitiveField: Boolean): String? {
        if (sensitiveField || locale?.language != "fr") return null
        val before = textBefore?.toString() ?: ""
        val opens = before.count { it == '«' }
        val closes = before.count { it == '»' }
        return if (opens > closes) "$NBSP»" else "«$NBSP"
    }

    /**
     * Seuil d'autocorrection selon la longueur du mot tapé. LatinIME applique un seuil fixe
     * « modeste » (0,185) : « excekkentissmie » → « excellentissime » (3 fautes sur 15 lettres)
     * reste sous le seuil. Un mot long est rarement ambigu : on descend à « agressif » (0,067)
     * dès 8 lettres et plus bas encore dès 12. Les mots courts gardent le seuil prudent, et
     * les mots-outils sont de toute façon protégés par [isProtectedWord].
     */
    @JvmStatic
    fun autocorrectThreshold(typedWord: String?, base: Float): Float {
        val n = typedWord?.length ?: 0
        return when {
            n >= 12 -> minOf(base, 0.03f)
            n >= 8 -> minOf(base, 0.067f)
            n >= 4 -> minOf(base, 0.09f)     // « awra » → « sera » (2 touches voisines sur 4 lettres)
            else -> base                     // 1-3 lettres : prudence maximale
        }
    }

    /** Vrai pour les espaces (y compris insécables) — Character.isWhitespace les exclut. */
    @JvmStatic
    fun isAnySpace(c: Char): Boolean = c == ' ' || c == NBSP || c == NARROW_NBSP || Character.isWhitespace(c)
}
