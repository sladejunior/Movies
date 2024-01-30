package com.sladejunior.movies30.data;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainViewModel extends AndroidViewModel {
    private static MovieDatabase database;

    private LiveData<List<Movie>> movies;
    private LiveData<List<FavouriteMovie>> favourite_movies;

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
    public LiveData<List<FavouriteMovie>> getFavouriteMovies() {return favourite_movies;}

    public MainViewModel(@NonNull Application application) {
        super(application);
        database = MovieDatabase.getInstance(application);
        movies = database.movieDao().getAllMovies();
        favourite_movies = database.movieDao().getAllFavouriteMovies();
    }

    public Movie getMovieById(int id){
        try {
            return new GetMovieTask().execute(id).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public FavouriteMovie getFavouriteMovieById(int id){
        try {
            return new GetFavouriteMovieTask().execute(id).get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAllMovies(){
        new DeleteMovieTask().execute();
    }

    public void insertMovie(Movie movie){
        new InsertTask().execute(movie);
    }
    public void DeleteMovie(Movie movie){
        new DeleteTask().execute(movie);
    }


    public void insertFavouriteMovie(FavouriteMovie movie){
        new InsertFavouriteTask().execute(movie);
    }
    public void deleteFavouriteMovie(FavouriteMovie movie){
        new DeleteFavouriteTask().execute(movie);
    }


    private static class DeleteTask  extends AsyncTask<Movie, Void, Void> {
        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies != null && movies.length>0){
                database.movieDao().deleteMovie(movies[0]);
            }
            return null;
        }}

    private static class InsertTask  extends AsyncTask<Movie, Void, Void>{
        @Override
        protected Void doInBackground(Movie... movies) {
            if(movies != null && movies.length>0){
                database.movieDao().insertMovie(movies[0]);
            }
            return null;
        }}

    private static class DeleteFavouriteTask  extends AsyncTask<FavouriteMovie, Void, Void> {
        @Override
        protected Void doInBackground(FavouriteMovie... favouriteMovies) {
            if(favouriteMovies != null && favouriteMovies.length>0){
                database.movieDao().deleteFavouriteMovie(favouriteMovies[0]);
            }
            return null;
        }}

    private static class InsertFavouriteTask  extends AsyncTask<FavouriteMovie, Void, Void>{
        @Override
        protected Void doInBackground(FavouriteMovie... favouriteMovies) {
            if(favouriteMovies != null && favouriteMovies.length>0){
                database.movieDao().insertFavouriteMovie(favouriteMovies[0]);
            }
            return null;
        }}

    private static class DeleteMovieTask  extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... integers) {
            database.movieDao().deleteAllMovies();
            return null;
        }
    }

    private static class GetMovieTask extends AsyncTask<Integer, Void, Movie> {
    @Override
    protected Movie doInBackground(Integer... integers) {
        if (integers != null && integers.length > 0) {
            return database.movieDao().getMovieById(integers[0]);
            }
        return null;
        }
    }

    private static class GetFavouriteMovieTask extends AsyncTask<Integer, Void, FavouriteMovie> {
        @Override
        protected FavouriteMovie doInBackground(Integer... integers) {
            if (integers != null && integers.length > 0) {
                return database.movieDao().getFavouriteMovieById(integers[0]);
            }
            return null;
        }
    }

}
