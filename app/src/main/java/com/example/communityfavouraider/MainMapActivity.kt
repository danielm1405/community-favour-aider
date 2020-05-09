package com.example.communityfavouraider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.communityfavouraider.adapter.FavourAdapter
import com.example.communityfavouraider.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MainMapActivity : AppCompatActivity(),
                        OnMapReadyCallback {

    private val TAG = "MainMapActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var viewModel: MainActivityViewModel

    private lateinit var map: GoogleMap

    // very bad, but necessary with this architecture :(
    private var onFavourSelectedListener: FavourAdapter.OnFavourSelectedListener =
        object : FavourAdapter.OnFavourSelectedListener(this) {
            override fun onFavourSelected(favour: DocumentSnapshot?) {
                Log.i(TAG, "Favour selected, do nothing.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_map)

        FirebaseFirestore.setLoggingEnabled(true)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        if (viewModel.favourAdapter == null) {
            viewModel.initFavourAdapter(onFavourSelectedListener)
        }

        findViewById<View>(R.id.main_map_add_button).setOnClickListener {
                val favour = viewModel.favourAdapter?.getFavour(2)
                Log.i(TAG, "2nd favour: ${favour!!.title}")

                val intent = Intent(this, AddFavourActivity::class.java)
                startActivity(intent)
        }
        findViewById<View>(R.id.main_map_recycler_button).setOnClickListener {
            onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()

        viewModel.favourAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()

        viewModel.favourAdapter?.stopListening()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "Inside onMapReady")

        map = googleMap
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                AuthUI.getInstance().signOut(this)
                startSignIn()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun shouldStartSignIn(): Boolean {
        return !viewModel.isSigningIn &&
                FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignIn() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(
                Collections.singletonList(
                    AuthUI.IdpConfig.EmailBuilder().build()))
            .setIsSmartLockEnabled(false)
            .build();

        startActivityForResult(intent, RC_SIGN_IN)
        viewModel.isSigningIn = true
    }
}