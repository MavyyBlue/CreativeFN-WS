package dev.creativelogic.mobile

import android.app.Application
import androidx.room.Room
import dev.creativelogic.mobile.mechanicstorage.MechanicDatabase
import dev.creativelogic.mobile.mechanicstorage.RoomMechanicRepository
import dev.creativelogic.model.MechanicRepository

class CreativeLogicApplication : Application() {
    val repository: MechanicRepository by lazy {
        val database = Room.databaseBuilder(
            applicationContext, MechanicDatabase::class.java, "creative-logic.db",
        ).build()
        RoomMechanicRepository(database.mechanics())
    }
}
