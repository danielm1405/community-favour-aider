package com.example.communityfavouraider.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
class Favour {
    private var title: String? = null
    private var city: String? = null

    constructor(favour: Favour) {
        title = favour.title
        city = favour.city
    }
}