package org.futo.inputmethod.latin.plume

import android.content.Context
import android.util.Log
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Vocabulaire personnel (palier 4) : compte chaque mot validé par l'utilisateur, par
 * langue, dans un fichier texte lisible `plume_vocab.tsv` (mot ⇥ nombre ⇥ langue [⇥ appris])
 * dans le sandbox externe de l'app. Rien ne quitte l'appareil ; le fichier voyage avec
 * « Exporter la configuration » et se réinjecte dans le dictionnaire avec
 * `build_fr_dict.py --extra-words plume_vocab.tsv`.
 *
 * La 4e colonne « appris » marque les mots absents du dictionnaire que le clavier a fait
 * entrer dans son historique (à la deuxième validation). L'écran « Mots appris » des réglages
 * les liste ; « Oublier » les met dans une file que le clavier vide à l'ouverture suivante.
 *
 * Ignoré : champs mot de passe / e-mail / URL / code, mots de 1 lettre, mots avec chiffres.
 */
object PlumeVocab {
    private const val TAG = "PlumeVocab"
    private const val FILENAME = "plume_vocab.tsv"
    private const val FORGET_FILENAME = "plume_forget.txt"
    private const val FLUSH_SECONDS = 60L
    private const val MAX_WORDS = 50_000

    /** Validations nécessaires pour qu'un mot absent du dictionnaire soit appris (3 depuis le 2026-09-11). */
    const val LEARN_AT = 3

    data class Entry(val word: String, val count: Int, val lang: String)

