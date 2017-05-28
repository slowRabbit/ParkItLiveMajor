package com.devlovepreet.parkitlive.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.devlovepreet.parkitlive.AppController;
import com.devlovepreet.parkitlive.R;
import com.devlovepreet.parkitlive.data.AppConfig;
import com.devlovepreet.parkitlive.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = SignupActivity.class.getSimpleName();

    @BindView(R.id.email)
    EditText et_email;
    @BindView(R.id.password)
    EditText et_password;
    @BindView(R.id.vehicleNumber)
    EditText et_vehicle_number;
    @BindView(R.id.name)
    EditText et_name;
    @BindView(R.id.btnSignup)
    Button btnSignup;
    @BindView(R.id.tv_login)
    TextView tv_login;
    String theme;
    private SessionManager session;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        theme = sharedPreferences.getString(getString(R.string.pref_theme_key),
                getString(R.string.pref_theme_light_value));
        if (theme.equals(getResources().getString(R.string.pref_theme_light_value))) {
            setTheme(R.style.AppTheme);
        } else {
            setTheme(R.style.AppTheme_Dark);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ButterKnife.bind(this);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
//        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnSignup.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = et_email.getText().toString().trim();
                String password = et_password.getText().toString().trim();
                String name = et_name.getText().toString().trim();
                String vehicleNumber = et_vehicle_number.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty() && !name.isEmpty() && !vehicleNumber.isEmpty()) {
                    // login user
                    signup(email, password, name, vehicleNumber);
                } else {
                    // Prompt user to enter credentials
                    Timber.d("You got it right");
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.toast_fill_all_details), Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        tv_login.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {

                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        });

    }

    /**
     * function to verify login details in mysql db
     */
    private void signup(final String email, final String password, final String name, final String vehicleNumber) {
        // Tag used to cancel the request
        String tag_json_obj = "json_obj_req_2";

        AppConfig appConfig = new AppConfig();

        pDialog.setMessage(getResources().getString(R.string.pd_signup));
        showDialog();

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, appConfig.URL_SIGNUP, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        hideDialog();

                        Log.d(TAG, response.toString());

                        try {
                            boolean success = response.getBoolean(getResources().getString(R.string.key_success));

                            if (success) {
                                // user successfully logged in
                                // Create login session
//                                String token = response.getString(getResources().getString(R.string.key_token));
//
//                                session.setLogin(true,token);

                                // Launch main activity
                                Intent intent = new Intent(SignupActivity.this,
                                        LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                // Error in login. Get the error message
                                String msg = response.getString(getResources().getString(R.string.key_msg));
                                Toast.makeText(getApplicationContext(),
                                        msg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(TAG, "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_some_error_occurred), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            public byte[] getBody() {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put(getResources().getString(R.string.key_email), email);
                params.put(getResources().getString(R.string.key_name), name);
                params.put(getResources().getString(R.string.key_vehicle_number), vehicleNumber);
                params.put(getResources().getString(R.string.key_password), password);
                return new JSONObject(params).toString().getBytes();
            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                return headers;
            }


        };

        AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);

    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
