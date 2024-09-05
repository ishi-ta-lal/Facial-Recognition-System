package com.example.finaldetectionapp

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


//class MainViewModel: ViewModel() {
//
//    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
//    val bitmaps = _bitmaps.asStateFlow()
//
//    fun onTakePhoto(bitmap: Bitmap) {
//        _bitmaps.value += bitmap
//    }
//
//
//}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // StateFlow to hold the list of employees
    private val _employees = MutableStateFlow<List<Employee1>>(emptyList())
    val employees: StateFlow<List<Employee1>> = _employees.asStateFlow()

    // StateFlow to hold the captured Bitmaps
    private val _bitmaps = MutableStateFlow<List<Bitmap>>(emptyList())
    val bitmaps: StateFlow<List<Bitmap>> = _bitmaps.asStateFlow()

    private val _photoPaths = MutableStateFlow<List<String>>(emptyList())
    val photoPaths: StateFlow<List<String>> = _photoPaths.asStateFlow()

    // Function to add a new photo path to the list
    fun addPhotoPath(path: String) {
        _photoPaths.value = _photoPaths.value + path
    }

    // Function to handle capturing and storing the Bitmap
    fun onTakePhoto(bitmap : Bitmap) {
        _bitmaps.value += bitmap
    }

    init {
        //loadEmployees()
    }

    fun saveEmployee(name: String,
                     employeeId: String,
                     photoPath: String,
                     boundingBoxes: List<BoundingBox>,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val employee = Employee1(
                name = name,
                employeeId = employeeId,
                photoPath = photoPath,
                boundingBoxes = boundingBoxes
            )
            // Save employee data in a JSON file
            saveEmployeeToFile(employee)
            //loadEmployees() // Reload employees to reflect the updated list
        }
    }

    fun loadEmployees() {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, "employees.json")
            if (file.exists()) {
                val jsonString = file.readText()
                val employeesList = Json.decodeFromString<List<Employee1>>(jsonString)
                _employees.value = employeesList
            }
        }
    }
//    private fun loadEmployees() {
//        viewModelScope.launch(Dispatchers.IO) {
//            val file = File(getApplication<Application>().filesDir, "employees.json")
//            if (file.exists()) {
//                val employeesList = mutableListOf<Employee1>()
//                file.readLines().forEach { line ->
//                    try {
//                        // Attempt to parse each line as JSON
//                        val employee = parseEmployeeJson(line)
//                        if (employee != null) {
//                            employeesList.add(employee)
//                        } else {
//                            Log.e("MainViewModel", "Failed to parse line as employee JSON: $line")
//                        }
//                    } catch (e: Exception) {
//                        Log.e("MainViewModel", "Error parsing employee data: $line", e)
//                    }
//                }
//                _employees.value = employeesList
//            }
//        }
//    }


    fun deleteEmployee(employee: Employee1) {
        viewModelScope.launch(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, "employees.json")
            if (file.exists()) {
                val employeesList = _employees.value.toMutableList()
                employeesList.remove(employee)
                _employees.value = employeesList
                val updatedJson = Json.encodeToString(employeesList)
                file.writeText(updatedJson)
            }
        }
    }

//    fun saveEmployeeWithEmbedding(
//        name: String, employeeId: String, photoPath: String, embeddings: FloatArray
//    ) {
//        viewModelScope.launch(Dispatchers.IO) {
//            val employee = Employee1(0, name, employeeId, photoPath, embeddings)
//            val file = File(getApplication<Application>().filesDir, "employees.json")
//            val employeeJson = """
//                {
//                    "name": "${employee.name}",
//                    "employeeId": "${employee.employeeId}",
//                    "photoPath": "${employee.photoPath}",
//                    "embeddings": ${employee.embeddings.joinToString(", ")}
//                }
//            """.trimIndent()
//            file.appendText("$employeeJson,\n")
//            loadEmployees()
//        }
//    }

    // Function to save the employee details to a JSON file
    private suspend fun saveEmployeeToFile(employee: Employee1) {
        withContext(Dispatchers.IO) {
            val file = File(getApplication<Application>().filesDir, "employees.json")
            val currentEmployees = _employees.value.toMutableList()
            currentEmployees.add(employee)
            val jsonString = Json.encodeToString(currentEmployees)
            file.writeText(jsonString)
        }
    }
}

@Serializable
data class Employee1(
    val name: String,
    val employeeId: String,
    val photoPath: String,
    val boundingBoxes: List<BoundingBox> = emptyList(),
)



//private fun parseEmployeeJson(json: String): Employee1? {
//    return try {
//        // Use Gson or other JSON library for parsing
//        val gson = Gson()
//        gson.fromJson(json, Employee1::class.java)
//    } catch (e: JsonSyntaxException) {
//        Log.e("MainViewModel", "Invalid JSON syntax: $json", e)
//        null
//    }
//}