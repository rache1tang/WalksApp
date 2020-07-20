package com.example.walksapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    EditText etNewPass;
    EditText etRepeatPass;
    Button btnSave;
    Button btnCancel;
    TextView tvNoMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        etRepeatPass = findViewById(R.id.etReEnterPassword);
        etNewPass = findViewById(R.id.etEnterPassword);
        tvNoMatch = findViewById(R.id.tvPasswordsDiff);

        // hide "wrong" notices
        tvNoMatch.setText("");

        btnCancel = findViewById(R.id.btnPasswordCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnSave = findViewById(R.id.btnSavePassword);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check if passwords repeat
                String newPass = etNewPass.getText().toString();
                String newRepeat = etRepeatPass.getText().toString();
                if (!newPass.equals(newRepeat)) {
                    tvNoMatch.setText("Passwords do not match");
                } else {
                    ParseUser.getCurrentUser().setPassword(newPass);
                    finish();
                }
            }
        });
    }
}