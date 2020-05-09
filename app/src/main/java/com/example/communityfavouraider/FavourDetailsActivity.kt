package com.example.communityfavouraider

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.communityfavouraider.model.Favour
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat


class FavourDetailsActivity : AppCompatActivity(),
                              OnMapReadyCallback,
                              EventListener<DocumentSnapshot> {

    private val TAG = "FavourDetailsActivity"

    companion object {
        const val KEY_FAVOUR_ID = "key_favour_id"
    }

    private var map: GoogleMap? = null

    private lateinit var favourTitle: TextView
    private lateinit var favourDescription: TextView
    private lateinit var favourUserName: TextView
    private lateinit var favourModificationDate: TextView
    private lateinit var favourAdress: TextView
    private var favourLatLng: LatLng? = null

    private lateinit var favourRef: DocumentReference
    private lateinit var favourRegistration: ListenerRegistration

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favour_detail)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.favour_details_location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        favourTitle = findViewById(R.id.favour_details_title)
        favourDescription = findViewById(R.id.favour_details_description)
        favourUserName = findViewById(R.id.favour_details_user_name)
        favourModificationDate = findViewById(R.id.favour_details_modification_date)
        favourAdress = findViewById(R.id.favour_details_location_adress)

        findViewById<Button>(R.id.favour_details_accept).setOnClickListener { onAcceptClicked() }

        val favourId: String = intent.getStringExtra(KEY_FAVOUR_ID)
            ?: throw IllegalArgumentException("Must pass extra $KEY_FAVOUR_ID")

        favourRef = FirebaseFirestore.getInstance()
            .collection("favours")
            .document(favourId)
    }

    override fun onStart() {
        super.onStart()

        favourRegistration = favourRef.addSnapshotListener(this)
    }

    override fun onStop() {
        super.onStop()

        favourRegistration.remove()
    }

    override fun onEvent(snapshot: DocumentSnapshot?, e: FirebaseFirestoreException?) {
        if (e != null) {
            Log.w(TAG, "favour:onEvent", e)
            return
        }

        val favour = snapshot?.toObject(Favour::class.java)

        favourTitle.text = favour?.title
        favourDescription.text = favour?.description
        favourUserName.text = favour?.userName
        favourModificationDate.text = dateFormatter.format(favour?.timeStamp)
        favourAdress.text = favour?.adress
        favourLatLng = LatLng(favour!!.latitiude, favour.longitude)

        if (map == null) {
            Log.w(TAG, "Map is null in 'onEvent'.")
        }

        centerCameraOnLocationAndSetMarker()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.i(TAG, "Inside onMapReady")

        map = googleMap

        if (favourLatLng == null) {
            Log.w(TAG, "favourLatLng is null in 'onMapReady'.")
        }

        centerCameraOnLocationAndSetMarker()
    }

    private fun onAcceptClicked() {
        Log.w(TAG, "Accept clicked!")
        // TODO: implement
        findViewById<Button>(R.id.favour_details_accept).text = "Clicked - TODO: impelement"
    }

    private fun centerCameraOnLocationAndSetMarker() {
        if (favourLatLng == null) {
            return
        }

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(favourLatLng, 15F))
        map?.addMarker(MarkerOptions().position(favourLatLng!!))
    }
}