package org.futo.inputmethod.latin.uix.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.futo.inputmethod.latin.ActiveSubtype
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.uix.KeyboardLayoutPreview
import org.futo.inputmethod.latin.uix.SettingsKey
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.setSetting
import org.futo.inputmethod.latin.uix.setSettingBlocking
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.latin.uix.theme.selector.ThemePicker
import org.futo.inputmethod.v2keyboard.LayoutManager
import java.util.Locale

/**
 * Plume : prise en main en sept écrans, chacun passable. Remplace les deux écrans système de
 * FUTO. Se relance depuis À propos → « Revoir la prise en main ».
 */
val PlumeSetupDone = SettingsKey(booleanPreferencesKey("plume_setup_done"), false)

private const val STEPS = 7

@Composable
fun PlumeSetupWizard(inputMethodEnabled: Boolean, inputMethodSelected: Boolean, initialStep: Int = 0, onFinished: () -> Unit) {
    val context = LocalContext.current
    // langue : celle de l'app (réglage par application d'Android compris) ; région : celle du système
    val appLocale = remember { context.resources.configuration.locales[0] ?: Locale.getDefault() }
    val sys = remember {
        val system = android.content.res.Resources.getSystem().configuration.locales[0] ?: Locale.getDefault()
        if (system.country.isNotEmpty()) system else appLocale
    }
    var step by rememberSaveable { mutableStateOf(initialStep) }
    var primary by rememberSaveable { mutableStateOf(if (appLocale.language == "en") "en" else "fr") }
    // Défauts d'après la région du téléphone : Suisse → fr_CH + QWERTZ ; Québec → fr_CA + QWERTY
    // (clavier canadien) ; France, Belgique, Luxembourg, Monaco → AZERTY ; Royaume-Uni / Irlande → en_GB.
    var frVariant by rememberSaveable {
        mutableStateOf(when (sys.country) { "CH", "LI" -> "fr_CH"; "CA" -> "fr_CA"; else -> "fr" })
    }
    var frLayout by rememberSaveable {
        mutableStateOf(when (sys.country) { "CH", "LI" -> "qwertz"; "CA", "US" -> "qwerty"; else -> "azerty" })
    }
    var enLayout by rememberSaveable { mutableStateOf(if (sys.country in setOf("CH", "LI", "DE", "AT")) "qwertz" else "qwerty") }
    var layoutTouched by rememberSaveable { mutableStateOf(false) }
    var enVariant by rememberSaveable { mutableStateOf(if (sys.country in setOf("GB", "IE", "AU", "NZ")) "en_GB" else "en_US") }

    fun applyLanguages() {
        val fr = Subtypes.subtypeToString(Subtypes.makeSubtype(frVariant, frLayout))
        val en = Subtypes.subtypeToString(Subtypes.makeSubtype(enVariant, enLayout))
        context.setSettingBlocking(SubtypesSetting.key, setOf(fr, en))
        context.setSettingBlocking(ActiveSubtype.key, if (primary == "fr") fr else en)
        // Plume : les deux dictionnaires consultés ensemble par défaut (réglage unique dans Langues)
        Subtypes.setBothLanguagesMode(context, true)
        context.setSettingBlocking(org.futo.inputmethod.latin.PlumeBilingualDefaultApplied.key, true)
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.plume_setup_step, step + 1, STEPS), style = Typography.SmallMl,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = (step + 1f) / STEPS, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            when (step) {
                0 -> StepWelcome()
                1 -> StepLanguage(primary, frVariant, enVariant, { primary = it },
                    { v -> frVariant = v; if (!layoutTouched) frLayout = when (v) { "fr_CH" -> "qwertz"; "fr_CA" -> "qwerty"; else -> "azerty" } },
                    { enVariant = it })
                2 -> StepLayouts(primary, frVariant, enVariant, frLayout, enLayout, { frLayout = it; layoutTouched = true }, { enLayout = it })
                3 -> StepActivate(inputMethodEnabled, inputMethodSelected)
                4 -> StepTheme()
                5 -> StepBasics()
                else -> StepTry(primary)
            }
            Spacer(Modifier.height(24.dp))
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (step > 0) TextButton(onClick = { step -= 1 }) { Text(stringResource(R.string.plume_setup_back)) } else Spacer(Modifier.width(1.dp))
            Row {
                if (step in 1..5 && !(step == 3 && !(inputMethodEnabled && inputMethodSelected))) {
                    // Passer ne change rien aux réglages existants (Suivant, lui, applique les dispositions)
                    TextButton(onClick = { step += 1 }) { Text(stringResource(R.string.plume_setup_skip)) }
                }
                val canNext = step != 3 || (inputMethodEnabled && inputMethodSelected)
                Button(enabled = canNext, onClick = {
                    if (step == 2) applyLanguages()
                    if (step == STEPS - 1) {
                        context.setSettingBlocking(PlumeSetupDone.key, true)
                        onFinished()
                    } else step += 1
                }) {
                    Text(stringResource(if (step == STEPS - 1) R.string.plume_setup_finish else R.string.plume_setup_next))
                }
            }
        }
    }
}

