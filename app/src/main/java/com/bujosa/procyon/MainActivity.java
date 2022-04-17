package com.bujosa.procyon;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.AutoTransition;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 534543;
    private FirebaseAuth oAuth;
    private Button signInButtonMail, signUpButton, signInButtonGoogle;

    private ProgressBar progressBar;

    private TextInputLayout loginEmailParent;
    private TextInputLayout loginPasswordParent;

    private AutoCompleteTextView loginEmail;
    private AutoCompleteTextView loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        oAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress);

        loginEmail = findViewById(R.id.login_email_auto_complete);
        loginPassword = findViewById(R.id.login_password_auto_complete);
        loginEmailParent = findViewById(R.id.login_email);
        loginPasswordParent = findViewById(R.id.login_password);

        signInButtonGoogle = findViewById(R.id.login_button_gmail);
        signInButtonMail = findViewById(R.id.login_button_mail);
        signUpButton = findViewById(R.id.login_button_register);

        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_client_id))
                .requestEmail()
                .build();

        signInButtonGoogle.setOnClickListener(l -> attemptLoginGoogle(googleSignInOptions));

        signInButtonMail.setOnClickListener(l -> attemptLoginEmail());
    }

    private void attemptLoginGoogle(GoogleSignInOptions googleSignInOptions) {
        GoogleSignInClient googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> result = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                GoogleSignInAccount account = result.getResult(ApiException.class);
                assert account != null;
                AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                if(oAuth == null){
                    oAuth = FirebaseAuth.getInstance();
                }
                oAuth.signInWithCredential(credential).addOnCompleteListener(this, task ->{
                    if(task.isSuccessful()){
                        FirebaseUser user = task.getResult().getUser();
                        checkUserDatabaseLogin(user);
                    }else{
                        showErrorDialogMail();
                    }
                });
            } catch (ApiException e){
                showErrorDialogMail();
            }
        }
    }

    private void attemptLoginEmail(){
        loginEmailParent.setError(null);
        loginPasswordParent.setError(null);

        if (loginEmail.getText().length() == 0){
            loginEmailParent.setErrorEnabled(true);
            loginEmailParent.setError(getString(R.string.login_email_error_one));
        } else if(loginPassword.getText().length() == 0){
            loginPasswordParent.setErrorEnabled(true);
            loginPasswordParent.setError(getString(R.string.login_email_error_two));
        }else{
            signInEmail();
        }
    }

    private void signInEmail(){
        if(oAuth == null){
            oAuth = FirebaseAuth.getInstance();
        }

        oAuth.signInWithEmailAndPassword(loginEmail.getText().toString(), loginPassword.getText().toString()).addOnCompleteListener(
                this, task -> {
                    if(!task.isSuccessful() || task.getResult().getUser() == null){
                        showErrorDialogMail();
                    }else if (!task.getResult().getUser().isEmailVerified()){
                        showErrorEmailVerified(task.getResult().getUser());
                    }else {
                        FirebaseUser user = task.getResult().getUser();
                        checkUserDatabaseLogin(user);
                    }
                }
        );
    }

    private void showErrorDialogMail(){
        hideLoginButton(false);
        Snackbar.make(signInButtonMail, getString(R.string.login_mail_access_token), Snackbar.LENGTH_SHORT).show();
    }

    private void showErrorEmailVerified(FirebaseUser user){
        hideLoginButton(false);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.login_verified_mail_error)
                .setPositiveButton(R.string.login_verified_mail_error_ok, ((dialog, which) -> {
                    user.sendEmailVerification().addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()){
                            Snackbar.make(loginEmail, R.string.login_verified_mail_error_sent, Snackbar.LENGTH_SHORT).show();
                        }else{
                            Snackbar.make(loginEmail, R.string.login_verified_mail_error_not_sent, Snackbar.LENGTH_SHORT).show();
                        }
                    });
                })).setNegativeButton(R.string.login_verified_mail_error_cancel, (dialog, which) -> {
        }).show();
    }

    private void hideLoginButton(boolean hide){

        TransitionSet transitionSet = new TransitionSet();
        Transition layoutFade = new AutoTransition();
        layoutFade.setDuration(1000);
        transitionSet.addTransition(layoutFade);

        if(hide){
            TransitionManager.beginDelayedTransition(findViewById(R.id.login_main_layout), transitionSet);
            progressBar.setVisibility(View.VISIBLE);
            signInButtonMail.setVisibility(View.GONE);
            signInButtonGoogle.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);
            loginPasswordParent.setEnabled(false);
            loginEmailParent.setEnabled(false);
        }else{
            TransitionManager.beginDelayedTransition(findViewById(R.id.login_main_layout), transitionSet);
            progressBar.setVisibility(View.GONE);
            signInButtonMail.setVisibility(View.VISIBLE);
            signInButtonGoogle.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
            loginPasswordParent.setEnabled(true);
            loginEmailParent.setEnabled(true);
        }
    }

    private void checkUserDatabaseLogin(FirebaseUser user){}
}