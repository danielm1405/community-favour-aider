package com.example.communityfavouraider

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.communityfavouraider.adapter.FavourAdapter
import com.example.communityfavouraider.viewmodel.MainActivityViewModel
import com.firebase.ui.auth.AuthUI
import com.google.api.client.util.DateTime
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*


class MainActivity : AppCompatActivity(),
                     View.OnClickListener {

    private val TAG = "MainActivity"
    private val RC_SIGN_IN = 9001
    private val LIMIT: Long = 20

    private lateinit var viewModel: MainActivityViewModel
    private lateinit var favourRecycler: RecyclerView

    private var query: Query = FirebaseFirestore.getInstance().collection("favours")
        .orderBy("timeStamp", Query.Direction.DESCENDING)
        .limit(LIMIT)

    private var onFavourSelectedListener: FavourAdapter.OnFavourSelectedListener =
        object : FavourAdapter.OnFavourSelectedListener(this) {
            override fun onFavourSelected(favour: DocumentSnapshot?) {
                Log.i(TAG, "Favour selected, go to activity that shows details")

                val intent = Intent(context, FavourDetailsActivity::class.java)
                intent.putExtra(FavourDetailsActivity.KEY_FAVOUR_ID, favour?.id)
                startActivity(intent)
            }
        }

    private val favourAdapter: FavourAdapter =
        object : FavourAdapter(query, onFavourSelectedListener) {
            override fun onDataChanged() {
                if (itemCount == 0) {
                    favourRecycler.visibility = View.GONE
                } else {
                    favourRecycler.visibility = View.VISIBLE
                }
            }
        }


    @ExperimentalStdlibApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT > 9) {
            val policy =
                StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
        }

        FirebaseFirestore.setLoggingEnabled(true)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        favourRecycler = findViewById(R.id.recycler_restaurants)
        favourRecycler.layoutManager = LinearLayoutManager(this)
        favourRecycler.adapter = favourAdapter

        findViewById<View>(R.id.add_button).setOnClickListener(this)

        if (shouldStartSignIn()) {
            startSignIn()
        }

        val googleCalendar = GoogleCalendar()
        val calendarService = googleCalendar.calendarService
        // List the next 10 events from the primary calendar.
        val now = DateTime(System.currentTimeMillis())
        val events = calendarService.events()
            .list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
            .execute()
        val items = events.items

        if (items.size == 0) {
            Log.i(TAG, "No upcoming events found.")
        } else {
            Log.i(TAG, "Upcoming events")
            for (event in items) {
                var start: DateTime? = event.start.dateTime
                if (start == null) {
                    start = event.start.date
                }
                Log.i(TAG, "${event.summary} $start")
            }
        }
    }

    override fun onStart() {
        super.onStart()

        favourAdapter.startListening()
    }

    override fun onStop() {
        super.onStop()

        favourAdapter.stopListening()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.add_button -> onAddClicked(v)
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
        val intent = Intent(this, AddFavourActivity::class.java)
        startActivity(intent)
    }

}
