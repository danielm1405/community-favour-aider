package com.example.communityfavouraider

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

import com.example.communityfavouraider.viewmodel.MainActivityViewModel

import com.firebase.ui.auth.AuthUI

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "MainActivity"
    private val RC_SIGN_IN = 9001

    private lateinit var viewModel: MainActivityViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseFirestore.setLoggingEnabled(true)

        viewModel = ViewModelProvider(this)[MainActivityViewModel::class.java]

        findViewById<View>(R.id.add_button).setOnClickListener(this)

        if (shouldStartSignIn()) {
            startSignIn()
        }

        findViewById<TextView>(R.id.main_text).text = FirebaseAuth.getInstance().currentUser?.uid.toString()
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.add_button -> onAddClicked(v)
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
        val intent = Intent(this, AddFavourActivity::class.java)
        startActivity(intent)
    }

}
