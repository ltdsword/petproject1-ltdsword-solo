package com.example.cuoi
import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QRGenerator {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.vietqr.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(VietQRGen::class.java)

    fun generateQRCode(accountNo: String, accountName: String, acqId: Int, amount: Int?, addInfo: String?, callback: (String?) -> Unit) {
        val request = QRRequest(accountNo, accountName, acqId, amount, addInfo)
        Log.d("URL1", "$accountNo,$accountName, $acqId, $amount") // [1]
        Log.d("URL1", "Line 20")
        api.generateQR(request).enqueue(object : Callback<QRResponse> {
            override fun onResponse(call: Call<QRResponse>, response: Response<QRResponse>) {
                Log.d("URL1", "Line 23")
                if (response.isSuccessful) {
                    Log.d("URL1", "Line 25")
                    val rawJson = response.body()
                    Log.d("FullRaw", rawJson.toString())  // Prints what your model parsed
                    Log.d("URL1", "Line 27")
                    val qrUrl = response.body()?.data?.qrDataURL
                    callback(qrUrl)  // Return QR Code URL
                } else {
                    Log.d("API_ERROR", "Code: ${response.code()}, ErrorBody: ${response.errorBody()?.string()}")
                    callback("")
                }
            }

            override fun onFailure(call: Call<QRResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
