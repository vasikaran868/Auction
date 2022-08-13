package com.example.auction

import android.app.Application
import com.example.auction.data.UserDataBase

class AuctionApplication : Application() {
    // Using by lazy so the database is only created when needed
    // rather than when the application starts
    val database: UserDataBase by lazy { UserDataBase.getDatabase(this) }
}
