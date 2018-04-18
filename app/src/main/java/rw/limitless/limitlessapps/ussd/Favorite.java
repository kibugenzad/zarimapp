package rw.limitless.limitlessapps.ussd;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
import java.util.Arrays;
import java.util.List;

/**
 * Created by limitlessapps on 04/01/2018.
 */

public class Favorite extends android.support.v4.app.Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ImageView info;
    JSONObject data;
    RecyclerView mRecycleView;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Intent intent;
    List<Service_item> serivceVOList = new ArrayList();
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    Context ctx;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.favorite, container, false);

        sharedpreferences = getContext().getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeColors(Color.GRAY, Color.GREEN, Color.BLUE,
                Color.RED, Color.CYAN);
        mSwipeRefreshLayout.setDistanceToTriggerSync(20);// in dips
        mSwipeRefreshLayout.setSize(SwipeRefreshLayout.DEFAULT);// LARGE also can be used

        mRecycleView = (RecyclerView) view.findViewById(R.id.list_services);

        submit_data();

        return view;
    }

    public void submit_data() {
        if (isNetworkAvailable()) {
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("sim_operator", sharedpreferences.getString("sim_operator", ""));

                String URL = "http://159.89.33.228:3000/api/get_favorite_service";

                AsyncClientData client = new AsyncClientData();
                client.execute(URL);
                Log.d("Data", String.valueOf(data));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Snackbar.make(getActivity().findViewById(android.R.id.content), "No internet connection! ", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onRefresh() {
        submit_data();
    }

    private class AsyncClientData extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(getContext());
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
            final String data_ussd_code_type = "ussd_code_type";
            final String data_id = "_id";
            final String data_user_key = "user_key";

            try {
                if (!(exception == null)) {
                    Snackbar.make(getActivity().findViewById(android.R.id.content), "Connection lost, Try again! ", Snackbar.LENGTH_LONG)
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

                        final StringBuilder finalussd =new StringBuilder();
                        StringBuilder finaltype =new StringBuilder();
                        String ussd, type;

                        for(int t=0;t<= ussd_code_counts;t++){
                            ussd = singleDoc.getString(data_ussd_code+t);
                            type = singleDoc.getString(data_ussd_code_type+t);
                            finalussd.append("("+type+")"+ussd);
                        }

                        System.out.println(finalussd);
                        String finalu = String.valueOf(finalussd);
                        serivceVOList.add(new Service_item(title,service_owner,data_id,finalu,data_user_key));

                        mRecycleView.setLayoutManager(new LinearLayoutManager(getContext()));
                        mRecycleView.setAdapter(new ServiceFavoriteAdapter(serivceVOList, new ServiceFavoriteAdapter.OnItemClickListener() {

                            @Override
                            public void onItemClick(Service_item item) {
                                Intent intent = new Intent(getContext(), ServiceProfile.class);
                                intent.putExtra("title", item.getTitle());
                                intent.putExtra("service_owner", item.getService_owner());
                                intent.putExtra("_id", item.getId());
                                intent.putExtra("user_key", item.getUser_key());
                                intent.putExtra("finalussd", (CharSequence) item.getUssd_code());
                                startActivity(intent);
                            }
                        }));
                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "caught exception in PostExecute " + e.getMessage());
            }
        }

    }

    //checking for internet connection
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
