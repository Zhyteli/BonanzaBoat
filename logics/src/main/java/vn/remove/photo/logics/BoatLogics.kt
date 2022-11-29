package vn.remove.photo.logics

import androidx.lifecycle.LiveData

interface BoatLogics {
    fun getBoatModelRoomData(): LiveData<BoatModelRoom>

    fun getterBoat(): BoatModelRoom?

    suspend fun setterBoatModelRoomData(link: BoatModelRoom)

    fun getBoatModelFireData(): LiveData<BoatModelFirebase>

    fun setterBoatModelFireData(data: BoatModelFirebase)
}