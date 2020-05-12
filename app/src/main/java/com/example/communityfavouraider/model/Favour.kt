package com.example.communityfavouraider.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.*


enum class FavourStatus(val value: Int) {
    FREE(0),
    ACCEPTED(1),
    BOUND(2)
}

@IgnoreExtraProperties
data class Favour(val submittingUserId: String = "",
                  val submittingUserName: String = "",
                  val option: String = "REQUEST",
                  val status: Int = FavourStatus.FREE.value,
                  val title: String = "",
                  val description: String = "",
                  val adress: String = "",
                  val latitiude: Double = 0.0,
                  val longitude: Double = 0.0,
                  val respondingUserId: String? = null,
                  val respondingUserName: String? = null,
                  @ServerTimestamp
                  var timeStamp: Date? = null)
