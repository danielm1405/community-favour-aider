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
import com.example.communityfavouraider.model.Favour
import com.example.communityfavouraider.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import java.util.*

class MainMapActivity : AppCompatActivity(),
                        OnMapReadyCallback {

    private val TAG = "MainMapActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var viewModel: MainActivityViewModel

    private var map: GoogleMap? = null

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

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.main_map_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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
        findViewById<View>(R.id.main_map_refresh_button).setOnClickListener {
            resetAllFavourMarkers()
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

        val centerOfPoland = LatLng(52.335, 19.381)
        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(centerOfPoland, 5F))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        // sooo baaad :(
        resetAllFavourMarkers()

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

    private fun addFavourMarker(favour: Favour) {
        val latLng = LatLng(favour.latitiude, favour.longitude)
        var title = favour.title
        if (title.length > 15) {
            title = title.take(12) + "..."
        }
        var userName = favour.userName
        if (userName.length > 23) {
            userName = userName.take(20) + "..."
        }

        map?.addMarker(MarkerOptions()
            .position(latLng)
            .title(title)
            .snippet("user: $userName")
        )
    }

    private fun resetAllFavourMarkers() {
        if (map == null) {
            return
        }

        map?.clear()

        val itemsCount = viewModel.favourAdapter?.itemCount ?: 0
        for (itemIndex in 0 until itemsCount) {
            val favour = viewModel.favourAdapter?.getFavour(itemIndex) ?: continue
            addFavourMarker(favour)
        }
    }
}