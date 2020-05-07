package com.example.communityfavouraider

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.communityfavouraider.model.Favour
import com.google.firebase.firestore.*


class FavourDetailsActivity : AppCompatActivity(),
                              EventListener<DocumentSnapshot> {

    private val TAG = "FavourDetailsActivity"

    companion object {
        const val KEY_FAVOUR_ID = "key_favour_id"
    }

    private lateinit var favourTitle: TextView
    private lateinit var favourDescription: TextView

    private lateinit var favourRef: DocumentReference
    private lateinit var favourRegistration: ListenerRegistration


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favour_detail)

        favourTitle = findViewById(R.id.favour_item_title)
        favourDescription = findViewById(R.id.favour_item_description)

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
    }
}