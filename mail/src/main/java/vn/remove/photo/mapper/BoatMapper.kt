package vn.remove.photo.mapper

import vn.remove.photo.logics.BoatModelFirebase
import vn.remove.photo.logics.BoatModelRoom

class BoatMapper {

    fun mapRmToFb(rm: BoatModelRoom) = BoatModelFirebase(
        id = rm.idFire,
        data = rm.data
    )
    fun mapFbToRm(fb: BoatModelFirebase) = BoatModelRoom(
        id = fb.id!!.toInt(),
        idFire = fb.id,
        data = fb.data
    )
}