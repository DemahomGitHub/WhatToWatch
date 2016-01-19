package com.toure.demahom.whattowatch.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.toure.demahom.whattowatch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import model.AllFavoritesModel;
import model.AllMoviesModel;
import model.DetailedMoviesListingAdapter;
import model.FavoriteMovie;
import model.User;

public class FavoritesMoviesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_movies_listing);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        initializeImagesLoader();

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait while loading");

        progressDialog.show();
        (new GetFavoritesMoviesFromDataBaseTask()).execute(urlFavoritesMovies);

    }

    public void initializeImagesLoader() {
        // Create default options which will be used for every
        //  displayImage(...) call if no options will be passed to this method
        DisplayImageOptions defaultOptions = new DisplayImageOptions
                .Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .build();
        ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getApplicationContext())
                .defaultDisplayImageOptions(defaultOptions)
                .build();

        ImageLoader.getInstance().init(config); // Do it on Application start

        // Create global configuration and initialize ImageLoader
        ImageLoaderConfiguration imgLoaderConfig = new ImageLoaderConfiguration.Builder(this).build();
        ImageLoader.getInstance().init(imgLoaderConfig);
    }

    public class GetFavoritesMoviesFromDataBaseTask extends AsyncTask<String,String,ArrayList<AllFavoritesModel>> {

        public GetFavoritesMoviesFromDataBaseTask() {
        }

        @Override
        protected ArrayList<AllFavoritesModel> doInBackground(String... params) {
            ArrayList<AllFavoritesModel> result = new ArrayList<>();
            try {
                createConnection(params[0]);
                reader = retrieveDataFromServer();
                readBuffer();
                result = parseJsonObject();

            } catch (MalformedURLException e) {
                Log.i("Exception: ", e.getMessage());
            } catch (IOException e) {
                Log.i("Exception: ", e.getMessage());
            }
            finally {
                closeConnection(connection,reader);
                return result;
            }
        }

        private ArrayList<AllFavoritesModel> parseJsonObject() {
            String jsonObjectString = buffer.toString();
            ArrayList<AllFavoritesModel> result = new ArrayList<>();

            try {
                JSONArray globalArray = new JSONArray(jsonObjectString);

                for (int i=0; i < globalArray.length(); i++) {
                    JSONObject jObject = globalArray.getJSONObject(i);
                    String email = jObject.getString("email");
                    Integer id = jObject.getInt("idFilm");
                    result.add(new AllFavoritesModel(email,id));
                }
            }
            catch (Exception e) {
                Log.i("Exception : ",e.getMessage());
            }
            finally {
                return result;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<AllFavoritesModel> result) {
            //super.onPostExecute(result);
            allFavorites = result;
            getMoviesFromServer();
        }

        private void createConnection(String link) throws IOException{
            URL url = new URL(link);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
        }

        private BufferedReader retrieveDataFromServer() throws IOException {
            InputStream stream = connection.getInputStream();

            return new BufferedReader(new InputStreamReader(stream));
        }

        private void readBuffer() throws IOException {
            buffer = new StringBuffer();
            String line="";
            while ((line=reader.readLine()) != null) {
                buffer.append(line);
            }
        }

        private void closeConnection(HttpURLConnection connection, BufferedReader reader) {
            if (connection != null) {
                connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.i("Connection Exception:", "No input Stream");
                    }
                }
            }
        }
        private HttpURLConnection connection = null;
        private BufferedReader reader = null;
        private StringBuffer buffer = null;
    }

    public void getMoviesFromServer() {
        if (hasFavorite(allFavorites)) {
            for (AllFavoritesModel movie : allFavorites) {
                if (movie.getEmail().equals(MyTabbedActivity.getsUserEmail())) {
                    String url = urlMovieById + movie.getMovieId() + API_KEY;
                    (new GetMovieFromServerTask()).execute(url);
                    numberOfTaskLaunched++;
                }
            }
        } else {
            progressDialog.dismiss();
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.msgEmptyFavoriteList),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    public boolean hasFavorite(ArrayList<AllFavoritesModel> favorites) {
        for (AllFavoritesModel movie : favorites) {
            if (movie.getEmail().equals(MyTabbedActivity.getsUserEmail()))
                return true;
        }
        return false;
    }

    public class GetMovieFromServerTask extends AsyncTask<String,String,AllMoviesModel> {

        public GetMovieFromServerTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected AllMoviesModel doInBackground(String... params) {
            AllMoviesModel movie = null;
            try {
                createConnection(params[0]);
                reader = retrieveDataFromServer();
                readBuffer();
                movie = this.parseJsonObject();

            } catch (MalformedURLException e) {
                Log.i("Exception: ", e.getMessage());
            } catch (IOException e) {
                Log.i("Exception: ", e.getMessage());
            } finally {
                closeConnection(connection, reader);
                return movie;
            }
        }

        public AllMoviesModel parseJsonObject() {
            String jsonObjectString = buffer.toString();
            AllMoviesModel result = null;

            try {
                JSONObject jsonObject = new JSONObject(jsonObjectString);

                result = getMovieFromJsonArray(jsonObject);
            } catch (Exception e) {
                Log.i("Exception : ", e.getMessage());
            }
            finally {
                return result;
            }
        }

        public AllMoviesModel getMovieFromJsonArray(JSONObject jsonObject) throws Exception {
            AllMoviesModel moviesModel = new AllMoviesModel();

            moviesModel.setId(jsonObject.getInt("id"));
            moviesModel.setOriginalLanguage(jsonObject.getString("original_language"));
            moviesModel.setTitle(jsonObject.getString("title"));
            moviesModel.setReleaseDate(jsonObject.getString("release_date"));
            moviesModel.setPosterPath(jsonObject.getString("poster_path"));
            moviesModel.setVoteAverage(jsonObject.getInt("vote_average"));
            moviesModel.setPopularity(jsonObject.getInt("popularity"));
            moviesModel.setOverview(jsonObject.getString("overview"));

            JSONArray genre = jsonObject.getJSONArray("genres");
            JSONObject jObj = genre.getJSONObject(0);

            moviesModel.setCategory(jObj.getString("name"));

            return moviesModel;
        }

        @Override
        protected void onPostExecute(AllMoviesModel result) {
            super.onPostExecute(result);

            if (allMoviesModels == null)
                allMoviesModels = new ArrayList<>();

            allMoviesModels.add(result);
            numberOfTaskFinished++;

            if (numberOfTaskFinished == numberOfTaskLaunched)
                setFavoritesListView();
        }

        private void createConnection(String link) throws IOException {
            URL url = new URL(link);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
        }

        private BufferedReader retrieveDataFromServer() throws IOException {
            InputStream stream = connection.getInputStream();

            return new BufferedReader(new InputStreamReader(stream));
        }

        private void readBuffer() throws IOException {
            buffer = new StringBuffer();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        }

        private void closeConnection(HttpURLConnection connection, BufferedReader reader) {
            if (connection != null) {
                connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.i("Connection Exception:", "No input Stream");
                    }
                }
            }
        }

        private HttpURLConnection connection = null;
        private BufferedReader reader = null;
        private StringBuffer buffer = null;
    }

    public void setFavoritesListView() {
        progressDialog.dismiss();

        DetailedMoviesListingAdapter adapter = new DetailedMoviesListingAdapter(
                getApplicationContext(),
                R.layout.favorites_movies_row,
                allMoviesModels
        );

        lvFavoritesListView = (ListView) findViewById(R.id.lvFavoritesListViewID);
        if (allMoviesModels != null) {
            if (allMoviesModels.size() > 0)
                lvFavoritesListView.setAdapter(adapter);
            else {
                TextView tvResults = (TextView)findViewById(R.id.tvResultsTitleID);
                tvResults.setText("No favorite yet");
            }
        }
    }


    private ArrayList<AllMoviesModel> allMoviesModels;
    private ArrayList<AllFavoritesModel> allFavorites;
    private ProgressDialog progressDialog;
    private final String urlFavoritesMovies = "http://whattowatchapi.azurewebsites.net/api/filmfavoris";
    private String urlMovieById = "http://api.themoviedb.org/3/movie/";
    private final String API_KEY = "?api_key=cc571afb50c2a89ff9f46b85c4e36a9d";
    private ListView lvFavoritesListView;
    private int numberOfTaskLaunched = 0;
    private int numberOfTaskFinished = 0;
}
