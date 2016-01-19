package model;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.toure.demahom.whattowatch.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Demahom on 14/12/2015.
 */
public class MoviesListingAdapter extends ArrayAdapter {
    public MoviesListingAdapter(Context context, int resource, ArrayList<AllMoviesModel> objects) {
        super(context, resource, objects);
        this.allMoviesModels = objects;
        this.resource = resource;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }

    public ArrayList<AllMoviesModel> getAllMoviesModels() {
        return allMoviesModels;
    }

    public void setAllMoviesModels(ArrayList<AllMoviesModel> allMoviesModels) {
        this.allMoviesModels = allMoviesModels;
    }

    public int getResource() {
        return resource;
    }

    public void setResource(int resource) {
        this.resource = resource;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public void setInflater(LayoutInflater inflater) {
        this.inflater = inflater;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private ArrayList<AllMoviesModel> allMoviesModels;
    private int resource;
    private LayoutInflater inflater;
    private String url = "http://image.tmdb.org/t/p/w500";
}
