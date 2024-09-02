package hr.ferit.dailymemo

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import hr.ferit.dailymemo.ui.theme.DailyMemoTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        scheduleDailyReminder()

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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Memo Channel"
            val descriptionText = "Channel for daily memo reminders"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("daily_memo_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleDailyReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("notificationId", 1)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 32)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}


@Composable
fun HomeScreen(navController: NavController) {
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "MY DAILY MEMO",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 5.dp)
                )

            }

            val groupedNotes = notes.groupBy {
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.second))
            }

            val listState = rememberLazyListState()

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                groupedNotes.forEach { (monthYear, notesInMonth) ->
                    item {
                        MonthYearHeader(monthYear)
                    }
                    val dailyGroupedNotes = notesInMonth.groupBy {
                        SimpleDateFormat("dd", Locale.getDefault()).format(Date(it.second))
                    }

                    dailyGroupedNotes.forEach { (day, notesInDay) ->
                        item {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                DateVerticalHeader(day)

                                Column(modifier = Modifier.weight(1f)) {
                                    notesInDay.forEach { note ->
                                        NoteItem(note = note, navController = navController)
                                    }
                                }
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.Gray)
                            )
                        }
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
fun MonthYearHeader(monthYear: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .padding(3.dp)
    ) {
        Text(
            text = monthYear,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


@Composable
fun DateVerticalHeader(day: String) {
    Text(
        text = day,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            color = Color.Black
        ),
        modifier = Modifier
            .padding(end = 8.dp)
            .background(Color.LightGray)
            .padding(8.dp)
            .width(30.dp),
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