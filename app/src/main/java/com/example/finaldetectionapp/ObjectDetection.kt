package com.example.finaldetectionapp

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import java.io.InputStream
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min

data class Result(
    var outputBitmap: Bitmap,
    var outputBoxes: List<BoundingBox>
)

internal class ObjectDetection(private val context: Context) {

    fun detect(inputStream: InputStream?, ortEnv: OrtEnvironment, ortSession: OrtSession): Result? {
        try {
            Log.d("ObjectDetection", "Starting detection")

            // Check if the inputStream is null
            if (inputStream == null) {
                Log.e("ObjectDetection", "InputStream is null")
                return null
            }

            val rawImageBytes = inputStream.readBytes()
            if (rawImageBytes.isEmpty()) {
                Log.e("ObjectDetection", "Image bytes are empty")
                return null
            }

            val bitmap = BitmapFactory.decodeByteArray(rawImageBytes, 0, rawImageBytes.size)
            if (bitmap == null) {
                Log.e("ObjectDetection", "Failed to decode bitmap, bitmap is null")
                return null
            }
            Log.d("ObjectDetection", "Bitmap decoded successfully: ${bitmap.width}x${bitmap.height}")

            val preprocessedImage = preprocessImage(bitmap)
            val expectedSize = 3 * 640 * 640
            if (preprocessedImage.capacity() != expectedSize) {
                Log.e("ObjectDetection", "Preprocessed image size mismatch: expected $expectedSize, got ${preprocessedImage.capacity()}")
                return null
            }

            preprocessedImage.rewind()
            val shape = longArrayOf(1, 3, 640, 640)
            val inputTensor = OnnxTensor.createTensor(ortEnv, preprocessedImage, shape)
            Log.d("ObjectDetection", "Input tensor created successfully")

            val output = ortSession.run(
                mapOf("input" to inputTensor),
                setOf("output")
            )
            Log.d("ObjectDetection", "Model run completed")

            output.use {
                val rawOutput = output[0].value
                if (rawOutput is Array<*>) {
                    val rawOutputArray = rawOutput as Array<Array<FloatArray>>
                    val bestBox = processOutput(rawOutputArray, bitmap.width, bitmap.height) ?: return null

                    Log.d("ObjectDetection", "Best box found: $bestBox")

                    val outputBitmap = drawBoundingBox(bitmap, listOf(bestBox))
                    return Result(outputBitmap, listOf(bestBox))
                } else {
                    Log.e("ObjectDetection", "Model output is not of the expected type: ${rawOutput?.javaClass?.name}")
                    return null
                }
            }
        } catch (e: Exception) {
            Log.e("ObjectDetection", "Error detecting objects", e)
            return null
        }
    }

    private fun preprocessImage(bitmap: Bitmap): FloatBuffer {
        Log.d("ObjectDetection", "Preprocessing image: ${bitmap.width}x${bitmap.height}")
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 640, 640, true)
        val floatBuffer = FloatBuffer.allocate(3 * 640 * 640)

        val intValues = IntArray(640 * 640)
        resizedBitmap.getPixels(
            intValues,
            0,
            resizedBitmap.width,
            0,
            0,
            resizedBitmap.width,
            resizedBitmap.height
        )

        intValues.forEach { value ->
            floatBuffer.put((value shr 16 and 0xFF) / 255.0f)
        }
        intValues.forEach { value ->
            floatBuffer.put((value shr 8 and 0xFF) / 255.0f)
        }
        intValues.forEach { value ->
            floatBuffer.put((value and 0xFF) / 255.0f)
        }

