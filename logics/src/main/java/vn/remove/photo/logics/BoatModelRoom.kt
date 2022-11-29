package vn.remove.photo.logics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fire")
data class BoatModelRoom(
    @PrimaryKey(autoGenerate = false)
    var id:Int = 0,
    val idFire: String? = "",
    val data: String
)