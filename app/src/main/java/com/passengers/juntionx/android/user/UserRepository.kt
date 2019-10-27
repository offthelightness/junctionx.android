package com.passengers.juntionx.android.user

import android.app.Activity
import android.content.Context
import java.util.*

class UserRepository(
    val context:Context
) {
    fun getUserId():String {

        val sharedPreferences = context.getSharedPreferences("UserRepository", Activity.MODE_PRIVATE)
        var userId = sharedPreferences
            .getString("USER_ID", null)
        if (userId== null) {
            userId = UUID.randomUUID().toString()
            sharedPreferences.edit()
                .putString("USER_ID", userId)
                .commit()
        }
        return userId
    }
}