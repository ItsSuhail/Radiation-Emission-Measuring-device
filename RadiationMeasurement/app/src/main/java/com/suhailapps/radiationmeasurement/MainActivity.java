package com.suhailapps.radiationmeasurement;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    // Initializing linear layouts
    LinearLayout ionizingLl, nonIonizingLl;

    // Initializing Logout button
    Button logoutBtn;

    // Firebase Auth
    FirebaseAuth mAuth;
    FirebaseUser cUser;
    String TAG = "APP_MSG"; // Setting TAG

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Defining layouts
        ionizingLl = findViewById(R.id.llIonizing);
        nonIonizingLl = findViewById(R.id.llNonIonizing);
        logoutBtn = findViewById(R.id.btnLogout);

        // Creating Instance
        mAuth = FirebaseAuth.getInstance();

        // Getting current user
        cUser = mAuth.getCurrentUser();

        if (cUser==null){
            Toast.makeText(this, "You are not logged in! Authentication error.", Toast.LENGTH_SHORT).show();
            mAuth.signOut();
            // Heading to LoginPage
            Intent LoginPage = new Intent(getApplicationContext(), LoginActivity.class);
            LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(LoginPage);
            finish();
        }

        // When pressed on ionizing layout
        ionizingLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Heading to Ionizing Activity
                Intent IonizingPage = new Intent(getApplicationContext(), IonizingRadiationActivity.class);
                IonizingPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(IonizingPage);
                finish();
            }
        });

        // When pressed on non-ionizing layout
        nonIonizingLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Heading to Ionizing Activity
                Intent NonIonizingPage = new Intent(getApplicationContext(), NonIonizingRadiationActivity.class);
                NonIonizingPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(NonIonizingPage);
                finish();
            }
        });

        // When pressed on logout button
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setMessage("Are you sure you want to Logout?");
                builder.setTitle("Logout");
                builder.setCancelable(false);
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mAuth.signOut();
                        Toast.makeText(MainActivity.this, "Successfully logged out.", Toast.LENGTH_SHORT).show();
                        // Heading to LoginPage
                        Intent LoginPage = new Intent(getApplicationContext(), LoginActivity.class);
                        LoginPage.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(LoginPage);
                        finish();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
        System.exit(0);
    }
}