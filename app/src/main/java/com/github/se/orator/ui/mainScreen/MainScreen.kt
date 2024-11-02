package com.github.se.orator.ui.theme.mainScreen

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.se.orator.R
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen

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
                  Modifier.padding(start = 42.dp).padding(top = 64.dp).testTag("mainScreenText1"),
              text = "Find your",
              fontSize = 50.sp)

          Text(
              modifier = Modifier.padding(start = 42.dp).testTag("mainScreenText2"),
              text = "practice mode",
              fontSize = 47.sp,
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
      modifier = Modifier.testTag("toolbar").fillMaxWidth().padding(top = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterHorizontally),
  ) {
    SectionButton("Popular") {
      // Do nothing, stays on the same screen
    }

    // Fun Button
    SectionButton("Fun") { navigationActions.navigateTo(Screen.FUN_SCREEN) }

    // Connect Button
    SectionButton("Connect") { navigationActions.navigateTo(Screen.CONNECT_SCREEN) }
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
    Text(text = text, color = Color.Black, fontSize = 20.sp)
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
              imageRes = R.drawable.job_interview,
              destinationScreen = Screen.SPEAKING_JOB_INTERVIEW),
          Mode(
              text = "Improve public speaking",
              imageRes = R.drawable.job_interview,
              destinationScreen = Screen.SPEAKING_PUBLIC_SPEAKING),
          Mode(
              text = "Master sales pitches",
              imageRes = R.drawable.job_interview,
              destinationScreen = Screen.SPEAKING_SALES_PITCH))

  LazyColumn(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(16.dp)) {
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
            shape = RoundedCornerShape(16.dp),
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = 30.dp).padding(top = 16.dp).clickable {
                  onCardClick()
                },
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))) {
              Column(modifier = Modifier.fillMaxWidth()) {
                // Top image
                Image(
                    painter = painter,
                    contentDescription = "Interview Preparation",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.height(160.dp).fillMaxWidth())

                // Text below the image
                Text(
                    text = text,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally))
              }
            }
      }
}
