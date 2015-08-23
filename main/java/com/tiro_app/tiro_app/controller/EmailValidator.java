package com.tiro_app.tiro_app.controller;

/**
 * Created by user on 01/07/2015.
 */

        import java.util.regex.Matcher;
        import java.util.regex.Pattern;

public class EmailValidator {

    private Pattern pattern;
    private Matcher matcher;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public EmailValidator() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    /**
     * Validate mail with regular expression
     *
     * @param mail
     *            hex for validation
     * @return true valid mail, false invalid mail
     */
    public boolean validate(final String mail) {

        matcher = pattern.matcher(mail);
        return matcher.matches();

    }
}
