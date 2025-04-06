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
        Log.d("URL", "$accountNo,$accountName, $acqId, $amount")
        api.generateQR(request).enqueue(object : Callback<QRResponse> {
            override fun onResponse(call: Call<QRResponse>, response: Response<QRResponse>) {
                if (response.isSuccessful) {
                    val rawJson = response.body()
                    Log.d("FullRaw", rawJson.toString())  // Prints what your model parsed
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
