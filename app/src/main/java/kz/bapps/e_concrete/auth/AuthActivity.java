package kz.bapps.e_concrete.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kz.bapps.e_concrete.EConcrete;
import kz.bapps.e_concrete.MainActivity;
import kz.bapps.e_concrete.R;

public class AuthActivity extends AppCompatActivity {

    final public static String TAG = "AuthActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);

        if(EConcrete.isNetworkAvailable(this)
                || EConcrete.getInstance(this).getCurrentUser() == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, SplashFragment.newInstance())
                    .commit();
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * @param email
     * @return
     */
    public static boolean isEmailValid(CharSequence email) {

        String expression = "^.+@(\\w+\\.)+\\w+$";

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * @param password
     * @return
     */
    public static boolean isPasswordValid(CharSequence password) {
        return password.length() > 5;
    }

}
