package com.toure.demahom.whattowatch.view;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.DocumentsContract;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.toure.demahom.whattowatch.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import model.AllCategoriesModel;
import model.AllMoviesModel;
import model.CategoryTabFragment;
import model.FavoriteMovie;
import model.HomeMoviesListingAdapter;
import model.HomeTabFragment;
import model.MoviesCategoryAdapter;
import model.MoviesListingAdapter;
import model.MyFragmentPagerAdapter;
import model.ResearchTabFragment;
import model.TopTenFragment;
import model.TopTenMoviesListingAdapter;

public class MyTabbedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tabbed);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait while loading...");

        homeMoviesListing = new MoviesListingTask();
        homeMoviesListing.execute(homeUrl);

        initializeViewPager();
        initializeTabHost();
        handleTabHost();
        getUserEmailFromIntent();
    }

    public void getUserEmailFromIntent() {
        Bundle mReceivedIntent = this.getIntent().getExtras();
        if (mReceivedIntent !=  null) {
            Bundle mReceivedEmail = mReceivedIntent.getBundle("userEmail");
            sUserEmail = mReceivedEmail.getString("email");
        }
    }

    public void setTopTenContentView() {
        MoviesListingAdapter adapter = new TopTenMoviesListingAdapter(
                getApplicationContext(),
                R.layout.top_ten_row,
                topTenMovies
        );
        lvTopTenListView = (ListView) findViewById(R.id.lvTopTenListViewID);
        lvTopTenListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle mMovie = new Bundle();
                Intent mIntent = new Intent(getApplicationContext(), SingleMovieDetailsActivity.class);

                AllMoviesModel mMovieToSend = topTenMovies.get(position);

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

        if (topTenMovies != null)
            lvTopTenListView.setAdapter(adapter);

        progressDialog.dismiss();
    }

    public void setResearchContentView() {
        Button btnConfirmResearch = (Button)findViewById(R.id.btnResearchID);
        final EditText etMovieTitleField = (EditText)findViewById(R.id.etMovieTitleFieldID);

        btnConfirmResearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getEnteredTextAndLaunchResultActivity(etMovieTitleField);
            }
        });

        etMovieTitleField.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (event != null && keyCode == event.KEYCODE_ENTER) {
                    getEnteredTextAndLaunchResultActivity(etMovieTitleField);
                    return true;
                }
                return false;
            }
        });
    }

    public void getEnteredTextAndLaunchResultActivity(EditText etMovieTitleField) {
        String query = etMovieTitleField.getText().toString();
        if (isValidQuery(query)) {
            Bundle myQuery = new Bundle();
            Intent resultActivity = new Intent(MyTabbedActivity.this, ResearchResultActivity.class);

            query = prepareQuery(query);
            myQuery.putString("queryKey", query);
            resultActivity.putExtra("queryObject", myQuery);
            startActivity(resultActivity);
            etMovieTitleField.getText().clear();
        }
    }
    public String prepareQuery(String query){
        String result="";
        for (int i=0; i < query.length(); i++) {
            if (query.charAt(i) == ' ')
                result += '+';
            else
                result += query.charAt(i);

        }
        return result;
    }

    public boolean isValidQuery(String query) {
        return !query.equals("") && query.length() > 1;
    }


    public void setHomeListView() {
        MoviesListingAdapter adapter = new HomeMoviesListingAdapter(
                getApplicationContext(),
                R.layout.home_tab_row,
                allHomeMovies
        );

        lvHomeTabListView = (ListView) findViewById(R.id.lvHomeTabListViewID);
        lvHomeTabListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Bundle mMovie = new Bundle();
                Intent mIntent = new Intent(MyTabbedActivity.this, SingleMovieDetailsActivity.class);

                AllMoviesModel mMovieToSend = allHomeMovies.get(position);

                mMovie.putInt("id",mMovieToSend.getId());
                mMovie.putString("original_language", mMovieToSend.getOriginalLanguage());
                mMovie.putString("title", mMovieToSend.getTitle());
                mMovie.putString("release_date", mMovieToSend.getReleaseDate());
                mMovie.putString("poster_path", mMovieToSend.getPosterPath());
                mMovie.putInt("vote_average", mMovieToSend.getVoteAverage());
                mMovie.putInt("popularity", mMovieToSend.getPopularity());
                mMovie.putString("overview", mMovieToSend.getOverview());
                mMovie.putString("userEmail",MyTabbedActivity.sUserEmail);

                mIntent.putExtra("mMovie",mMovie);
                startActivity(mIntent);
            }
        });

        if (allHomeMovies != null)
            lvHomeTabListView.setAdapter(adapter);

        progressDialog.dismiss();
    }


    private void setCategoryListView() {
        MoviesCategoryAdapter adapter = new MoviesCategoryAdapter(
                MyTabbedActivity.this,
                R.layout.category_tab_row,
                allCategoriesModels
        );

        lvMoviesCategories = (ListView) findViewById(R.id.lvMoviesCategoriesID);
        lvMoviesCategories.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked, int position, long id) {
                int categoryID = allCategoriesModels.get(position).getId();
                String categoryName = allCategoriesModels.get(position).getName();
                Bundle dataToSend = new Bundle();
                Intent categoryToSend = new Intent(MyTabbedActivity.this, SingleCategoryActivity.class);

                dataToSend.putInt("id", categoryID);
                dataToSend.putString("name", categoryName);
                categoryToSend.putExtra("categoryObject", dataToSend);

                startActivity(categoryToSend);
            }
        });
        if (allCategoriesModels != null)
            lvMoviesCategories.setAdapter(adapter);
    }

    private void handleTabHost() {
        this.mTabs.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                setSelectedItemAndSmoothScrollToCenter();
                displayDataInTheSelectedTab(tabId);
            }

            public void setSelectedItemAndSmoothScrollToCenter() {
                int selectedItem = mTabs.getCurrentTab();
                mViewPager.setCurrentItem(selectedItem);

                // To put the current tab in the middle of the screen
                HorizontalScrollView hScrollView = (HorizontalScrollView) findViewById(R.id.hScrollView);
                View mTabView = mTabs.getCurrentTabView();
                int scrollPosition = mTabs.getLeft() - (hScrollView.getWidth() - mTabView.getWidth()) / 2;
                hScrollView.smoothScrollTo(scrollPosition, 0);
            }

            public void displayDataInTheSelectedTab(String tabId) {
                if (tabId.equals(tabsTitles[0])) {
                    if (homeMoviesListing == null) {
                        homeMoviesListing = new MoviesListingTask();
                        homeMoviesListing.execute(homeUrl);
                    }
                    setHomeListView();
                } else if (tabId.equals(tabsTitles[1])) {
                    if (categoryMovieListing == null) {
                        categoryMovieListing = new CategoriesListingTask();
                        categoryMovieListing.execute(categoriesNameUrl);
                    }
                    setCategoryListView();

                } else if (tabId.equals(tabsTitles[2])) {
                    if (topTenListingTask == null) {
                        topTenListingTask = new TopTenListingTask();
                        topTenListingTask.execute(topTenUrl);
                    }
                    setTopTenContentView();
                } else
                    setResearchContentView();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tabbed_actitivty, menu);
        return true;
    }

    public void initializeViewPager() {
        this.mViewPager = (ViewPager)findViewById(R.id.view_pager);
        this.fragmentsList = new ArrayList<>();

        fragmentsList.add(new HomeTabFragment());
        fragmentsList.add(new CategoryTabFragment());
        fragmentsList.add(new TopTenFragment());
        fragmentsList.add(new ResearchTabFragment());

        MyFragmentPagerAdapter mFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager(), fragmentsList);

        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mTabs.setCurrentTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initializeTabHost() {
        initTabsTitles();

        mTabs = (TabHost) findViewById(android.R.id.tabhost);
        mTabs.setup();

        for (int i=0; i < tabsTitles.length; i++) {
            TabHost.TabSpec tSpec = mTabs.newTabSpec(tabsTitles[i]);
            tSpec.setIndicator(tabsTitles[i]);
            tSpec.setContent(new TabContent(getApplicationContext()));
            mTabs.addTab(tSpec);
        }
    }

    public void initTabsTitles() {
        tabsTitles[0] = getResources().getString(R.string.tvHomeTabName);
        tabsTitles[1] = getResources().getString(R.string.tvCategoryTabName);
        tabsTitles[2] = getResources().getString(R.string.tvTopTenTabName);
        tabsTitles[3] = getResources().getString(R.string.tvResearchTabName);
    }

    private class TabContent implements TabHost.TabContentFactory {

        public TabContent(Context context) {
            this.context = context;
        }

        @Override
        public View createTabContent(String tag) {
            View cont = new View(context);

            cont.setMinimumHeight(0);
            cont.setMinimumWidth(0);

            return cont;
        }
        private Context context;
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
            moviesModel.setPosterPath(jObject.getString("poster_path"));
            moviesModel.setVoteAverage(jObject.getInt("vote_average"));
            moviesModel.setPopularity(jObject.getInt("popularity"));
            moviesModel.setOverview(jObject.getString("overview"));

           return moviesModel;
        }

        @Override
        protected void onPostExecute(ArrayList<AllMoviesModel> result) {
            super.onPostExecute(result);
            allHomeMovies = result;
            setHomeListView();
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

        public StringBuffer getBuffer() {
            return buffer;
        }

        private HttpURLConnection connection = null;
        private BufferedReader reader = null;
        private StringBuffer buffer = null;
    }

    public class TopTenListingTask extends MoviesListingTask {
        public TopTenListingTask() {
            super();
        }
        public ArrayList<AllMoviesModel> parseJsonObject() {
            String jsonObjectString = super.getBuffer().toString();
            ArrayList<AllMoviesModel> result = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                JSONArray jsonArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < 10; i++) {
                    result.add(this.getMovieFromJsonArray(jsonArray,i));
                }
            } catch (Exception e) {
                Log.i("Exception : ", e.getMessage());
            }
            finally {
                return result;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<AllMoviesModel> result) {
            topTenMovies = result;
            setTopTenContentView();
        }
    }

    public class CategoriesListingTask extends AsyncTask<String,String,ArrayList<AllCategoriesModel>> {

        public CategoriesListingTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected ArrayList<AllCategoriesModel> doInBackground(String... params) {
            ArrayList<AllCategoriesModel> result = new ArrayList<>();
            try {
                createConnection(params[0]);
                reader = retrieveDataFromServer();
                readBuffer();
                result= parseJsonObject();

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

        private ArrayList<AllCategoriesModel> parseJsonObject() {
            String jsonObjectString = buffer.toString();
            ArrayList<AllCategoriesModel> result = new ArrayList<>();

            try {
                JSONObject jsonObject = new JSONObject(jsonObjectString);
                JSONArray jsonArray = jsonObject.getJSONArray("genres");
                for (int i=0; i < jsonArray.length(); i++) {
                    JSONObject jObject = jsonArray.getJSONObject(i);
                    int id = jObject.getInt("id");
                    String name = jObject.getString("name");
                    result.add(new AllCategoriesModel(id,name));
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
        protected void onPostExecute(ArrayList<AllCategoriesModel> result) {
            //super.onPostExecute(result);
            allCategoriesModels = result;
            setCategoryListView();
            progressDialog.dismiss();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_favorites :
                startActivity(new Intent(this,FavoritesMoviesActivity.class));
                return  true;
            case R.id.action_settings :
                Toast.makeText(
                        getApplicationContext(),
                        "No setting",
                        Toast.LENGTH_LONG
                ).show();
                return true;
            case R.id.action_disconnect:
                this.finish();
                return true;
            default :
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getsUserEmail() {
        return sUserEmail;
    }

    private ViewPager mViewPager;
    private ArrayList<Fragment> fragmentsList;
    private TabHost mTabs;
    private final String[] tabsTitles = new String[4];
    private ArrayList<AllCategoriesModel> allCategoriesModels;
    private ArrayList<AllMoviesModel> topTenMovies;
    private ArrayList<AllMoviesModel> allHomeMovies;
    private ListView lvMoviesCategories;
    private ListView lvHomeTabListView;
    private ListView lvTopTenListView;
    private ProgressDialog progressDialog;
    private MoviesListingTask topTenListingTask, homeMoviesListing;
    private CategoriesListingTask categoryMovieListing=null;
    private static String sUserEmail;

    private final String API_KEY = "cc571afb50c2a89ff9f46b85c4e36a9d";
    private final String categoriesNameUrl = "http://api.themoviedb.org/3/genre/movie/list?api_key="+API_KEY;
    private final String homeUrl="http://api.themoviedb.org/3/discover/movie?api_key=cc571afb50c2a89ff9f46b85c4e36a9d&page=10";
    private final String topTenUrl="http://api.themoviedb.org/3/movie/top_rated?api_key=cc571afb50c2a89ff9f46b85c4e36a9d";
}
