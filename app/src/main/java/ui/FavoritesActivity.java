package com.example.fantom.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fantom.R;
import com.example.fantom.adapter.FavoritesAdapter;
import com.example.fantom.database.AppDatabase;
import com.example.fantom.database.FavoriteMessageDao;
import com.example.fantom.model.FavoriteMessage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FavoritesActivity extends AppCompatActivity implements FavoritesAdapter.OnFavoriteClickListener {

    private RecyclerView recyclerView;
    private FavoritesAdapter adapter;
    private FavoriteMessageDao favoriteMessageDao;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        recyclerView = findViewById(R.id.rvFavorites);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new FavoritesAdapter(this);
        recyclerView.setAdapter(adapter);

        favoriteMessageDao = AppDatabase.getInstance(getApplicationContext()).favoriteMessageDao();
        executorService = Executors.newSingleThreadExecutor();

        favoriteMessageDao.getAllFavorites().observe(this, favoriteMessages -> {
            adapter.setFavoriteMessages(favoriteMessages);
        });
    }

    @Override
    public void onFavoriteClick(FavoriteMessage message) {
        Toast.makeText(this, "Открыть сообщение: " + message.getMessageText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFavoriteRemove(FavoriteMessage message) {
        executorService.execute(() -> favoriteMessageDao.delete(message));
    }
}
