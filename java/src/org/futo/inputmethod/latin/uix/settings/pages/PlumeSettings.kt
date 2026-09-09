package org.futo.inputmethod.latin.uix.settings.pages

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.BuildConfig
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.settings.Settings
import org.futo.inputmethod.latin.settings.Settings.PREF_VIBRATION_DURATION_SETTINGS
import org.futo.inputmethod.latin.uix.KeyHintsSetting
import org.futo.inputmethod.latin.uix.LocalNavController
import org.futo.inputmethod.latin.uix.SHOW_EMOJI_SUGGESTIONS
import org.futo.inputmethod.latin.uix.SettingsExporter
import org.futo.inputmethod.latin.uix.TextEditPopupActivity
import org.futo.inputmethod.latin.uix.actions.ActionCategory
import org.futo.inputmethod.latin.uix.actions.ActionsSettings
import org.futo.inputmethod.latin.uix.actions.EmojiAction
import org.futo.inputmethod.latin.uix.actions.SwitchLanguageAction
import org.futo.inputmethod.latin.uix.actions.VoiceInputAction
import org.futo.inputmethod.latin.uix.actions.clipboard.ClipboardHistoryEnabled
import org.futo.inputmethod.latin.uix.actions.clipboard.ClipboardQuickClipsEnabled
import org.futo.inputmethod.latin.uix.actions.ensureWellFormed
import org.futo.inputmethod.latin.uix.actions.toActionEditorItems
import org.futo.inputmethod.latin.uix.actions.toActionMap
import org.futo.inputmethod.latin.uix.actions.updateSettingsWithNewActions
import org.futo.inputmethod.latin.uix.getSetting
import org.futo.inputmethod.latin.uix.settings.BottomSpacer
import org.futo.inputmethod.latin.uix.settings.DropDownPickerSettingItem
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.PlumeParagraph
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.uix.settings.SettingSlider
import org.futo.inputmethod.latin.uix.settings.SettingSliderSharedPrefsInt
import org.futo.inputmethod.latin.uix.settings.SettingToggleRaw
import org.futo.inputmethod.latin.uix.settings.SyncDataStoreToPreferencesInt
import org.futo.inputmethod.latin.uix.settings.UserSetting
import org.futo.inputmethod.latin.uix.settings.UserSettingsMenu
import org.futo.inputmethod.latin.uix.settings.render
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsBool
import org.futo.inputmethod.latin.uix.settings.useSharedPrefsInt
import org.futo.inputmethod.latin.uix.settings.userSettingDecorationOnly
import org.futo.inputmethod.latin.uix.settings.userSettingNavigationItem
import org.futo.inputmethod.latin.uix.settings.userSettingToggleDataStore
import org.futo.inputmethod.latin.uix.settings.userSettingToggleSharedPrefs
import org.futo.inputmethod.latin.uix.theme.Typography
import kotlin.math.roundToInt

/*
 * Plume: minimal settings structure (see plume/docs/05-reglages-minimalistes.md, §18).
 * Seven top-level screens, two levels at most. The FUTO menus stay in the code base but
 * are no longer reachable from here.
 */

private fun section(@androidx.annotation.StringRes title: Int) = userSettingDecorationOnly {
    SectionHeader(stringResource(title))
}

// ---------------------------------------------------------------------------------------------
// Home
// ---------------------------------------------------------------------------------------------

val PlumeHomeMenu = UserSettingsMenu(
    title = R.string.plume_settings_title,
    navPath = "home", registerNavPath = false,
    settings = listOf(
        userSettingNavigationItem(
            title = R.string.plume_home_languages,
            style = NavigationItemStyle.Misc,
            navigateTo = "languages",
            icon = R.drawable.globe
        ),
        userSettingNavigationItem(
            title = R.string.plume_home_keyboard,
            style = NavigationItemStyle.Misc,
            navigateTo = "keyboard",
            icon = R.drawable.keyboard
        ),
        userSettingNavigationItem(
            title = R.string.plume_home_typing,
            style = NavigationItemStyle.Misc,
            navigateTo = "typing",
            icon = R.drawable.icon_spellcheck
        ),
        userSettingNavigationItem(
            title = R.string.plume_home_actions,
            style = NavigationItemStyle.Misc,
            navigateTo = "actions",
            icon = R.drawable.more_horizontal
        ),
        userSettingNavigationItem(
            title = R.string.plume_home_appearance,
            style = NavigationItemStyle.Misc,
            navigateTo = "themes",
            icon = R.drawable.themes
        ),
        userSettingNavigationItem(
            title = R.string.plume_home_backup,
            style = NavigationItemStyle.Misc,
            navigateTo = "backup",
            icon = R.drawable.copy
        ),
        userSettingNavigationItem(
            title = R.string.plume_home_about,
            style = NavigationItemStyle.Misc,
            navigateTo = "about",
            icon = R.drawable.help_circle
        ),
    )
)

