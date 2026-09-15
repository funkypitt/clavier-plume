package org.futo.inputmethod.latin.uix.settings.pages

import android.view.inputmethod.InputMethodSubtype
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.MultilingualBucketSetting
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.SubtypesSetting
import org.futo.inputmethod.latin.uix.settings.BottomSpacer
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.Route
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.useDataStore
import org.futo.inputmethod.latin.uix.settings.useDataStoreValue
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.latin.uix.theme.UixThemeWrapper
import org.futo.inputmethod.latin.uix.theme.presets.DynamicDarkTheme
import org.futo.inputmethod.latin.utils.SubtypeLocaleUtils
import java.util.Locale

/*
 * Plume: the language screen only shows, per language, its layouts, the multilingual
 * suggestions checkbox and the add-layout / remove-language actions. Voice model, dictionary
 * and transformer rows, the resource importer and the FUTO "explore online" links are gone
 * (docs/05-reglages-minimalistes.md §2).
 */

private val InputMethodSubtype.layoutSetName
    get() = SubtypeLocaleUtils.getKeyboardLayoutSetName(this)

data class LanguageItem(
    val languageName: String,
    val layouts: List<Pair<InputMethodSubtype, String>>,
    val inMultilingualBucket: Boolean
)

@Composable
private fun LayoutRow(
    name: String,
    onDelete: () -> Unit,
    canDelete: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .padding(start = 16.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            name,
            style = Typography.Body.RegularMl,
            modifier = Modifier.weight(1.0f)
        )

        if (canDelete) {
            IconButton(onClick = onDelete) {
                Icon(
                    painterResource(id = R.drawable.close),
                    contentDescription = stringResource(R.string.language_settings_remove_this_language),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
    }
}

@Composable
private fun CardAction(
    icon: Int,
    text: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(id = icon),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(text, style = Typography.Body.RegularMl, color = tint)
    }
}

@Composable
fun LanguageSurface(
    item: LanguageItem,
    modifier: Modifier = Modifier,
    onLayoutRemoved: (InputMethodSubtype) -> Unit,
    onLayoutAdditionRequested: () -> Unit,
    onLanguageRemoved: () -> Unit,
    onToggleMultilingualBucket: (Boolean) -> Unit,
    showBucketToggle: Boolean = true          // Plume : masquée quand l'interrupteur bilingue unique s'applique
) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(vertical = 8.dp)
    ) {
        Text(
            item.languageName,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            style = Typography.Body.MediumMl.copy(fontWeight = FontWeight.Medium)
        )

        Text(
            stringResource(R.string.plume_languages_layouts),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 2.dp),
            style = Typography.Small,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        item.layouts.forEach {
            LayoutRow(
                name = it.second,
                onDelete = { onLayoutRemoved(it.first) },
                canDelete = (item.layouts.size > 1)
            )
        }

        CardAction(
            icon = R.drawable.plus_circle,
            text = stringResource(R.string.plume_languages_add_layout),
            tint = MaterialTheme.colorScheme.primary,
            onClick = onLayoutAdditionRequested
        )

        HorizontalDivider(
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        if (showBucketToggle) Row(
            Modifier
                .fillMaxWidth()
                .clickable { onToggleMultilingualBucket(!item.inMultilingualBucket) }
                .padding(start = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                stringResource(R.string.plume_languages_multilingual),
                modifier = Modifier.weight(1.0f),
                style = Typography.Body.RegularMl
            )
            Checkbox(
                checked = item.inMultilingualBucket,
                onCheckedChange = { onToggleMultilingualBucket(it) }
            )
        }

        if (showBucketToggle) HorizontalDivider(
            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        CardAction(
            icon = R.drawable.trash,
            text = stringResource(R.string.plume_languages_remove),
            tint = MaterialTheme.colorScheme.error,
            onClick = onLanguageRemoved
        )
    }
}

/**
 * Plume : avec exactement deux langues, un seul réglage nommé par son effet remplace les deux cases
 * « suggestions dans cette langue en même temps » (dont la combinaison devait être devinée, et dont le
 * défaut vide corrigeait « boot » en « boit »). La touche EN/FR reste : disposition, typographie et
 * langue par défaut en dépendent dans les deux modes.
 */
@Composable
fun BilingualModeCard(both: Boolean, onChange: (Boolean) -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(color = MaterialTheme.colorScheme.surfaceContainerLow, shape = RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp)
    ) {
        Text(
            stringResource(R.string.plume_languages_mode_title),
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
            style = Typography.Body.MediumMl.copy(fontWeight = FontWeight.Medium)
        )
        listOf(
            true to (R.string.plume_languages_mode_both to R.string.plume_languages_mode_both_subtitle),
            false to (R.string.plume_languages_mode_one to R.string.plume_languages_mode_one_subtitle),
        ).forEach { (value, labels) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onChange(value) }
                    .padding(start = 8.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                androidx.compose.material3.RadioButton(selected = both == value, onClick = { onChange(value) })
                Column(Modifier.weight(1.0f)) {
                    Text(stringResource(labels.first), style = Typography.Body.RegularMl)
                    Text(
                        stringResource(labels.second),
                        style = Typography.Small,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LanguageSurfacePreview() {
    UixThemeWrapper(colorScheme = DynamicDarkTheme.obtainColors(LocalContext.current)) {
        LanguageSurface(
            item = LanguageItem(
                languageName = "Language Name",
                layouts = listOf(
                    InputMethodSubtype.InputMethodSubtypeBuilder().build() to "QWERTY",
                    InputMethodSubtype.InputMethodSubtypeBuilder().build() to "Dvorak"
                ),
                inMultilingualBucket = true
            ),
            onLanguageRemoved = { }, onLayoutRemoved = { },
            onLayoutAdditionRequested = { }, onToggleMultilingualBucket = { })
    }
}

@Composable
fun ConfirmDeleteLanguageDialog(
    onDismissRequest: () -> Unit,
    onDelete: () -> Unit,
    locale: Locale
) {
    AlertDialog(
        icon = {
            Icon(painterResource(id = R.drawable.trash), contentDescription = null)
        },
        title = {
            Text(
                text = stringResource(
                    R.string.language_settings_remove_language_title,
                    locale.displayLanguage
                )
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.language_settings_remove_language_body,
                    locale.displayLanguage
                )
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.language_settings_remove_language_remove_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.language_settings_remove_language_cancel_button))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun LanguagesScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val languageDeleteInfo: MutableState<Locale?> = remember { mutableStateOf(null) }

    val inputMethods = useDataStoreValue(SubtypesSetting)
    val inputMethodList = remember(inputMethods) {
        Subtypes.layoutsMappedByLanguage(inputMethods)
    }

    val multilingualBucket = useDataStore(MultilingualBucketSetting)

    val inputMethodKeys = remember(inputMethodList) { inputMethodList.keys.toList().sorted() }

    if (languageDeleteInfo.value != null) {
        val info = languageDeleteInfo.value!!
        ConfirmDeleteLanguageDialog(
            locale = info,
            onDelete = {
                languageDeleteInfo.value = null

                val key = inputMethodKeys.find { localeString ->
                    Subtypes.getLocale(localeString) == info
                }

                if (key != null) {
                    val subtypes = inputMethodList[key]!!
                    subtypes.forEach { Subtypes.removeLanguage(context, it) }

                    if(multilingualBucket.value.contains(key)) {
                        multilingualBucket.value.toMutableSet().apply {
                            remove(key)
                            multilingualBucket.setValue(this)
                        }
                    }
                }
            },
            onDismissRequest = {
                languageDeleteInfo.value = null
            }
        )
    }
    LazyColumn {
        item {
            ScreenTitle(
                stringResource(R.string.plume_home_languages),
                showBack = true,
                navController
            )
        }

        val bilingual = inputMethodKeys.size == 2
        if (bilingual) item {
            BilingualModeCard(
                both = multilingualBucket.value.containsAll(inputMethodKeys),
                onChange = { both -> multilingualBucket.setValue(if (both) inputMethodKeys.toSet() else emptySet()) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        items(inputMethodKeys) { localeString ->
            val subtypes = inputMethodList[localeString]!!
            val locale = Subtypes.getLocale(localeString)
            val name = Subtypes.getName(subtypes.first())

            LanguageSurface(
                LanguageItem(
                    languageName = name,
                    layouts = subtypes.map {
                        val layoutName = Subtypes.getLayoutName(context, it.layoutSetName)
                        it to layoutName
                    },
                    inMultilingualBucket = multilingualBucket.value.contains(localeString)
                ),
                onLanguageRemoved = {
                    languageDeleteInfo.value = locale
                },
                onLayoutRemoved = { subtype ->
                    Subtypes.removeLanguage(context, subtype)
                },
                onLayoutAdditionRequested = {
                    navController.navigate(Route.AddLayout(locale.toLanguageTag()))
                },
                onToggleMultilingualBucket = { to ->
                    val newSet = multilingualBucket.value.toMutableSet()
                    if (to) {
                        newSet.add(localeString)
                    } else {
                        newSet.remove(localeString)
                    }

                    multilingualBucket.setValue(newSet)
                },
                showBucketToggle = !bilingual
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            NavigationItem(
                title = stringResource(R.string.language_settings_add_language_button),
                style = NavigationItemStyle.Misc,
                icon = painterResource(R.drawable.plus_circle),
                navigate = { navController.navigate("addLanguage") }
            )
            BottomSpacer()
        }
    }
}