        Log.d("ObjectDetection", "Image preprocessing complete")
        floatBuffer.rewind()
        return floatBuffer
    }

    private fun drawBoundingBox(bitmap: Bitmap, boundingBoxes: List<BoundingBox>): Bitmap {
        Log.d("ObjectDetection", "Drawing bounding boxes: ${boundingBoxes.size} boxes")
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
            Log.d("ObjectDetection", "Drew bounding box: $rect")

            // Draw landmarks
            for (i in box.landmarks.indices step 2) {
                val cx = box.landmarks[i]
                val cy = box.landmarks[i + 1]
                canvas.drawCircle(cx, cy, 4f, landmarkPaint)
                Log.d("ObjectDetection", "Drew landmark at: ($cx, $cy)")
            }
        }

        Log.d("ObjectDetection", "Bounding boxes drawn successfully")
        return outputBitmap
    }

    private fun processOutput(
        rawOutput: Array<Array<FloatArray>>,
        originalWidth: Int,
        originalHeight: Int
    ): BoundingBox? {
        Log.d("ObjectDetection", "Processing output with original dimensions: $originalWidth x $originalHeight")

        val boxes = mutableListOf<BoundingBox>()
        rawOutput[0].forEach { output ->
            if (output[4] > 0.6f) {
                val (centerX, centerY, width, height) = output.slice(0..3)
                val left = centerX - width / 2
                val top = centerY - height / 2
                val right = centerX + width / 2
                val bottom = centerY + height / 2

                val box = BoundingBox(
                    coordinates = floatArrayOf(left, top, right, bottom),
                    confidence = output[4],
                    landmarks = output.sliceArray(5..14),
                    classNum = output[15]
                )
                boxes.add(box)
                Log.d("ObjectDetection", "Box added: $box")
            }
        }

        Log.d("ObjectDetection", "Detected ${boxes.size} boxes before NMS")

        val filteredBoxes = nonMaxSuppressionFace(boxes, 0.6f, 0.5f)
        Log.d("ObjectDetection", "Filtered boxes after NMS: ${filteredBoxes.size}")

        val bestBox = filteredBoxes.maxByOrNull { it.confidence } ?: return null

        val gain = min(640.0f / originalWidth, 640.0f / originalHeight)
        val pad = floatArrayOf((640 - originalWidth * gain) / 2, (640 - originalHeight * gain) / 2)
        scaleCoords(bestBox.coordinates, gain, pad)
        scaleCoordsLandmarks(bestBox.landmarks, gain, pad)
        clipCoords(bestBox.coordinates, originalWidth, originalHeight)

        Log.d("ObjectDetection", "Best box after scaling and clipping: $bestBox")
        return bestBox
    }

    private fun nonMaxSuppressionFace(
        predictions: List<BoundingBox>,
        confThreshold: Float,
        iouThreshold: Float
    ): List<BoundingBox> {
        Log.d("ObjectDetection", "Running non-max suppression with thresholds: conf=$confThreshold, iou=$iouThreshold")

        val filteredBoxes = predictions.filter { it.confidence > confThreshold }
        if (filteredBoxes.isEmpty()) {
            Log.d("ObjectDetection", "No boxes left after confidence thresholding")
            return emptyList()
        }

        val selectedBoxes = mutableListOf<BoundingBox>()
        val boxes = filteredBoxes.map {
            RectF(
                it.coordinates[0],
                it.coordinates[1],
                it.coordinates[2],
                it.coordinates[3]
            )
        }
        val scores = filteredBoxes.map { it.confidence }

        val indices = nms(boxes, scores, iouThreshold)
        indices.maxByOrNull { filteredBoxes[it].confidence }
            ?.let { selectedBoxes.add(filteredBoxes[it]) }

        Log.d("ObjectDetection", "Boxes selected after NMS: ${selectedBoxes.size}")
        return selectedBoxes
    }

    private fun nms(boxes: List<RectF>, scores: List<Float>, iouThreshold: Float): List<Int> {
        Log.d("ObjectDetection", "Performing NMS on ${boxes.size} boxes")

        val sortedIndices = scores.indices.sortedByDescending { scores[it] }.toMutableList()
        val selectedIndices = mutableListOf<Int>()

        while (sortedIndices.isNotEmpty()) {
            val currentIndex = sortedIndices.removeAt(0)
            selectedIndices.add(currentIndex)
            val currentBox = boxes[currentIndex]

            sortedIndices.removeAll { index ->
                calculateIoU(currentBox, boxes[index]) > iouThreshold
            }
        }

        Log.d("ObjectDetection", "NMS complete, selected ${selectedIndices.size} indices")
        return selectedIndices
    }

    private fun calculateIoU(box1: RectF, box2: RectF): Float {
        val intersectLeft = max(box1.left, box2.left)
        val intersectTop = max(box1.top, box2.top)
        val intersectRight = min(box1.right, box2.right)
        val intersectBottom = min(box1.bottom, box2.bottom)

        val intersectArea = max(0f, intersectRight - intersectLeft) * max(0f, intersectBottom - intersectTop)
        val box1Area = (box1.right - box1.left) * (box1.bottom - box1.top)
        val box2Area = (box2.right - box2.left) * (box2.bottom - box2.top)

        val iou = intersectArea / (box1Area + box2Area - intersectArea)
        Log.d("ObjectDetection", "Calculated IoU: $iou")
        return iou
    }

    private fun clipCoords(coords: FloatArray, width: Int, height: Int) {
        coords[0] = max(0f, min(coords[0], width.toFloat()))
        coords[1] = max(0f, min(coords[1], height.toFloat()))
        coords[2] = max(0f, min(coords[2], width.toFloat()))
        coords[3] = max(0f, min(coords[3], height.toFloat()))
        Log.d("ObjectDetection", "Clipped coordinates: ${coords.joinToString()}")
    }

    private fun scaleCoords(coords: FloatArray, gain: Float, pad: FloatArray) {
        coords[0] = (coords[0] - pad[0]) / gain
        coords[1] = (coords[1] - pad[1]) / gain
        coords[2] = (coords[2] - pad[0]) / gain
        coords[3] = (coords[3] - pad[1]) / gain
        Log.d("ObjectDetection", "Scaled coordinates: ${coords.joinToString()}")
    }

    private fun scaleCoordsLandmarks(landmarks: FloatArray, gain: Float, pad: FloatArray) {
        for (i in landmarks.indices step 2) {
            landmarks[i] = (landmarks[i] - pad[0]) / gain
            landmarks[i + 1] = (landmarks[i + 1] - pad[1]) / gain
        }
        Log.d("ObjectDetection", "Scaled landmarks: ${landmarks.joinToString()}")
    }
}
