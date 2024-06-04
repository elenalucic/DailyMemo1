package hr.ferit.dailymemo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun NoteDetailScreen(noteId: String, navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var note by remember { mutableStateOf<Pair<String, Long>?>(null) }

    LaunchedEffect(noteId) {
        db.collection("notes").document(noteId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val text = document.getString("text") ?: ""
                    val date = document.getLong("date") ?: 0L
                    note = text to date
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load note", Toast.LENGTH_SHORT).show()
            }
    }

    Surface(
        color = Color.LightGray,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
            Spacer(modifier = Modifier.height(16.dp))
            note?.let { (text, date) ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(16.dp)
                ) {
                    Column {
                        val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(
                            Date(date)
                        )
                        Text(
                            text = formattedDate,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Light),
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = text,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("homeScreen") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(Color.Gray)
            ) {
                Text("DONE", color = Color.White)
            }
        }
    }
}