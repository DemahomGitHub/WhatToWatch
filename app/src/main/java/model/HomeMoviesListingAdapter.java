package model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.toure.demahom.whattowatch.R;

import java.util.ArrayList;

/**
 * Created by Demahom on 15/12/2015.
 */
public class HomeMoviesListingAdapter extends MoviesListingAdapter {
    public HomeMoviesListingAdapter(Context context, int resource, ArrayList<AllMoviesModel> objects) {
        super(context, resource, objects);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = super.getInflater().inflate(super.getResource(), null);
            holder = new ViewHolder();
            holder.setImage((ImageView) convertView.findViewById(R.id.ivAndroidPosterID));
            holder.setTitle((TextView) convertView.findViewById(R.id.tvMovieTitleID));
            holder.setLanguage((TextView) convertView.findViewById(R.id.tvMovieOriginalLanguageID));
            holder.setReleaseDate((TextView) convertView.findViewById(R.id.tvMovieReleaseDateID));
            holder.setRating((RatingBar) convertView.findViewById(R.id.rbMovieRatingBarID));
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        String posterPath = super.getAllMoviesModels().get(position).getPosterPath();
        if (posterPath != null) {
            ImageLoader.getInstance().displayImage(super.getUrl() + posterPath, holder.getImage()); // Default options will be used
            holder.getImage().setClickable(true);
        }

        holder.getTitle().setText(super.getAllMoviesModels().get(position).getTitle());
        holder.getTitle().setClickable(true);
        holder.getLanguage().setText("Original language : " + super.getAllMoviesModels().get(position).getOriginalLanguage());
        holder.getReleaseDate().setText("Release date : " + super.getAllMoviesModels().get(position).getReleaseDate());
        holder.getRating().setRating(super.getAllMoviesModels().get(position).getVoteAverage() / 2);
        holder.getRating().setFocusable(false);

        return convertView;
    }
}
