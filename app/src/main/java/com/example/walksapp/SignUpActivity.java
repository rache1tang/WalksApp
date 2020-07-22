package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    public static final String TAG = "SignUpActivity";

    EditText etUsername;
    EditText etPassword;
    EditText etPassRepeat;
    Button btnMakeAcc;
    EditText etName;
    boolean userExists;
    EditText etEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        etUsername = findViewById(R.id.etSignupUsername);
        etPassword = findViewById(R.id.etSignupPassword);
        etPassRepeat = findViewById(R.id.etPasswordRepeat);
        btnMakeAcc = findViewById(R.id.btnMakeAccount);
        etName = findViewById(R.id.etSignupName);
        etEmail = findViewById(R.id.etSignupEmail);

        btnMakeAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString();
                String password = etPassword.getText().toString();
                String passRepeat = etPassRepeat.getText().toString();
                String name = etName.getText().toString();
                String email = etEmail.getText().toString();

                makeAccount(username, name, password, passRepeat, email);
            }
        });
    }

    private void makeAccount(String username, String name, String password, String passRepeat, String email) {
        if (username.isEmpty() || password.isEmpty() || passRepeat.isEmpty() || name.isEmpty() || email.isEmpty()) {
            Toast.makeText(SignUpActivity.this, "All fields must be filled out", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(passRepeat)) {
            Toast.makeText(SignUpActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userExists(username)) {
            Toast.makeText(SignUpActivity.this, "Username is already taken", Toast.LENGTH_SHORT).show();
            return;
        }

        ParseUser user = new ParseUser();
        user.setEmail(email);
        user.setPassword(password);
        user.setUsername(username);
        user.put("name", name);

        user.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error saving new user", e);
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Sign in to use app!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

    }

    private boolean userExists(String username) {
        ParseQuery<ParseUser> query = ParseQuery.getQuery(ParseUser.class);
        query.whereEqualTo("username", username);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if (e != null) {
                    Log.e(TAG, "error checking username");
                    return;
                }
                userExists = objects.size() > 0;
            }
        });
        return userExists;
    }
}