package com.example.communityfavouraider

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
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
                     View.OnClickListener,
                     FilterDialogFragment.FilterListener {

    private val TAG = "MainActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var favourRecycler: RecyclerView

    private lateinit var currentSearchView: TextView
    private lateinit var currentSortView: TextView

    private val filterDialog = FilterDialogFragment()

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

        currentSearchView = findViewById(R.id.text_current_search)
        currentSortView = findViewById(R.id.text_current_sort_by)

        findViewById<View>(R.id.main_add_button).setOnClickListener(this)
        findViewById<View>(R.id.main_google_maps_button).setOnClickListener(this)
        findViewById<CardView>(R.id.filter_bar).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        if (shouldStartSignIn()) {
            startSignIn()
        } else {
            Log.d(TAG, "Not needed to log in.")
        }

        onFilter(viewModel.filters)

        viewModel.favourAdapter?.startListening()
    }

    override fun onStop() {
        super.onStop()

        viewModel.favourAdapter?.stopListening()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.main_add_button -> onAddClicked()
            R.id.main_google_maps_button -> onGoogleMapButtonClicked()
            R.id.filter_bar -> onFilterClicked()
        }
    }

    override fun onFilter(filters: Filters) {
        var query = MainActivityViewModel.defaultQuery

        if (filters.hasOption()) {
            query = query.whereEqualTo("option", filters.option)
        }

        if (filters.hasStatus()) {
            val splittedStatus = filters.status!!.split(delimiters = *arrayOf(" "), limit = 2)

            val status = splittedStatus[0]
            query = query.whereEqualTo("status", status)

            val isSubmittedByMe = splittedStatus.last().contains("submitted by me")
            val isAcceptedByMe = !isSubmittedByMe && splittedStatus.last().contains("me")
            if (isSubmittedByMe) {
                query = query.whereEqualTo("submittingUserId",
                    FirebaseAuth.getInstance().currentUser?.uid)
            } else if (isAcceptedByMe && status == "ACCEPTED") {
                query = query.whereEqualTo("respondingUserId",
                    FirebaseAuth.getInstance().currentUser?.uid)
            }
        }

        if (filters.hasSortBy()) {
            query = query.orderBy(filters.sortBy!!, filters.sortDirection!!)
        }

        query = query.limit(MainActivityViewModel.LIMIT)

        MainActivityViewModel.query = query

        viewModel.favourAdapter?.setQuery(query)
        viewModel.filters = filters

        currentSearchView.text = Html.fromHtml(filters.getFiltersDescription())
        currentSortView.text = filters.getSortingDescription(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_sign_out -> {
                MainActivityViewModel.reset()

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

    private fun onAddClicked() {
        Log.i(TAG, "onAddClicked called, starting AddFavourActivity")

        val intent = Intent(this, AddFavourActivity::class.java)
        startActivity(intent)
    }

    private fun onGoogleMapButtonClicked() {
        Log.i(TAG, "onGoogleMapButtonClicked called, starting MainMapActivity")

        val intent = Intent(this, MainMapActivity::class.java)
        startActivity(intent)
    }

    private fun onFilterClicked() {
        Log.i(TAG, "onFilterClicked called, starting FilterDialogFragment")

        filterDialog.show(supportFragmentManager, FilterDialogFragment.TAG)
    }
}
