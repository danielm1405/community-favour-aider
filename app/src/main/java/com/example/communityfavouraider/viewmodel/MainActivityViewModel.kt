package com.example.communityfavouraider.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.communityfavouraider.adapter.FavourAdapter
import com.example.communityfavouraider.model.FavourStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivityViewModel : ViewModel() {
    private val LIMIT: Long = 20

    var isSigningIn = false

    private var query: Query = FirebaseFirestore.getInstance().collection("favours")
        .whereEqualTo("status", FavourStatus.FREE.value)
        .orderBy("timeStamp", Query.Direction.DESCENDING)
        .limit(LIMIT)

    public var favourAdapter: FavourAdapter? = null

    fun initFavourAdapter(listener: FavourAdapter.OnFavourSelectedListener) {
        favourAdapter = FavourAdapter(query, listener)
    }
}