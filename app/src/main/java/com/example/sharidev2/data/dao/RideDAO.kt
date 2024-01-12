package com.example.sharidev2.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.sharidev2.data.model.Ride

@Dao
interface RideDAO {

    @Insert()
    suspend fun insertTransaction(ride: Ride)

    @Delete
    suspend fun deleteTransaction(ride: Ride)

    // Retrieve Ride
    @Query("SELECT * FROM ride_table ORDER BY date DESC")
    fun getAllRidesSortedByDate(): LiveData<List<Ride>>


}