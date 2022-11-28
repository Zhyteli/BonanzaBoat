package vn.remove.photo.mail

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import vn.remove.photo.RoomOneData
import vn.remove.photo.logics.BoatLogics
import vn.remove.photo.logics.BoatModelFirebase
import vn.remove.photo.logics.BoatModelRoom

class BoatLogicsIm(
    application: Application,
    val gadid: String
) : BoatLogics {

    private lateinit var firebase: DatabaseReference
    private lateinit var valueLoop: ValueEventListener
    private var firebaseData: BoatModelFirebase? = null
    val roomDao = RoomOneData.getInstance(application).roomDao()

    override fun getBoatModelRoomData(): LiveData<BoatModelRoom> {
        return roomDao.boatLiveData()
    }

    override fun getterBoat(): BoatModelRoom? {
        return roomDao.getter()
    }

    override suspend fun setterBoatModelRoomData(link: BoatModelRoom) {
        if (getBoatModelRoomData().value == null) {
            roomDao.saveBoat(link)
        } else if (getBoatModelRoomData().value!!.data.contains(DEFAULT_STRING)) {
            roomDao.saveBoat(link)
        }
    }

    override fun getBoatModelFireData(): LiveData<BoatModelFirebase> {
        firebase = FirebaseDatabase.getInstance().getReference(gadid)

        val boatModelFirebaseMutableLiveData: MutableLiveData<BoatModelFirebase> = MutableLiveData()

        valueLoop = firebase.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children) {
                    firebaseData = postSnapshot.getValue(BoatModelFirebase::class.java)!!
                    firebaseData?.id = postSnapshot.key
                    boatModelFirebaseMutableLiveData.value = firebaseData
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", error.message)
            }
        })
        return boatModelFirebaseMutableLiveData
    }

    override fun saveCleopatraData(data: BoatModelFirebase) {
        firebase = FirebaseDatabase.getInstance().getReference(gadid)
        data.id = gadid
        firebase.child(gadid).setValue(data)
    }
    companion object {
        const val DEFAULT_STRING = "bonanzaboat.store/boat.php"
    }
}