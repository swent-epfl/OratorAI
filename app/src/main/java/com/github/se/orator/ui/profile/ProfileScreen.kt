package com.github.se.orator.ui.profile

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.github.se.orator.R
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.AppShapes
import com.github.se.orator.ui.theme.AppTypography
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navigationActions: NavigationActions, profileViewModel: UserProfileViewModel) {
  // Get the context
  val context = LocalContext.current

  // State to control whether the profile picture dialog is open
  var isDialogOpen by remember { mutableStateOf(false) }

  // Collect the profile data from the ViewModel
  val userProfile by profileViewModel.userProfile.collectAsState()

  Scaffold(
      topBar = {
        TopAppBar(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.inverseSurface,
            elevation = AppDimensions.appBarElevation,
            title = {
              Text(
                  modifier = Modifier.testTag("profile_title"),
                  text = "Profile",
                  fontWeight = FontWeight.Bold,
                  style = AppTypography.appBarTitleStyle)
            },
            actions = {
              IconButton(
                  onClick = { navigationActions.navigateTo(Screen.SETTINGS) },
                  modifier = Modifier.testTag("settings_button")) {
                    Icon(
                        Icons.Outlined.Settings,
                        contentDescription = "Settings",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("settings_icon"))
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = {
                    // Sign out the user
                    FirebaseAuth.getInstance().signOut()
                    // Display a toast message
                    Toast.makeText(context, "Logout successful!", Toast.LENGTH_SHORT).show()
                    // Navigate to the sign in screen
                    navigationActions.navigateTo(Screen.AUTH)
                  },
                  modifier = Modifier.testTag("sign_out_button")) {
                    Icon(
                        Icons.Filled.Logout,
                        contentDescription = "Sign out",
                        modifier =
                            Modifier.size(AppDimensions.iconSizeMedium).testTag("sign_out_icon"))
                  }
            })
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = Route.PROFILE)
      }) {
        Column(
            modifier = Modifier.fillMaxSize().padding(it).padding(AppDimensions.paddingMedium),
            horizontalAlignment = Alignment.CenterHorizontally) {
              userProfile?.let { profile ->
                Box(
                    modifier =
                        Modifier.fillMaxWidth()
                            .height(200.dp)
                            .padding(top = AppDimensions.paddingXXXLarge),
                    contentAlignment = Alignment.TopCenter) {
                      // Background "card" behind the profile picture
                      Card(
                          modifier = Modifier.fillMaxWidth(0.95f).height(140.dp),
                          elevation = AppDimensions.elevationSmall) {}

                      // Profile Picture with overlapping positioning
                      ProfilePicture(
                          profilePictureUrl = profile.profilePic,
                          onClick = { isDialogOpen = true },
                          modifier =
                              Modifier.align(Alignment.TopCenter)
                                  .offset(y = (-AppDimensions.profilePictureSize / 2)))

                      // Edit button
                      Button(
                          onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) },
                          modifier =
                              Modifier.testTag("edit_button")
                                  .width(40.dp)
                                  .height(40.dp)
                                  .align(Alignment.TopEnd)
                                  .offset(y = (-20).dp),
                          // .offset(x = (AppDimensions.profilePictureSize / 2.2f)),
                          shape = AppShapes.circleShape,
                          colors =
                              ButtonDefaults.buttonColors(backgroundColor = AppColors.surfaceColor),
                          contentPadding = PaddingValues(0.dp)) {
                            Icon(
                                Icons.Outlined.Edit,
                                contentDescription = "Edit button",
                                modifier = Modifier.size(30.dp),
                                tint = AppColors.primaryColor)
                          }

                      Column(
                          horizontalAlignment = Alignment.CenterHorizontally,
                          modifier = Modifier.align(Alignment.TopCenter)) {
                            Spacer(modifier = Modifier.height(AppDimensions.MediumSpacerHeight))
                            // Username
                            Text(
                                text = profile.name,
                                fontSize = 20.sp,
                                modifier = Modifier.testTag("profile_name"))
                            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacerHeight))

                            Text(
                                text =
                                    if (profile.bio.isNullOrBlank()) "Write your bio here"
                                    else profile.bio,
                                modifier =
                                    Modifier.padding(horizontal = AppDimensions.paddingMedium))
                          }
                    }

                Spacer(modifier = Modifier.height(AppDimensions.paddingMedium))
                Log.d("scn", "bio is: ${profile.bio}")

                // stats section
                CardSection(
                    title = "My stats",
                    imageVector = Icons.Outlined.QueryStats,
                    onClick = { /*TODO: Handle achievements click */},
                    modifier = Modifier.testTag("statistics_section"))

                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall))

                // Previous Sessions Section
                CardSection(
                    title = "Previous Recordings",
                    imageVector = Icons.Outlined.History,
                    onClick = { /*TODO: Handle previous sessions click */},
                    modifier = Modifier.testTag("previous_sessions_section"))
              }
                  ?: run {
                    Text(
                        text = "Loading profile...",
                        style = AppTypography.bodyLargeStyle,
                        modifier = Modifier.testTag("loading_profile_text"))
                  }
            }

        // Dialog to show the profile picture in larger format
        if (isDialogOpen && userProfile?.profilePic != null) {
          ProfilePictureDialog(
              profilePictureUrl = userProfile!!.profilePic!!, onDismiss = { isDialogOpen = false })
        }
      }
}

@Composable
fun ProfilePicture(profilePictureUrl: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
  val painter = rememberAsyncImagePainter(model = profilePictureUrl ?: R.drawable.profile_picture)

  Image(
      painter = painter,
      contentDescription = "Profile Picture",
      contentScale = ContentScale.Crop,
      modifier =
          modifier
              .size(AppDimensions.profilePictureSize)
              .clip(CircleShape)
              .clickable(onClick = onClick)
              .testTag("profile_picture"))
}

/**
 * Composable function to display a dialog with a larger profile picture.
 *
 * @param profilePictureUrl URL of the profile picture.
 * @param onDismiss Callback to handle dismiss events.
 */
@Composable
fun ProfilePictureDialog(profilePictureUrl: String, onDismiss: () -> Unit) {
  Dialog(onDismissRequest = { onDismiss() }) {
    Box(
        modifier =
            Modifier.fillMaxSize()
                .padding(AppDimensions.paddingMedium)
                .clickable { onDismiss() }
                .testTag("OnDismiss"), // Dismiss the dialog when clicked outside
        contentAlignment = Alignment.Center) {
          Image(
              painter = rememberAsyncImagePainter(model = profilePictureUrl),
              contentDescription = "Large Profile Picture",
              contentScale = ContentScale.Crop,
              modifier =
                  Modifier.size(AppDimensions.profilePictureDialogSize)
                      .clip(CircleShape)
                      .testTag("profile_picture_dialog"))
        }
  }
}

/**
 * Composable function to display a card section with an icon and title.
 *
 * @param title Title of the card section.
 * @param imageVector Icon of the card.
 * @param onClick Callback to handle click events.
 * @param modifier Modifier to be applied to the card.
 */
@Composable
fun CardSection(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .height(AppDimensions.cardSectionHeight)
              .clickable { onClick() }
              .testTag("cardSection"),
      elevation = AppDimensions.elevationSmall) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(AppDimensions.paddingSmallMedium)) {
              Icon(
                  imageVector,
                  contentDescription = "Card icon",
                  modifier = Modifier.size(AppDimensions.iconSizeMedium))
              Spacer(modifier = Modifier.width(AppDimensions.paddingSmallMedium))
              Text(
                  text = title,
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag("titleText"))
            }
      }
}
