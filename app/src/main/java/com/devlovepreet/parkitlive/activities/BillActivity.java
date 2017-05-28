package com.devlovepreet.parkitlive.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

public class BillActivity extends AppCompatActivity {

    String startDate, endDate, bill;
    TextView tvBill, tvDuration;

    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill);

        tvBill=(TextView)findViewById(R.id.tvBill);
        tvDuration=(TextView)findViewById(R.id.tvDuration);

        pDialog=new ProgressDialog(this);
        pDialog.setCancelable(true);

        Intent i=getIntent();
        if(i!=null)
        {
            startDate=i.getStringExtra("startDate");
            endDate=i.getStringExtra("endDate");
            bill=i.getStringExtra("bill");

            tvBill.setText("Rs. 30 /-");
            tvDuration.setText(startDate+" - "+endDate);
        }

        updateBillOnServer();

    }

    private void updateBillOnServer() {
        // Tag used to cancel the request
        final String tag_json_obj = "parking_status_request";

        AppConfig appConfig = new AppConfig();

        pDialog.setMessage("Updating on server");
        showDialog();
        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, appConfig.USER_HISTORY_POST, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject main) {

                        hideDialog();

                        Log.d("billpoststatus", main.toString());

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                hideDialog();
                VolleyLog.d("billpoststatus", "Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.toast_some_error_occurred), Toast.LENGTH_LONG).show();

            }
        }) {

            @Override
            public byte[] getBody() {
              //needed in post requests

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("startTime", startDate);
                params.put("endTime", endDate);
                params.put("charges", "30");

                return new JSONObject(params).toString().getBytes();

            }


            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");

                SessionManager sessionManager=new SessionManager(getApplicationContext());
                String token=sessionManager.getToken();
                headers.put("Authorization", token);
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
