package com.github.se.orator

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.se.orator.model.apiLink.ApiLinkViewModel
import com.github.se.orator.model.chatGPT.ChatViewModel
import com.github.se.orator.model.profile.UserProfileViewModel
import com.github.se.orator.model.speaking.InterviewContext
import com.github.se.orator.model.speaking.PracticeContext
import com.github.se.orator.model.speaking.PublicSpeakingContext
import com.github.se.orator.model.speaking.SalesPitchContext
import com.github.se.orator.model.symblAi.SpeakingRepository
import com.github.se.orator.model.symblAi.SpeakingViewModel
import com.github.se.orator.ui.authentification.SignInScreen
import com.github.se.orator.ui.friends.AddFriendsScreen
import com.github.se.orator.ui.friends.LeaderboardScreen
import com.github.se.orator.ui.friends.ViewFriendsScreen
import com.github.se.orator.ui.navigation.NavigationActions
import com.github.se.orator.ui.navigation.Route
import com.github.se.orator.ui.navigation.Screen
import com.github.se.orator.ui.network.ChatGPTService
import com.github.se.orator.ui.network.createChatGPTService
import com.github.se.orator.ui.overview.ChatScreen
import com.github.se.orator.ui.overview.FeedbackScreen
import com.github.se.orator.ui.overview.SpeakingJobInterviewModule
import com.github.se.orator.ui.overview.SpeakingPublicSpeaking
import com.github.se.orator.ui.overview.SpeakingSalesPitchModule
import com.github.se.orator.ui.profile.CreateAccountScreen
import com.github.se.orator.ui.profile.EditProfileScreen
import com.github.se.orator.ui.profile.ProfileScreen
import com.github.se.orator.ui.screens.ViewConnectScreen
import com.github.se.orator.ui.screens.ViewFunScreen
import com.github.se.orator.ui.settings.SettingsScreen
import com.github.se.orator.ui.speaking.SpeakingScreen
import com.github.se.orator.ui.theme.ProjectTheme
import com.github.se.orator.ui.theme.mainScreen.MainScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.JsonParser

/** The MainActivity class is the main entry point for the OratorAI application. */
class MainActivity : ComponentActivity() {
  private lateinit var auth: FirebaseAuth
  private lateinit var chatViewModel: ChatViewModel

  @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize Firebase Auth
    auth = FirebaseAuth.getInstance()
    auth.currentUser?.let {
      // Sign out the user if they are already signed in
      // This is useful for testing purposes
      auth.signOut()
    }

    val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
    val apiKey = appInfo.metaData.getString("GPT_API_KEY")
    val organizationId = appInfo.metaData.getString("GPT_ORGANIZATION_ID")

    // Ensure apiKey and organizationId are not null
    requireNotNull(apiKey) { "GPT API Key is missing in the manifest" }
    requireNotNull(organizationId) { "GPT Organization ID is missing in the manifest" }

    val chatGPTService = createChatGPTService(apiKey, organizationId)

    //    val factory = ChatViewModelFactory(chatGPTService)
    //    chatViewModel = ViewModelProvider(this, factory).get(ChatViewModel::class.java)

    //    val interviewContext =
    //        InterviewContext(
    //            interviewType = "job interview",
    //            role = "Consultant",
    //            company = "McKinsey",
    //            focusAreas = listOf("Problem-solving", "Leadership", "Teamwork"))
    //
    //    chatViewModel.initializeConversation(interviewContext)

    enableEdgeToEdge()
    setContent {
      ProjectTheme { Scaffold(modifier = Modifier.fillMaxSize()) { OratorApp(chatGPTService) } }
    }
  }
}

