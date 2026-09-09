package org.futo.inputmethod.latin.uix.settings.pages

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.plume.PlumeVocab
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.ScrollableList
import org.futo.inputmethod.latin.uix.settings.SettingItem
import org.futo.inputmethod.latin.uix.settings.Tip

/**
 * Plume : « Mots appris » — les mots absents du dictionnaire que le clavier a retenus
 * (deuxième validation), lus dans plume_vocab.tsv. « Oublier » les retire de l'historique
 * (file vidée par le clavier à sa prochaine ouverture) sans les mettre en liste noire.
 */
@Composable
fun LearnedWordsScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    var entries by remember { mutableStateOf<List<PlumeVocab.Entry>?>(null) }
    LaunchedEffect(Unit) {
        entries = withContext(Dispatchers.IO) {
            PlumeVocab.ensureLoaded(context.applicationContext)
            PlumeVocab.learnedEntries()
        }
    }
    ScrollableList {
        ScreenTitle(stringResource(R.string.plume_learned_words), showBack = true, navController)
        Tip(stringResource(R.string.plume_learned_words_tip))
        val list = entries ?: return@ScrollableList
        if (list.isEmpty()) {
            Tip(stringResource(R.string.plume_learned_words_none))
        }
        list.forEach { entry ->
            SettingItem(
                title = entry.word,
                subtitle = pluralStringResource(R.plurals.plume_learned_words_count, entry.count, entry.count)
                        + " · " + entry.lang,
            ) {
                IconButton(onClick = {
                    PlumeVocab.requestForget(entry)
                    entries = list.filter { it != entry }
                }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.plume_learned_words_forget))
                }
            }
        }
    }
}
