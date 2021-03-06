package com.example.communityfavouraider

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.communityfavouraider.model.Favour
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException
import java.util.*


class AddFavourActivity : AppCompatActivity(),
                          OnMapReadyCallback,
                          GoogleMap.OnMapLongClickListener {

    private val TAG = "AddFavourActivity"
    private val FAVOUR_OPTIONS = arrayOf("REQUEST", "OFFER")

    // Forms
    private lateinit var favourOption: Spinner
    private lateinit var favourTitle: EditText
    private lateinit var favourDescription: EditText
    private lateinit var favourAdress: TextView

    private var favourLatLng: LatLng? = null
    private var favourRespondingUserId: String? = null
    private var favourRespondingUserName: String? = null


    // Google Maps
    private lateinit var map: GoogleMap
    private lateinit var geocoder: Geocoder

    // Firebase
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_favour)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.add_favour_location_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        geocoder = Geocoder(this, Locale.getDefault())

        favourTitle = findViewById(R.id.add_favour_title)
        favourDescription = findViewById(R.id.add_favour_description)
        favourAdress = findViewById(R.id.add_favour_adress)

        // spinner
        favourOption = findViewById(R.id.add_favour_option)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
                                    FAVOUR_OPTIONS)
        favourOption.adapter = adapter

        favourRespondingUserId = intent.getStringExtra("addresseeUserId")
        favourRespondingUserName = intent.getStringExtra("addresseeUserName")

        if (isResponse()) {
            favourOption.isClickable = false
            favourOption.setSelection(1)
            findViewById<TextView>(R.id.add_favour_text).text = "Add favour in response!"
        }

        setValidators()

        // set on click listeners for all the buttons
        findViewById<Button>(R.id.add_favour_submit).setOnClickListener { onSubmitClicked() }
        findViewById<Button>(R.id.add_favour_cancel).setOnClickListener { onCancelClicked() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setOnMapLongClickListener(this@AddFavourActivity)

        val warsaw = LatLng(52.228, 21.005)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(warsaw, 11F))
    }

    override fun onMapLongClick(latLng: LatLng) {
        Log.i(TAG, "Clicked in (${latLng.latitude}, ${latLng.longitude})")

        try {
            val adresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
            if (adresses.isNotEmpty()) {
                Log.i(TAG, "Country: ${adresses[0].countryName}, " +
                        "city: ${adresses[0].locality}, " +
                        "state: ${adresses[0].adminArea}, " +
                        "adress: ${adresses[0].getAddressLine(0)}, " +
                        "${adresses[0].thoroughfare}, " +
                        "${adresses[0].subThoroughfare}")

                favourAdress.text = adresses[0].getAddressLine(0)
                favourLatLng = latLng

                map.clear()
                map.addMarker(MarkerOptions().position(latLng).title("Your favour location"))
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to reverse geocode. Try again.")
        }
    }

    private fun onCancelClicked() {
        onBackPressed()
    }

    private fun onSubmitClicked() {
        var errorMessage: String? = null
        if (isAnyInformationInFormMissing()) {
            errorMessage = "Failed to submit, because one of the forms is in error state"
        } else if (isLocationMissing()) {
            errorMessage = "Failed to submit, because location is not chosen"
        }

        if (errorMessage != null) {
            Log.e(TAG, errorMessage)

            Snackbar.make(
                findViewById(R.id.add_favour_layout),
                errorMessage,
                Snackbar.LENGTH_SHORT
            ).show()

            return
        }

        val favours: CollectionReference = firestore.collection("favours");

        val userName = (FirebaseAuth.getInstance().currentUser?.displayName ?:
                            FirebaseAuth.getInstance().currentUser?.email) ?: ""

        val favour = Favour(submittingUserId = FirebaseAuth.getInstance().currentUser!!.uid,
                            submittingUserName = userName,
                            option = favourOption.selectedItem.toString(),
                            title = favourTitle.text.toString(),
                            description = favourDescription.text.toString(),
                            adress = favourAdress.text.toString(),
                            latitiude = favourLatLng!!.latitude,
                            longitude = favourLatLng!!.longitude)

        if (isResponse()) {
            favour.respondingUserId = favourRespondingUserId
            favour.respondingUserName = favourRespondingUserName
            favour.status = "ACCEPTED"
        }

        favours.add(favour)
            .addOnSuccessListener { documentReference ->
                Log.i(TAG, "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

        onBackPressed()
    }

    private fun setValidators() {
        favourTitle.validate("Title should have at least 10 characters.") {
                s -> s.length >= 10
        }

        favourDescription.validate("Description should have at least 20 characters.") {
                s -> s.length >= 20
        }
    }

    private fun isAnyInformationInFormMissing() : Boolean {
        return favourTitle.error != null ||
                favourDescription.error != null
    }

    private fun isLocationMissing() : Boolean {
        return favourLatLng == null
    }

    private fun isResponse() : Boolean {
        return favourRespondingUserId != null && favourRespondingUserName != null
    }
}
