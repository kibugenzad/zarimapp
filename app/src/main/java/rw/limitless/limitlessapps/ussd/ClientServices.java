package rw.limitless.limitlessapps.ussd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ClientServices extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    ImageButton back;
    TextView addservice;
    JSONObject data;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    RecyclerView mRecycleView;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Intent intent;
    List<Service_item> serivceVOList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_services);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addservice = (TextView) findViewById(R.id.addservice);
        addservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(ClientServices.this, AddService.class);
                startActivity(intent);
            }
        });

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(ClientServices.this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN);
        mSwipeRefreshLayout.setDistanceToTriggerSync(20);// in dips
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);// LARGE also can be used

        mRecycleView = (RecyclerView) findViewById(R.id.list_services);

        submit_data();
    }

    public void submit_data() {
        if (isNetworkAvailable()) {
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("user_key", sharedpreferences.getString("user_key", ""));

                String URL = "http://159.89.33.228:3000/api/get_all_services";

                AsyncClientData client = new AsyncClientData();
                client.execute(URL);
                Log.d("Data", String.valueOf(data));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Snackbar.make(findViewById(android.R.id.content), "No internet connection! ", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRefresh() {
        submit_data();
    }

    private class AsyncClientData extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(ClientServices.this);
        private Exception exception = null;

        @Override
        protected void onPreExecute() {
            // put ui changes here
            progress.setTitle("Searching");
            progress.setMessage("Please hold on a sec...");
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
            progress.show();
        }

        @Override
        protected String doInBackground(String... params) {
            URL url;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String JsonDATA = data.toString();
            String JsonResponse = null;

            try {

                Log.e(LOG_TAG, "Started Connecting to " + params[0]);
                url = new URL(params[0]);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/json");
                urlConnection.setRequestProperty("Accept", "application/json");

                Writer writer = new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream(), "UTF-8"));
                writer.write(JsonDATA);
                writer.close();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String inputLine;
                while ((inputLine = reader.readLine()) != null)
                    buffer.append(inputLine + "\n");
                if (buffer.length() == 0) {
                    // Stream was empty. No point in parsing.
                    return null;
                }
                JsonResponse = buffer.toString();


                Log.i(LOG_TAG, JsonResponse);
                return JsonResponse;

            } catch (IOException e) {
                exception = e;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            progress.dismiss();
            final String data_status = "status";
            final String data_data = "data";
            final String data_title = "title";
            final String data_description = "description";
            final String data_ussd_code_counts = "ussd_code_counts";
            final String data_service_owner = "service_owner";
            final String data_ussd_code = "ussd_code";
            final String data_id = "_id";
            final String data_user_key = "user_key";

            Service_item service_item = new Service_item();

            mSwipeRefreshLayout.setEnabled(false);

            try {
                if (!(exception == null)) {
                    Snackbar.make(findViewById(android.R.id.content), "Connection lost, Try again! ", Snackbar.LENGTH_LONG)
                            .show();
                    Log.e(LOG_TAG, "Exception at login " + exception.getMessage());
                } else if (s != null) {

                    JSONObject documentJson = new JSONObject(s);

                    JSONArray docs = documentJson.getJSONArray(data_data);
                    for (int i = 0; i < docs.length(); i++) {
                        JSONObject singleDoc = docs.getJSONObject(i);
                        String title = singleDoc.getString(data_title);
                        String description = singleDoc.getString(data_description);
                        int ussd_code_counts = Integer.parseInt(singleDoc.getString(data_ussd_code_counts));
                        String service_owner = singleDoc.getString(data_service_owner);
                        String _id = singleDoc.getString(data_user_key);
                        String fullussd = "";

                        for (int u = 0; u < ussd_code_counts; u++) {
                            Log.d("ussd", singleDoc.getString(data_ussd_code + u));
                        }

                        serivceVOList.add(new Service_item(title,service_owner,data_id,data_ussd_code,data_user_key));

                        mRecycleView.setLayoutManager(new LinearLayoutManager(ClientServices.this));
                        mRecycleView.setAdapter(new ServiceFavoriteAdapter(serivceVOList, new ServiceFavoriteAdapter.OnItemClickListener() {

                            @Override
                            public void onItemClick(Service_item item) {
                                Intent intent = new Intent(ClientServices.this, ServiceProfile.class);
                                intent.putExtra("title", item.getTitle());
                                intent.putExtra("service_owner", item.getService_owner());
                                intent.putExtra("_id", item.getId());
                                intent.putExtra("user_key", item.getUser_key());
                                startActivity(intent);
                            }
                        }));
                    }
                } else {

                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "caught exception in PostExecute " + e.getMessage());
            }
        }

    }

    //checking for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
