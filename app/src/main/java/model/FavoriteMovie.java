package model;

/**
 * Created by Demahom on 03/12/2015.
 */
public class FavoriteMovie {
    public FavoriteMovie(String username, Integer movieID) {
        this.username = username;
        this.movieID = movieID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getMovieID() {
        return movieID;
    }

    public void setMovieID(Integer movieID) {
        this.movieID = movieID;
    }

    private String username;
    private Integer movieID;
}
