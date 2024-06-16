import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.runpath.database.SessionManager
import com.example.runpath.ui.theme.ProfileAndCommunity.DiscoverPage
import com.example.runpath.ui.theme.ProfileAndCommunity.GeneralPage

@Composable
fun CommunityPage(navController: NavController, sessionManager: SessionManager) {
    val sharedPreferences = sessionManager.getsharedPreferences()
    val username = sharedPreferences.getString("username", "N/A") ?: "N/A"
    val userId = sharedPreferences.getString("user_id", "N/A")

    val tabTitles = listOf("General", "Discover")
    var selectedTabIndex by remember { mutableStateOf(0) }

    Column {
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }) {
                    Text(title, style = MaterialTheme.typography.h6, modifier = Modifier.padding(10.dp))
                }
            }
        }

        when (selectedTabIndex) {
            0 -> userId?.let { GeneralPage(it, username, navController) }
            1 -> userId?.let { DiscoverPage(it, navController) }
        }
    }
}