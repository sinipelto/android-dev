package fi.tuni.sinipelto.disqs.model

import com.google.android.gms.maps.model.LatLng

data class UserMarker(

    val pos: LatLng,

    var count: Int

)
