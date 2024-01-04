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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    // Initializing Views
    EditText emailEdt, passwordEdt;
    Button signupBtn;
    TextView loginLbl;
    ProgressBar signupPb;

    // Initializing Firebase Tools
    private FirebaseAuth mAuth;
    private FirebaseDatabase loginDb;
    private DatabaseReference emailRef;

    // Initializing vars
    String emailTxt, passwordTxt;
    String TAG = "APP_MSG"; // Setting TAG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        /*
        and when pressed on signup ->
            get Email and password from Edittext
            verify the email and password
            if email already present in db ->
                error go bom
            if not present in db ->
                register user and store data in db
        */

        // Creating FB instances
        mAuth = FirebaseAuth.getInstance();
        loginDb = FirebaseDatabase.getInstance();
        emailRef = loginDb.getReference("EmailUser");

        // Getting Views
        emailEdt = findViewById(R.id.edtSignEmail);
        passwordEdt = findViewById(R.id.edtSignPassword);
        signupBtn = findViewById(R.id.btnSignup);
        loginLbl = findViewById(R.id.lblLoginAccount);
        signupPb = findViewById(R.id.pbSignup);

        signupBtn.setEnabled(true);
        loginLbl.setEnabled(true);


        // Changing Pb's color
        signupPb.getIndeterminateDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.yellow1), PorterDuff.Mode.SRC_IN );

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signupBtn.setEnabled(false);
                loginLbl.setEnabled(false);

                // Hiding keyboard
                hideKeyboard(SignupActivity.this);


                Log.d(TAG, "Pressed on Signup button");
                Log.d(TAG, "Getting emails and passwords");

                emailTxt = emailEdt.getText().toString();
                passwordTxt = passwordEdt.getText().toString();

                // Validating email and password
                if(isValidEmail(emailTxt) && !passwordTxt.isEmpty()) {
                    // Show progressbar
                    showSignupPb();

                    // Create account
                    createAccount();
                }
                else{
                    Log.e(TAG, "Invalid email or password");
                    Toast.makeText(SignupActivity.this, "Invalid Email or Password!", Toast.LENGTH_SHORT).show();
                    signupBtn.setEnabled(true);
                    loginLbl.setEnabled(true);
                }
            }
        });

        // When pressed on Signup
        loginLbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Hiding Keyboard
                hideKeyboard(SignupActivity.this);

                // Heading to SignUpPage
                Intent LoginPage = new Intent(getApplicationContext(), LoginActivity.class);
                LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(LoginPage);
                finish();
            }
        });

    }

    public boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void createAccount(){
        mAuth.createUserWithEmailAndPassword(emailTxt, passwordTxt).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                hideSignupPb();
                if(task.isSuccessful()){
                    Log.d(TAG, "Registration Successful");
                    Toast.makeText(SignupActivity.this, "Successfully Registered and logged in!", Toast.LENGTH_SHORT).show();

                    // Adding user to db
                    FirebaseUser cUser = mAuth.getCurrentUser();
                    assert cUser != null;
                    String cUID = cUser.getUid();
                    String cEmail = cUser.getEmail();
                    addNewUser(cEmail, cUID);

                    // Heading to MainPage
                    Intent MainPage = new Intent(getApplicationContext(), MainActivity.class);
                    MainPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(MainPage);
                    finish();
                }
                else{
                    Log.e(TAG, "Registration Unsuccessful: "+ String.valueOf(task.getException()));

                    if(task.getException() != null){
                        try{
                            throw task.getException();
                        }
                        catch(FirebaseAuthWeakPasswordException e){
                            Toast.makeText(SignupActivity.this, "Your password is weak.", Toast.LENGTH_SHORT).show();
                        }
                        catch(FirebaseAuthUserCollisionException e){
                            Toast.makeText(SignupActivity.this, "Email already in use!", Toast.LENGTH_SHORT).show();
                        }
                        catch(Exception e){
                            Toast.makeText(SignupActivity.this, "Authentication Failed! Please check your network.", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(SignupActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                    }

                    signupBtn.setEnabled(true);
                    loginLbl.setEnabled(true);
                }
            }
        });
    }

    public void addNewUser(String email, String UID){
        email = FBStringEncryption.Encode(email);
        emailRef.child(email).setValue(UID);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void showSignupPb(){
        signupPb.setVisibility(View.VISIBLE);
    }
    public void hideSignupPb(){
        signupPb.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Hide keyboard
        hideKeyboard(SignupActivity.this);
        if(signupBtn.isEnabled() && loginLbl.isEnabled()){
            // Heading to LoginPage
            Intent LoginPage = new Intent(getApplicationContext(), LoginActivity.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            finish();
        }
    }
}