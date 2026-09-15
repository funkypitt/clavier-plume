package org.futo.inputmethod.latin.plume

import android.content.Context
import android.net.Uri
import org.futo.inputmethod.latin.DictionaryFactory
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.uix.PersonalWord
import org.futo.inputmethod.latin.uix.UserDictionaryIO
import java.io.ByteArrayInputStream
import java.util.Locale
import java.util.zip.ZipInputStream

/**
 * Import d'une liste de mots dans le dictionnaire personnel (2.1.0, décision de l'utilisateur du
 * 2026-09-15). Un mot déclaré par l'utilisateur est un mot voulu : il va dans le dictionnaire personnel
 * (permanent, fortement pondéré), pas dans « Mots appris » (qui oublie).
 *
 * Formats reconnus au contenu, pas à l'extension : texte brut (un mot par ligne), CSV / TSV
 * (`mot[;,⇥fréquence][;,⇥raccourci]`, dont l'export `plume_vocab.tsv`), export Gboard (zip contenant
 * `dictionary.txt`, `mot⇥raccourci⇥langue`), Hunspell `.dic` (première ligne = nombre, `mot/DRAPEAUX`),
 * LibreOffice `.dic` (en-tête `WBSWG…`, mots après `---`). Les mots déjà dans les dictionnaires livrés sont
 * ignorés (sinon leur poids doublerait) ; un fichier de plus de [MAX_WORDS] mots ressemble à un
 * dictionnaire de langue, pas à une liste personnelle : refusé.
 */
object PlumeWordListImport {
    const val MAX_WORDS = 20_000
    private const val FREQUENCY = 250          // dictionnaire personnel Android : 1..255

    data class Report(val added: Int, val known: Int, val rejected: Int, val total: Int, val refused: Boolean)

    fun parse(bytes: ByteArray): List<String> {
        if (bytes.size >= 4 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()) return parseGboardZip(bytes)
        val text = String(bytes, Charsets.UTF_8)
        val lines = text.split('\n').map { it.trimEnd('\r') }
        val first = lines.firstOrNull { it.isNotBlank() }?.trim() ?: return emptyList()
        return when {
            first.all { it.isDigit() } -> lines.drop(1).mapNotNull { hunspell(it) }            // Hunspell .dic
            first.startsWith("WBSWG") || first.startsWith("OOoUserDict") -> {              // LibreOffice .dic
                val body = lines.dropWhile { it.trim() != "---" }.drop(1)
                (if (body.isEmpty()) lines.drop(1) else body).mapNotNull { plain(it) }
            }
            else -> lines.mapNotNull { csv(it) }
        }
    }

    private fun parseGboardZip(bytes: ByteArray): List<String> {
        val out = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                if (entry.name.endsWith(".txt")) {
                    String(zip.readBytes(), Charsets.UTF_8).split('\n').forEach { line ->
                        if (!line.startsWith("#")) out += listOfNotNull(csv(line))
                    }
                }
                entry = zip.nextEntry
            }
        }
        return out
    }

    private fun hunspell(line: String): String? = plain(line.substringBefore('/'))

    private fun csv(line: String): String? {
        val l = line.trim()
        if (l.isEmpty() || l.startsWith("#")) return null
        val field = l.split('\t', ';', ',').first().trim().trim('"')
        return plain(field)
    }

    /** Un mot : 1 à 48 caractères, sans espace ni caractère de contrôle. */
    private fun plain(s: String): String? {
        val w = s.trim().replace('’', '\'')
        if (w.isEmpty() || w.length > 48 || w.any { it.isWhitespace() || it.isISOControl() }) return null
        return w
    }

    /** Importe [uri] pour [locale] (null = toutes les langues installées). Bloquant : à appeler hors du fil principal. */
    fun import(context: Context, uri: Uri, locale: Locale?): Report {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return Report(0, 0, 0, 0, true)
        val words = parse(bytes).distinct()
        if (words.size > MAX_WORDS) return Report(0, 0, 0, words.size, refused = true)
        val locales = if (locale != null) listOf(locale) else Subtypes.installedLanguageKeys(context).map { Subtypes.getLocale(it) }
        val shipped = locales.map { DictionaryFactory.createMainDictionaryFromManager(context.applicationContext, it) }
        val io = UserDictionaryIO(context)
        val existing = io.get().map { it.word }.toHashSet()
        var added = 0; var known = 0
        val toAdd = mutableListOf<PersonalWord>()
        for (w in words) {
            if (w in existing || shipped.any { it.isValidWord(w) }) { known++; continue }
            toAdd += PersonalWord(w, FREQUENCY, locale?.toString(), 0, null); added++
        }
        io.put(toAdd)
        shipped.forEach { it.close() }
        // rejetés = lignes non vides et non commentées qui n'ont pas donné de mot (espace, trop long, en-tête)
        val candidateLines = String(bytes, Charsets.UTF_8).split('\n').count { val t = it.trim(); t.isNotEmpty() && !t.startsWith("#") }
        val rejected = if (bytes.size >= 2 && bytes[0] == 'P'.code.toByte() && bytes[1] == 'K'.code.toByte()) 0 else maxOf(0, candidateLines - words.size)
        return Report(added, known, rejected, words.size, refused = false)
    }

    /** « Garder pour de bon » : un mot appris passe au dictionnaire personnel. */
    fun keepForGood(context: Context, word: String, lang: String) {
        UserDictionaryIO(context).put(listOf(PersonalWord(word, FREQUENCY, lang, 0, null)))
    }
}
