package com.example.communityfavouraider

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.communityfavouraider.model.Favour
import com.google.firebase.firestore.*
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter


class FavourDetailsActivity : AppCompatActivity(),
                              EventListener<DocumentSnapshot> {

    private val TAG = "FavourDetailsActivity"

    companion object {
        const val KEY_FAVOUR_ID = "key_favour_id"
    }

    private lateinit var favourTitle: TextView
    private lateinit var favourDescription: TextView
    private lateinit var favourUserName: TextView
    private lateinit var favourModificationDate: TextView
    private lateinit var favourCity: TextView

    private lateinit var favourRef: DocumentReference
    private lateinit var favourRegistration: ListenerRegistration

    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favour_detail)

        favourTitle = findViewById(R.id.favour_item_title)
        favourDescription = findViewById(R.id.favour_item_description)
        favourUserName = findViewById(R.id.favour_item_user_name)
        favourModificationDate = findViewById(R.id.favour_item_modification_date)
        favourCity = findViewById(R.id.favour_item_location_city)

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
        favourCity.text = favour?.city
    }
}