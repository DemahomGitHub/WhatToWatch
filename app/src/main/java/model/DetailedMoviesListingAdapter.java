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
public class DetailedMoviesListingAdapter extends MoviesListingAdapter {
    public DetailedMoviesListingAdapter(Context context, int resource, ArrayList<AllMoviesModel> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder mHolder = null;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = super.getInflater().inflate(super.getResource(), null);
            mHolder.setImage((ImageView) convertView.findViewById(R.id.ivAndroidPosterID));
            mHolder.setTitle((TextView) convertView.findViewById(R.id.tvMovieTitleID));
            mHolder.setLanguage((TextView) convertView.findViewById(R.id.tvMovieOriginalLanguageID));
            mHolder.setReleaseDate((TextView) convertView.findViewById(R.id.tvMovieReleaseDateID));
            mHolder.setCategory((TextView) convertView.findViewById(R.id.tvMovieCategoryID));
            mHolder.setRating((RatingBar) convertView.findViewById(R.id.rbMovieRatingBarID));
            mHolder.setOverview((TextView) convertView.findViewById(R.id.tvMovieOverviewID));
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder)convertView.getTag();
        }

        // Then later, when you want to display image
        String posterPath = super.getAllMoviesModels().get(position).getPosterPath();
        if (posterPath == null || posterPath.equals(""))
            posterPath="none";

        ImageLoader.getInstance().displayImage(super.getUrl() + posterPath, mHolder.getImage());


        mHolder.getTitle().setText(super.getAllMoviesModels().get(position).getTitle());
        mHolder.getLanguage().setText("Original language : " + super.getAllMoviesModels().get(position).getOriginalLanguage());
        mHolder.getReleaseDate().setText("Release date : " + super.getAllMoviesModels().get(position).getReleaseDate());
        mHolder.getRating().setRating(super.getAllMoviesModels().get(position).getVoteAverage() / 2);
        mHolder.getOverview().setText(super.getAllMoviesModels().get(position).getOverview());

        TextView descriptionTitle = (TextView) convertView.findViewById(R.id.tvMovieOverviewTitleID);
        descriptionTitle.setText("Synopsis");

        mHolder.getRating().setFocusable(false);


        String genre = super.getAllMoviesModels().get(position).getCategory();
        if (genre == null)
            genre = "unknown";

        mHolder.getCategory().setText("Genre : " + genre);

        return convertView;
    }
}
