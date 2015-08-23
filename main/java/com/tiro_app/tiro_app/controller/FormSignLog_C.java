package com.tiro_app.tiro_app.controller;

import android.app.Activity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.tiro_app.tiro_app.R;

/**
 * Created by user on 01/07/2015.
 */
public class FormSignLog_C {

    public Activity _activity;
    public Boolean theReturn  = true ;

    public String mail, username, password,gender, age;

    public FormSignLog_C(Activity activity){
        this._activity = activity;
    }

    public void checkMail(String mail, TextView error){
        EmailValidator ev = new EmailValidator();

        if(ev.validate(mail.trim())){
            error.setVisibility(View.GONE);
            this.mail = mail;
        }
        else{
            error.setText(R.string.signIn_error_mail_invalid);
            error.setVisibility(View.VISIBLE);
            this.theReturn = false;
        }
    }

    public void checkUsername(String username, TextView error){

        int usernameLength = username.length();


        if(usernameLength > 3 & usernameLength < 30){
            UsernameValidator uv = new UsernameValidator();
            error.setVisibility(View.GONE);
            if(uv.validate(username)){
                error.setVisibility(View.GONE);
                this.username = username;

            }else{
                error.setText(R.string.signIn_error_username_notValid);
                error.setVisibility(View.VISIBLE);
                this.theReturn = false;
            }

        }
        else{
            error.setText(R.string.signIn_error_username_tooShort);
            error.setVisibility(View.VISIBLE);
            this.theReturn = false;
        }

    }

    public void checkPassword (TextView password, TextView error){


        if (password.length()>= 8){
            error.setVisibility(View.GONE);
            for (char ch: password.getText().toString().toCharArray()) {
                if(ch == ' '){
                    error.setText("Whitespace has been deleted");
                    error.setVisibility(View.VISIBLE);
                    this.theReturn = false;
                }
            }
            this.password = password.getText().toString().replace(" ", "");
            password.setText(password.getText().toString().replace(" ", ""));

        }
        else{
            error.setText(R.string.signIn_error_password_short);
            error.setVisibility(View.VISIBLE);
            this.theReturn = false;
        }
    }

    public void checkSex (RadioGroup radioSex, TextView error){
        int selected = radioSex.getCheckedRadioButtonId();
        RadioButton b = (RadioButton) this._activity.findViewById(selected);


        if(b != null){
            error.setVisibility(View.GONE);
            this.gender = b.getText().toString();
        }else{
            error.setText(R.string.signIn_error_sex);
            error.setVisibility(View.VISIBLE);
            this.theReturn = false;
        }

    }

    public void checkAge(TextView age, TextView error){

        if(!age.getText().toString().equals("")){
            int realAge =Integer.parseInt(age.getText().toString());

            error.setVisibility(View.GONE);
            if (realAge < 13) {
                error.setText(R.string.signIn_TextView_Age);
                error.setVisibility(View.VISIBLE);
                this.theReturn = false;
            } else if (realAge > 99) {
                error.setText(R.string.signIn_TextView_Age_old);
                error.setVisibility(View.VISIBLE);
                this.theReturn = false;
            } else {
                error.setVisibility(View.GONE);
                this.age = age.getText().toString();
            }
        }
    }

    public String toString(){
        return String.format("User : Username - %s ; mail - %s ; age - %s ; password - %s ; gender - %s", this.username, this.mail, this.age, this.password, this.gender);
    }
}
