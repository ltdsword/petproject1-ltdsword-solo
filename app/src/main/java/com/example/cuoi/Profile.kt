package com.example.cuoi

import android.util.Log

data class Object(val name: String, val price: Int, val date: String)

class History {
    private var total = 0
    private var hist: MutableList<Object> = mutableListOf()

    private fun recalculate(): Int {
        total = 0
        for (i in hist) {
            total += i.price
        }
        return total
    }

    fun addObject(date: String, name: String, price: Int) {
        hist.add(Object(name, price, date))
        total += price
    }

    fun addObject(obj: Object) {
        hist.add(obj)
        total += obj.price
    }

    fun clear() {
        hist.clear()
        total = 0
    }

    fun recal() {
        recalculate()
    }

    fun getList(): MutableList<Object> {
        return hist
    }

    fun getTotal(): Int {
        return total
    }
}


class Friend(var name: String, var email: String, var phoneNumber: String) {

    private val colors = arrayOf(
        R.color.grey, R.color.green, R.color.greener,
        R.color.teal_200, R.color.teal_700, R.color.blue,
        R.color.purple_200, R.color.purple_500, R.color.purple_700,
        R.color.yellow, R.color.orange, R.color.red,
        R.color.redder, R.color.black
    )

    private var level = arrayOf(
        10000, 20000, 50000, 70000, 100000, 120000, 150000,
        200000, 300000, 500000, 700000, 1000000, 3000000, 5000000
    )

    private val len = 14
    private val lim = 5000000
    var hist = History()
    var index = 0
    var color = R.color.grey

    fun syncLevel() {
        level = arrayOf(
            10000, 20000, 50000, 70000, 100000, 120000, 150000,
            200000, 300000, 500000, 700000, 1000000, 3000000, 5000000
        )
    }

    fun sync() {
        for (i in 0 until len) {
            if (hist.getTotal() < level[i]) {
                index = i
                break
            }
        }
        color = colors[index]
    }
}

class Profile() {
    private var cache: MutableMap<String, Int> = mutableMapOf()
    private var hist: MutableList<Pair<String, Int>> = mutableListOf()
    private var friends: MutableList<Friend> = mutableListOf()

    var name = ""
    var age = 0
    var phoneNumber = ""
    var email = ""

    private var cacheExist = false

    // cache: save the destination/place in the mutable map format (place --> price)
    // friends: save the list of friends

    fun addFriend(f: Friend) {
        friends.add(f)
    }

    fun addCache(place: String, price: Int) {
        cache[place] = price
    }

    fun addHist(place: String, price: Int) {
        hist.add(Pair(place, price))
    }

    fun clearAllHist() {
        hist.clear()
    }

    fun clearFriend(f: Friend) {
        friends.remove(f)
    }

    fun clearFriend(index: Int) {
        friends.removeAt(index)
    }

    fun findCache(place: String): Int? {
        return cache[place]
    }

    fun findFriend(name: String): Friend? {
        for (i in friends) {
            if (i.name == name) {
                return i
            }
        }
        return null
    }

    fun setCache(cache: MutableMap<String, Int>) {
        this.cache = cache
    }

    fun setFriends(friends: MutableList<Friend>) {
        this.friends = friends
    }

    fun getCache(): MutableMap<String, Int> {
        return cache
    }

    fun getFriends(): MutableList<Friend> {
        return friends
    }

    fun getHist(): MutableList<Pair<String, Int>> {
        return hist
    }

    fun getTotal() : Int {
        var tot = 0
        for (i in friends) {
            tot += i.hist.getTotal()
        }
        return tot
    }
}