    private val counts = ConcurrentHashMap<String, Int>()   // clé = "lang\tmot"
    private val learned = ConcurrentHashMap.newKeySet<String>()
    @Volatile private var file: File? = null
    @Volatile private var forgetFile: File? = null
    @Volatile private var dirty = false
    @Volatile private var loaded = false
    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "PlumeVocab").apply { isDaemon = true }
    }

    @JvmStatic
    fun init(context: Context) {
        if (file != null) return
        val dir = context.getExternalFilesDir(null) ?: return
        val f = File(dir, FILENAME)
        file = f
        forgetFile = File(dir, FORGET_FILENAME)
        executor.execute { load(f) }
        executor.scheduleWithFixedDelay({ flush() }, FLUSH_SECONDS, FLUSH_SECONDS, TimeUnit.SECONDS)
    }

    /** Chargement synchrone (écran des réglages). */
    @JvmStatic
    fun ensureLoaded(context: Context) {
        init(context)
        val f = file ?: return
        if (!loaded) load(f)
    }

    private fun key(word: String, locale: Locale?) = "${locale?.language ?: "und"}\t$word"

    private fun normalize(word: CharSequence) = word.toString().replace('’', '\'').trim()

    /** Même règle que l'historique AOSP : un mot capitalisé par l'auto-majuscule est compté
     *  en minuscules (« Vrabenko » en début de phrase = « vrabenko »). */
    @JvmStatic
    fun canonical(word: CharSequence?, wasAutoCapitalized: Boolean, locale: Locale?): String? {
        if (word == null) return null
        val w = normalize(word)
        if (!wasAutoCapitalized || w.length < 2) return w
        if (w[0].isUpperCase() && w.substring(1).none { it.isUpperCase() }) {
            return w.substring(0, 1).lowercase(locale ?: Locale.ROOT) + w.substring(1)
        }
        return w
    }

    @JvmStatic
    fun record(word: CharSequence?, locale: Locale?, sensitiveField: Boolean) {
        if (sensitiveField || word == null || file == null) return
        val w = normalize(word)
        if (w.length < 2 || w.length > 40) return
        if (w.any { it.isDigit() } || !w.any { it.isLetter() }) return
        counts.merge(key(w, locale), 1, Int::plus)
        dirty = true
    }

    /** Retour arrière sur le mot ou annulation : la dernière validation ne compte plus. */
    @JvmStatic
    fun uncount(word: CharSequence?, locale: Locale?) {
        if (word == null) return
        val k = key(normalize(word), locale)
        val n = counts[k] ?: return
        if (n <= 1) { counts.remove(k); learned.remove(k) } else counts[k] = n - 1
        dirty = true
    }

    /** Nombre de validations déjà comptées pour ce mot (0 si inconnu). */
    @JvmStatic
    fun count(word: CharSequence?, locale: Locale?): Int {
        if (word == null) return 0
        return counts[key(normalize(word), locale)] ?: 0
    }

    /** Le clavier vient de faire entrer ce mot (absent du dictionnaire) dans son historique. */
    @JvmStatic
    fun markLearned(word: CharSequence?, locale: Locale?) {
        if (word == null) return
        val k = key(normalize(word), locale)
        if (counts.containsKey(k) && learned.add(k)) dirty = true
    }

    @JvmStatic
    fun unmarkLearned(word: CharSequence?, locale: Locale?) {
        if (word == null) return
        if (learned.remove(key(normalize(word), locale))) dirty = true
    }

    /** Mots appris, les plus validés d'abord. */
    @JvmStatic
    fun learnedEntries(): List<Entry> = learned.mapNotNull { k ->
        val (lang, w) = k.split('\t', limit = 2)
        counts[k]?.let { Entry(w, it, lang) }
    }.sortedWith(compareByDescending<Entry> { it.count }.thenBy { it.word })

    /** Depuis les réglages : oublier ce mot. Le clavier videra la file à sa prochaine ouverture. */
    @JvmStatic
    fun requestForget(entry: Entry) {
        val k = "${entry.lang}\t${entry.word}"
        learned.remove(k)
        counts.remove(k)
        dirty = true
        try {
            forgetFile?.appendText("${entry.lang}\t${entry.word}\n")
        } catch (e: Exception) {
            Log.w(TAG, "forget queue", e)
        }
        flush()
    }

    /** Côté clavier : mots à retirer de l'historique (lang, mot) ; vide la file. */
    @JvmStatic
    fun drainForgetQueue(): List<Pair<String, String>> {
        val f = forgetFile ?: return emptyList()
        if (!f.exists()) return emptyList()
        return try {
            val items = f.readLines().mapNotNull { l ->
                val p = l.split('\t', limit = 2)
                if (p.size == 2 && p[1].isNotBlank()) p[0] to p[1] else null
            }
            f.delete()
            items
        } catch (e: Exception) {
            Log.w(TAG, "forget drain", e); emptyList()
        }
    }

    @JvmStatic
    fun flush() {
        val f = file ?: return
        if (!dirty || !loaded) return        // jamais écraser le fichier avant de l'avoir lu
        dirty = false
        try {
            val snapshot = counts.entries.sortedByDescending { it.value }.take(MAX_WORDS)
            val tmp = File(f.path + ".tmp")
            tmp.bufferedWriter().use { out ->
                for ((key, n) in snapshot) {
                    val (lang, w) = key.split('\t', limit = 2)
                    out.write(if (key in learned) "$w\t$n\t$lang\t1\n" else "$w\t$n\t$lang\n")
                }
            }
            if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f) }
        } catch (e: Exception) {
            Log.w(TAG, "flush failed", e)
        }
    }

    @Synchronized
    private fun load(f: File) {
        if (loaded) return
        loaded = true
        if (!f.exists()) return
        try {
            f.forEachLine { line ->
                val parts = line.split('\t')
                if (parts.size >= 3) {
                    val n = parts[1].toIntOrNull() ?: return@forEachLine
                    val k = "${parts[2]}\t${parts[0]}"
                    counts.merge(k, n, ::maxOf)          // frappes arrivées avant la lecture
                    if (parts.size >= 4 && parts[3] == "1") learned.add(k)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "load failed", e)
        }
    }
}
