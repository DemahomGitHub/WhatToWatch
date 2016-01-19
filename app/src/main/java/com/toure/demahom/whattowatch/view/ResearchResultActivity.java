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
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.toure.demahom.whattowatch.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import model.AllMoviesModel;
import model.DetailedMoviesListingAdapter;

public class ResearchResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_research_result);
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


        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); */

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait while loading");

        Bundle myReceivedIntent = this.getIntent().getExtras();
        Bundle myReceivedQuery = myReceivedIntent.getBundle("queryObject");
        String query = myReceivedQuery.getString("queryKey");

        myMovieListingTask = new MoviesListingTask();
        myFullQuery = myFullQuery+query;
        myMovieListingTask.execute(myFullQuery);

    }
    public class MoviesListingTask extends AsyncTask<String,String,ArrayList<AllMoviesModel>> {

        public MoviesListingTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected ArrayList<AllMoviesModel> doInBackground(String... params) {
            ArrayList<AllMoviesModel> moviesList = new ArrayList<>();
            try {
                createConnection(params[0]);
                reader = retrieveDataFromServer();
                readBuffer();
                moviesList = this.parseJsonObject();

            } catch (MalformedURLException e) {
                Log.i("Exception: ", e.getMessage());
            } catch (IOException e) {
                Log.i("Exception: ", e.getMessage());
            } finally {
                closeConnection(connection, reader);
                return moviesList;
            }
        }

        public ArrayList<AllMoviesModel> parseJsonObject() {
            String jsonObjectString = buffer.toString();
            ArrayList<AllMoviesModel> result = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < jsonArray.length(); i++) {
                    result.add(this.getMovieFromJsonArray(jsonArray, i));
                }
            } catch (Exception e) {
                Log.i("Exception : ", e.getMessage());
            }
            finally {
                return result;
            }
        }

        public AllMoviesModel getMovieFromJsonArray(JSONArray jsonArray, int position) throws Exception {
            AllMoviesModel moviesModel = new AllMoviesModel();
            JSONObject jObject = jsonArray.getJSONObject(position);

            moviesModel.setId(jObject.getInt("id"));
            moviesModel.setOriginalLanguage(jObject.getString("original_language"));
            moviesModel.setTitle(jObject.getString("title"));
            moviesModel.setReleaseDate(jObject.getString("release_date"));
            moviesModel.setPosterPath(jObject.getString("poster_path") == null ? "" : jObject.getString("poster_path"));
            moviesModel.setVoteAverage(jObject.getInt("vote_average"));
            moviesModel.setPopularity(jObject.getInt("popularity"));
            moviesModel.setOverview(jObject.getString("overview"));

            return moviesModel;
        }

        @Override
        protected void onPostExecute(ArrayList<AllMoviesModel> result) {
            super.onPostExecute(result);
            myQueryResults = result;
            setResultsListView();
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

    private void setResultsListView() {
        DetailedMoviesListingAdapter adapter = new DetailedMoviesListingAdapter(
                getApplicationContext(),
                R.layout.result_row,
                myQueryResults
        );

        lvMyQueryResultsListView = (ListView) findViewById(R.id.lvResultsListViewID);
        lvMyQueryResultsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle mMovie = new Bundle();
                Intent mIntent = new Intent(getApplicationContext(), SingleMovieDetailsActivity.class);

                AllMoviesModel mMovieToSend = myQueryResults.get(position);

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
        if (myQueryResults != null) {
            if (myQueryResults.size() > 0)
                lvMyQueryResultsListView.setAdapter(adapter);
            else {
                TextView tvResults = (TextView)findViewById(R.id.tvResultsTitleID);
                tvResults.setText("No result for your query");
            }
        }
        progressDialog.dismiss();
    }

    private ListView lvMyQueryResultsListView;
    private ArrayList<AllMoviesModel> myQueryResults;
    private String myFullQuery=""
            +"http://api.themoviedb.org/3/search/movie?api_key=cc571afb50c2a89ff9f46b85c4e36a9d&query=";
    private MoviesListingTask myMovieListingTask;
    private ProgressDialog progressDialog;
}
