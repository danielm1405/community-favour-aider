package com.example.communityfavouraider

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity


class AddFavourActivity : AppCompatActivity(), View.OnClickListener {

    private var favourTitle: EditText? = null
    private var favourDescription: EditText? = null
    private var favourCity: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_favour)

        favourTitle = findViewById(R.id.add_favour_title)
        favourDescription = findViewById(R.id.add_favour_description)
        favourCity = findViewById(R.id.add_favour_city)

        // set on click listeners for all the buttons
        findViewById<Button>(R.id.add_favour_submit).setOnClickListener(this)
        findViewById<Button>(R.id.add_favour_cancel).setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v!!.id) {
            R.id.add_favour_cancel -> onCancelClicked()
            R.id.add_favour_submit -> onSubmitClicked()
        }
    }

    private fun onCancelClicked() {
        onBackPressed()
    }

    private fun onSubmitClicked() {
        // TODO: implement submission
    }
}
