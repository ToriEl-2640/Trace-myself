package com.example.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException

enum class OperationType {
    CREATE, READ, UPDATE, DELETE, LIST
}

fun handleFirestoreError(exception: Exception, operation: OperationType, path: String) {
    if (exception is FirebaseFirestoreException) {
        when (exception.code) {
            FirebaseFirestoreException.Code.PERMISSION_DENIED -> {
                Log.e("FirestoreSecurity", "PERMISSION_DENIED for $operation at path $path: ${exception.message}", exception)
            }
            FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> {
                Log.e("FirestoreQuota", "RESOURCE_EXHAUSTED for $operation at path $path: ${exception.message}", exception)
            }
            FirebaseFirestoreException.Code.UNAVAILABLE -> {
                Log.w("FirestoreNetwork", "Service UNAVAILABLE for $operation at path $path: ${exception.message}", exception)
            }
            else -> {
                Log.e("FirestoreError", "Error ${exception.code} for $operation at path $path: ${exception.message}", exception)
            }
        }
    } else {
        Log.e("FirestoreError", "Unexpected error for $operation at path $path: ${exception.message}", exception)
    }
}
