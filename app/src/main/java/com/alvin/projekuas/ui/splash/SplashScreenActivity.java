package com.alvin.projekuas.ui.splash;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.alvin.projekuas.R;
import com.alvin.projekuas.ui.login.LoginActivity;
import com.alvin.projekuas.ui.main.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreenActivity extends AppCompatActivity {

    // widget
    private TextView tvSplashTitle;

    // vars
    private static final int SPLASH_SCREEN_DURATION = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // casting widget
        tvSplashTitle = findViewById(R.id.tv_splash_title);

        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setInterpolator(new DecelerateInterpolator());
        fadeIn.setDuration(3000);

        tvSplashTitle.setAnimation(fadeIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                checkLogin();
            }
        }, SPLASH_SCREEN_DURATION);
    }


    private void checkLogin() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            Intent home = new Intent(this, HomeActivity.class);
            finish();
            startActivity(home);
        } else {
            Intent login = new Intent(this, LoginActivity.class);
            finish();
            startActivity(login);
        }
    }
}