@Preview(showBackground = true)
@Composable
fun PlumeHomeScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column {
        Column(
            modifier = Modifier
                .weight(1.0f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.plume_settings_title),
                style = Typography.Heading.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            PlumeHomeMenu.render(showTitle = false)

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "v${BuildConfig.VERSION_NAME}",
                style = Typography.Small,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
        TextButton(onClick = {
            val intent = Intent()
            intent.setClass(context, TextEditPopupActivity::class.java)
            intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            context.startActivity(intent)
        }, modifier = Modifier.fillMaxWidth()) {
            Text(
                stringResource(R.string.settings_try_typing_here),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Clavier
// ---------------------------------------------------------------------------------------------

val PlumeKeyboardMenu = UserSettingsMenu(
    title = R.string.plume_home_keyboard,
    navPath = "keyboard", registerNavPath = true,
    settings = listOf(
        section(R.string.plume_section_size),
        userSettingNavigationItem(
            title = R.string.plume_keyboard_size_title,
            subtitle = R.string.plume_keyboard_size_subtitle,
            style = NavigationItemStyle.Misc,
            navigateTo = "resize",
        ),

        section(R.string.plume_section_keys),
        userSettingToggleSharedPrefs(
            title = R.string.plume_number_row,
            subtitle = R.string.plume_number_row_subtitle,
            key = Settings.PREF_ENABLE_NUMBER_ROW,
            default = { false },
        ),
        userSettingToggleSharedPrefs(
            title = R.string.plume_key_preview,
            subtitle = R.string.plume_key_preview_subtitle,
            key = Settings.PREF_POPUP_ON,
            default = { booleanResource(R.bool.config_default_key_preview_popup) }
        ),
        userSettingToggleDataStore(
            title = R.string.plume_key_hints,
            subtitle = R.string.plume_key_hints_subtitle,
            setting = KeyHintsSetting
        ),
        UserSetting(name = R.string.plume_long_press_duration) {
            val resources = LocalResources.current
            SettingSliderSharedPrefsInt(
                title = stringResource(R.string.plume_long_press_duration),
                key = Settings.PREF_KEY_LONGPRESS_TIMEOUT,
                default = 300,
                range = 100.0f..700.0f,
                hardRange = 25.0f..1200.0f,
                transform = { it.roundToInt() },
                indicator = { resources.getString(R.string.abbreviation_unit_milliseconds, "$it") },
                steps = 23
            )
        },

        section(R.string.plume_section_feedback),
        userSettingToggleSharedPrefs(
            title = R.string.plume_vibrate,
            key = Settings.PREF_VIBRATE_ON,
            default = { booleanResource(R.bool.config_default_vibration_enabled) }
        ),
        UserSetting(
            name = R.string.plume_vibration_strength,
            visibilityCheck = {
                useSharedPrefsBool(
                    Settings.PREF_VIBRATE_ON,
                    booleanResource(R.bool.config_default_vibration_enabled)
                ).value
            }
        ) {
            val resources = LocalResources.current
            SyncDataStoreToPreferencesInt(vibrationDurationSetting, PREF_VIBRATION_DURATION_SETTINGS)
            SettingSlider(
                title = stringResource(R.string.plume_vibration_strength),
                setting = vibrationDurationSetting,
                range = -1.0f..100.0f,
                hardRange = -1.0f..2000.0f,
                transform = { it.roundToInt() },
                indicator = {
                    if (it == -1) {
                        resources.getString(R.string.plume_vibration_default)
                    } else {
                        resources.getString(R.string.abbreviation_unit_milliseconds, "$it")
                    }
                }
            )
        },
        userSettingToggleSharedPrefs(
            title = R.string.plume_key_sound,
            key = Settings.PREF_SOUND_ON,
            default = { booleanResource(R.bool.config_default_sound_enabled) }
        ),

        section(R.string.plume_section_spacebar),
        UserSetting(name = R.string.plume_spacebar_swipe) {
            val setting = useSharedPrefsInt(
                key = Settings.PREF_SPACEBAR_SWIPE_MODE,
                default = Settings.SPACEBAR_MODE_LANGUAGE
            )
            val modes = mapOf(
                Settings.SPACEBAR_MODE_OFF to stringResource(R.string.plume_spacebar_swipe_off),
                Settings.SPACEBAR_MODE_CURSOR to stringResource(R.string.plume_spacebar_swipe_cursor),
                Settings.SPACEBAR_MODE_LANGUAGE to stringResource(R.string.plume_spacebar_swipe_language),
            )
            DropDownPickerSettingItem(
                label = stringResource(R.string.plume_spacebar_swipe),
                options = modes.keys.toList(),
                selection = setting.value,
                onSet = { setting.setValue(it) },
                getDisplayName = { modes[it] ?: "?" },
            )
        },
    )
)

// ---------------------------------------------------------------------------------------------
// Saisie et correction
// ---------------------------------------------------------------------------------------------

val PlumeTypingMenu = UserSettingsMenu(
    title = R.string.plume_home_typing,
    navPath = "typing", registerNavPath = true,
    settings = listOf(
        section(R.string.plume_section_correction),
        userSettingToggleSharedPrefs(
            title = R.string.plume_autocorrect,
            subtitle = R.string.plume_autocorrect_subtitle,
            key = Settings.PREF_AUTO_CORRECTION,
            default = { true },
        ),
        userSettingToggleSharedPrefs(
            title = R.string.plume_show_suggestions,
            subtitle = R.string.plume_show_suggestions_subtitle,
            key = Settings.PREF_SHOW_SUGGESTIONS,
            default = { true }
        ),
        userSettingToggleDataStore(
            title = R.string.plume_emoji_suggestions,
            setting = SHOW_EMOJI_SUGGESTIONS,
        ),

        section(R.string.plume_section_typing),
        userSettingToggleSharedPrefs(
            title = R.string.plume_auto_cap,
            subtitle = R.string.plume_auto_cap_subtitle,
            key = Settings.PREF_AUTO_CAP,
            default = { true },
        ),
        userSettingToggleSharedPrefs(
            title = R.string.plume_double_space_period,
            subtitle = R.string.plume_double_space_period_subtitle,
            key = Settings.PREF_KEY_USE_DOUBLE_SPACE_PERIOD,
            default = { true },
        ),
        userSettingToggleSharedPrefs(
            title = R.string.plume_swipe_typing,
            subtitle = R.string.plume_swipe_typing_subtitle,
            key = Settings.PREF_GESTURE_INPUT,
            default = { true },
        ),

        section(R.string.plume_section_vocabulary),
        userSettingToggleSharedPrefs(
            title = R.string.plume_learn_words,
            subtitle = R.string.plume_learn_words_subtitle,
            key = Settings.PREF_KEY_USE_PERSONALIZED_DICTS,
            default = { true }
        ),
        userSettingNavigationItem(
            title = R.string.plume_learned_words,
            subtitle = R.string.plume_learned_words_subtitle,
            style = NavigationItemStyle.Misc,
            navigateTo = "learned"
        ),
        userSettingNavigationItem(
            title = R.string.plume_personal_dictionary,
            subtitle = R.string.plume_personal_dictionary_subtitle,
            style = NavigationItemStyle.Misc,
            navigateTo = "pdict"
        ),
        userSettingNavigationItem(
            title = R.string.plume_blacklist,
            subtitle = R.string.plume_blacklist_subtitle,
            style = NavigationItemStyle.Misc,
            navigateTo = "blacklist"
        ),
    )
)

// ---------------------------------------------------------------------------------------------
// Barre d'actions
// ---------------------------------------------------------------------------------------------

@Composable
private fun currentActionMap() = useDataStoreValue(ActionsSettings)
    .toActionEditorItems()
    .ensureWellFormed()
    .toActionMap()

val PlumeActionsMenu = UserSettingsMenu(
    title = R.string.plume_home_actions,
    navPath = "actions", registerNavPath = true,
    settings = listOf(
        section(R.string.plume_section_action_key),

        UserSetting(
            name = R.string.plume_action_key,
            subtitle = R.string.plume_action_key_subtitle
        ) {
            val context = LocalContext.current
            val currActionKey = currentActionMap()[ActionCategory.ActionKey]?.firstOrNull()

            SettingToggleRaw(
                title = stringResource(R.string.plume_action_key),
                subtitle = when (currActionKey) {
                    null -> stringResource(R.string.plume_action_key_subtitle)
                    else -> stringResource(currActionKey.name)
                },
                enabled = currActionKey != null,
                setValue = { to ->
                    val actionMap = context.getSetting(ActionsSettings)
                        .toActionEditorItems()
                        .ensureWellFormed()
                        .toActionMap()
                        .toMutableMap()

                    if (to) {
                        actionMap[ActionCategory.ActionKey] = listOf(EmojiAction)
                    } else {
                        val prevActionKey = actionMap[ActionCategory.ActionKey]!!
                        actionMap[ActionCategory.Favorites] =
                            prevActionKey + actionMap[ActionCategory.Favorites]!!
                        actionMap[ActionCategory.ActionKey] = listOf()
                    }

                    context.updateSettingsWithNewActions(actionMap)
                }
            )
        },

        UserSetting(
            name = R.string.plume_language_key,
            subtitle = R.string.plume_language_key_subtitle,
            visibilityCheck = {
                currentActionMap()[ActionCategory.ActionKey]?.firstOrNull() != null
            }
        ) {
            val context = LocalContext.current
            val currActionKey = currentActionMap()[ActionCategory.ActionKey]?.firstOrNull()
            val isLanguageKeyEnabled = currActionKey == SwitchLanguageAction

            SettingToggleRaw(
                title = stringResource(R.string.plume_language_key),
                subtitle = stringResource(R.string.plume_language_key_subtitle),
                enabled = isLanguageKeyEnabled,
                disabled = currActionKey == null,
                setValue = { to ->
                    var actionMap = context.getSetting(ActionsSettings)
                        .toActionEditorItems()
                        .ensureWellFormed()
                        .toActionMap()
                        .toMutableMap()

                    if (to) {
                        val prevActionKey = actionMap[ActionCategory.ActionKey]!!
                        actionMap[ActionCategory.Favorites] =
                            prevActionKey + actionMap[ActionCategory.Favorites]!!.filter { it != SwitchLanguageAction }
                        actionMap[ActionCategory.ActionKey] = listOf(SwitchLanguageAction)
                    } else {
                        actionMap = actionMap.mapValues { entry ->
                            entry.value.filter { it != EmojiAction && it != SwitchLanguageAction }
                        }.toMutableMap()
                        actionMap[ActionCategory.Favorites] =
                            listOf(SwitchLanguageAction) + actionMap[ActionCategory.Favorites]!!
                        actionMap[ActionCategory.ActionKey] = listOf(EmojiAction)
                    }

                    context.updateSettingsWithNewActions(actionMap)
                }
            )
        },

        section(R.string.plume_section_voice),
        UserSetting(
            name = R.string.plume_voice_key,
            subtitle = R.string.plume_voice_key_subtitle
        ) {
            val context = LocalContext.current
            val map = currentActionMap()
            val pinned = map[ActionCategory.PinnedKey]?.contains(VoiceInputAction) == true

            SettingToggleRaw(
                title = stringResource(R.string.plume_voice_key),
                subtitle = stringResource(R.string.plume_voice_key_subtitle),
                enabled = pinned,
                setValue = { to ->
                    val actionMap = context.getSetting(ActionsSettings)
                        .toActionEditorItems()
                        .ensureWellFormed()
                        .toActionMap()
                        .mapValues { entry -> entry.value.filter { it != VoiceInputAction } }
                        .toMutableMap()

                    if (to) {
                        actionMap[ActionCategory.PinnedKey] = listOf(VoiceInputAction) + actionMap[ActionCategory.PinnedKey]!!
                    } else {
                        actionMap[ActionCategory.More] = actionMap[ActionCategory.More]!! + listOf(VoiceInputAction)
                    }

                    context.updateSettingsWithNewActions(actionMap)
                }
            )
        },

        section(R.string.plume_section_clipboard),
        userSettingToggleDataStore(
            title = R.string.plume_clipboard_quick,
            subtitle = R.string.plume_clipboard_quick_subtitle,
            setting = ClipboardQuickClipsEnabled
        ),
        userSettingToggleDataStore(
            title = R.string.plume_clipboard_history,
            subtitle = R.string.plume_clipboard_history_subtitle,
            setting = ClipboardHistoryEnabled
        ),
    )
)

// ---------------------------------------------------------------------------------------------
// Sauvegarde
// ---------------------------------------------------------------------------------------------

val PlumeBackupMenu = UserSettingsMenu(
    title = R.string.plume_home_backup,
    navPath = "backup", registerNavPath = true,
    settings = listOf(
        userSettingNavigationItem(
            title = R.string.plume_backup_export,
            subtitle = R.string.plume_backup_export_subtitle,
            style = NavigationItemStyle.Misc,
            navigateTo = "exportingcfg"
        ),
        userSettingNavigationItem(
            title = R.string.plume_backup_import,
            subtitle = R.string.plume_backup_import_subtitle,
            style = NavigationItemStyle.Misc,
            navigate = { nav ->
                SettingsExporter.triggerImportSettings(nav.context)
            }
        ),
    )
)

// ---------------------------------------------------------------------------------------------
// À propos
// ---------------------------------------------------------------------------------------------

val PlumeAboutMenu = UserSettingsMenu(
    title = R.string.plume_home_about,
    navPath = "about", registerNavPath = true,
    settings = listOf(
        userSettingDecorationOnly {
            Text(
                stringResource(R.string.plume_settings_title),
                style = Typography.Heading.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        },
        UserSetting(name = R.string.plume_about_version) {
            val context = LocalContext.current
            val copied = stringResource(R.string.plume_about_copied)
            NavigationItem(
                title = stringResource(R.string.plume_about_version, BuildConfig.VERSION_NAME),
                subtitle = stringResource(R.string.plume_about_version_subtitle),
                style = NavigationItemStyle.MiscNoArrow,
                navigate = {
                    context.copyToClipboard(BuildConfig.VERSION_NAME)
                    Toast.makeText(context, copied, Toast.LENGTH_SHORT).show()
                }
            )
        },
        userSettingDecorationOnly {
            Spacer(Modifier.height(8.dp))
            PlumeParagraph(stringResource(R.string.plume_about_modified_notice))
            Spacer(Modifier.height(8.dp))
        },
        userSettingNavigationItem(
            title = R.string.plume_about_licenses,
            subtitle = R.string.plume_about_licenses_subtitle,
            style = NavigationItemStyle.Misc,
            navigateTo = "credits"
        ),
        UserSetting(name = R.string.plume_about_restart_setup) {
            val context = LocalContext.current
            NavigationItem(
                title = stringResource(R.string.plume_about_restart_setup),
                subtitle = stringResource(R.string.plume_about_restart_setup_subtitle),
                style = NavigationItemStyle.Misc,
                navigate = { org.futo.inputmethod.latin.uix.settings.plumeRestartSetup(context) }
            )
        },
    )
)

/** All Plume menus that register a navigation route. */
val PlumeSettingsMenus = listOf(
    PlumeKeyboardMenu,
    PlumeTypingMenu,
    PlumeActionsMenu,
    PlumeBackupMenu,
    PlumeAboutMenu,
)

@Composable
fun PlumeMenuScreen(menu: UserSettingsMenu) {
    val nav = LocalNavController.current
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTitle(stringResource(menu.title), showBack = true, nav)
        menu.render(showTitle = false)
        BottomSpacer()
    }
}
