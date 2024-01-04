package com.suhailapps.radiationmeasurement;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthEmailException;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    // Initializing Views
    EditText emailEdt, passwordEdt;
    Button loginBtn;
    TextView signupLbl;
    ProgressBar loginPb;

    // Initializing FAuth
    private FirebaseAuth mAuth;

    // Initializing vars
    String emailTxt, passwordTxt;
    String TAG = "APP_MSG"; // Setting TAG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        /*
        \\ Always depended upon Firebase Auth instance, not using SharedPreferences
        Login the user if authenticated
        if not -> pressed on signup ->
            Open Signup Activity
            destroy this activity
        if not and registered ->
            login with the input cred
            if aint success ->
                Error go boom
            if success ->
                Head to main activity
                destroy this activity
         */
        // Creating Instance
        mAuth = FirebaseAuth.getInstance();

        // Getting Views
        emailEdt = findViewById(R.id.edtEmail);
        passwordEdt = findViewById(R.id.edtPassword);
        loginBtn = findViewById(R.id.btnLogin);
        signupLbl = findViewById(R.id.lblSignupAccount);
        loginPb = findViewById(R.id.pbLogin);

        loginBtn.setEnabled(true);
        signupLbl.setEnabled(true);

        // Changing Pb's color
        loginPb.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.yellow1), PorterDuff.Mode.SRC_IN );

        // When pressed on loginBtn
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Disabling button
                loginBtn.setEnabled(false);
                signupLbl.setEnabled(false);

                // Hide keyboard
                hideKeyboard(LoginActivity.this);

                // Getting email and password
                emailTxt = emailEdt.getText().toString();
                passwordTxt = passwordEdt.getText().toString();

                // Validating email and password
                if(isValidEmail(emailTxt) && !passwordTxt.isEmpty()){
                    showLoginPb();
                    mAuth.signInWithEmailAndPassword(emailTxt, passwordTxt)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    hideLoginPb(); // Hide progressbar
                                    // If login is successful
                                    if(task.isSuccessful()){
                                        Log.d(TAG, "Logged in successfully");

                                        // Getting current user
                                        FirebaseUser cUser = mAuth.getCurrentUser();
                                        if(cUser==null){
                                            Log.e(TAG, "Logged in successfully, but User not present");
                                            mAuth.signOut();

                                            loginBtn.setEnabled(true);
                                            signupLbl.setEnabled(true);
                                            return;
                                        }

                                        // Toasting
                                        Toast.makeText(LoginActivity.this, "Logged in successfully!", Toast.LENGTH_SHORT).show();

                                        // Heading to MainPage
                                        Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
                                        MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(MainPage);
                                        finish();
                                    }
                                    else{
                                        Log.e(TAG, "Login Not Successful: "+ Objects.requireNonNull(task.getException()).toString());

                                        if(task.getException() != null){
                                            try {
                                                throw task.getException();
                                            }
                                            catch(FirebaseTooManyRequestsException e){
                                                Toast.makeText(LoginActivity.this, "Too many requests from this device. The device is temporarily blocked.", Toast.LENGTH_SHORT).show();
                                            }
                                            catch (Exception e){
                                                if(Objects.requireNonNull(e.getMessage()).contains("INVALID_LOGIN_CREDENTIALS")){
                                                    Toast.makeText(LoginActivity.this, "Invalid login credentials.", Toast.LENGTH_SHORT).show();
                                                }
                                                else {
                                                    Toast.makeText(LoginActivity.this, "An error occurred. Unable to Login. Please check your network!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }
                                        else{
                                            Toast.makeText(LoginActivity.this, "Login Failed!", Toast.LENGTH_SHORT).show();
                                        }

                                        loginBtn.setEnabled(true);
                                        signupLbl.setEnabled(true);
                                    }
                                }
                            });

                }
                else{
                    Log.e(TAG, "Invalid Email or Password.");
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();

                    loginBtn.setEnabled(true);
                    signupLbl.setEnabled(true);
                }
            }
        });


        // When pressed on Signup
        signupLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiding keyboard
                hideKeyboard(LoginActivity.this);

                // Heading to SignUpPage
                Intent SignUpPage = new Intent(getApplicationContext(), SignupActivity.class);
                SignUpPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(SignUpPage);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser cUser = mAuth.getCurrentUser();
        if (cUser!=null){
            // Heading to MainPage
            Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
            MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(MainPage);
            finish();
        }
    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showLoginPb(){
        loginPb.setVisibility(View.VISIBLE);
    }
    public void hideLoginPb(){
        loginPb.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideKeyboard(LoginActivity.this);
    }
}