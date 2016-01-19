package model;

/**
 * Created by Demahom on 28/12/2015.
 */
public class AllFavoritesModel {
    public AllFavoritesModel() {
    }

    public AllFavoritesModel(String email, Integer movieId) {
        this.email = email;
        this.movieId = movieId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getMovieId() {
        return movieId;
    }

    public void setMovieId(Integer movieId) {
        this.movieId = movieId;
    }

    private String email;
    private Integer movieId;
}
