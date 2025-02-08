package com.example.cuoi

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.Date

@Keep
data class Item(val name: String = "", val price: Int = 0, val date: String = "")

@Keep
data class History(
    var total: Int = 0,
    var hist: List<Item> = listOf()
    ) {
    @Exclude
    private fun recalculate(): Int {
        total = 0
        for (i in hist) {
            total += i.price
        }
        return total
    }

    @Exclude
    fun addObject(date: String, name: String, price: Int) {
        hist = hist + (Item(name, price, date))
        total += price
    }

    @Exclude
    fun clear() {
        hist = emptyList()
        total = 0
    }

    @Exclude
    fun getList(): List<Item> {
        return hist
    }
}

@Keep
data class Friend(
    var name: String = "",
    var email: String = "",
    var phoneNumber: String = "",
    var hist: History = History(),
    var index: Int = 0,
    var color: Int = 0,
    var verified: Boolean = false,
    var lastSent: Long = -80000
) {
    @Exclude
    private val colors = arrayOf(
        R.color.grey, R.color.green, R.color.greener,
        R.color.teal_200, R.color.teal_700, R.color.blue,
        R.color.purple_200, R.color.purple_500, R.color.purple_700,
        R.color.yellow, R.color.orange, R.color.red,
        R.color.redder, R.color.black
    )

    @Exclude
    private var level = arrayOf(
        10000, 20000, 50000, 70000, 100000, 120000, 150000,
        200000, 300000, 500000, 700000, 1000000, 3000000, 5000000
    )

    @Exclude
    private val len = 14
    @Exclude
    private val lim = 5000000


    @Exclude
    fun syncLevel() {
        level = arrayOf(
            10000, 20000, 50000, 70000, 100000, 120000, 150000,
            200000, 300000, 500000, 700000, 1000000, 3000000, 5000000
        )
    }

    @Exclude
    fun sync() {
        for (i in 0 until len) {
            if (hist.total < level[i]) {
                index = i
                break
            }
        }
        color = colors[index]
    }
}

@Keep
data class Profile(
    var name: String = "",
    var password: String = "",
    var age: Int = 0,
    var phoneNumber: String = "",
    var email: String = "",
    var bankAccount: String = "",
    var bankName: String = "",
    var cache: Map<String, Int> = emptyMap(),
    //var hist: MutableList<Pair<String, Int>> = mutableListOf(),
    var friends: List<Friend> = listOf()
) {
    @Exclude
    fun addCache(place: String, price: Int) {
        cache = cache + (place to price)
    }

    @Exclude
    fun getTotal() : Int {
        var tot = 0
        for (i in friends) {
            tot += i.hist.total
        }
        return tot
    }
}