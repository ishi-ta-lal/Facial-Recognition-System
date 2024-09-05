package com.example.finaldetectionapp

import com.example.finaldetectionapp.ui.theme.FinalDetectionAppTheme

//import androidx.compose.foundation.layout.ColumnScopeInstance.align
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.finaldetectionapp.ui.theme.PhotoBottomSheetContent
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            if (!hasRequiredPermissions()) {
                ActivityCompat.requestPermissions(this, CAMERAX_PERMISSIONS, 0)
            }
            enableEdgeToEdge()
            setContent {
                FinalDetectionAppTheme {
                    MainScreen()
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error in onCreate", e)
            // Consider showing an error dialog to the user
        }
    }

    private fun hasRequiredPermissions(): Boolean {
        return CAMERAX_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                applicationContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    companion object {
        private val CAMERAX_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    }
}

@Composable
fun MainScreen() {

    val navController = rememberNavController()
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val viewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "pin_entry") {
        composable("pin_entry") {
            PinEntryScreen { isValid ->
                if (isValid) {
                    navController.navigate("registration")
                }
            }
        }
        composable("registration"){
            RegistrationScreen(
                onPhotoTaken = { bitmap ->
                    capturedBitmap = bitmap
                    navController.navigate("register_user")
                },
                navController = navController
            )
        }
        composable("register_user") {
            capturedBitmap?.let {bitmap ->
                RegisterUserScreen(
                    bitmap = bitmap,
                    onSave = { name, id, photoPath, boundingBoxes->
                        // Save the photo
                        //val viewModel: MainViewModel = viewModel()
                        viewModel.saveEmployee(name, id, photoPath, boundingBoxes)

                        // Navigate back to registration
                        navController.navigate("registration")

                    },
                    onBack = {
                        navController.navigate("registration")
                    }
                )
            }

        }
        composable("registered_employees") {
            val viewModel: MainViewModel = viewModel()
            RegisteredEmployeeScreen(viewModel = viewModel)
        }

    }
}

@Composable
fun RegistrationContent(onOpenCamera: () -> Unit, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Register Here", modifier = Modifier.padding(bottom = 20.dp))
        Button(onClick = onOpenCamera) {
            Text("Open Camera")
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("see registered employees", modifier = Modifier.padding(bottom = 20.dp))
        Button(onClick = { navController.navigate("registered_employees") }) {
            Text("go to employee list")
        }
    }
}

fun takePhoto(context: Context,
              controller: LifecycleCameraController,
              onPhotoTaken: (Bitmap) -> Unit
) {
    //val dbHelper = DatabaseHelper(context)
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)

                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0,
                    0,
                    image.width,
                    image.height,
                    matrix,
                    true
                )



                onPhotoTaken(rotatedBitmap)
            }

            override fun onError(exception: ImageCaptureException) {
                super.onError(exception)
                Log.e("Camera", "Couldn't take photo: ", exception)
            }
        }
    )
}

@Composable
@kotlin.OptIn(ExperimentalMaterial3Api::class)
fun camerafunc(onBack: () -> Unit, onPhotoTaken: (Bitmap) -> Unit) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember{
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE or CameraController.VIDEO_CAPTURE
            )
        }

    }
    val scope = rememberCoroutineScope()
    BackHandler {
        onBack()
    }

    val viewModel = viewModel<MainViewModel>()
    val photoPaths by viewModel.photoPaths.collectAsState()
    val bitmaps by viewModel.bitmaps.collectAsState()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 10.dp,
        sheetContent ={
            PhotoBottomSheetContent(
                bitmaps = bitmaps,
                modifier = Modifier
                    .fillMaxWidth())

        } ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CamPrev(
                controller = controller,
                modifier = Modifier
                    .fillMaxSize()
            )

            IconButton(
                onClick = {
                    controller.cameraSelector =
                        if (controller.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                            CameraSelector.DEFAULT_FRONT_CAMERA
                        } else CameraSelector.DEFAULT_BACK_CAMERA
                },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp, 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "Switch Camera"
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                IconButton(
                    onClick = {
                        scope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Photo,
                        contentDescription = "Open gallery"
                    )
                }
                IconButton(
                    onClick = {
                        takePhoto(
                            context = context,
                            controller = controller,
                            onPhotoTaken = onPhotoTaken
                        )
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Take photo"
                    )
                }
            }

        }
    }
}

@Composable
fun RegistrationScreen(onPhotoTaken: (Bitmap) -> Unit, navController: NavController) {
    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        camerafunc(onBack = { showCamera = false },
            onPhotoTaken = {bitmap ->
                showCamera = false
                onPhotoTaken(bitmap)
            })
    } else {
        //RegistrationContent(onOpenCamera = { showCamera = true })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            RegistrationContent(onOpenCamera = { showCamera = true }, navController = navController)

            Spacer(modifier = Modifier.height(20.dp))

            Button(onClick = { navController.navigate("registered_employees") }) {
                Text("View Registered Employees")
            }
        }
    }
}

@Composable
fun PinEntryScreen(onPinValidated: (Boolean) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Enter your PIN", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = pin,
            onValueChange = { pin = it },
            label = { Text("PIN") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val savedPin = "1234"  // Replace this with your actual saved PIN retrieval logic
            if (savedPin == pin) {
                onPinValidated(true)
            } else {
                error = true
            }
        }) {
            Text("Submit")
        }
        if (error) {
            Text(text = "Invalid PIN",
//                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun RegisteredEmployeeScreen(viewModel: MainViewModel){
    val employees by viewModel.employees.collectAsState(initial = emptyList())
    var employeeToDelete by remember { mutableStateOf<Employee1?>(null) }
    val showDialog = remember { mutableStateOf(false) }


    if (showDialog.value) {
        employeeToDelete?.let { employee ->
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = "Delete Employee") },
                text = { Text(text = "Are you sure you want to delete this employee?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteEmployee(employee)
                            showDialog.value = false
                        }
                    ) {
                        Text("Confirm")
                    }
                },
                dismissButton = {
                    Button(onClick = { showDialog.value = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
    LazyColumn{
        items(employees){
                employee->
            EmployeeItem(employee, onDeleteClick = {employeeToDelete = employee
                showDialog.value = true})

        }
    }
}

@Composable
fun EmployeeItem(employee: Employee1, onDeleteClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberAsyncImagePainter(model = employee.photoPath),
            contentDescription = "Employee photo",
            modifier = Modifier
                .height(64.dp) //it should be size
                .clip(CircleShape)
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(text = employee.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = "ID: ${employee.employeeId}", style = MaterialTheme.typography.bodyMedium)
        }
        IconButton(onClick = onDeleteClick) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Employee")
        }
    }
}

@Composable
fun CamPrev(
    controller: LifecycleCameraController,
    modifier: Modifier = Modifier
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(factory ={
        PreviewView(it).apply {
            this.controller = controller
            controller.bindToLifecycle(lifecycleOwner)
        }
    },
        modifier = modifier
    )
}