package rw.limitless.limitlessapps.ussd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Pattern;

public class ClientRegistration extends AppCompatActivity {

    Button next;
    ImageButton back;
    EditText country,city,legal_name,tax_identification,email,zipcode,postcode,province;
    int pos = 0;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    CountryPicker picker;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_TIMEOUT = 15000;
    JSONObject data;
    View legal_name_view,country_view,tax_identification_view,email_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_registration);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        next = (Button) findViewById(R.id.next);

        back = (ImageButton) findViewById(R.id.back);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        //country
        country = (EditText) findViewById(R.id.country);
        country.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker = CountryPicker.newInstance("Select Country");
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        // Implement your code here
                        country.setText(name);
                        callCountryData(name);
                    }
                });
            }
        });

        trackUserBahaviours();

        //city
        city = (EditText) findViewById(R.id.city);
        legal_name = (EditText) findViewById(R.id.legal_name);
        tax_identification = (EditText) findViewById(R.id.tax_identification);
        email = (EditText) findViewById(R.id.email);
        zipcode = (EditText) findViewById(R.id.zipcode);
        postcode = (EditText) findViewById(R.id.postcode);
        province = (EditText) findViewById(R.id.province);
        legal_name_view = (View) findViewById(R.id.legal_name_view);
        country_view = (View) findViewById(R.id.country_view);
        email_view = (View) findViewById(R.id.email_view);
        tax_identification_view = (View) findViewById(R.id.tax_identification_view);
    }

    public void trackUserBahaviours(){
        if(sharedpreferences.contains("billing_address") && sharedpreferences.contains("client_registration")) {
            if (sharedpreferences.getBoolean("billing_address", true) && sharedpreferences.getBoolean("client_registration",true)) {
                Intent StepTwoIntent = new Intent(ClientRegistration.this, ProductInformation.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }else if(sharedpreferences.getBoolean("client_registration", true)){
                Intent StepTwoIntent = new Intent(ClientRegistration.this,BillingAddress.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }
        }else if(sharedpreferences.contains("client_registration")){
            if(sharedpreferences.getBoolean("client_registration", true)){
                Intent StepTwoIntent = new Intent(ClientRegistration.this,BillingAddress.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }
        }else{
            populateCountries();
        }
    }

    public void populateCountries(){
        if(!sharedpreferences.getString("country","").isEmpty()){
            country.setText(sharedpreferences.getString("country",""));
            callCountryData(sharedpreferences.getString("country",""));
        }else{
            country.setText("Select country");
        }
    }

    public void callCountryData(String country){
        if(isNetworkAvailable()){
            if (!country.isEmpty()){
                try {
                    data = new JSONObject();
                    data.put("source", "rw.limitless.limitlessapps.ussd");
                    data.put("country", country);
                    String URL = "https://restcountries.eu/rest/v2/name/"+country;
                    AsyncFetchCountries countries = new AsyncFetchCountries();
                    countries.execute(URL);
                    Log.d("Data", String.valueOf(data));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Snackbar.make(findViewById(android.R.id.content), "Country is empty,Please add your business country", Snackbar.LENGTH_LONG)
                        .show();
            }
        }else {
            Snackbar.make(findViewById(android.R.id.content), "No internet connection! ", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private class AsyncFetchCountries extends AsyncTask<String, String, String> {
        ProgressDialog pdLoading = new ProgressDialog(ClientRegistration.this);
        HttpURLConnection conn;
        URL url = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tSearching your location...");
            pdLoading.setCancelable(false);
            pdLoading.show();
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                // Enter URL address where your json file resides
                // Even you can make call to php file which returns json data
                url = new URL(params[0]);

            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return e.toString();
            }
            try {

                // Setup HttpURLConnection class to send and receive data from php and mysql
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(READ_TIMEOUT);
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setRequestMethod("GET");

                // setDoOutput to true as we recieve data from json file
                conn.setDoOutput(false);
                conn.setDoInput(true);

            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
                return e1.toString();
            }

            try {

                int response_code = conn.getResponseCode();


                Log.d("Country data", String.valueOf(response_code));

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {

                    // Read data sent from server
                    InputStream input = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    // Pass data to onPostExecute method
                    return (result.toString());

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            //this method will be running on UI thread
            Log.d("data", result);

            pdLoading.dismiss();
            try {

                JSONArray jArray = new JSONArray(result);

                // Extract data from json and store into ArrayList as class objects
                for(int i=0;i<jArray.length();i++) {
                    JSONObject json_data = jArray.getJSONObject(i);
                    Log.d("Country data", json_data.getString("capital"));
                    if (!json_data.getString("capital").isEmpty()){
                        city.setText(json_data.getString("capital"));
                    }else{
                        city.setText("Select City");
                    }
                }

            } catch (JSONException e) {
                Toast.makeText(ClientRegistration.this, e.toString(), Toast.LENGTH_LONG).show();
            }

        }

    }

    private void saveData() {
        if (legal_name.getText().toString().isEmpty()){
            legal_name_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (country.getText().toString().isEmpty()){
            country_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (tax_identification.getText().toString().isEmpty()){
            tax_identification_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (email.getText().toString().isEmpty()){
            email_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (legal_name.getText().toString().isEmpty() ||
                country.getText().toString().isEmpty() ||
                tax_identification.getText().toString().isEmpty() ||
                email.getText().toString().isEmpty()
                ){
            AlertDialog.Builder netbuilder = new AlertDialog.Builder(ClientRegistration.this);
            netbuilder.setTitle("Oups");
            netbuilder.setCancelable(true);
            netbuilder.setMessage("field with border red color is required");
            netbuilder.setPositiveButton("OK", null);
            netbuilder.show();
        }else{
            String checkemail = email.getText().toString().trim();
            // onClick of button perform this simplest code.
            if (isValidEmail(checkemail))
            {
                saveClientInfo();
            }
            else
            {
                email_view.setBackgroundColor(Color.parseColor("#dd4b39"));
                Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public void saveClientInfo(){
        if(isNetworkAvailable()){
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("legal_name", legal_name.getText().toString());
                data.put("tax_identification", tax_identification.getText().toString());
                data.put("country", country.getText().toString());
                data.put("email", email.getText().toString());
                data.put("city", city.getText().toString());
                data.put("province", province.getText().toString());
                data.put("zipcode", zipcode.getText().toString());
                data.put("postcode", postcode.getText().toString());

                String URL = "http://159.89.33.228:3000/api/create_account";

                AsyncClientData client = new AsyncClientData();
                client.execute(URL);
                Log.d("Data", String.valueOf(data));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            Snackbar.make(findViewById(android.R.id.content), "No internet connection! ", Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private class AsyncClientData extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(ClientRegistration.this);
        private Exception exception = null;

        @Override
        protected void onPreExecute() {
            // put ui changes here
            progress.setTitle("Saving");
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
            final String data_message = "message";
            final String data_user_key = "user_key";

            try {
                if(!(exception == null)){
                    Snackbar.make(findViewById(android.R.id.content), "Connection lost, Try again! ", Snackbar.LENGTH_LONG)
                            .show();
                    Log.e(LOG_TAG, "Exception at login " + exception.getMessage());
                }else{
                    JSONArray arr = new JSONArray(s);
                    for(int i = 0; i < arr.length(); i++){
                        JSONObject accountJson = (JSONObject) arr.get(i);
                        if (accountJson.getString(data_status).equals("success")) {
                            Log.d("status", "user_key"+accountJson.getString("user_key"));
                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("business_country", country.getText().toString());
                            editor.putString("business_city", city.getText().toString());
                            editor.putString("business_province", province.getText().toString());
                            editor.putString("business_zipcode", zipcode.getText().toString());
                            editor.putString("business_postcode", postcode.getText().toString());
                            editor.putString("business_province", province.getText().toString());
                            editor.putString("user_key", accountJson.getString("user_key").toString());
                            editor.putBoolean("client_registration", true);
                            editor.commit();

                            Log.d("remote", accountJson.getString(data_status));

                            Intent StepTwoIntent = new Intent(ClientRegistration.this,BillingAddress.class);
                            StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(StepTwoIntent);
                        } else {
                            AlertDialog.Builder netbuilder = new AlertDialog.Builder(ClientRegistration.this);
                            netbuilder.setTitle("Oups");
                            netbuilder.setCancelable(true);
                            netbuilder.setMessage(accountJson.getString(data_message));
                            netbuilder.setPositiveButton("OK", null);
                            netbuilder.show();
                        }
                    }
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
