package com.toure.demahom.whattowatch.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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

import model.AllCategoriesModel;
import model.AllMoviesModel;
import model.DetailedMoviesListingAdapter;
import model.MoviesListingAdapter;

import static java.lang.Thread.sleep;

public class SingleCategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        Bundle intent = this.getIntent().getExtras();
        Bundle categoryReceived = intent.getBundle("categoryObject");
        categoriesModel = new AllCategoriesModel(categoryReceived.getInt("id"),categoryReceived.getString("name"));

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);


        //this.setCategoryID(categoriesModel.getId());
        //oneCategoryUrl = firstUrlPart + categoryID + secondUrlPart;

        (new SingleCategoryMoviesListingTask()).execute(firstUrlPart + this.categoriesModel.getId() + secondUrlPart);
    }

    public class SingleCategoryMoviesListingTask extends AsyncTask<String,String,ArrayList<AllMoviesModel>> {

        public SingleCategoryMoviesListingTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected ArrayList<AllMoviesModel> doInBackground(String... params) {
            try {
                createConnection(params[0]);
                reader = retrieveDataFromServer();
                readBuffer();
                return parseJsonObject();

            } catch (MalformedURLException e) {
                Log.i("Exception: ", e.getMessage());
            } catch (IOException e) {
                Log.i("Exception: ", e.getMessage());
            } finally {
                closeConnection(connection, reader);
            }
            return null;
        }

        private ArrayList<AllMoviesModel> parseJsonObject() {
            String jsonObjectString = buffer.toString();
            ArrayList<AllMoviesModel> result = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    AllMoviesModel moviesModel = new AllMoviesModel();
                    JSONObject jObject = jsonArray.getJSONObject(i);

                    moviesModel.setId(jObject.getInt("id"));
                    moviesModel.setOriginalLanguage(jObject.getString("original_language"));
                    moviesModel.setTitle(jObject.getString("title"));
                    moviesModel.setReleaseDate(jObject.getString("release_date"));
                    moviesModel.setPosterPath(jObject.getString("poster_path"));
                    moviesModel.setVoteAverage(jObject.getInt("vote_average"));
                    moviesModel.setPopularity(jObject.getInt("popularity"));
                    moviesModel.setOverview(jObject.getString("overview"));
                    moviesModel.setCategory(categoriesModel.getName());

                    result.add(moviesModel);
                }
            } catch (Exception e) {
                Log.i("Exception : ", e.getMessage());
            }
            return result;
        }

        @Override
        protected void onPostExecute(ArrayList<AllMoviesModel> result) {
            super.onPostExecute(result);
            allMoviesModels = result;


            MoviesListingAdapter adapter = new DetailedMoviesListingAdapter(
                    getApplicationContext(),
                    R.layout.single_category_row,
                    allMoviesModels
            );
            lvSingleCategoryListing = (ListView) findViewById(R.id.lvSingleCategoryListViewID);
            lvSingleCategoryListing.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Bundle mMovie = new Bundle();
                    Intent mIntent = new Intent(getApplicationContext(), SingleMovieDetailsActivity.class);

                    AllMoviesModel mMovieToSend = allMoviesModels.get(position);

                    mMovie.putInt("id",mMovieToSend.getId());
                    mMovie.putString("original_language", mMovieToSend.getOriginalLanguage());
                    mMovie.putString("title", mMovieToSend.getTitle());
                    mMovie.putString("release_date", mMovieToSend.getReleaseDate());
                    mMovie.putString("poster_path", mMovieToSend.getPosterPath());
                    mMovie.putInt("vote_average", mMovieToSend.getVoteAverage());
                    mMovie.putInt("popularity", mMovieToSend.getPopularity());
                    mMovie.putString("overview", mMovieToSend.getOverview());
                    mMovie.putString("userEmail",MyTabbedActivity.getsUserEmail());

                    mIntent.putExtra("mMovie",mMovie);
                    startActivity(mIntent);
                }
            });
            lvSingleCategoryListing.setAdapter(adapter);
            progressDialog.dismiss();
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

    private final String API_KEY = "cc571afb50c2a89ff9f46b85c4e36a9d";
    //private Integer categoryID=null;
    //private String oneCategoryUrl=null;
    private String firstUrlPart = "http://api.themoviedb.org/3/genre/";
    private String secondUrlPart = "/movies?api_key="+API_KEY;;
    //private ApplicationController myAppController;
    private ArrayList<AllMoviesModel> allMoviesModels;
    private ProgressDialog progressDialog;
    private ListView lvSingleCategoryListing;
    private AllCategoriesModel categoriesModel;
}
