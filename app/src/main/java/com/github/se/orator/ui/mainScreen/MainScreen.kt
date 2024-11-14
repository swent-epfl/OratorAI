package com.github.se.orator.ui.mainScreen

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TextButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppFontSizes
import com.github.se.orator.ui.theme.AppTypography

/**
 * The main screen's composable responsible to display the welcome text, the practice mode cards and
 * the toolbar containing buttons for different sections
 */
@Composable
fun MainScreen(navigationActions: NavigationActions) {
  Scaffold(
      modifier = Modifier.fillMaxSize(),
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
          Text(
              modifier =
                  Modifier.padding(start = AppDimensions.paddingXXLarge)
                      .padding(top = AppDimensions.paddingXXXLarge)
                      .testTag("mainScreenText1"),
              text = "Find your",
              style = AppTypography.largeTitleStyle, // Apply custom style for title
          )

          Text(
              modifier =
                  Modifier.padding(start = AppDimensions.paddingXXLarge).testTag("mainScreenText2"),
              text = "practice mode",
              style = AppTypography.largeTitleStyle, // Apply custom style for subtitle
              fontWeight = FontWeight.Bold)

          ButtonRow(navigationActions)

          // Practice mode cards
          AnimatedCards(navigationActions)
        }
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.HOME)
      })
}

/**
 * The implementation of the toolbar containing the different selection buttons of the main screen
 */
@Composable
fun ButtonRow(navigationActions: NavigationActions) {
  Row(
      modifier =
          Modifier.testTag("toolbar").fillMaxWidth().padding(top = AppDimensions.paddingMedium),
      horizontalArrangement =
          Arrangement.spacedBy(AppDimensions.spacingXLarge, Alignment.CenterHorizontally),
  ) {
    SectionButton("Popular") {
      // Do nothing, stays on the same screen
    }
  }
}

/**
 * @param text the text displayed in each button describing the different selections
 *
 * The implementation of a button
 */
@Composable
fun SectionButton(text: String, onClick: () -> Unit) {
  TextButton(onClick = onClick, modifier = Modifier.testTag("button")) {
    Text(text = text, color = AppColors.textColor, fontSize = AppFontSizes.buttonText)
  }
}

data class Mode(val text: String, val imageRes: Int, val destinationScreen: String)

/** Function to create the sliding animation to browse between modes */
@Composable
fun AnimatedCards(navigationActions: NavigationActions) {
  val modes =
      listOf(
          Mode(
              text = "Prepare for an interview",
              imageRes = R.drawable.speaking_interview,
              destinationScreen = Screen.SPEAKING_JOB_INTERVIEW),
          Mode(
              text = "Improve public speaking",
              imageRes = R.drawable.speaking_speaking,
              destinationScreen = Screen.SPEAKING_PUBLIC_SPEAKING),
          Mode(
              text = "Master sales pitches",
              imageRes = R.drawable.speaking_sales,
              destinationScreen = Screen.SPEAKING_SALES_PITCH))

  LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium),
      contentPadding = PaddingValues(AppDimensions.paddingMedium)) {
        items(modes) { mode ->
          ModeCard(
              text = mode.text,
              painter = painterResource(mode.imageRes),
              visible = true,
              onCardClick = {
                Log.d("MainScreen", "Navigating to ${mode.destinationScreen}")
                navigationActions.navigateTo(mode.destinationScreen)
              })
        }
      }
}

/**
 * @param text the text describing each mode
 * @param painter the image displayed for each mode
 * @param visible boolean used for the animation effect
 * @param onCardClick callback function for a on click event
 *
 * The implementation of a mode card
 */
@Composable
fun ModeCard(text: String, painter: Painter, visible: Boolean, onCardClick: () -> Unit) {
  AnimatedVisibility(
      visible = visible,
      enter = slideInVertically() + fadeIn(),
      exit = slideOutVertically() + fadeOut()) {
        Card(
            shape = RoundedCornerShape(AppDimensions.cardCornerRadius),
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = AppDimensions.cardHorizontalPadding)
                    .padding(top = AppDimensions.paddingMedium)
                    .clickable { onCardClick() },
            colors = CardDefaults.cardColors(containerColor = AppColors.cardBackgroundColor)) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Top image
                Image(
                    painter = painter,
                    contentDescription = "Interview Preparation",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(AppDimensions.cardImageHeight).fillMaxWidth())

                // Text below the image
                Text(
                    text = text,
                    fontSize = AppFontSizes.cardTitle,
                    fontWeight = FontWeight.Bold,
                    modifier =
                        Modifier.padding(AppDimensions.paddingMedium)
                            .align(Alignment.CenterHorizontally))
              }
            }
      }
}
