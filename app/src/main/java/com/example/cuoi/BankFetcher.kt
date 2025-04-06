package com.example.cuoi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BankFetcher {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.vietqr.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(VietQRBankService::class.java)

    fun fetchBanks(callback: (List<Bank>?) -> Unit) {
        api.getBanks().enqueue(object : Callback<BankListResponse> {
            override fun onResponse(call: Call<BankListResponse>, response: Response<BankListResponse>) {
                if (response.isSuccessful) {
                    callback(response.body()?.banks)
                } else {
                    callback(null)
                }
            }

            override fun onFailure(call: Call<BankListResponse>, t: Throwable) {
                callback(null)
            }
        })
    }
}
