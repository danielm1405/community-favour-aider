package com.example.communityfavouraider

import android.content.Context
import com.google.firebase.firestore.Query
import java.lang.StringBuilder


class Filters {
    public var option: String? = null
    public var status: String? = null

    public var sortBy: String? = null
    public var sortDirection: Query.Direction? = null

    companion object {
        public fun getDefault() : Filters {
            val filters = Filters()

            filters.sortBy = "timeStamp"
            filters.sortDirection = Query.Direction.DESCENDING

            return filters
        }
    }

    public fun hasOption() : Boolean {
        return option != null && option!!.isNotBlank()
    }

    public fun hasStatus() : Boolean {
        return status != null && status!!.isNotBlank()
    }

    public fun hasSortBy () : Boolean {
        return sortBy != null && sortBy!!.isNotBlank() && sortDirection != null
    }

    public fun getFiltersDescription() : String {
        val desc = StringBuilder()

        if (option == null) {
            desc.append("<b>")
            desc.append("All")
            desc.append("</b>")
        }

        if (option != null) {
            desc.append("<b>")
            desc.append(option?.toLowerCase()?.capitalize() + "s")
            desc.append("</b>")
        }

        if (status != null) {
            desc.append(" with status ")
            desc.append("<b>")
            desc.append(status?.toLowerCase())
            desc.append("</b>")
        }

        return desc.toString()
    }

    public fun getSortingDescription(context: Context) : String {
        if (sortBy == "title") {
            return context.getString(R.string.sort_by_title)
        } else if (sortBy == "timeStamp") {
            return context.getString(R.string.sort_by_submit_date)
        }

        // should never reach it
        return "No sorting applied"
    }
}