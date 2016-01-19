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
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.toure.demahom.whattowatch.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import model.AllFavoritesModel;
import model.FavoriteMovie;
import model.User;

public class SingleMovieDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait while adding the movie to favorites");

        (new GetFavoritesMoviesFromDataBaseTask()).execute(url);

        initializeImagesLoader();
        setContentDetails();
        setLikeButton();

    }
    public void setLikeButton() {
        FloatingActionButton fab = (FloatingActionButton)findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (notFavorite()) {
                    Log.i("Test","Pas favoris");
                    progressDialog.show();
                    (new HttpPostAsyncTask()).execute(url);
                }
                else {
                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(R.string.msgMovieAddingToFavoritesError),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
        });
    }

    public boolean notFavorite() {
        for (AllFavoritesModel movie : allFavorites) {
            Log.i("Email1",movie.getEmail()+", mv id 1: "+movie.getMovieId());
            Log.i("Email2",mFavoriteMovie.getUsername()+", mv id 2: "+mFavoriteMovie.getMovieID());
            Log.i("sUserM",sUserEmail);
            if (movie.getEmail().equals(sUserEmail) && movie.getMovieId() == mFavoriteMovie.getMovieID()) {
                return false;
            }
        }
        return true;
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

    public void setContentDetails() {
        ImageView mPoster = ((ImageView) findViewById(R.id.ivAndroidPosterID));
        TextView mTitle = ((TextView) findViewById(R.id.tvMovieTitleID));
        TextView mLanguage = ((TextView) findViewById(R.id.tvMovieOriginalLanguageID));
        TextView mReleaseDate = ((TextView) findViewById(R.id.tvMovieReleaseDateID));
        TextView mCategory = ((TextView) findViewById(R.id.tvMovieCategoryID));
        RatingBar mRating = ((RatingBar) findViewById(R.id.rbMovieRatingBarID));
        TextView mOverview = ((TextView) findViewById(R.id.tvMovieOverviewID));


        Bundle myReceivedIntent = this.getIntent().getExtras();
        Bundle myReceivedMovie = myReceivedIntent.getBundle("mMovie");

        Integer id = myReceivedMovie.getInt("id");
        sUserEmail = MyTabbedActivity.getsUserEmail();
        mFavoriteMovie = new FavoriteMovie(sUserEmail,id);
        ImageLoader.getInstance().displayImage(
                basePosterPath + myReceivedMovie.getString("poster_path"),
                mPoster
        );
        mTitle.setText("Title : " + myReceivedMovie.getString("title"));
        mLanguage.setText("Language : " + myReceivedMovie.getString("original_language"));
        mReleaseDate.setText("Release date : "+myReceivedMovie.getString("release_date"));
        mCategory.setText("Genre : "+"Unknown");
        mRating.setRating(myReceivedMovie.getInt("vote_average") / 2);
        mOverview.setText(myReceivedMovie.getString("overview"));
    }
    public String POST(String url, FavoriteMovie movie){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("email", movie.getUsername());
            jsonObject.accumulate("idFilm", movie.getMovieID());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
            else
                result = "Can not send data";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }

    public class HttpPostAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            return POST(params[0],mFavoriteMovie);
        }
        protected void onPostExecute(String result) {
            boolean isRegistered=false;
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String email = jsonObject.getString("email");
                String id = jsonObject.getString("idFilm");
                if (email.equals(sUserEmail))
                    isRegistered = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (isRegistered) {
                Toast.makeText(
                        getBaseContext(),
                        "Successfully added to favorites",
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                Toast.makeText(
                        getBaseContext(),
                        "Can not add the movie to favorites",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
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
            super.onPostExecute(result);
            allFavorites = result;
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

    private ArrayList<AllFavoritesModel> allFavorites;
    private String basePosterPath = "http://image.tmdb.org/t/p/w500";
    private static String sUserEmail;
    private FavoriteMovie mFavoriteMovie;
    private ProgressDialog progressDialog;
    private final String url = "http://whattowatchapi.azurewebsites.net/api/filmfavoris";
}
