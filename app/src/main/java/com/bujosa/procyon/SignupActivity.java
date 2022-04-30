package com.bujosa.procyon;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    public static final String EMAIL_PARAM = "email_parameter";

    private TextInputLayout  registerEmail, registerPassword, registerConfirmPassword;

    private AutoCompleteTextView registerEmailAutoComplete, registerPasswordAutoComplete, registerConfirmPasswordAutoComplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        String emailParam = getIntent().getStringExtra(EMAIL_PARAM);


        registerEmail = findViewById(R.id.register_email);
        registerPassword = findViewById(R.id.register_password);
        registerConfirmPassword = findViewById(R.id.register_password_confirmation);

        registerEmailAutoComplete = findViewById(R.id.register_email_auto_complete);
        registerPasswordAutoComplete = findViewById(R.id.register_password_auto_complete);
        registerConfirmPasswordAutoComplete = findViewById(R.id.register_password_confirmation_auto_complete);

        registerEmailAutoComplete.setText(emailParam);

       findViewById(R.id.register_button).setOnClickListener(l -> {
           if(registerEmailAutoComplete.getText().length() == 0){
               registerEmail.setErrorEnabled(true);
               registerEmail.setError(getString(R.string.register_error_user));
           } else  if(registerPasswordAutoComplete.getText().length() == 0){
               registerPassword.setErrorEnabled(true);
               registerPassword.setError(getString(R.string.register_error_password));
           } else if (registerConfirmPasswordAutoComplete.getText().length() == 0){
               registerConfirmPassword.setErrorEnabled(true);
               registerConfirmPassword.setError(getString(R.string.register_error_password));
           } else if(registerEmailAutoComplete.getText().toString().equals(registerConfirmPasswordAutoComplete.getText().toString())){
               registerPassword.setErrorEnabled(true);
               registerPassword.setError(getString(R.string.register_error_password_not_match));
           } else {
               FirebaseAuth.getInstance().createUserWithEmailAndPassword(registerEmailAutoComplete.getText().toString(),
                       registerPasswordAutoComplete.getText().toString()).addOnCompleteListener(
                               task -> {
                                   if(task.isSuccessful()){
                                       Toast.makeText(this, R.string.register_created, Toast.LENGTH_SHORT).show();
                                       SignupActivity.this.finish();
                                   }else{
                                       Toast.makeText(this, R.string.register_created_error, Toast.LENGTH_SHORT).show();
                                   }
                               }
               );
           }
       });

    }
}