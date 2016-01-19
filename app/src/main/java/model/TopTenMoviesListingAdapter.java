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
 * Created by Demahom on 16/12/2015.
 */
public class TopTenMoviesListingAdapter extends MoviesListingAdapter {
    public TopTenMoviesListingAdapter(Context context, int resource, ArrayList<AllMoviesModel> objects) {
        super(context, resource, objects);
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        if (convertView == null) {
            mHolder = new model.ViewHolder();
            convertView = super.getInflater().inflate(super.getResource(), null);
            mHolder.setImage((ImageView) convertView.findViewById(R.id.ivAndroidPosterID));
            mHolder.setTitle((TextView) convertView.findViewById(R.id.tvMovieTitleID));
            mHolder.setLanguage((TextView) convertView.findViewById(R.id.tvMovieOriginalLanguageID));
            mHolder.setReleaseDate((TextView) convertView.findViewById(R.id.tvMovieReleaseDateID));
            mHolder.setCategory((TextView) convertView.findViewById(R.id.tvMovieCategoryID));
            mHolder.setRating((RatingBar) convertView.findViewById(R.id.rbMovieRatingBarID));
            convertView.setTag(mHolder);
        } else {
            mHolder = (model.ViewHolder)convertView.getTag();
        }

        String posterPath = super.getAllMoviesModels().get(position).getPosterPath();
        if (posterPath != null) {
            ImageLoader.getInstance().displayImage(super.getUrl() + posterPath, mHolder.getImage()); // Default options will be used
            mHolder.getImage().setClickable(true);
        }

        mHolder.getTitle().setText(super.getAllMoviesModels().get(position).getTitle());
        mHolder.getLanguage().setText("Original language : " + super.getAllMoviesModels().get(position).getOriginalLanguage());
        mHolder.getReleaseDate().setText("Release date : " + super.getAllMoviesModels().get(position).getReleaseDate());
        mHolder.getRating().setRating(super.getAllMoviesModels().get(position).getVoteAverage() / 2);
        mHolder.getRating().setFocusable(false);

        return convertView;
    }
}
