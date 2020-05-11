package com.example.communityfavouraider.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

@IgnoreExtraProperties
data class Favour(val userId: String = "",
                  val userName: String = "",
                  val option: String = "REQUEST",
                  val title: String = "",
                  val description: String = "",
                  val adress: String = "",
                  val latitiude: Double = 0.0,
                  val longitude: Double = 0.0,
                  @ServerTimestamp
                  var timeStamp: Date? = null)
