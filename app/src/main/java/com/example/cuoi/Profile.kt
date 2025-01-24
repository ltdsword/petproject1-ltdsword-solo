package com.example.cuoi

data class Object(val name: String, val price: Int)

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

    fun addObject(name: String, price: Int) {
        hist.add(Object(name, price))
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
        recalculate();
    }

    fun getTotal(): Int {
        return total;
    }
}


class Friend(var name: String, var email: String, var phoneNumber: String) {
    var hist = History()
}

class Profile() {
    private var cache: MutableMap<String, Int> = mutableMapOf()
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
        if (cache[place] != null) {
            cacheExist = true
            return
        }
        else {
            cache[place] = price
            cacheExist = false
        }
    }

    fun findCache(place: String): Boolean {
        return cache[place] != null
    }

    fun findFriend(name: String): Friend? {
        for (i in friends) {
            if (i.name == name) {
                return i
            }
        }
        return null
    }
}