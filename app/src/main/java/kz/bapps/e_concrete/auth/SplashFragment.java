package kz.bapps.e_concrete.auth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.activeandroid.query.Delete;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import kz.bapps.e_concrete.EConcrete;
import kz.bapps.e_concrete.JSONParser;
import kz.bapps.e_concrete.MainActivity;
import kz.bapps.e_concrete.R;
import kz.bapps.e_concrete.model.MenuItemModel;
import kz.bapps.e_concrete.model.UserModel;
import kz.bapps.e_concrete.service.QuickstartPreferences;
import kz.bapps.e_concrete.service.RegistrationIntentService;

public class SplashFragment extends Fragment
        implements View.OnClickListener {

    final private static String LOG_TAG = "SplashFragment";
    public static final String ARG_EMAIL = "email";
    public static final String ARG_PASSWORD = "password";


    private JSONParser jpar;
    private TextView textViewLoging;
    private Button btnSkipSms;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private View mainView;

    public SplashFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SplashFragment.
     */
    public static SplashFragment newInstance() {
        SplashFragment fragment = new SplashFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static SplashFragment newInstance(String sEmail, String sPassword) {
        SplashFragment fragment = new SplashFragment();
        Bundle args = new Bundle();
        args.putString("email",sEmail);
        args.putString("password",sPassword);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        jpar = new JSONParser(getActivity());
        textViewLoging = (TextView) mainView.findViewById(R.id.textViewLoging);
        btnSkipSms = (Button) mainView.findViewById(R.id.btn_skip_sms);
        btnSkipSms.setOnClickListener(this);

        /** Проверка на подключение к гугл плей сервисам
        ---------------------------------------------*/
        if (checkPlayServices()) {
            textViewLoging.setText(R.string.registering_message);
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(getActivity(), RegistrationIntentService.class);
            getActivity().startService(intent);
        } else {
            GetUserDataTask userDataTask = new GetUserDataTask();
            userDataTask.execute();
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mainView = inflater.inflate(R.layout.fragment_splash, container, false);

        return mainView;
    }

    /**
     *      АУТЕНТИФИКАЦИЯ
     */
    public class AuthTask extends AsyncTask<String,Void,Boolean> {

        private String result = "";

        @Override
        protected void onPreExecute() {
            textViewLoging.setText(R.string.action_sign_in_short);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            JSONObject jsonObject;

            if(getArguments().isEmpty()) {
                result = getString(R.string.insert_login_password);
                return false;
            }

            String sEmail = getArguments().getString("email");
            String sPassword = getArguments().getString("password");

            /** Аутентификация пользователя
             --------------------------------*/
            if(!jpar.makeAuth(sEmail,sPassword)) {
                result = getString(R.string.connection_error);
                return false;
            }

            jsonObject = jpar.toJsonObject();

            try {
                result = jsonObject.getString("Result");
            } catch (JSONException e) {
                result = getString(R.string.no_message);
            }

            /** Если ответ не ok, то выходи
             --------------------------------*/
            if(!result.toLowerCase().equals("ok")) {
                result = getString(R.string.no_message);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (getActivity() == null) return;
            textViewLoging.setText(result);
            if(success) {
                GetUserDataTask userDataTask = new GetUserDataTask();
                userDataTask.execute();
            } else {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container,
                                AuthFragment.newInstance(result))
                        .commit();
            }
        }
    }

    /**
     *       ПОЛУЧЕНИЕ ДАННЫХ ПОЛЬЗОВАТЕЛЯ С СЕРВЕРА
     */
    public class GetUserDataTask extends AsyncTask<String, Void, Boolean> {

        private String result = "";

        @Override
        protected void onPreExecute() {
            textViewLoging.setText(R.string.loading);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            JSONObject jsonObject;

            /** Получаем пользователя
             --------------------------------*/
            jpar.setResource("restapi/query/get?code=sessioninfo");
            jpar.setMethod(JSONParser.METHOD_GET);
            if(!jpar.execute()) {
                result = getString(R.string.no_message);
                return false;
            }

            jsonObject = jpar.toJsonObject();

            if(jsonObject.has("error_text")) {
                result = getString(R.string.session_expired);
                return false;
            }

            /** удаляем всех пользователей
             --------------------------------*/
            new Delete()
                    .from(UserModel.class)
//                    .where("user_id =  ?",user.id)
                    .execute();

            /** JSON данные о пользователе
             --------------------------------*/
            try {
                jsonObject = jpar.toJsonObject()
                        .getJSONArray("items")
                        .getJSONObject(0);

                Log.d("AUTH",jsonObject.toString());
                UserModel user = EConcrete.getInstance(getActivity()).getGson()
                        .fromJson(jsonObject.toString(),
                                new TypeToken<UserModel>() {
                                    //
                                }.getType());

                /** добавляем этого пользователя из JSON
                 --------------------------------*/
                user.save();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            /** Получаем Меню пользователя
             --------------------------------*/
            jpar.setResource("restapi/menus/tree");
            jpar.setMethod(JSONParser.METHOD_GET);
            if(!jpar.execute()) {
                result = getString(R.string.no_message);
                return false;
            }

            new Delete().from(MenuItemModel.class).where("1 = 1").execute();

            /** JSON данные Меню пользователя
             --------------------------------*/
            JSONArray jsonArray = jpar.toJsonArray();
            List<MenuItemModel> menuItems = EConcrete.getInstance(getActivity()).getGson()
                    .fromJson(jsonArray.toString(),
                            new TypeToken<List<MenuItemModel>>() {
                                //
                            }.getType());

            /** Очищаем таблицу
             --------------------------------*/
            MenuItemModel.truncate(MenuItemModel.class);

            /** Добавляем недостающее меню
             --------------------------------*/
            for(MenuItemModel menuItem : menuItems) {
                menuItem.save();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (getActivity() == null) return;
            textViewLoging.setText(result);
            if(success) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(intent);
                getActivity().finish();
            } else {
                AuthTask getUserTask = new AuthTask();
                getUserTask.execute();
            }
        }
    }

    /**
     *      GOOGLE CLOUD MESSAGING
     */

    private BroadcastReceiver mRegistrationBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            SharedPreferences sharedPreferences = getActivity()
                    .getSharedPreferences(EConcrete.appName, getActivity().MODE_PRIVATE);

            boolean sentToken = sharedPreferences
                    .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);

            if (sentToken) {
                textViewLoging.setText(getString(R.string.gcm_send_message));
            } else {
                textViewLoging.setText(getString(R.string.token_error_message));
            }

            GetUserDataTask getUserDataTask = new GetUserDataTask();
            getUserDataTask.execute();

        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(getActivity());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(getActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(LOG_TAG, "Это устройство не поддерживается.");
                //getActivity().finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, AuthFragment.newInstance(getString(R.string.error_incorrect_password)))
                .commit();
        btnSkipSms.setVisibility(View.GONE);
    }
}
