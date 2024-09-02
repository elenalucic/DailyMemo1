package hr.ferit.dailymemo

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun NoteDetailScreen(noteId: String, navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var note by remember { mutableStateOf<Pair<String, Long>?>(null) }
    var photoUrl by remember { mutableStateOf<String?>(null) }
    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(noteId) {
        db.collection("notes").document(noteId).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val text = document.getString("text") ?: ""
                    val date = document.getLong("date") ?: 0L
                    note = text to date
                    photoUrl = document.getString("photoUrl")
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val formattedDate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(
                                Date(date)
                            )
                            Text(
                                text = formattedDate,
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Light),
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { expanded = true }
                            ) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                offset = DpOffset(x = 200.dp, y = -10.dp)
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        db.collection("notes").document(noteId).delete()
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "Note deleted successfully", Toast.LENGTH_SHORT).show()
                                                navController.navigate("homeScreen") {
                                                    popUpTo("homeScreen") { inclusive = true }
                                                }
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(context, "Failed to delete note", Toast.LENGTH_SHORT).show()
                                            }
                                        expanded = false
                                    }
                                )

                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = text,
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Normal),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        photoUrl?.let {
                            AsyncImage(
                                model = it,
                                contentDescription = "Note Photo",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(20.dp))
                            )
                        }
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