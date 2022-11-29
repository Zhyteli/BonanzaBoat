package vn.remove.photo

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import vn.remove.photo.logics.BoatModelRoom

@Dao
interface RoomDao {
    @Query("SELECT * FROM dataone LIMIT 1")
    fun getter(): BoatModelRoom?

    @Query("SELECT * FROM dataone")
    fun boatLiveData(): LiveData<BoatModelRoom>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveBoat(data: BoatModelRoom)


}