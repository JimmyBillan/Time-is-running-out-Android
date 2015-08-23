package com.tiro_app.tiro_app.controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by user on 01/07/2015.
 */
public class UsernameValidator {
    private Pattern pattern;
    private Matcher matcher;

    private static final String USERNAME_PATTERN = "^[a-z0-9_-]{4,30}$";

    public UsernameValidator(){
        pattern = Pattern.compile(USERNAME_PATTERN);
    }

    /**
     * Validate tv_username with regular expression
     * @param username tv_username for validation
     * @return true valid tv_username, false invalid tv_username
     */
    public boolean validate(final String username){

        matcher = pattern.matcher(username);
        return matcher.matches();

    }
}
