package com.sladejunior.movies30;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.sladejunior.movies30.adapters.MovieAdapter;
import com.sladejunior.movies30.data.FavouriteMovie;
import com.sladejunior.movies30.data.MainViewModel;
import com.sladejunior.movies30.data.Movie;

import java.util.ArrayList;
import java.util.List;

public class FavouriteActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFavouriteMovies;
    private MovieAdapter adapter;
    private MainViewModel viewModel;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
              final int idMain = R.id.item_main;
        final int idFav = R.id.itemFavourite;
        int id = item.getItemId();
        if(id == idMain) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else if (id==idFav) {
            Intent intentFav = new Intent(this, FavouriteActivity.class);
            startActivity(intentFav);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite);
        recyclerViewFavouriteMovies = findViewById(R.id.recyclerViewFavouriteMovies);
        adapter = new MovieAdapter();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        recyclerViewFavouriteMovies.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerViewFavouriteMovies.setAdapter(adapter);
        LiveData<List<FavouriteMovie>> favouriteMovies = viewModel.getFavouriteMovies();

        favouriteMovies.observe(this, new Observer<List<FavouriteMovie>>() {
            @Override
            public void onChanged(List<FavouriteMovie> favouriteMovies) {
                if(favouriteMovies!=null){
                List<Movie> movies = new ArrayList<>(favouriteMovies);
                adapter.setMovies(movies);}
            }
        });
        adapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = adapter.getMovies().get(position);
                Intent intent = new Intent(FavouriteActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                startActivity(intent);
            }
        });

    }
}