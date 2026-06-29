package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.HospitalResource
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockResources = listOf(
      HospitalResource("icu_beds", "ICU Bed Capacity", 35, 50, "Beds", "ICU Beds"),
      HospitalResource("ventilators", "Ventilator Usage", 12, 25, "Units", "Ventilators"),
      HospitalResource("oxygen_supply", "Oxygen Supply Level", 6500, 10000, "Liters", "Oxygen Supply")
    )

    composeTestRule.setContent { 
      MyApplicationTheme { 
        DashboardScreen(
          resources = mockResources,
          onUpdateResource = { _, _, _ -> }
        ) 
      } 
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
