package hr.ferit.dailymemo

import android.content.Context
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hr.ferit.dailymemo.ui.theme.DailyMemoTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            NavHost(navController = navController, startDestination = "loginScreen") {
                composable("homeScreen") {
                    HomeScreen(navController = navController)
                }
                composable("addNoteScreen") {
                    AddNoteScreen(navController = navController)
                }
                composable("loginScreen") {
                    LoginScreen(navController = navController)
                }
                composable("registerScreen") {
                    RegisterScreen(navController = navController)
                }
                composable("noteDetailScreen/{noteId}") { backStackEntry ->
                    val noteId = backStackEntry.arguments?.getString("noteId") ?: ""
                    NoteDetailScreen(noteId = noteId, navController = navController)
                }
            }
        }
    }
}


@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val notesCollection = db.collection("notes")
    var notes by remember { mutableStateOf(listOf<Triple<String, Long, String>>()) }

    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(userId) {
        if (userId != null) {
            notesCollection
                .whereEqualTo("userId", userId)
                .orderBy("date", Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        return@addSnapshotListener
                    }
                    val noteList = mutableListOf<Triple<String, Long, String>>()
                    for (document in snapshot!!) {
                        val text = document.getString("text") ?: ""
                        val date = document.getLong("date") ?: 0L
                        val id = document.id
                        noteList.add(Triple(text, date, id))
                    }
                    notes = noteList
                }
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
            Text(
                text = "MY DAILY MEMO",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val groupedNotes = notes.groupBy { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.second)) }

            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedNotes.forEach { (date, notes) ->
                    item {
                        DateHeader(date)
                    }
                    items(notes) { note ->
                        NoteItem(note = note, navController = navController)
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            FloatingActionButton(
                onClick = { navController.navigate("addNoteScreen") },
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }
}


@Composable
fun DateHeader(date: String) {
    Text(
        text = date,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(Color.Gray)
            .padding(8.dp),
        textAlign = TextAlign.Center
    )
}

@Composable
fun NoteItem(note: Triple<String, Long, String>, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(15.dp)
            .clickable { navController.navigate("noteDetailScreen/${note.third}") }
    ) {
        Column {
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(note.second)),
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                text = note.first,
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

        }
    }
}







@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    DailyMemoTheme {
        HomeScreen(navController = rememberNavController())
    }
}