@Composable
private fun Title(text: String) {
    Text(text, style = Typography.Heading.Medium, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp))
}

@Composable
private fun Body(text: String) {
    Text(text, style = Typography.Body.RegularMl, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
private fun ChoiceCard(title: String, subtitle: String?, selected: Boolean, onClick: () -> Unit,
                       content: (@Composable () -> Unit)? = null) {
    // choisi : bordure épaisse couleur d'accent, fond teinté et étiquette « Choisi ✓ » ; les autres
    // options restent en filet fin (l'utilisateur ne distinguait pas assez le choix courant)
    val border = if (selected) BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
                 else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    Surface(
        shape = RoundedCornerShape(12.dp), border = border,
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(12.dp)).clickable { onClick() }
    ) {
        Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = if (selected) 6.dp else 14.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally) {
            if (selected) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary) {
                        Text(stringResource(R.string.plume_setup_chosen), Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            style = Typography.SmallMl)
                    }
                }
            }
            content?.invoke()
            Text(title, style = Typography.Body.MediumMl, textAlign = TextAlign.Center)
            if (subtitle != null) Text(subtitle, style = Typography.SmallMl, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(end = 8.dp).clip(RoundedCornerShape(20.dp)).clickable { onClick() }
    ) { Text(text, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), style = Typography.SmallMl) }
}

// ---------------------------------------------------------------------------------------------

@Composable
private fun StepWelcome() {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Icon(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.width(56.dp).height(56.dp),
            tint = MaterialTheme.colorScheme.onBackground)
        Text(stringResource(R.string.plume_setup_welcome_title), style = Typography.Heading.Medium)
    }
    Body(stringResource(R.string.plume_setup_welcome_body))
    Text(stringResource(R.string.plume_setup_welcome_differs_title), style = Typography.Body.MediumMl, modifier = Modifier.padding(bottom = 6.dp))
    Body(stringResource(R.string.plume_setup_welcome_differs))
    Body(stringResource(R.string.plume_setup_welcome_privacy))
}

@Composable
private fun StepLanguage(primary: String, frVariant: String, enVariant: String,
                         setPrimary: (String) -> Unit, setFr: (String) -> Unit, setEn: (String) -> Unit) {
    Title(stringResource(R.string.plume_setup_language_title))
    Body(stringResource(R.string.plume_setup_language_body))
    ChoiceCard("Français", stringResource(R.string.plume_setup_language_fr_subtitle), primary == "fr", { setPrimary("fr") })
    ChoiceCard("English", stringResource(R.string.plume_setup_language_en_subtitle), primary == "en", { setPrimary("en") })
    Spacer(Modifier.height(12.dp))
    Text(stringResource(R.string.plume_setup_language_variant_fr), style = Typography.SmallMl, modifier = Modifier.padding(bottom = 6.dp))
    Row {
        Chip("Suisse", frVariant == "fr_CH") { setFr("fr_CH") }
        Chip("France", frVariant == "fr") { setFr("fr") }
        Chip("Canada", frVariant == "fr_CA") { setFr("fr_CA") }
    }
    Spacer(Modifier.height(10.dp))
    Text(stringResource(R.string.plume_setup_language_variant_en), style = Typography.SmallMl, modifier = Modifier.padding(bottom = 6.dp))
    Row {
        Chip("US", enVariant == "en_US") { setEn("en_US") }
        Chip("UK", enVariant == "en_GB") { setEn("en_GB") }
    }
}

