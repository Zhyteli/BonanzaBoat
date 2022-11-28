package vn.remove.photo.mapper

import vn.remove.photo.logics.BoatModelFirebase
import vn.remove.photo.logics.BoatModelRoom

class BoatMapper {

    fun mapRmToFb(rm: BoatModelRoom) = BoatModelFirebase(
        id = rm.id.toString(),
        data = rm.data
    )
    fun mapFbToRm(fb: BoatModelFirebase) = BoatModelRoom(
        id = fb.id!!.toInt(),
        data = fb.data
    )
}