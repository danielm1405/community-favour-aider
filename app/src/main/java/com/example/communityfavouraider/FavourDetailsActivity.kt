package com.example.communityfavouraider

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
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
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
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

    // Views
    private lateinit var favourTitle: TextView
    private lateinit var favourDescription: TextView
    private lateinit var favourSubmittingUserName: TextView
    private lateinit var favourModificationDate: TextView
    private lateinit var favourAdress: TextView
    private lateinit var favourStatus: TextView
    private lateinit var favourRespondingUserName: TextView
    private lateinit var favourAcceptAndReturnButton: Button
    private lateinit var favourAcceptButton: Button

    // Additional info
    private var favourLatLng: LatLng? = null
    private var favourSubmittingUserId: String? = null
    private var favourRespondingUserId: String? = null

    // Firebase
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
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
        favourSubmittingUserName = findViewById(R.id.favour_details_user_name)
        favourModificationDate = findViewById(R.id.favour_details_modification_date)
        favourAdress = findViewById(R.id.favour_details_location_adress)
        favourStatus = findViewById(R.id.favour_details_status)
        favourRespondingUserName = findViewById(R.id.favour_details_responding_user_name)
        favourAcceptAndReturnButton = findViewById(R.id.favour_details_accept_and_return)
        favourAcceptButton = findViewById(R.id.favour_details_accept)

        findViewById<TextView>(R.id.favour_details_user_name).setOnClickListener {
            onSubmittingUserNameClicked()
        }
        findViewById<TextView>(R.id.favour_details_responding_user_name).setOnClickListener {
            onRespondingUserNameClicked()
        }
        favourAcceptAndReturnButton.setOnClickListener {
            onAcceptClicked { goToAddNewWithParametersDelayed() }
        }
        favourAcceptButton.setOnClickListener {
            onAcceptClicked { goBackDelayed() }
        }

        val favourId: String = intent.getStringExtra(KEY_FAVOUR_ID)
            ?: throw IllegalArgumentException("Must pass extra $KEY_FAVOUR_ID")

        favourRef = firestore.collection("favours")
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

        val favour = snapshot?.toObject(Favour::class.java) ?: return

        favourTitle.text = favour.title
        favourDescription.text = favour.description
        favourSubmittingUserName.text = favour.submittingUserName
        favourModificationDate.text = dateFormatter.format(favour.timeStamp!!)
        favourAdress.text = favour.adress

        favourLatLng = LatLng(favour.latitiude, favour.longitude)
        favourSubmittingUserId = favour.submittingUserId
        favourRespondingUserId = favour.respondingUserId

        if (favour.status == "ACCEPTED") {
            favourStatus.visibility = View.VISIBLE
            favourRespondingUserName.text = favour.respondingUserName
        }

        // Handle button
        if (favour.status == "ACCEPTED") {
            favourAcceptAndReturnButton.visibility = View.GONE
            favourAcceptButton.text = "TOO LATE, OFFER ALREADY CLOSED"
            favourAcceptButton.visibility = View.GONE
        } else if (favour.option == "REQUEST") {
            favourAcceptAndReturnButton.visibility = View.GONE
            favourAcceptButton.text = "OFFER YOUR HELP"
        } else if (favour.option == "OFFER") {
            favourAcceptAndReturnButton.visibility = View.VISIBLE
            favourAcceptButton.text = "ACCEPT HELP"
        }

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

    private fun onAcceptClicked(onSuccess: () -> Unit) {
        Log.w(TAG, "onAcceptClicked: Accept clicked!")

        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(favourRef)
            val currentFavour = snapshot.toObject(Favour::class.java)

            if (currentFavour?.status == "ACCEPTED") {
                return@runTransaction -1
            }

            transaction.update(favourRef, "status", "ACCEPTED")
            transaction.update(favourRef, "respondingUserId", currentUser.uid)
            transaction.update(favourRef, "respondingUserName", currentUser.displayName)
        }
            .addOnSuccessListener {
                if(it == -1) {
                    Log.e(TAG, "onAcceptClicked: Favour not updated, " +
                            "because status is already ACCEPTED.")

                    Snackbar.make(
                        findViewById(R.id.activity_favour_details),
                        "Failed to accept the offer, offer is already accepted",
                        Snackbar.LENGTH_SHORT
                    ).show()
                } else {
                    Log.i(TAG, "onAcceptClicked: Favour successfully updated: status = ACCEPTED.")

                    Snackbar.make(
                        findViewById(R.id.activity_favour_details),
                        "Offer successfully accepted",
                        Snackbar.LENGTH_SHORT
                    ).show()

                    onSuccess()
                }
            }
            .addOnFailureListener {e ->
                Log.e(TAG, "onAcceptClicked: Favour not updated, error occurred.", e)

                Snackbar.make(
                    findViewById(R.id.activity_favour_details),
                    "Failed to accept the offer, error occurred",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
    }

    private fun goBackDelayed() {
        Handler().postDelayed({onBackPressed()}, 1500)
    }

    private fun goToAddNewWithParametersDelayed() {
        val intent = Intent(this, AddFavourActivity::class.java)

        intent.putExtra("addresseeUserId", favourSubmittingUserId)
        intent.putExtra("addresseeUserName", favourSubmittingUserName.text)

        Handler().postDelayed({startActivity(intent)}, 1500)
    }

    private fun onSubmittingUserNameClicked() {
        Log.w(TAG, "onSubmittingUserNameClicked: User name " +
                "${favourSubmittingUserName.text} clicked!")

        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra(UserDetailsActivity.KEY_USER_ID, favourSubmittingUserId)
        startActivity(intent)
    }

    private fun onRespondingUserNameClicked() {
        Log.w(TAG, "onRespondingUserNameClicked: User name " +
                "${favourRespondingUserName.text} clicked!")

        val intent = Intent(this, UserDetailsActivity::class.java)
        intent.putExtra(UserDetailsActivity.KEY_USER_ID, favourRespondingUserId)
        startActivity(intent)
    }

    private fun centerCameraOnLocationAndSetMarker() {
        if (favourLatLng == null) {
            return
        }

        map?.moveCamera(CameraUpdateFactory.newLatLngZoom(favourLatLng, 15F))
        map?.addMarker(MarkerOptions().position(favourLatLng!!))
    }
}