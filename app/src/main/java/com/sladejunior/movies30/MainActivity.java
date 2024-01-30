package com.sladejunior.movies30;


import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sladejunior.movies30.adapters.MovieAdapter;
import com.sladejunior.movies30.data.MainViewModel;
import com.sladejunior.movies30.data.Movie;
import com.sladejunior.movies30.utils.JSONUtils;
import com.sladejunior.movies30.utils.NetworkUtils;

import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<JSONObject> {
    private RecyclerView recyclerViewPosters;
    private MovieAdapter movieAdapter;
    private Switch switchSort;
    private TextView textViewPopularity;
    private TextView textViewTopRated;

    private MainViewModel viewModel;
    private Menu itemMain;
    private Menu itemFav;


    private static final int LOADER_ID = 133;
    private LoaderManager loaderManager;

    private static int page = 1;
    private static boolean isLoading = false;
    private static int methodOfSort;
    private ProgressBar progressBarLoading;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        itemMain = findViewById(R.id.item_main);
        itemFav = findViewById(R.id.itemFavourite);

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
        setContentView(R.layout.activity_main);
       loaderManager = LoaderManager.getInstance(this);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        recyclerViewPosters = findViewById(R.id.recyclerViewPosters);
        movieAdapter = new MovieAdapter();
        switchSort = findViewById(R.id.switchSort);
        recyclerViewPosters.setAdapter(movieAdapter);
        textViewPopularity = findViewById(R.id.textViewPopularity);
        textViewTopRated = findViewById(R.id.textViewTopRated);
        progressBarLoading = findViewById(R.id.progressBarLoading);


        switchSort.setChecked(true);
        switchSort.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
               page = 1;
               setMethodOfSort(isChecked);
            }
        });
        switchSort.setChecked(false);

        movieAdapter.setOnPosterClickListener(new MovieAdapter.OnPosterClickListener() {
            @Override
            public void onPosterClick(int position) {
                Movie movie = movieAdapter.getMovies().get(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", movie.getId());
                startActivity(intent);
                Toast.makeText(MainActivity.this, String.valueOf(movie.getId()), Toast.LENGTH_SHORT).show();

            }
        });
        movieAdapter.setOnReachEndListener(new MovieAdapter.OnReachEndListener() {
            @Override
            public void onReachEnd() {
                if(!isLoading){
                downloadData(methodOfSort,page);
                }
            }
        });

        textViewTopRated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMethodOfSort(true);
            }
        });
        textViewPopularity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setMethodOfSort(false);
            }
        });

        recyclerViewPosters.setLayoutManager(new GridLayoutManager(this,getColumnCount()));

        LiveData<List<Movie>> moviesFromLiveData = viewModel.getMovies();
        moviesFromLiveData.observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(List<Movie> movies) {
                if(page==1){
                    movieAdapter.setMovies(movies);
                }
            }
        });
    }

    private int getColumnCount(){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = (int) (displayMetrics.widthPixels / displayMetrics.density);
        return Math.max(width / 185, 2);
    }

    public void setMethodOfSort(boolean isTopRated){
        if(isTopRated){
            methodOfSort = NetworkUtils.TOP_RATED;
            textViewTopRated.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            textViewPopularity.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            switchSort.setChecked(true);
        } else {
            methodOfSort = NetworkUtils.POPULARITY;
            textViewTopRated.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            textViewPopularity.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
            switchSort.setChecked(false);
        }
        downloadData(methodOfSort,page);

    }

    public void downloadData(int methodOfSort, int page){
        URL url = NetworkUtils.buildURL(methodOfSort, page);
        Bundle bundle = new Bundle();
        bundle.putString("url", url.toString());
        loaderManager.restartLoader(LOADER_ID, bundle, this);
    }


    @NonNull
    @Override
    public Loader<JSONObject> onCreateLoader(int id, @Nullable Bundle bundle) {
        NetworkUtils.JSONLoader jsonLoader = new NetworkUtils.JSONLoader(this,bundle);
        jsonLoader.setOnStartLoadingListener(new NetworkUtils.JSONLoader.OnStartLoadingListener() {
            @Override
            public void onStartLoading() {
                progressBarLoading.setVisibility(View.VISIBLE);
                isLoading = true;
            }
        });
        return jsonLoader;
    }

    @Override
    public void onLoadFinished(@NonNull androidx.loader.content.Loader<JSONObject> loader, JSONObject jsonObject) {
        ArrayList<Movie> movies = JSONUtils.getMoviesFromJSON(jsonObject);
        if (movies != null && !movies.isEmpty()) {
            if(page==1){
            viewModel.deleteAllMovies();
            movieAdapter.clear();
            }
            for (Movie movie : movies) {
                viewModel.insertMovie(movie);
            }
            movieAdapter.addMovies(movies);
            page++;
        }
        isLoading = false;
        loaderManager.destroyLoader(LOADER_ID);
        progressBarLoading.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull androidx.loader.content.Loader<JSONObject> loader) {

    }


}
