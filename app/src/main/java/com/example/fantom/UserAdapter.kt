package com.example.fantom

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class UserAdapter(private val userList: List<User>) :
    RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        val userName: TextView = itemView.findViewById(R.id.tv_user_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.displayName
        if (user.avatarUrl.isNotEmpty()) {
            // Загрузка круглой аватарки с помощью Glide
            Glide.with(holder.itemView.context)
                .load(user.avatarUrl)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.userAvatar)
        } else {
            Glide.with(holder.itemView.context)
                .load(R.drawable.ic_profile_placeholder)
                .apply(RequestOptions.circleCropTransform())
                .into(holder.userAvatar)
        }
    }

    override fun getItemCount(): Int = userList.size
}