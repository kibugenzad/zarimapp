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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.alihafizji.library.CreditCardEditText;

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
import java.util.Calendar;
import java.util.regex.Pattern;

public class BillingAddress extends AppCompatActivity {

    Button next;
    ImageButton back;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    EditText billingaddress,city,province,zipcode,postcode,country,exp_date,verification,fullname,phone_number,email;
    CreditCardEditText card_number;
    JSONObject data;
    View billingaddress_view,city_view,country_view,card_number_view,fullname_view,card_info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_billing_address);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        billingaddress = (EditText) findViewById(R.id.billingaddress);
        city = (EditText) findViewById(R.id.city);
        province = (EditText) findViewById(R.id.province);
        zipcode = (EditText) findViewById(R.id.zipcode);
        postcode = (EditText) findViewById(R.id.postcode);
        country = (EditText) findViewById(R.id.country);
        card_number = (CreditCardEditText) findViewById(R.id.card_number);
        exp_date = (EditText) findViewById(R.id.exp_date);
        verification = (EditText) findViewById(R.id.verification);
        fullname = (EditText) findViewById(R.id.fullname);
        phone_number = (EditText) findViewById(R.id.phone_number);
        email = (EditText) findViewById(R.id.email);
        billingaddress_view = (View) findViewById(R.id.billingaddress_view);
        city_view = (View) findViewById(R.id.city_view);
        country_view = (View) findViewById(R.id.country_view);
        card_number_view = (View) findViewById(R.id.card_number_view);
        fullname_view = (View) findViewById(R.id.fullname_view);
        card_info = (View) findViewById(R.id.card_info);

        populateData();

        exp_date.addTextChangedListener(mDateEntryWatcher);

        trackUserBahaviours();
    }

    public void trackUserBahaviours(){
        if(sharedpreferences.contains("billing_address") && sharedpreferences.contains("product_info")) {
            if (sharedpreferences.getBoolean("billing_address", true) && sharedpreferences.getBoolean("product_info",true)) {
                Intent StepTwoIntent = new Intent(BillingAddress.this, Dashboard.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }else if(sharedpreferences.getBoolean("billing_address", true)){
                Intent StepTwoIntent = new Intent(BillingAddress.this,ProductInformation.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }
        }else if(sharedpreferences.contains("billing_address")){
            if (sharedpreferences.getBoolean("billing_address", true)){
                Intent StepTwoIntent = new Intent(BillingAddress.this,ProductInformation.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }
        }
    }

    public void populateData(){
        if (!sharedpreferences.getString("user_key","").isEmpty() ||
                !sharedpreferences.getString("business_city","").isEmpty() ||
                !sharedpreferences.getString("business_country","").isEmpty()){

            city.setText(sharedpreferences.getString("business_city",""));
            country.setText(sharedpreferences.getString("business_country",""));
            postcode.setText(sharedpreferences.getString("business_postcode",""));
            zipcode.setText(sharedpreferences.getString("business_zipcode",""));
            province.setText(sharedpreferences.getString("business_province",""));
        }
    }

    private TextWatcher mDateEntryWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String working = s.toString();
            boolean isValid = true;
            if (working.length()==2 && before ==0) {
                if (Integer.parseInt(working) < 1 || Integer.parseInt(working)>12) {
                    isValid = false;
                } else {
                    working+="/";
                    exp_date.setText(working);
                    exp_date.setSelection(working.length());
                }
            }
            else if (working.length()==7 && before ==0) {
                String enteredYear = working.substring(3);
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (Integer.parseInt(enteredYear) < currentYear) {
                    isValid = false;
                }
            } else if (working.length()!=7) {
                isValid = false;
            }

            if (!isValid) {
                exp_date.setError("Enter a valid date: MM/YYYY");
            } else {
                exp_date.setError(null);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {}

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    };

    public void saveData(){
        if (billingaddress.getText().toString().isEmpty()){
            billingaddress.setError("Address is required eg: 10 KG 632 Street)");
            billingaddress_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (city.getText().toString().isEmpty()){
            city.setError("Your city is required eg: Kigali)");
            city_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (country.getText().toString().isEmpty()){
            country_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (exp_date.getText().toString().isEmpty()){
            exp_date.setError("Card exp date is required");
            card_info.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (card_number.getText().toString().isEmpty()){
            card_number.setError("Card number is required");
            card_number_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }else if(card_number.getText().toString().length() < 16){
            card_number.setError("Card number must be 16 digits");
            card_info.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (verification.getText().toString().isEmpty()){
            card_info.setBackgroundColor(Color.parseColor("#dd4b39"));
        }else if(verification.getText().toString().length() < 3){
            verification.setError("Check on back there is 3 digits, that is your carv verification");
            card_info.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (fullname.getText().toString().isEmpty()){
            fullname.setError("Full Name is required");
            fullname_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }
        if (phone_number.getText().toString().isEmpty()){
            phone_number.setError("Phone number is required");
            fullname_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }

        if (billingaddress.getText().toString().isEmpty() ||
                country.getText().toString().isEmpty() ||
                city.getText().toString().isEmpty() ||
                email.getText().toString().isEmpty()||
                card_number.getText().toString().isEmpty()||
                exp_date.getText().toString().isEmpty()||
                fullname.getText().toString().isEmpty()||
                phone_number.getText().toString().isEmpty()||
                verification.getText().toString().isEmpty()
                ){
            Toast.makeText(getApplicationContext(),"All is required", Toast.LENGTH_LONG).show();
        }else{
            String checkemail = email.getText().toString().trim();
            // onClick of button perform this simplest code.
            if (isValidEmail(checkemail))
            {
                saveClientInfo();
            }
            else
            {
                fullname_view.setBackgroundColor(Color.parseColor("#dd4b39"));
                Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void saveClientInfo() {
        if(isNetworkAvailable()){
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("billingaddress", billingaddress.getText().toString());
                data.put("card_number", card_number.getText().toString());
                data.put("exp_date", exp_date.getText().toString());
                data.put("verification", verification.getText().toString());
                data.put("country", country.getText().toString());
                data.put("email", email.getText().toString());
                data.put("city", city.getText().toString());
                data.put("province", province.getText().toString());
                data.put("zipcode", zipcode.getText().toString());
                data.put("postcode", postcode.getText().toString());
                data.put("fullname", fullname.getText().toString());
                data.put("phone_number", phone_number.getText().toString());
                data.put("user_key", sharedpreferences.getString("user_key",""));

                String URL = "http://159.89.33.228:3000/api/billing_client";

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

    private boolean isValidEmail(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private class AsyncClientData extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(BillingAddress.this);
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

            try {
                if(!(exception == null)){
                    Snackbar.make(findViewById(android.R.id.content), "Connection lost, Try again! ", Snackbar.LENGTH_LONG)
                            .show();
                    Log.e(LOG_TAG, "Exception at login " + exception.getMessage());
                }else{
                    JSONObject accountJson = new JSONObject(s);
                    if (accountJson.getString(data_status).equals("success")) {
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean("billing_address", true);
                        editor.commit();

                        Intent StepTwoIntent = new Intent(BillingAddress.this,ProductInformation.class);
                        StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(StepTwoIntent);
                    } else {
                        AlertDialog.Builder netbuilder = new AlertDialog.Builder(BillingAddress.this);
                        netbuilder.setTitle("Oups");
                        netbuilder.setCancelable(true);
                        netbuilder.setMessage(accountJson.getString(data_message));
                        netbuilder.setPositiveButton("OK", null);
                        netbuilder.show();
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
