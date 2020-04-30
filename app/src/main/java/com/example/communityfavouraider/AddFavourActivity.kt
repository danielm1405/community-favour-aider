package com.example.communityfavouraider

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class AddFavourActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var favourTitle: EditText
    private lateinit var favourDescription: EditText
    private lateinit var favourCity: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_favour)

        favourTitle = findViewById(R.id.add_favour_title)
        favourDescription = findViewById(R.id.add_favour_description)
        favourCity = findViewById(R.id.add_favour_city)

        setValidators()

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

    private fun setValidators() {
        favourTitle.validate("Title should have at least 10 characters.") {
                s -> s.length >= 10
        }

        favourDescription.validate("Description should have at least 20 characters.") {
                s -> s.length >= 20
        }

        favourCity.validate("City name should be Warsaw or Cracow.") {
            // TODO: real validation
                s -> s == "Warsaw" || s == "Cracow"
        }
    }

    private fun isAnyEditTextInErrorState(): Boolean {
        return favourTitle.error != null ||
                favourDescription.error != null ||
                favourCity.error != null
    }

    private fun onCancelClicked() {
        onBackPressed()
    }

    private fun onSubmitClicked() {
        if (isAnyEditTextInErrorState()) {
            // TODO: make popup window
            findViewById<TextView>(R.id.add_favour_text).text = "Error occured!"

            return
        }

        findViewById<TextView>(R.id.add_favour_text).text = "OK!"
    }
}
