package org.futo.inputmethod.latin.uix.settings.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.settings.BottomSpacer
import org.futo.inputmethod.latin.uix.settings.NavigationItem
import org.futo.inputmethod.latin.uix.settings.NavigationItemStyle
import org.futo.inputmethod.latin.uix.settings.Route
import org.futo.inputmethod.latin.uix.settings.ScreenTitle
import org.futo.inputmethod.latin.uix.settings.SectionHeader
import org.futo.inputmethod.latin.uix.settings.PlumeParagraph
import org.futo.inputmethod.latin.uix.settings.ScrollableList
import org.futo.inputmethod.latin.uix.settings.SpacedColumn
import org.futo.inputmethod.latin.uix.settings.UserSettingsMenu
import org.futo.inputmethod.latin.uix.settings.pages.credits.ThirdPartyItem
import org.futo.inputmethod.latin.uix.settings.pages.credits.ThirdPartyList
import org.futo.inputmethod.latin.uix.settings.pages.credits.codeContribs
import org.futo.inputmethod.latin.uix.settings.pages.credits.languageContribs
import org.futo.inputmethod.latin.uix.settings.pages.credits.layoutContribs
import org.futo.inputmethod.latin.uix.settings.pages.credits.text
import org.futo.inputmethod.latin.uix.settings.render
import org.futo.inputmethod.latin.uix.settings.userSettingNavigationItem
import org.futo.inputmethod.latin.uix.theme.Typography
import org.futo.inputmethod.updates.openURI

@Composable
@Preview(showBackground = true)
fun ProjectInfoView(
    projectIndex: Int = 1,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val info = ThirdPartyList[projectIndex]
    ScrollableList {
        ScreenTitle(
            stringResource(R.string.credits_menu_project_information_title, info.name),
            showBack = true, navController
        )

        PlumeParagraph(info.description)

        NavigationItem(
            title = stringResource(R.string.credits_menu_project_url_link),
            subtitle = info.projectUrl,
            style = NavigationItemStyle.ExternalLink,
            navigate = {
                context.openURI(info.projectUrl)
            }
        )

        Spacer(Modifier.height(16.dp))

        Text(
            info.copyright,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = Typography.SmallMl
        )
        Text(
            info.license.text(context),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = Typography.SmallMl,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BottomSpacer()
    }
}

@Composable
fun <T> VerticalGrid(
    modifier: Modifier = Modifier,
    items: List<T>,
    columns: Int,
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable (item: T) -> Unit
) {
    val rows = items.chunked(columns)
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        rows.forEachIndexed { rowindex, rowItems ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = horizontalArrangement) {
                rowItems.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        content(item)
                    }
                    if (index == rowItems.lastIndex && rowindex == rows.lastIndex) {
                        // Add a placeholder empty view
                        for (i in 0 until (columns - rowItems.size)) {
                            Spacer(
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Plume: plain section (grey label + names), no coloured card. */
@Composable
fun CreditCategorySection(
    title: String, names: List<String>, columns: Int = 2,
    thirdPartyInformation: List<ThirdPartyItem>? = null, navController: NavHostController? = null
) {
    val foregroundColor = MaterialTheme.colorScheme.onSurface

    Column(Modifier.fillMaxWidth()) {
        SectionHeader(title)
        if (thirdPartyInformation != null) {
            thirdPartyInformation.forEachIndexed { i, info ->
                NavigationItem(
                    title = info.description,
                    subtitle = info.copyright,
                    style = NavigationItemStyle.Misc,
                    navigate = { navController!!.navigate(Route.ThirdPartyInfo(i)) }
                )
            }
        } else {
            VerticalGrid(
                items = names,
                columns = columns,
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(it, color = foregroundColor, style = Typography.SmallMl)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
fun CreditsScreen(navController: NavHostController = rememberNavController()) {
    ScrollableList {
        ScreenTitle(stringResource(R.string.plume_about_licenses), showBack = true, navController)

        PlumeParagraph(stringResource(R.string.plume_credits_header))

        CreditCategorySection(
            title = stringResource(R.string.credits_menu_team_translators_title),
            names = languageContribs,
        )

        CreditCategorySection(
            title = stringResource(R.string.credits_menu_team_keyboard_layouts_title),
            names = layoutContribs,
        )

        CreditCategorySection(
            title = stringResource(R.string.credits_menu_team_code_title),
            names = codeContribs,
        )

        CreditCategorySection(
            title = stringResource(R.string.credits_menu_team_third_party_libraries_title2),
            columns = 1,
            names = ThirdPartyList.map { it.description },
            thirdPartyInformation = ThirdPartyList,
            navController = navController,
        )

        Spacer(Modifier.height(16.dp))
        PlumeParagraph(stringResource(R.string.credits_menu_nonaffiliation_notice))
        BottomSpacer()
    }
}