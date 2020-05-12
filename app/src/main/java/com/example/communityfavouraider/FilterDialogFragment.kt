package com.example.communityfavouraider

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.Query

class FilterDialogFragment : DialogFragment(), View.OnClickListener {

    interface FilterListener {
        fun onFilter(filters: Filters)
    }

    companion object {
        const val TAG = "FilterDialogFragment"
    }

    private lateinit var rootView: View

    private lateinit var optionSpinner: Spinner
    private lateinit var sortSpinner: Spinner

    private lateinit var filterListener: FilterListener

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?) : View? {
        rootView = inflater.inflate(R.layout.dialog_filters, container, false)

        optionSpinner = rootView.findViewById(R.id.spinner_option)
        sortSpinner = rootView.findViewById(R.id.spinner_sort)

        rootView.findViewById<Button>(R.id.button_search).setOnClickListener(this)
        rootView.findViewById<Button>(R.id.button_cancel).setOnClickListener(this)

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is FilterListener) {
            filterListener = context as FilterListener
        }
    }

    override fun onResume() {
        super.onResume()
        dialog!!.window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.button_search -> onSearchClicked()
            R.id.button_cancel -> onCancelClicked()
        }
    }

    private fun onSearchClicked() {
        Log.i(TAG, "onSearchClicked")

        filterListener.onFilter(getFilters())

        dismiss()
    }

    private fun onCancelClicked() {
        Log.i(TAG, "onCancelClicked")

        dismiss()
    }

    private fun getSelectedOption(): String? {
        val selected = optionSpinner.selectedItem as String
        return if (getString(R.string.value_any_option) == selected) {
            null
        } else {
            selected
        }
    }

    private fun getSelectedSortBy(): String? {
        val selected = sortSpinner.selectedItem as String
        if (getString(R.string.sort_by_submit_date) == selected) {
            return "timeStamp"
        } else {
            return null
        }
    }

    private fun getSortDirection(): Query.Direction? {
        val selected = sortSpinner.selectedItem as String
        if (getString(R.string.sort_by_submit_date) == selected) {
            return Query.Direction.DESCENDING
        } else {
            return null
        }
    }

    private fun getFilters(): Filters {
        val filters = Filters()

        filters.option = getSelectedOption()

        filters.sortBy = getSelectedSortBy()
        filters.sortDirection = getSortDirection()

        return filters
    }
}