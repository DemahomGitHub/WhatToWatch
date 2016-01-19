package model;

import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * Created by Demahom on 22/12/2015.
 */
public class ViewHolder {
    public ViewHolder() {
    }

    public ImageView getImage() {
        return image;
    }

    public void setImage(ImageView image) {
        this.image = image;
    }

    public TextView getTitle() {
        return title;
    }

    public void setTitle(TextView title) {
        this.title = title;
    }

    public TextView getLanguage() {
        return language;
    }

    public void setLanguage(TextView language) {
        this.language = language;
    }

    public TextView getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(TextView releaseDate) {
        this.releaseDate = releaseDate;
    }

    public TextView getCategory() {
        return category;
    }

    public void setCategory(TextView category) {
        this.category = category;
    }

    public RatingBar getRating() {
        return rating;
    }

    public void setRating(RatingBar rating) {
        this.rating = rating;
    }

    public TextView getDescriptionTitle() {
        return descriptionTitle;
    }

    public void setDescriptionTitle(TextView descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public TextView getOverview() {
        return overview;
    }

    public void setOverview(TextView overview) {
        this.overview = overview;
    }

    private ImageView image ;
    private TextView title ;
    private TextView language;
    private TextView releaseDate ;
    private TextView category;
    private RatingBar rating;
    private TextView descriptionTitle;
    private TextView overview;
}
