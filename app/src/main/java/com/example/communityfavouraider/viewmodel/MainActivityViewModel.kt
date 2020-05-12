package com.example.communityfavouraider.viewmodel

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.communityfavouraider.Filters
import com.example.communityfavouraider.adapter.FavourAdapter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MainActivityViewModel : ViewModel() {
    companion object {
        public const val LIMIT: Long = 20
    }

    var isSigningIn = false

    val defaultQuery: Query = FirebaseFirestore.getInstance().collection("favours")
    var query: Query = defaultQuery

    var filters = Filters.getDefault()

    var favourAdapter: FavourAdapter? = null

    fun initFavourAdapter(listener: FavourAdapter.OnFavourSelectedListener) {
        favourAdapter = FavourAdapter(query, listener)
    }
}