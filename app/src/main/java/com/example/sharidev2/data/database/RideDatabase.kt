package com.example.sharidev2.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.sharidev2.data.dao.RideDAO
import com.example.sharidev2.data.model.Ride
import com.example.sharidev2.utility.Converters


@Database(
    entities = [Ride::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RideDatabase: RoomDatabase() {

    abstract fun getRideDao(): RideDAO
}