package org.futo.inputmethod.latin.uix.settings.pages.modelmanager

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.runBlocking
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.ScrollableList
import org.futo.inputmethod.latin.uix.settings.Tip
import org.futo.inputmethod.latin.xlm.ModelInfo
import org.futo.inputmethod.latin.xlm.ModelPaths
import org.futo.inputmethod.updates.openURI
import java.net.URLEncoder
import java.util.Locale

@Composable
fun ModelNavigationItem(navController: NavHostController, name: String, isPrimary: Boolean, path: String) {
    val style = if (isPrimary) {
        NavigationItemStyle.HomePrimary
    } else {
        NavigationItemStyle.MiscNoArrow
    }

    NavigationItem(
        title = name,
        style = style,
        navigate = {
            navController.navigate("model/${URLEncoder.encode(path, "utf-8")}")
        },
        icon = painterResource(id = R.drawable.cpu)
    )
}

@Preview(showBackground = true)
@Composable
fun ModelListScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    // Plume : la liste est recalculée à chaque retour sur l'écran (après un import, elle restait figée)
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val refresh = remember { androidx.compose.runtime.mutableIntStateOf(0) }
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) refresh.intValue++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val refreshKey = refresh.intValue
    val models = if(LocalInspectionMode.current) { PreviewModels } else {
        remember(refreshKey) {
            ModelPaths.getModels(context).mapNotNull {
                it.loadDetails()
            }
        }
    }

    val modelChoices = remember(refreshKey) { runBlocking { ModelPaths.getModelOptions(context) } }

    val modelsByLanguage: MutableMap<String, MutableList<ModelInfo>> = mutableMapOf()
    models.forEach { model ->
        modelsByLanguage.getOrPut(model.languages.joinToString(" ")) { mutableListOf() }.add(model)
    }

    ScrollableList {
        ScreenTitle(stringResource(R.string.prediction_settings_transformer_models), showBack = true, navController)

        Tip(stringResource(R.string.prediction_settings_transformer_english_notice))

        modelsByLanguage.forEach { item ->
            Spacer(modifier = Modifier.height(32.dp))
            ScreenTitle(Locale(item.key).displayLanguage)

            item.value.forEach { model ->
                val name = if (model.finetune_count > 0) {
                    model.name.trim() + " (local finetune)"
                } else {
                    model.name.trim()
                }

                ModelNavigationItem(
                    name = name,
                    isPrimary = model.path == modelChoices[item.key]?.path?.absolutePath,
                    path = model.path,
                    navController = navController
                )
            }
        }

        // Plume : modèles de dictée par langue. Intégré : Whisper multilingue 74 (anglais : modèle anglais 39).
        // Option : le 244, téléchargé par le navigateur puis importé ; « Revenir au modèle intégré » le retire.
        if (!LocalInspectionMode.current) {
            val voiceRows = remember(refreshKey) {
                org.futo.inputmethod.latin.uix.getActiveLanguages(context).map { lang ->
                    val locale = org.futo.inputmethod.latin.Subtypes.getLocale(lang.tag)
                    val file = org.futo.inputmethod.latin.uix.ResourceHelper.findFileForKind(
                        context, locale, org.futo.inputmethod.latin.uix.FileKind.VoiceInput)
                    Triple(lang, locale, file)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            ScreenTitle(stringResource(R.string.plume_voice_models_title))
            val builtinMulti = stringResource(R.string.plume_voice_model_builtin)
            val builtinEn = stringResource(R.string.plume_voice_model_builtin_en)
            val revertLabel = stringResource(R.string.plume_voice_revert_builtin)
            voiceRows.forEach { (lang, locale, file) ->
                NavigationItem(
                    title = lang.name,
                    subtitle = file?.let { "${it.name} · ${humanReadableByteCountSI(it.length())}" }
                        ?: (if (locale.language == "en") builtinEn else builtinMulti),
                    style = NavigationItemStyle.MiscNoArrow,
                    navigate = { }
                )
                if (file != null) {
                    NavigationItem(
                        title = revertLabel,
                        subtitle = lang.name,
                        style = NavigationItemStyle.Misc,
                        navigate = {
                            org.futo.inputmethod.latin.uix.ResourceHelper.deleteResourceForLanguage(
                                context, org.futo.inputmethod.latin.uix.FileKind.VoiceInput, locale)
                            refresh.intValue++
                        }
                    )
                }
            }
            org.futo.inputmethod.latin.uix.settings.Tip(stringResource(R.string.plume_voice_precise_tip))
            NavigationItem(
                title = stringResource(R.string.plume_voice_precise_download),
                style = NavigationItemStyle.ExternalLink,
                navigate = {
                    context.openURI("https://keyboard.futo.org/voice-input-multilingual-244.bin", true)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        ScreenTitle("Actions")
        NavigationItem(
            title = stringResource(R.string.plume_import_model_file),
            style = NavigationItemStyle.Misc,
            navigate = {
                openModelImporter(context)
            }
        )
    }
}