package com.example.fantom  // Замените на ваш пакет!

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fantom.R

class ChatsAdapter(
    private val onChatClick: (chatId: String, chatName: String) -> Unit
) : ListAdapter<ChatItem, ChatsAdapter.ChatViewHolder>(ChatDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_tile, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatItem = getItem(position)
        holder.bind(chatItem, onChatClick)
    }

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textName: TextView = itemView.findViewById(R.id.textChatName)

        fun bind(chatItem: ChatItem, onChatClick: (String, String) -> Unit) {
            textName.text = chatItem.name
            itemView.setOnClickListener {
                onChatClick(chatItem.id, chatItem.name)
            }
            if (chatItem.isPersonal) {
                itemView.setBackgroundResource(R.drawable.bg_personal_chat_tile)
            } else {
                itemView.setBackgroundResource(R.drawable.bg_normal_chat_tile)
            }
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<ChatItem>() {
    override fun areItemsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: ChatItem, newItem: ChatItem): Boolean = oldItem == newItem
}
