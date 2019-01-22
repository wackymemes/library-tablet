package com.wackymemes.library_tablet;

import android.util.Log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CurrentUser {
    private static CurrentUser sharedInstance = null;
    private static final String TAG = CurrentUser.class.getSimpleName();

    public static CurrentUser getInstance() {
        if (sharedInstance == null) {
            sharedInstance = new CurrentUser();
        }
        return sharedInstance;
    }

    private static User user = new User();
    private static List<User> users = new ArrayList<User>();

    private CurrentUser() {}

    public boolean processJson(String json) {
        ObjectMapper mapper = new ObjectMapper();

        User[] usersArray = new User[4];
        try {
            usersArray = mapper.readValue(json, User[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        users = new ArrayList<User>(Arrays.asList(usersArray));
        return !users.isEmpty();
    }

    public void setLoggedIn() {
        user = users.get(0);
        users.remove(0);
    }

    public void clearUser() {
        user.id = 0;
        user.username = null;
        user.firstName = null;
        user.lastName = null;
        users.clear();
        Log.d(TAG, "user cleared");
    }

    public String getUsername() {
        return user.username;
    }

    // TODO: add photo from Google account
    public static class User {
        private int id;
        private String username;
        private String firstName;
        private String lastName;

        @JsonCreator
        public User(@JsonProperty("id") int id,
                    @JsonProperty("username") String username,
                    @JsonProperty("first_name") String firstName,
                    @JsonProperty("last_name") String lastName) {
            this.id = id;
            this.username = username;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public User() { this(0, null, null, null); }

        public String getUsername() { return username; }
        public void setUsername(String username) {
            this.username = username;
        }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }
}
