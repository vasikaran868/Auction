package com.example.auction.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface UserDao {

    @Query("SELECT * from UserDetails")
    fun getallusers():Flow<List<UserDetails>>
}