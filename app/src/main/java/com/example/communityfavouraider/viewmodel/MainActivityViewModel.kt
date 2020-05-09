package com.example.communityfavouraider.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.communityfavouraider.adapter.FavourAdapter
import com.google.firebase.firestore.Query

class MainActivityViewModel : ViewModel() {
    var isSigningIn = false

    public var favourAdapter: FavourAdapter? = null

    fun initFavourAdapter(query: Query, listener: FavourAdapter.OnFavourSelectedListener) {
        favourAdapter = FavourAdapter(query, listener)
    }
}