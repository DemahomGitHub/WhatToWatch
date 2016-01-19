package model;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Demahom on 03/12/2015.
 */
public class User {
    public User(String username, String password, java.sql.Date birthDate) {
        this.username = username;
        this.password = password;
        this.birthDate = birthDate;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public java.sql.Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(java.sql.Date birthDate) {
        this.birthDate = birthDate;
    }


    private String username;
    private String password;
    private java.sql.Date birthDate;
}
