//package com.example.finaldetectionapp
//
//import com.example.finaldetectionapp.Result
//import com.example.finaldetectionapp.BoundingBox
//
//import android.content.Context
//import android.graphics.Bitmap
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.material3.Button
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.finaldetectionapp.MainViewModel
////import com.google.mlkit.vision.objects.ObjectDetection
//import java.io.File
//import ai.onnxruntime.OnnxTensor
//import ai.onnxruntime.OrtEnvironment
//import ai.onnxruntime.OrtSession
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.RectF
//import androidx.compose.runtime.LaunchedEffect
//import java.io.ByteArrayInputStream
//import java.io.ByteArrayOutputStream
//import java.io.InputStream
//
//data class BoundingBox(
//    val coordinates: FloatArray,
//    val confidence: Float,
//    val landmarks: FloatArray,
//    val classNum: Float
//) {
//    override fun toString(): String {
//        return "BoundingBox(coordinates=${coordinates.joinToString(", ")}, confidence=$confidence, landmarks=${landmarks.joinToString(", ")})"
//    }
//}
////internal data class Result(
////    var outputBitmap: Bitmap,
////    var outputBoxes: List<BoundingBox>
////)
//@Composable
//fun RegisterUserScreen(
//    bitmap: Bitmap,
//    onSave: (String, String, String, List<BoundingBox>) -> Unit,
//    onBack: () -> Unit
//) {
//    var employeeName by remember { mutableStateOf("") }
//    var employeeId by remember { mutableStateOf("") }
//    val context = LocalContext.current
//    val ortEnv = remember { OrtEnvironment.getEnvironment() }
//    val ortSession by remember {
//        mutableStateOf(createOrtSession(context, ortEnv, "model.onnx"))
//    }
//
//    //val detectionResult = remember {mutableStateOf<Result?>(null)}
//    val processedBitmap = remember { mutableStateOf(bitmap) }
//    val boundingBoxes = remember { mutableStateOf(emptyList<BoundingBox>()) }
//
//    LaunchedEffect(bitmap) {
//        val inputStream = bitmapToInputStream(bitmap)
//
//        val detectionResult = ObjectDetection(context).detect(inputStream, ortEnv, ortSession!!)
////        val result = ortSession?.let { session ->
////            ObjectDetection(context).detect(inputStream, ortEnv, session)
////        }
//
//        processedBitmap.value = drawBoundingBox(bitmap, detectionResult?.outputBoxes ?: emptyList())
//        boundingBoxes.value = detectionResult?.outputBoxes ?: emptyList()
//        // Update detectionResult only if the result is non-null, or use a default value
//        //detectionResult.value = result ?: Result(bitmap, emptyList()) // Default to original bitmap and empty list
//    }
//
//    //val result = detectionResult.value
//    //val outputBitmap = result?.outputBitmap ?: bitmap
//    //val boundingBoxes = result?.outputBoxes ?: emptyList()
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(16.dp)
//    ) {
//        Text("Register User", style = MaterialTheme.typography.headlineMedium)
//
////        Image(
////            bitmap = bitmap.asImageBitmap(),
////            contentDescription = "Captured photo",
////            modifier = Modifier
////                .size(200.dp)
////                .align(Alignment.CenterHorizontally)
////        )
//
//        Image(
//            bitmap = processedBitmap.value.asImageBitmap(),
//            contentDescription = "Processed photo",
//            modifier = Modifier
//                .size(200.dp)
//                .align(Alignment.CenterHorizontally)
//        )
//
//        TextField(
//            value = employeeName,
//            onValueChange = { employeeName = it },
//            label = { Text("Employee Name") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        TextField(
//            value = employeeId,
//            onValueChange = { employeeId = it },
//            label = { Text("Employee ID") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Button(onClick = onBack) {
//                Text("Back")
//            }
//            Button(
//                onClick = {
//                    if (employeeName.isNotBlank() && employeeId.isNotBlank()) {
//                        // Start processing
//                        //isProcessing = true
//                        val photoPath = saveBitmapToFile(context, bitmap, "$employeeId.jpg")
//
//                        // Run object detection
//                        //val ortEnv = OrtEnvironment.getEnvironment()
//                        //val ortSession = ortEnv.createSession("model.onnx")
//                        //val inputStream = context.contentResolver.openInputStream(Uri.fromFile(File(filePath)))
//                        //val objectDetection = ObjectDetection(context)
//                        //detectionResult = objectDetection.detect(inputStream!!, ortEnv, ortSession)
//                        onSave(employeeName, employeeId, photoPath, boundingBoxes.value)
//                    }
//                },
//                enabled = employeeName.isNotBlank() && employeeId.isNotBlank()
//            ) {
//                Text("Save")
//            }
//        }
//    }
//
//}
//
//fun bitmapToInputStream(bitmap: Bitmap): InputStream {
//    val outputStream = ByteArrayOutputStream()
//    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//    return ByteArrayInputStream(outputStream.toByteArray())
//}
//
//fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): String {
//    val file = File(context.filesDir, filename)
//    file.outputStream().use {
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
//    }
//    return file.absolutePath
//}
//
//
//fun createOrtSession(context: Context, ortEnv: OrtEnvironment, modelName: String): OrtSession? {
//    return try {
//        val modelBytes = context.assets.open(modelName).readBytes()
//        ortEnv.createSession(modelBytes)
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}
//
//
//
//private fun drawBoundingBox(bitmap: Bitmap, boundingBoxes: List<BoundingBox>): Bitmap {
//    val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
//    val canvas = Canvas(outputBitmap)
//    val paint = Paint().apply {
//        style = Paint.Style.STROKE
//        color = Color.RED
//        strokeWidth = 3f
//    }
//    val landmarkPaint = Paint().apply {
//        style = Paint.Style.FILL
//        color = Color.BLUE
//        strokeWidth = 4f
//    }
//
//    boundingBoxes.forEach { box ->
//        val rect = RectF(
//            box.coordinates[0],
//            box.coordinates[1],
//            box.coordinates[2],
//            box.coordinates[3]
//        )
//        canvas.drawRect(rect, paint)
//
//        // Draw landmarks
//        for (i in box.landmarks.indices step 2) {
//            val cx = box.landmarks[i]
//            val cy = box.landmarks[i + 1]
//            canvas.drawCircle(cx, cy, 4f, landmarkPaint)
//        }
//    }
//
//    return outputBitmap
//}

