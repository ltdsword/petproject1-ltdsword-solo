package com.example.cuoi

//import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.codec.binary.Base64
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.Base64

class UploadQR {
    @RequiresApi(Build.VERSION_CODES.O)
    private fun decodeBase64Image(base64: String): ByteArray {
        return Base64.getDecoder().decode(
            base64.removePrefix("data:image/png;base64,")
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun uploadQrToFirebase(base64QR: String, onComplete: (String?) -> Unit) {
        val storageRef = Firebase.storage.reference
        val qrRef = storageRef.child("qr_codes/qr_${System.currentTimeMillis()}.png")

        val imageBytes = decodeBase64Image(base64QR)

        val uploadTask = qrRef.putBytes(imageBytes)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            qrRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUrl = task.result.toString()
                onComplete(downloadUrl)
            } else {
                onComplete(null)
            }
        }
    }

}