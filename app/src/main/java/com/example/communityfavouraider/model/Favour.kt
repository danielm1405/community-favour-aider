package com.example.communityfavouraider.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Favour(var title: String = "",
                  var description: String = "",
                  // TODO: adapt location information to GoogleMapsAPI
                  var city: String = ""
)
{
    constructor(favour: Favour) : this() {
        title = favour.title
        description = favour.description
        city = favour.city
    }
}