@Composable
private fun LayoutChoice(id: String, locale: Locale, selected: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current
    val name = remember(id) { LayoutManager.getLayoutOrNull(context, id)?.name ?: id }
    ChoiceCard(name, null, selected, onClick) {
        KeyboardLayoutPreview(id = id, width = 220.dp, locale = locale)
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun StepLayouts(primary: String, frVariant: String, enVariant: String, frLayout: String, enLayout: String,
                        setFr: (String) -> Unit, setEn: (String) -> Unit) {
    Title(stringResource(R.string.plume_setup_layout_title))
    Body(stringResource(R.string.plume_setup_layout_body))
    val frLocale = Subtypes.getLocale(frVariant)
    val enLocale = Subtypes.getLocale(enVariant)
    val first = if (primary == "fr") "fr" else "en"
    for (lang in listOf(first, if (first == "fr") "en" else "fr")) {
        if (lang == "fr") {
            Text(stringResource(R.string.plume_setup_layout_for_fr), style = Typography.Body.MediumMl, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            // le choix courant en tête, pour qu'il soit visible sans défiler
            val frIds = listOf("qwertz", "swiss", "azerty", "qwerty")
            for (id in listOf(frLayout) + frIds.filter { it != frLayout }) LayoutChoice(id, frLocale, frLayout == id) { setFr(id) }
        } else {
            Text(stringResource(R.string.plume_setup_layout_for_en), style = Typography.Body.MediumMl, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
            val enIds = listOf("qwerty", "qwertz")
            for (id in listOf(enLayout) + enIds.filter { it != enLayout }) LayoutChoice(id, enLocale, enLayout == id) { setEn(id) }
        }
    }
}

@Composable
private fun StepActivate(inputMethodEnabled: Boolean, inputMethodSelected: Boolean) {
    val context = LocalContext.current
    Title(stringResource(R.string.plume_setup_activate_title))
    Body(stringResource(R.string.plume_setup_activate_body))
    ChoiceCard(
        stringResource(if (inputMethodEnabled) R.string.plume_setup_activate_enabled_done else R.string.plume_setup_activate_enable),
        stringResource(R.string.plume_setup_activate_enable_subtitle), inputMethodEnabled,
        onClick = {
            if (!inputMethodEnabled) {
                val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                context.startActivity(intent)
            }
        })
    ChoiceCard(
        stringResource(if (inputMethodSelected) R.string.plume_setup_activate_selected_done else R.string.plume_setup_activate_select),
        stringResource(R.string.plume_setup_activate_select_subtitle), inputMethodSelected,
        onClick = {
            if (inputMethodEnabled && !inputMethodSelected) {
                (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()
                (context as? SettingsActivity)?.updateSystemState()
            }
        })
    if (!(inputMethodEnabled && inputMethodSelected)) {
        Body(stringResource(R.string.plume_setup_activate_wait))
    }
}

@Composable
private fun StepTheme() {
    val context = LocalContext.current
    Title(stringResource(R.string.plume_setup_theme_title))
    Body(stringResource(R.string.plume_setup_theme_body))
    Box(Modifier.fillMaxWidth().height(570.dp)) {
        ThemePicker(onDeleteCustomTheme = {}, onCustomTheme = {})
    }
    val vibrate = useSharedPrefsBool(org.futo.inputmethod.latin.settings.Settings.PREF_VIBRATE_ON, true)
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.plume_setup_vibrate), style = Typography.Body.MediumMl)
            Text(stringResource(R.string.plume_setup_vibrate_subtitle), style = Typography.SmallMl, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = vibrate.value, onCheckedChange = { vibrate.setValue(it) })
    }
}

@Composable
private fun BasicsCard(icon: Int?, label: String?, title: String, body: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(Modifier.width(44.dp).height(44.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center) {
                if (icon != null) Icon(painterResource(icon), null, Modifier.width(20.dp).height(20.dp))
                else Text(label ?: "", style = Typography.Body.MediumMl)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, style = Typography.Body.MediumMl)
                Text(body, style = Typography.SmallMl, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StepBasics() {
    val context = LocalContext.current
    Title(stringResource(R.string.plume_setup_basics_title))
    BasicsCard(null, "EN", stringResource(R.string.plume_setup_basics_lang_title), stringResource(R.string.plume_setup_basics_lang_body))
    BasicsCard(null, "␣", stringResource(R.string.plume_setup_basics_space_title), stringResource(R.string.plume_setup_basics_space_body))
    BasicsCard(null, "“ ”", stringResource(R.string.plume_setup_basics_bar_title), stringResource(R.string.plume_setup_basics_bar_body))
    BasicsCard(null, "↶", stringResource(R.string.plume_setup_basics_learn_title), stringResource(R.string.plume_setup_basics_learn_body))
    BasicsCard(R.drawable.mic_fill, null, stringResource(R.string.plume_setup_basics_voice_title), stringResource(R.string.plume_setup_basics_voice_body))

    // Contacts : facultatif, hors ligne
    val useContacts = useSharedPrefsBool(org.futo.inputmethod.latin.settings.Settings.PREF_KEY_USE_CONTACTS_DICT, false)
    var granted by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        granted = ok
        useContacts.setValue(ok)
    }
    Spacer(Modifier.height(8.dp))
    Text(stringResource(R.string.plume_setup_contacts_title), style = Typography.Body.MediumMl)
    Body(stringResource(R.string.plume_setup_contacts_body))
    if (granted && useContacts.value) {
        Text(stringResource(R.string.plume_setup_contacts_done), style = Typography.SmallMl, color = MaterialTheme.colorScheme.primary)
    } else {
        OutlinedButton(onClick = { launcher.launch(Manifest.permission.READ_CONTACTS) }) {
            Text(stringResource(R.string.plume_setup_contacts_allow))
        }
    }
}

@Composable
private fun StepTry(primary: String) {
    var text by rememberSaveable { mutableStateOf("") }
    Title(stringResource(R.string.plume_setup_try_title))
    Body(stringResource(if (primary == "fr") R.string.plume_setup_try_body_fr else R.string.plume_setup_try_body_en))
    OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth().height(140.dp),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Sentences),
        placeholder = { Text(stringResource(R.string.plume_setup_try_placeholder)) })
    Spacer(Modifier.height(12.dp))
    Body(stringResource(R.string.plume_setup_try_after))
}

/** À propos → « Revoir la prise en main » */
fun plumeRestartSetup(context: Context) {
    context.setSettingBlocking(PlumeSetupDone.key, false)
}
