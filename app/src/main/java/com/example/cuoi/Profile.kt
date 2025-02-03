package com.example.cuoi

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

    fun getTotal(): Int {
        return total
    }
}


class Friend(var name: String, var email: String, var phoneNumber: String) {
    var hist = History()
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