package com.example.finaldetectionapp

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.google.mlkit.vision.objects.ObjectDetection
import kotlinx.serialization.Serializable
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

@Serializable
data class BoundingBox(
    val coordinates: FloatArray,
    val confidence: Float,
    val landmarks: FloatArray,
    val classNum: Float
) {
    override fun toString(): String {
        return "BoundingBox(coordinates=${coordinates.joinToString(", ")}, confidence=$confidence, landmarks=${landmarks.joinToString(", ")})"
    }
}

@Composable
fun RegisterUserScreen(
    bitmap: Bitmap,
    onSave: (String, String, String, List<BoundingBox>) -> Unit,
    onBack: () -> Unit
) {
    var employeeName by remember { mutableStateOf("") }
    var employeeId by remember { mutableStateOf("") }
    val context = LocalContext.current
    val ortEnv = remember { OrtEnvironment.getEnvironment() }
    val ortSession by remember {
        mutableStateOf(createOrtSession(context, ortEnv, "smallface.onnx"))
    }

    val processedBitmap = remember { mutableStateOf(bitmap) }
    val boundingBoxes = remember { mutableStateOf(emptyList<BoundingBox>()) }

    LaunchedEffect(bitmap) {
        Log.d("RegisterUserScreen", "LaunchedEffect triggered with new bitmap")
        val inputStream = bitmapToInputStream(bitmap)

        val detectionResult = ObjectDetection(context).detect(inputStream, ortEnv, ortSession!!)
        Log.d("RegisterUserScreen", "Object detection result: $detectionResult")

        processedBitmap.value = drawBoundingBox(bitmap, detectionResult?.outputBoxes ?: emptyList())
        boundingBoxes.value = detectionResult?.outputBoxes ?: emptyList()
        Log.d("RegisterUserScreen", "Processed bitmap and bounding boxes updated")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Register User", style = MaterialTheme.typography.headlineMedium)

        Image(
            bitmap = processedBitmap.value.asImageBitmap(),
            contentDescription = "Processed photo",
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.CenterHorizontally)
        )

        TextField(
            value = employeeName,
            onValueChange = { employeeName = it },
            label = { Text("Employee Name") },
            modifier = Modifier.fillMaxWidth()
        )

        TextField(
            value = employeeId,
            onValueChange = { employeeId = it },
            label = { Text("Employee ID") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onBack) {
                Text("Back")
            }
            Button(
                onClick = {
                    if (employeeName.isNotBlank() && employeeId.isNotBlank()) {
                        val photoPath = saveBitmapToFile(context, bitmap, "$employeeId.jpg")
                        Log.d("RegisterUserScreen", "Photo saved to: $photoPath")

                        onSave(employeeName, employeeId, photoPath, boundingBoxes.value)
                    }
                },
                enabled = employeeName.isNotBlank() && employeeId.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}

fun bitmapToInputStream(bitmap: Bitmap): InputStream {
    Log.d("Utility", "Converting bitmap to InputStream")
    val outputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return ByteArrayInputStream(outputStream.toByteArray())
}

fun saveBitmapToFile(context: Context, bitmap: Bitmap, filename: String): String {
    Log.d("Utility", "Saving bitmap to file: $filename")
    val file = File(context.filesDir, filename)
    file.outputStream().use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
    }
    Log.d("Utility", "Bitmap saved to ${file.absolutePath}")
    return file.absolutePath
}

fun createOrtSession(context: Context, ortEnv: OrtEnvironment, modelName: String): OrtSession? {
    return try {
        Log.d("ObjectDetection", "Creating OrtSession for model: $modelName")
        val modelBytes = context.assets.open(modelName).readBytes()
        ortEnv.createSession(modelBytes).also {
            Log.d("ObjectDetection", "OrtSession created successfully")
        }
    } catch (e: Exception) {
        Log.e("ObjectDetection", "Failed to create OrtSession", e)
        null
    }
}

private fun drawBoundingBox(bitmap: Bitmap, boundingBoxes: List<BoundingBox>): Bitmap {
    Log.d("Utility", "Drawing bounding boxes: ${boundingBoxes.size} boxes")
    val outputBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(outputBitmap)
    val paint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.RED
        strokeWidth = 3f
    }
    val landmarkPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.BLUE
        strokeWidth = 4f
    }

    boundingBoxes.forEach { box ->
        val rect = RectF(
            box.coordinates[0],
            box.coordinates[1],
            box.coordinates[2],
            box.coordinates[3]
        )
        canvas.drawRect(rect, paint)
        Log.d("Utility", "Drew bounding box: $rect")

        // Draw landmarks
        for (i in box.landmarks.indices step 2) {
            val cx = box.landmarks[i]
            val cy = box.landmarks[i + 1]
            canvas.drawCircle(cx, cy, 4f, landmarkPaint)
            Log.d("Utility", "Drew landmark at: ($cx, $cy)")
        }
    }

    Log.d("Utility", "Bounding boxes drawn successfully")
    return outputBitmap
}
