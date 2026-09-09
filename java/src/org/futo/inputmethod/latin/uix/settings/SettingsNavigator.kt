package org.futo.inputmethod.latin.uix.settings

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import org.futo.inputmethod.latin.toLocale
import org.futo.inputmethod.latin.uix.ErrorDialog
import org.futo.inputmethod.latin.uix.InfoDialog
import org.futo.inputmethod.latin.uix.LocalNavController
import org.futo.inputmethod.latin.uix.SettingsExporter.ExportingMenu
import org.futo.inputmethod.latin.uix.settings.pages.BlacklistScreen
import org.futo.inputmethod.latin.uix.settings.pages.CreditsScreen
import org.futo.inputmethod.latin.uix.settings.pages.LanguagesScreen
import org.futo.inputmethod.latin.uix.settings.pages.PlumeHomeScreen
import org.futo.inputmethod.latin.uix.settings.pages.PlumeMenuScreen
import org.futo.inputmethod.latin.uix.settings.pages.PlumeSettingsMenus
import org.futo.inputmethod.latin.uix.settings.pages.ProjectInfoView
import org.futo.inputmethod.latin.uix.settings.pages.ResizeScreen
import org.futo.inputmethod.latin.uix.settings.pages.SelectLanguageScreen
import org.futo.inputmethod.latin.uix.settings.pages.SelectLayoutsScreen
import org.futo.inputmethod.latin.uix.settings.pages.pdict.ConfirmDeleteExtraDictFileDialog
import org.futo.inputmethod.latin.uix.settings.pages.pdict.PersonalDictionaryLanguageList
import org.futo.inputmethod.latin.uix.settings.pages.pdict.PersonalDictionaryLanguageListForLocale
import org.futo.inputmethod.latin.uix.settings.pages.pdict.WordPopupDialogF
import org.futo.inputmethod.latin.uix.settings.pages.themes.ThemeScreen

// Utility function for quick error messages
fun NavHostController.navigateToError(title: String, body: String) {
    this.navigate(Route.Error(title, body))
}

fun NavHostController.navigateToInfo(title: String, body: String) {
    this.navigate(Route.Info(title, body))
}


object Route {
    @Serializable data class Error(val title: String, val body: String)
    @Serializable data class Info(val title: String, val body: String)
    @Serializable data class AddLayout(val lang: String)
    @Serializable data class PersonalDictList(val lang: String?)
    @Serializable data class PersonalDictWord(val lang: String?, val word: String?)
    @Serializable data class PersonalDictDelete(val dict: String)
    @Serializable data class DevLayoutEdit(val i: Int)
    @Serializable data class CustomTheme(val uri: String)
    @Serializable data class DeleteTheme(val name: String)
    @Serializable data class ThirdPartyInfo(val idx: Int)
}


/*
 * Plume: only the menus of the minimal structure are registered. The FUTO menus (developer,
 * payment, help, model manager, theme editor, KASROZ, voice engine, zh/ja input...) are kept
 * in the code base but have no route any more.
 */
val SettingsMenus = PlumeSettingsMenus


// Improves the semantics so that we don't have to deal with NavBackStackEntry when we don't need it
@JvmInline
value class NavGraphBuilderWrapper(val parent: NavGraphBuilder)
internal inline fun <reified T : Any> NavGraphBuilderWrapper.dialog(noinline content: @Composable (T) -> Unit) =
    parent.dialog<T> { content(it.toRoute()) }
internal inline fun <reified T : Any> NavGraphBuilderWrapper.composable(noinline content: @Composable (T) -> Unit) =
    parent.composable<T> { content(it.toRoute()) }


@Composable
fun SettingsNavigator(
    navController: NavHostController = rememberNavController()
) {
    val nav = navController
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = "home",
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            with(NavGraphBuilderWrapper(this)) {
                composable<Route.AddLayout> { SelectLayoutsScreen(nav, it.lang.toLocale()) }

                parent.composable<Route.PersonalDictList> {
                    val route = it.toRoute<Route.PersonalDictList>()
                    PersonalDictionaryLanguageListForLocale(nav, it, route.lang?.toLocale())
                }
                dialog<Route.PersonalDictWord> { WordPopupDialogF(it.word, it.lang?.toLocale()) }
                dialog<Route.PersonalDictDelete> { ConfirmDeleteExtraDictFileDialog(it.dict) }

                composable<Route.ThirdPartyInfo> { ProjectInfoView(it.idx, nav) }

                dialog<Route.Error> { ErrorDialog(it.title, it.body, nav) }
                dialog<Route.Info> { InfoDialog(it.title, it.body, nav) }
            }
            composable("home") { PlumeHomeScreen(navController) }

            // Langues
            composable("languages") { LanguagesScreen(navController) }
            composable("addLanguage") { SelectLanguageScreen(navController) }

            // Clavier, Saisie et correction, Barre d'actions, Sauvegarde, À propos
            SettingsMenus.forEach { menu ->
                if(menu.registerNavPath) composable(menu.navPath) { PlumeMenuScreen(menu) }
            }
            composable("resize") { ResizeScreen(navController) }
            composable("pdict") { PersonalDictionaryLanguageList() }
            composable("blacklist") { BlacklistScreen(navController) }
            composable("learned") { org.futo.inputmethod.latin.uix.settings.pages.LearnedWordsScreen(navController) }

            // Apparence
            composable("themes") { ThemeScreen(navController) }

            // Sauvegarde, À propos
            composable("exportingcfg") { ExportingMenu(navController) }
            composable("credits") { CreditsScreen(navController) }
        }
    }
}
