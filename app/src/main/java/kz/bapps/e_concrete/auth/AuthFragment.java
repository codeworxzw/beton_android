package kz.bapps.e_concrete.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import kz.bapps.e_concrete.EConcrete;
import kz.bapps.e_concrete.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class AuthFragment extends Fragment
        implements View.OnClickListener {

    final private static String TAG = "AuthFragment";
    final static public String ARG_ERROR = "errors";

    public EditText nEmail;
    public EditText nPassword;
    public Button nNextBtn;
    private String mError;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LoginFragment.
     */
    public static AuthFragment newInstance(String error) {
        AuthFragment fragment = new AuthFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ERROR, error);
        fragment.setArguments(args);
        return fragment;
    }
    /**
     *      КОНСТРУКТОР
     */
    public AuthFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mError = getArguments().getString(ARG_ERROR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auth, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nEmail = (EditText) getActivity().findViewById(R.id.editTextEmail);
        nPassword = (EditText) getActivity().findViewById(R.id.editTextPassword);

        if(mError.equals(getString(R.string.error_incorrect_password))
                && !nPassword.getText().toString().equals("")) {
            nPassword.setError(mError);
        } else {
            nPassword.setError(mError);
        }

        nNextBtn = (Button) getActivity().findViewById(R.id.btn_sign_in);

        nNextBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v == nNextBtn) {

            boolean correct = true;
            if (!AuthActivity.isEmailValid(nEmail.getText())) {
                nEmail.setError(getString(R.string.error_invalid_email));
                correct = false;
            }
            if (!AuthActivity.isPasswordValid(nPassword.getText())) {
                nPassword.setError(getString(R.string.error_invalid_password));
                correct = false;
            }

            if(correct) attempt();
        }
    }

    private void attempt() {
        SharedPreferences prefs = getActivity()
                .getSharedPreferences(EConcrete.appName, getActivity().MODE_PRIVATE);
        prefs.edit()
                .putString(SplashFragment.ARG_EMAIL, nEmail.getText().toString())
                .putString(SplashFragment.ARG_PASSWORD, nPassword.getText().toString())
                .apply();

        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container,
                        SplashFragment.newInstance(
                                nEmail.getText().toString(),
                                nPassword.getText().toString()))
                .commit();
    }

}
