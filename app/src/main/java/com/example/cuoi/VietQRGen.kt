package com.example.cuoi
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName
import retrofit2.http.GET

// get the list of bank
interface VietQRBankService {
    @GET("v2/banks")
    fun getBanks(): Call<BankListResponse>
}

data class BankListResponse(
    @SerializedName("data") val banks: List<Bank>
)

data class Bank(
    @SerializedName("bin") val acqId: Int,
    @SerializedName("shortName") val shortName: String,
    @SerializedName("name") val name: String
)

// interface for generating QR code
interface VietQRGen {
    @Headers("Content-Type: application/json")
    @POST("v2/generate")
    fun generateQR(@Body request: QRRequest): Call<QRResponse>
}

// data class of QR generator
data class QRRequest(
    @SerializedName("accountNo") val accountNo: String,
    @SerializedName("accountName") val accountName: String,
    @SerializedName("acqId") val acqId: Int,
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName("addInfo") val addInfo: String? = null,
    @SerializedName("format") val format: String = "png"
)

data class QRResponse(
    @SerializedName("code") val code: String,
    @SerializedName("desc") val desc: String,
    @SerializedName("data") val data: QRData?
)

data class QRData(
    @SerializedName("qrDataURL") val qrDataURL: String  // The URL of the QR Code image
)
