package com.example.communityfavouraider.model

import android.provider.ContactsContract
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(val id: String = "",
                val displayName: String = "",
                val email: String = "")