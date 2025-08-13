package com.example.fantom  // Замените на ваш пакет!

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.fantom.R

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val chatName = intent.getStringExtra("chat_name") ?: "Чат"
        val textViewTitle: TextView = findViewById(R.id.textViewChatTitle)
        textViewTitle.text = chatName

        // Здесь реализуйте логику загрузки сообщений по chat_id
    }
}
