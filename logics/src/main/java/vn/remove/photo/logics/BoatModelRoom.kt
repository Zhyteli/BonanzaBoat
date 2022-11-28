package vn.remove.photo.logics

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dataone")
data class BoatModelRoom(
    @PrimaryKey(autoGenerate = false)
    var id:Int = 0,
    val data: String
)