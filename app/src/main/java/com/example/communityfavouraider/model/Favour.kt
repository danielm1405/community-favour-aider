package com.example.communityfavouraider.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favour(val userId: String,
                  val userName: String = "",
                  var title: String = "",
                  var description: String = "",
                  // TODO: adapt location information to GoogleMapsAPI
                  var city: String = ""
)
{
}
