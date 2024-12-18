package com.github.se.orator.ui.friends

import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import com.github.se.orator.model.profile.UserProfile
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.ui.navigation.BottomNavigationMenu
import com.github.se.orator.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.profile.ProfilePictureDialog
import com.github.se.orator.ui.theme.AppColors
import com.github.se.orator.ui.theme.AppDimensions
import com.github.se.orator.ui.theme.ProjectTheme

/**
 * Composable function that displays the "Add Friends" screen, allowing users to search and add
 * friends. The screen contains a top app bar with a back button, a search field to look for
 * friends, and a list of matching user profiles based on the search query.
 *
 * @param navigationActions Actions to handle navigation within the app.
 * @param userProfileViewModel ViewModel for managing user profile data and friend addition logic.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddFriendsScreen(
    navigationActions: NavigationActions,
    userProfileViewModel: UserProfileViewModel
) {
  val userProfile by userProfileViewModel.userProfile.collectAsState()
  val friendsProfiles by userProfileViewModel.friendsProfiles.collectAsState()
  var query by remember { mutableStateOf("") } // Holds the search query input
  var expanded by remember { mutableStateOf(false) } // Controls if search results are visible
  val allProfiles by userProfileViewModel.allProfiles.collectAsState() // All user profiles
  val focusRequester = FocusRequester() // Manages focus for the search field

  // Exclude the current user's profile and their friends' profiles from the list
  val filteredProfiles =
      allProfiles.filter { profile ->
        profile.uid != userProfile?.uid && // Exclude own profile
            friendsProfiles.none { friend -> friend.uid == profile.uid } && // Exclude friends
            profile.name.contains(query, ignoreCase = true) // Match search query
      }

  // State variable to keep track of the selected user's profile picture
  var selectedProfilePicUser by remember { mutableStateOf<UserProfile?>(null) }

  ProjectTheme {
    Scaffold(
        topBar = {
          TopAppBar(
              title = { Text("Add a Friend", modifier = Modifier.testTag("addFriendTitle")) },
              navigationIcon = {
                IconButton(
                    onClick = { navigationActions.goBack() },
                    modifier = Modifier.testTag("addFriendBackButton")) {
                      Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
              },
          )
          Divider()
        },
        bottomBar = {
          BottomNavigationMenu(
              onTabSelect = { route -> navigationActions.navigateTo(route) },
              tabList = LIST_TOP_LEVEL_DESTINATION,
              selectedItem = Route.FRIENDS)
        }) { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(paddingValues)
                      .padding(AppDimensions.paddingMedium)) {
                // Text field with search icon and clear button
                OutlinedTextField(
                    value = query,
                    onValueChange = { newValue ->
                      query = newValue
                      expanded = newValue.isNotEmpty()
                    },
                    modifier =
                        Modifier.wrapContentWidth()
                            .horizontalScroll(rememberScrollState())
                            .height(AppDimensions.mediumHeight)
                            .focusRequester(focusRequester)
                            .testTag("addFriendSearchField"),
                    label = { Text("Username", modifier = Modifier.testTag("searchFieldLabel")) },
                    leadingIcon = {
                      Icon(
                          Icons.Default.Search,
                          contentDescription = "Search Icon",
                          modifier = Modifier.testTag("searchIcon"))
                    },
                    trailingIcon = {
                      if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { query = "" },
                            modifier = Modifier.testTag("clearSearchButton")) {
                              Icon(
                                  Icons.Default.Clear,
                                  contentDescription = "Clear Icon",
                                  modifier = Modifier.testTag("clearIcon"))
                            }
                      }
                    },
                    singleLine = true,
                    keyboardActions = KeyboardActions.Default)
                // Display search results if there is a query
                if (query.isNotEmpty()) {
                  LazyColumn(
                      contentPadding = PaddingValues(vertical = AppDimensions.paddingSmall),
                      verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingSmall),
                      modifier = Modifier.testTag("searchResultsList")) {
                        // Filter and display profiles matching the query
                        items(
                            filteredProfiles.filter { profile ->
                              profile.name.contains(query, ignoreCase = true)
                            }) { user ->
                              UserItem(
                                  user = user,
                                  userProfileViewModel = userProfileViewModel,
                                  onProfilePictureClick = { selectedUser ->
                                    selectedProfilePicUser = selectedUser
                                  })
                            }
                      }
                }
              }

          // Dialog to show the enlarged profile picture
          if (selectedProfilePicUser?.profilePic != null) {
            ProfilePictureDialog(
                profilePictureUrl = selectedProfilePicUser?.profilePic ?: "",
                onDismiss = { selectedProfilePicUser = null })
          }
        }
  }
}

/**
 * Composable function that represents a single user item in a list. Displays the user's profile
 * picture, name, and bio, and allows adding the user as a friend.
 *
 * @param user The [UserProfile] object representing the user being displayed.
 * @param userProfileViewModel The [UserProfileViewModel] that handles the logic of adding a user as
 *   a friend.
 * @param onProfilePictureClick Callback when the profile picture is clicked.
 */
@Composable
fun UserItem(
    user: UserProfile,
    userProfileViewModel: UserProfileViewModel,
    onProfilePictureClick: (UserProfile) -> Unit
) {
  val context = LocalContext.current // Get the context for showing Toast

  Surface(
      modifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = AppDimensions.smallPadding)
              .clip(RoundedCornerShape(AppDimensions.roundedCornerRadius))
              .clickable {
                // Add friend when the item is clicked
                userProfileViewModel.addFriend(user)
                // Show Toast message
                Toast.makeText(
                        context, "${user.name} has been added as a friend", Toast.LENGTH_SHORT)
                    .show()
              },
      color = AppColors.LightPurpleGrey,
      shadowElevation = AppDimensions.elevationSmall) {
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(AppDimensions.paddingMedium)
                    .testTag("addFriendUserItem#${user.uid}")) {
              // Profile picture click listener to show enlarged picture
              ProfilePicture(
                  profilePictureUrl = user.profilePic, onClick = { onProfilePictureClick(user) })
              Spacer(modifier = Modifier.width(AppDimensions.smallWidth))
              Column {
                // Display user name
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = AppDimensions.smallPadding))
                // Display user bio, with ellipsis if it exceeds one line
                Text(
                    text = user.bio ?: "No bio available",
                    style = MaterialTheme.typography.bodySmall,
                    color = AppColors.secondaryTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis)
              }
            }
      }
}
