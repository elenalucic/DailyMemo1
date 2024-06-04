package hr.ferit.dailymemo

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Icon") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { signIn(context, email, password, navController) },
            colors = ButtonDefaults.buttonColors(Color.Gray)

        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextWithLink(
            normalText = "Don't have an account? ",
            linkText = "Register here",
            onClick = { navController.navigate("registerScreen") }
        )
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = "Email Icon") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = "Lock Icon") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { register(context, email, password, firstName, lastName, navController) } ,
            colors = ButtonDefaults.buttonColors(Color.Gray)

        )
        {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(8.dp))
        TextWithLink(
            normalText = "Already have an account? ",
            linkText = "Go to Login",
            onClick = { navController.navigate("loginScreen") }
        )
    }
}

@Composable
fun TextWithLink(normalText: String, linkText: String, onClick: () -> Unit) {
    val annotatedString = buildAnnotatedString {
        append(normalText)
        pushStringAnnotation(tag = "URL", annotation = linkText)
        withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
            append(linkText)
        }
        pop()
    }
    androidx.compose.foundation.text.ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { _ ->
                    onClick()
                }
        }
    )
}

private fun register(context: Context, email: String, password: String, firstName: String, lastName: String, navController: NavController) {
    if (email.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
        Toast.makeText(context, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
        return
    }

    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    saveUserToFirestore(userId, firstName, lastName, email)
                }
                Toast.makeText(context, "Registered successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("homeScreen")
            } else {
                Toast.makeText(context, "Registration failed", Toast.LENGTH_SHORT).show()
            }
        }
}

private fun saveUserToFirestore(userId: String, firstName: String, lastName: String, email: String) {
    val db = FirebaseFirestore.getInstance()
    val user = hashMapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "email" to email
    )
    db.collection("users").document(userId).set(user)
}

fun signIn(context: Context, email: String, password: String, navController: NavController) {
    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
        return
    }

    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate("homeScreen")
            } else {
                Toast.makeText(context, "Login failed", Toast.LENGTH_SHORT).show()
            }
        }
}