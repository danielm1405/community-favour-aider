package com.example.communityfavouraider

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.communityfavouraider.adapter.FavourAdapter
import com.example.communityfavouraider.model.User
import com.example.communityfavouraider.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class MainActivity : AppCompatActivity(),
                     View.OnClickListener {

    private val TAG = "MainActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var favourRecycler: RecyclerView

    // Firebase
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private var onFavourSelectedListener: FavourAdapter.OnFavourSelectedListener =
        object : FavourAdapter.OnFavourSelectedListener(this) {
            override fun onFavourSelected(favour: DocumentSnapshot?) {
                Log.i(TAG, "Favour selected, go to activity that shows details")

                val intent = Intent(context, FavourDetailsActivity::class.java)
                intent.putExtra(FavourDetailsActivity.KEY_FAVOUR_ID, favour?.id)
                startActivity(intent)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseFirestore.setLoggingEnabled(true)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]
        if (viewModel.favourAdapter == null) {
            viewModel.initFavourAdapter(onFavourSelectedListener)
        }

        favourRecycler = findViewById(R.id.main_recycler_restaurants)
        favourRecycler.layoutManager = LinearLayoutManager(this)
        favourRecycler.adapter = viewModel.favourAdapter

        findViewById<View>(R.id.main_add_button).setOnClickListener(this)
        findViewById<View>(R.id.main_google_maps_button).setOnClickListener(this)

        if (shouldStartSignIn()) {
            startSignIn()
        } else {
            Log.d(TAG, "Not needed to log in.")
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

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.main_add_button -> onAddClicked(v)
            R.id.main_google_maps_button -> onGoogleMapButtonClicked(v)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            val usersRef: CollectionReference = firestore.collection("users")
            val userQuery = usersRef.whereEqualTo("id", currentUser?.uid).limit(3)

            userQuery.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w(TAG, "onEvent:error", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot == null || querySnapshot.isEmpty) {
                    Log.i(TAG, "User not found, trying to add new to the database!")

                    if (currentUser == null ||
                        currentUser.displayName == null ||
                        currentUser.email == null)
                    {
                        Log.e(TAG, "User info is not well defined, " +
                            "not able to add user to the database.")
                    }

                    val user = User(currentUser!!.uid,
                                    currentUser.displayName!!,
                                    currentUser.email!!)

                    usersRef.add(user)
                        .addOnSuccessListener {documentReference ->
                            Log.i(TAG, "ADDING NEW USER: DocumentSnapshot written with ID: " +
                                    documentReference.id
                            )
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "ADDING NEW USER: Error adding document", e)
                        }

                } else if (querySnapshot.size() == 1) {
                    Log.i(TAG, "The user with id: ${currentUser?.uid} already exist, " +
                            "not adding new one.")
                } else {
                    Log.e(TAG, "More than 1 user with id: ${currentUser?.uid} already " +
                            "exists ${querySnapshot.size()}, not adding new one.")
                }
            }

            userQuery.get()
        }
    }

    private fun shouldStartSignIn(): Boolean {
        return !viewModel.isSigningIn &&
                FirebaseAuth.getInstance().currentUser == null
    }

    private fun startSignIn() {
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
            .setAvailableProviders(Collections.singletonList(
                AuthUI.IdpConfig.EmailBuilder().build()))
            .setIsSmartLockEnabled(false)
            .build();

        startActivityForResult(intent, RC_SIGN_IN)
        viewModel.isSigningIn = true
    }

    private fun onAddClicked(v: View?) {
        Log.i(TAG, "onAddClicked called, starting AddFavourActivity")

        val intent = Intent(this, AddFavourActivity::class.java)
        startActivity(intent)
    }

    private fun onGoogleMapButtonClicked(v: View?) {
        Log.i(TAG, "onGoogleMapButtonClicked called, starting MainMapActivity")

        val intent = Intent(this, MainMapActivity::class.java)
        startActivity(intent)
    }
}
