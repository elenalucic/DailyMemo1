package hr.ferit.dailymemo
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun AddNoteScreen(navController: NavController) {
    var noteText by rememberSaveable { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val user = FirebaseAuth.getInstance().currentUser


    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    val photoFile = remember { createImageFile() }
    val photoFileUri = remember { FileProvider.getUriForFile(context, "${context.packageName}.provider", photoFile) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = photoFileUri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(photoFileUri)
        } else {
            Toast.makeText(context, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }

    Surface(
        color = Color.LightGray,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    BasicTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        singleLine = false,
                        textStyle = TextStyle(fontSize = 15.sp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp),
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.fillMaxWidth()) {
                                if (noteText.isEmpty()) {
                                    Text(
                                        text = "Add text...",
                                        style = TextStyle(fontSize = 15.sp, color = Color.Gray),
                                        modifier = Modifier.padding(horizontal = 5.dp)
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    photoUri?.let { uri ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Captured Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(330.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Fit
                            )

                            IconButton(
                                onClick = { photoUri = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-25).dp, y = (-15).dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Remove Image", tint = Color.Red)
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        when (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.CAMERA
                        )) {
                            PackageManager.PERMISSION_GRANTED -> {
                                cameraLauncher.launch(photoFileUri)
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(5.dp)
                ) {
                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Take Photo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if (user != null) {
                            saveNoteToFirebase(
                                context = context,
                                notesCollection = db.collection("notes"),
                                noteText = noteText,
                                userId = user.uid,
                                photoUri = photoUri,
                                storage = storage,
                                navController = navController
                            )
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

fun saveNoteToFirebase(
    context: Context,
    notesCollection: CollectionReference,
    noteText: String,
    userId: String,
    photoUri: Uri?,
    storage: FirebaseStorage,
    navController: NavController,
) {
    if (noteText.isNotEmpty()) {
        val note = hashMapOf(
            "text" to noteText,
            "date" to System.currentTimeMillis(),
            "userId" to userId
        )
        notesCollection.add(note)
            .addOnSuccessListener { documentReference ->
                val noteId = documentReference.id
                if (photoUri != null) {
                    val storageRef = storage.reference.child("notes/$userId/$noteId.jpg")
                    val uploadTask = storageRef.putFile(photoUri)

                    uploadTask.addOnSuccessListener {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            notesCollection.document(noteId).update("photoUrl", uri.toString())
                                .addOnSuccessListener {
                                    navController.navigate("homeScreen") {
                                        popUpTo("homeScreen") { inclusive = true }
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to update note with photo URL: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to get photo URL: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to upload photo: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    navController.navigate("homeScreen") {
                        popUpTo("homeScreen") { inclusive = true }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Failed to save note: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    } else {
        Toast.makeText(context, "Note cannot be empty", Toast.LENGTH_SHORT).show()
    }
}



