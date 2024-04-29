import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.runpath.database.SessionManager
import com.example.runpath.database.UserDAO


@Composable
fun LoginPage(navController: NavController, dbHelper: FeedReaderDbHelper) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val dbHelper = FeedReaderDbHelper(context = LocalContext.current)
    val userDAO = UserDAO(context = LocalContext.current, dbHelper = dbHelper)
    var showErrorDialog by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    )
    {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Login Account", fontSize = 30.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(80.dp))

            // campul pentru username
            BasicTextField(
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(Color.LightGray)
                            .padding(8.dp)
                            .fillMaxWidth()
                    ) {
                        if (username.isEmpty()) {
                            Text("Username", color = Color.Gray)
                        }
                        innerTextField()
                    }

                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // campul pentru parola
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .background(Color.LightGray)
                            .padding(8.dp)
                            .fillMaxWidth()

                    )
                    {
                        if (password.isEmpty()) {
                            Text("Password", color = Color.Gray)
                        }
                        innerTextField()
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

            )


            // buton de login


            Spacer(modifier = Modifier.height(16.dp))
            val sessionManager = SessionManager(context = LocalContext.current)

            Button(
                onClick = {
                    val userId = userDAO.login(username, password)
                    if (userId != -1) {
                        //daca loginul este reusit, se creeaza o sesiune
                        val user = userDAO.getUserById(userId)
                        println(user)
                        sessionManager.createSession(userId, user.username, user.email, user.dateCreated)
                        navController.navigate("mainInterface")
                    } else {
                        showErrorDialog = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = username.isNotEmpty() && password.isNotEmpty()
            ) {
                Text("Login")
            }
            if (showErrorDialog) {
                AlertDialog(
                    onDismissRequest = { showErrorDialog = false },
                    title = { Text("Error") },
                    text = { Text("Username or password is incorrect") },
                    confirmButton = {
                        Button(
                            onClick = { showErrorDialog = false }
                        ) {
                            Text("OK")
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { /* functia pentru a uita parola */ }
            ) {
                Text("Ai uitat parola?", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = { navController.navigate("registerPage") },
            ) {
                Text("Register", color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}