/**
 * The OratorApp composable is the main entry point for the OratorAI application.
 *
 * @param chatGPTService The ChatGPTService instance used for chat conversations.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun OratorApp(chatGPTService: ChatGPTService) {

  // Main layout using a Scaffold
  Scaffold(modifier = Modifier.fillMaxSize()) {

    // Initialize the navigation controller
    val navController = rememberNavController()
    val navigationActions = NavigationActions(navController)

    // Initialize the view models
    val userProfileViewModel: UserProfileViewModel =
        viewModel(factory = UserProfileViewModel.Factory)
      val apiLinkViewModel: ApiLinkViewModel = viewModel(factory = ApiLinkViewModel.Factory)

    // Replace the content of the Scaffold with the desired screen
    NavHost(navController = navController, startDestination = Route.AUTH) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.AUTH,
      ) {

        // Authentication Flow
        composable(Screen.AUTH) { SignInScreen(navigationActions, userProfileViewModel) }

        // Profile Creation Flow
        composable(Screen.CREATE_PROFILE) {
          CreateAccountScreen(navigationActions, userProfileViewModel)
        }
      }

      navigation(
          startDestination = Screen.HOME,
          route = Route.HOME,
      ) {
        composable(Screen.HOME) { MainScreen(navigationActions) }

        composable(Screen.SPEAKING_JOB_INTERVIEW) { SpeakingJobInterviewModule(navigationActions) }
        composable(Screen.SPEAKING_PUBLIC_SPEAKING) { SpeakingPublicSpeaking(navigationActions) }
        composable(Screen.SPEAKING_SALES_PITCH) { SpeakingSalesPitchModule(navigationActions) }
        composable(Screen.SPEAKING) { backStackEntry ->
          val context = LocalContext.current
          val repository = SpeakingRepository(context) // Adjust as needed
          val factory = SpeakingViewModel.SpeakingViewModelFactory(repository)
          val speakingViewModel: SpeakingViewModel = viewModel(factory = factory)
          SpeakingScreen(viewModel = speakingViewModel, navController = navController)
        }
        composable(
            route = "${Screen.CHAT_SCREEN}/{practiceContext}/{feedbackType}",
            arguments =
                listOf(
                    navArgument("practiceContext") { type = NavType.StringType },
                    navArgument("feedbackType") { type = NavType.StringType })) { backStackEntry ->
              val contextJson = backStackEntry.arguments?.getString("practiceContext")
              val feedbackType = backStackEntry.arguments?.getString("feedbackType") ?: ""
              val decodedJson = Uri.decode(contextJson)

              val gson = Gson()

              // Parse the JSON string into a JsonObject
              val jsonObject = JsonParser.parseString(decodedJson).asJsonObject
              // Deserialize based on the type
              val practiceContext: PracticeContext =
                  when (val type = jsonObject.get("type").asString) {
                    "InterviewContext" -> gson.fromJson(decodedJson, InterviewContext::class.java)
                    "PublicSpeakingContext" ->
                        gson.fromJson(decodedJson, PublicSpeakingContext::class.java)
                    "SalesPitchContext" -> gson.fromJson(decodedJson, SalesPitchContext::class.java)
                    else -> throw IllegalArgumentException("Unknown PracticeContext type: $type")
                  }

              // Initialize ChatViewModel with the practiceContext and feedbackType

              val chatViewModel: ChatViewModel = ChatViewModel(chatGPTService, practiceContext, feedbackType, apiLinkViewModel)

              ChatScreen(
                    navigationActions = navigationActions,
                    chatViewModel = chatViewModel,
                  )
            }
        composable(Screen.FEEDBACK) {

          // Retrieve the same ChatViewModel instance
          val chatViewModel: ChatViewModel = viewModel(navController.previousBackStackEntry!!)

          // Navigate to FeedbackScreen
          FeedbackScreen(
              chatViewModel = chatViewModel,
              navController = navController,
              navigationActions = navigationActions)
        }
      }

      navigation(
          startDestination = Screen.FRIENDS,
          route = Route.FRIENDS,
      ) {
        composable(Screen.FRIENDS) { ViewFriendsScreen(navigationActions, userProfileViewModel) }
      }
      //// temporarily adding those empty screens before we implement their functionalities
      composable(Screen.FUN_SCREEN) {
        ViewFunScreen(
            navigationActions, userProfileViewModel) // Your composable function for Fun Screen
      }
      composable(Screen.CONNECT_SCREEN) {
        ViewConnectScreen(
            navigationActions, userProfileViewModel) // Your composable function for Connect Screen
      }

      navigation(
          startDestination = Screen.PROFILE,
          route = Route.PROFILE,
      ) {
        composable(Screen.PROFILE) { ProfileScreen(navigationActions, userProfileViewModel) }
        composable(Screen.CREATE_PROFILE) {
          CreateAccountScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.EDIT_PROFILE) {
          EditProfileScreen(navigationActions, userProfileViewModel)
        }

        composable(Screen.LEADERBOARD) {
          LeaderboardScreen(navigationActions, userProfileViewModel)
        }
        composable(Screen.ADD_FRIENDS) { AddFriendsScreen(navigationActions, userProfileViewModel) }
        composable(Screen.SETTINGS) { SettingsScreen(navigationActions, userProfileViewModel) }
      }
    }
  }
}
