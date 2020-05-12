package com.example.communityfavouraider

import com.google.firebase.firestore.Query


class Filters {
    public var option: String? = null

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

    public fun hasSortBy () : Boolean {
        return sortBy != null && sortBy!!.isNotBlank() && sortDirection != null
    }


}