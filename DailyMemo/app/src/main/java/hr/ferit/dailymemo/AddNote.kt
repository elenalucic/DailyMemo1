package hr.ferit.dailymemo

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(navController: NavController) {
    var noteText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val user = FirebaseAuth.getInstance().currentUser

    Surface(
        color = Color.LightGray,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Gray)
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MY DAILY MEMO",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 5.dp)
                )
            }
            Spacer(modifier = Modifier.padding(10.dp))

            Box( modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp) )
            {
                    BasicTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        singleLine = false,
                        textStyle = TextStyle(fontSize = 15.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .padding(horizontal = 16.dp),
                    )
                    if (noteText.isEmpty()) {
                        Text(
                            text = "Add text...",
                            modifier = Modifier.align(Alignment.TopStart)
                                .padding(horizontal = 16.dp),
                            style = TextStyle(fontSize = 15.sp),
                        )
                    }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (user != null) {
                            saveNoteToFirebase(context, db.collection("notes"), noteText, user.uid, navController)
                        } else {
                            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(Color.Gray)
                ) {
                    Text("Save")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .padding(8.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(Color.Gray)

                ) {
                    Text("Back")
                }
            }
        }
    }
}



fun saveNoteToFirebase(context: Context, notesCollection: CollectionReference, noteText: String, userId: String, navController: NavController) {
    if (noteText.isNotEmpty()) {
        val note = hashMapOf(
            "text" to noteText,
            "date" to System.currentTimeMillis(),
            "userId" to userId
        )
        notesCollection.add(note)
            .addOnSuccessListener {
                Toast.makeText(context, "Note saved successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("homeScreen") {
                    popUpTo("homeScreen") { inclusive = true }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to save note", Toast.LENGTH_SHORT).show()
            }
    } else {
        Toast.makeText(context, "Note cannot be empty", Toast.LENGTH_SHORT).show()
    }
}
