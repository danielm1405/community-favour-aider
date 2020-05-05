package com.example.communityfavouraider.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.communityfavouraider.R
import com.example.communityfavouraider.model.Favour
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query

open class FavourAdapter(query: Query, private val listener: OnFavourSelectedListener) :
                FirestoreAdapter<FavourAdapter.ViewHolder>(query) {

    abstract class OnFavourSelectedListener {
        abstract fun onFavourSelected(restaurant: DocumentSnapshot?)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return ViewHolder(inflater.inflate(R.layout.item_favour, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        private var titleView: TextView = itemView.findViewById(R.id.favour_item_title)
        private var descriptionView: TextView = itemView.findViewById(R.id.favour_item_description)
        private var userNameView: TextView = itemView.findViewById(R.id.favour_item_user_name)

        fun bind(snapshot: DocumentSnapshot, listener: OnFavourSelectedListener?) {
            val favour: Favour? = snapshot.toObject(Favour::class.java)

            titleView.text = favour?.title
            descriptionView.text = favour?.description
            userNameView.text = favour?.userName
        }
    }
}
