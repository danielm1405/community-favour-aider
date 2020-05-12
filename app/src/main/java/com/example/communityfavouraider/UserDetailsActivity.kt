package com.example.communityfavouraider

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.communityfavouraider.model.User
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore


class UserDetailsActivity : AppCompatActivity() {

    val TAG = "UserDetailsActivity"

    companion object {
        const val KEY_USER_ID = "key_user_id"
    }

    private lateinit var userName: TextView
    private lateinit var userEmail: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_details)

        val userId: String = intent.getStringExtra(KEY_USER_ID)
            ?: throw IllegalArgumentException("Must pass extra $KEY_USER_ID")

        userName = findViewById(R.id.user_details_name)
        userEmail = findViewById(R.id.user_details_email)

        val usersRef: CollectionReference = FirebaseFirestore.getInstance()
            .collection("users")
        val userQuery = usersRef.whereEqualTo("id", userId).limit(3)

        userQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            if (firebaseFirestoreException != null) {
                Log.w(TAG, "onEvent:error", firebaseFirestoreException)
                return@addSnapshotListener
            }

            if (querySnapshot == null || querySnapshot.isEmpty) {
                Log.i(TAG, "User not found :(")
            } else if (querySnapshot.size() != 1) {
                Log.e(TAG, "More than 1 user with id: $userId exists, something is wrong!")
            } else {
                Log.i(TAG, "User found, let's display it!")

                val user = querySnapshot.documents[0].toObject(User::class.java)
                    ?: run {
                        Log.i(TAG, "Could not convert snapshot to 'User' class object, " +
                                "aborting.")
                        return@addSnapshotListener
                    }

                userName.text = user.displayName
                userEmail.text = user.email
            }
        }
    }
}