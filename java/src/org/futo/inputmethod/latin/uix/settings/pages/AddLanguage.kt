package org.futo.inputmethod.latin.uix.settings.pages

import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.Subtypes
import org.futo.inputmethod.latin.uix.KeyboardLayoutPreview
import org.futo.inputmethod.latin.uix.SettingsTextEdit
import org.futo.inputmethod.latin.uix.actions.searchMultiple
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.Route
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.ScrollableList
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.v2keyboard.LayoutManager
import java.text.Normalizer
import java.util.Locale

/** Plume: languages offered by "Add language", in display order. */
val PlumeLanguages = listOf(
    Locale("fr"),
    Locale("fr", "CH"),
    Locale("fr", "CA"),
    Locale("en", "US"),
    Locale("en", "GB"),
)

private val alphaRegex = Regex("[^a-zA-Z\\s]")
fun normalize(str: String): String {
    return Normalizer.normalize(str, Normalizer.Form.NFD)
        .replace(alphaRegex, "")
}


@Preview
@Composable
fun SelectLanguageScreen(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    val resources = LocalResources.current

    val layoutMapping = remember { LayoutManager.getLayoutMapping(context) }

    val systemLocale = remember { resources.configuration.locales[0] }

    // Plume: closed list of languages (the fork ships FR/EN dictionaries only)
    val locales = remember {
        val allowed = PlumeLanguages.map { it.toLanguageTag() }
        layoutMapping.keys.filter { it.toLanguageTag() in allowed }.sortedBy {
            allowed.indexOf(it.toLanguageTag())
        }
    }

    LazyColumn {
        item(key=0) {
            ScreenTitle(stringResource(R.string.language_settings_select_language), showBack = true, navController)
        }

        items(locales) {
            NavigationItem(
                title = Subtypes.getLocaleDisplayName(it, systemLocale),
                subtitle = Subtypes.getLocaleDisplayName(it, it),
                style = NavigationItemStyle.MiscNoArrow,
                navigate = {
                    navController.navigate(Route.AddLayout(it.toLanguageTag()))
                }
            )
        }
    }
}


@Composable
fun LayoutPreview(name: String, locale: Locale, onClick: () -> Unit) {
    val context = LocalContext.current
    val layoutName = remember { LayoutManager.getLayout(context, name).name }

    Box(
        Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            KeyboardLayoutPreview(id = name, width = 172.dp, locale = locale)

            Text(
                layoutName,
                style = Typography.SmallMl,
                modifier = Modifier.padding(4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
fun SelectLayoutsScreen(navController: NavHostController = rememberNavController(), locale: Locale = Locale.ENGLISH) {
    val context = LocalContext.current

    val layoutMapping = remember { LayoutManager.getLayoutMapping(context) }

    val relevantLayouts = remember {
        layoutMapping.entries.filter {
            (it.key.language == locale.language) && (it.key.script == locale.script)
        }.flatMap { it.value }.toSet()
    }

    ScrollableList {
        ScreenTitle(Subtypes.getLocaleDisplayName(locale, locale), showBack = true, navController)

        ScreenTitle(stringResource(R.string.language_settings_select_layout))

        relevantLayouts.forEach {
            LayoutPreview(it, locale) {
                Subtypes.addLanguage(context, locale, it)

                // Go back to languages
                for(x in 0 until 3) navController.navigateUp()
                navController.navigate("languages")
            }
        }
    }
}