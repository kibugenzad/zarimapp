package rw.limitless.limitlessapps.ussd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeCustomDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeWarningDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.ClosureEdit;
import com.google.android.gms.vision.text.Line;

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
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class AddService extends AppCompatActivity {

    ImageButton back;
    TextView operatorname;
    LinearLayout savedata;
    EditText title, description, service_owner;
    View view;
    LayoutInflater inflater;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    private LinearLayout parentLinearLayout;
    Button add_field_button;
    EditText oldcoderequired;
    Spinner oldtype_spinner;
    Spinner type_spinner;
    SharedPreferences.Editor editor;
    JSONObject data;
    Intent intent;
    int from; //This must be declared as global !

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_service);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        //check if is a client
        areYouClient();

        //add service dynamically
        inflater = getLayoutInflater();

        oldcoderequired = (EditText) findViewById(R.id.coderequired);
        savedata = (LinearLayout) findViewById(R.id.savedata);
        add_field_button = (Button) findViewById(R.id.add_field_button);
        oldtype_spinner = (Spinner) findViewById(R.id.oldtype_spinner);
        title = (EditText) findViewById(R.id.title);
        description = (EditText) findViewById(R.id.description);
        operatorname = (TextView) findViewById(R.id.operatorname);
        service_owner = (EditText) findViewById(R.id.service_owner);

        //populate ui
        if (!sharedpreferences.getString("sim_operator", "").isEmpty()) {
            operatorname.setText(sharedpreferences.getString("sim_operator", ""));
            service_owner.setHint("Service owner eg:" + " " + sharedpreferences.getString("sim_operator", ""));
        } else {
            operatorname.setText("Unknown SIM CARD");
            service_owner.setHint("Service owner");
        }

        parentLinearLayout = (LinearLayout) findViewById(R.id.parent_linear_layout);

        oldtype_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("ussd_code_type0", parent.getItemAtPosition(position).toString());
                editor.commit();

                if (parent.getItemAtPosition(position).toString().equals("User info")) {
                    oldcoderequired.setHint("required Char length");
                    oldcoderequired.setText("");
                }

                if (parent.getItemAtPosition(position).toString().equals("Pin")) {
                    oldcoderequired.setHint("required pin length");
                }

                if (parent.getItemAtPosition(position).toString().equals("System field")) {
                    oldcoderequired.setHint("eg: 131");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        savedata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (title.getText().toString().isEmpty() || description.getText().toString().isEmpty()) {
                    AlertDialog.Builder netbuilder = new AlertDialog.Builder(AddService.this);
                    netbuilder.setTitle("Oups");
                    netbuilder.setCancelable(true);
                    netbuilder.setMessage("Please we need title and description of your service");
                    netbuilder.setPositiveButton("OK", null);
                    netbuilder.show();
                } else if (service_owner.getText().toString().isEmpty()) {
                    service_owner.setError("Please owner of service is required");
                } else {
                    if (sharedpreferences.getString("user_key", "").isEmpty()) {
                        new AwesomeWarningDialog(AddService.this)
                                .setTitle("Sorry,")
                                .setMessage("this, page is accessible by a client only, if you don't have an account? please ")
                                .setColoredCircle(R.color.dialogWarningBackgroundColor)
                                .setDialogIconAndColor(R.drawable.ic_warning, R.color.white)
                                .setCancelable(true)
                                .setButtonText("Register")
                                .setButtonTextColor(R.color.white)
                                .setButtonBackgroundColor(R.color.button_action_color)
                                .setWarningButtonClick(new Closure() {
                                    @Override
                                    public void exec() {
                                        // click
                                        intent = new Intent(AddService.this, ClientRegistration.class);
                                        startActivity(intent);
                                    }
                                })
                                .show();
                    } else {
                        submit_data(title.getText().toString(), description.getText().toString(), service_owner.getText().toString());
                    }
                }
            }
        });
    }

    //check if is a client
    public void areYouClient() {
        //remove any ussd code in sharedpreference
        for (int i = 0; i < 100; i++) {
            sharedpreferences.edit().remove("ussd_code" + i).commit();
            sharedpreferences.edit().remove("ussd_code_type" + i).commit();
        }
        if (sharedpreferences.getString("user_key", "").isEmpty()) {
            new AwesomeWarningDialog(this)
                    .setTitle("Sorry,")
                    .setMessage("this, page is accessible by a client only, if you don't have an account? please ")
                    .setColoredCircle(R.color.dialogWarningBackgroundColor)
                    .setDialogIconAndColor(R.drawable.ic_warning, R.color.white)
                    .setCancelable(true)
                    .setButtonText("Register")
                    .setButtonTextColor(R.color.white)
                    .setButtonBackgroundColor(R.color.button_action_color)
                    .setWarningButtonClick(new Closure() {
                        @Override
                        public void exec() {
                            // click
                            intent = new Intent(AddService.this, ClientRegistration.class);
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    public void onDelete(View v) {
        oldcoderequired.setText("");
    }

    public void onAddField(View v) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.create_service_form, null);
        // Add the new row before the add field button.
        parentLinearLayout.addView(rowView, parentLinearLayout.getChildCount());

        for (int i = 0; i < parentLinearLayout.getChildCount(); i++) {

            final Spinner type_spinner = (Spinner) rowView.findViewById(R.id.type_spinner);
            final EditText coderequired = (EditText) rowView.findViewById(R.id.coderequired);
            coderequired.setHint("eg: " + i);

            final int finalI = i;
            type_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (parent.getItemAtPosition(position).toString().equals("User info")) {

                        final CharSequence[] items = {"Phone number", "Meter number", "Bank account number", "Other"};

                        AlertDialog.Builder alert = new AlertDialog.Builder(AddService.this);
                        alert.setTitle("Choose type of this field");
                        alert.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (items[which] == "Phone number") {
                                    from = 1;
                                } else if (items[which] == "Meter number") {
                                    from = 2;
                                }else if (items[which] == "Bank account number") {
                                    from = 3;
                                }else if (items[which] == "Other") {
                                    from = 5;
                                }
                            }
                        });
                        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (from == 0) {
                                    Toast.makeText(getApplicationContext(), "Select One Choice",
                                            Toast.LENGTH_SHORT).show();
                                } else if (from == 1) {
                                    // Your Code
                                    editor = sharedpreferences.edit();
                                    editor.putString("ussd_code_type" + finalI, "Phone number");
                                    editor.commit();
                                    coderequired.setHint("required length of numbers ");
                                } else if (from == 2) {
                                    // Your Code
                                    editor = sharedpreferences.edit();
                                    editor.putString("ussd_code_type" + finalI, "Meter number");
                                    editor.commit();
                                    coderequired.setHint("required length of numbers ");
                                }
                                else if (from == 3) {
                                    // Your Code
                                    editor = sharedpreferences.edit();
                                    editor.putString("ussd_code_type" + finalI, "Bank account number");
                                    editor.commit();
                                } else if (from == 4) {
                                    // Your Code
                                    editor = sharedpreferences.edit();
                                    editor.putString("ussd_code_type" + finalI, "Other");
                                    editor.commit();

                                    coderequired.setHint("required character length");
                                }
                            }
                        });
                        alert.show();
                    }

                    if (parent.getItemAtPosition(position).toString().equals("Pin")) {
                        coderequired.setHint("required pin length");

                        editor = sharedpreferences.edit();
                        editor.putString("ussd_code_type" + finalI, parent.getItemAtPosition(position).toString());
                        editor.commit();
                    }

                    if (parent.getItemAtPosition(position).toString().equals("System field")) {
                        coderequired.setHint("eg: 131");

                        editor = sharedpreferences.edit();
                        editor.putString("ussd_code_type" + finalI, parent.getItemAtPosition(position).toString());
                        editor.commit();
                    }

                    if (parent.getItemAtPosition(position).toString().equals("Amount")) {
                        coderequired.setHint("Min Amount");

                        editor = sharedpreferences.edit();
                        editor.putString("ussd_code_type" + finalI, parent.getItemAtPosition(position).toString());
                        editor.commit();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            final LinearLayout add_layout = (LinearLayout) rowView.findViewById(R.id.add_layout);
            final LinearLayout delete_form_layout = (LinearLayout) rowView.findViewById(R.id.delete_form_layout);
            ImageButton add = (ImageButton) rowView.findViewById(R.id.add);

            final int finalI2 = i;
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor = sharedpreferences.edit();
                    editor.putString("ussd_code" + finalI2, coderequired.getText().toString());
                    editor.putString("ussd_code_type" + finalI2, type_spinner.getSelectedItem().toString());
                    editor.commit();

                    add_layout.setVisibility(View.GONE);
                    delete_form_layout.setVisibility(View.VISIBLE);
                }
            });

            ImageButton delete = (ImageButton) rowView.findViewById(R.id.delete);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((LinearLayout) rowView.getParent()).removeView(rowView);
                    sharedpreferences.edit().remove("ussd_code" + finalI2).commit();
                }
            });

            editor = sharedpreferences.edit();
            editor.putString("ussd_code0", oldcoderequired.getText().toString());
            editor.putInt("ussd_code_counts", i);
            editor.commit();
        }
    }

    public void retrieve_data() {
        for (int i = 0; i <= sharedpreferences.getInt("ussd_code_counts", 0); i++) {
            //show ussd to client
            String ussd = sharedpreferences.getString("ussd_code" + i, "");
            String array[] = {ussd};
            if (!ussd.isEmpty()) {
                String finalussd = ussd;
                StringBuilder sb = new StringBuilder();
                String s = "";
                s += ussd;

                Log.d("finalussd", s);
                if (finalussd != null && finalussd.length() > 0 && finalussd.charAt(finalussd.length() - 1) == '*') {
                    finalussd = finalussd.substring(0, finalussd.length() - 1);
                    finalussd = finalussd + "#";

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddService.this);
                    alertDialog.setTitle(title.getText().toString());
                    alertDialog.setMessage(description.getText().toString());

                    final EditText input = new EditText(AddService.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    input.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_CLASS_PHONE);
                    input.setText(sb);
                    alertDialog.setView(input);

                    final String finalUssd = finalussd;
                    alertDialog.setPositiveButton("SAVE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!sharedpreferences.getString("user_key", "").isEmpty()) {
//                                        submit_data(title.getText().toString(), description.getText().toString(), finalUssd, service_owner.getText().toString());
                                    } else {
                                        areYouClient();
                                    }
                                }
                            });

                    alertDialog.setNegativeButton("Reject",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();


                                }
                            });

                    alertDialog.show();
                }
            }
        }
    }

    private void submit_data(String title, String description, String service_owner) {
        if (isNetworkAvailable()) {
            Map<String, ?> allEntries = sharedpreferences.getAll();
            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
            }
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("title", title);
                data.put("description", description);
                data.put("ussd_code_counts", sharedpreferences.getInt("ussd_code_counts", 0));
                data.put("ussd_code0", sharedpreferences.getString("ussd_code0", ""));
                for (int i = 1; i <= sharedpreferences.getInt("ussd_code_counts", 0); i++) {
                    data.put("ussd_code" + i, sharedpreferences.getString("ussd_code" + i, ""));
                }
                for (int i = 0; i <= sharedpreferences.getInt("ussd_code_counts", 0); i++) {
                    data.put("ussd_code_type" + i, sharedpreferences.getString("ussd_code_type" + i, ""));
                }
                data.put("sim_operator", sharedpreferences.getString("sim_operator", ""));
                data.put("service_owner", service_owner);
                data.put("user_key", sharedpreferences.getString("user_key", ""));

                String URL = "http://159.89.33.228:3000/api/create_service";

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

    private class AsyncClientData extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(AddService.this);
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
                if (!(exception == null)) {
                    Snackbar.make(findViewById(android.R.id.content), "Connection lost, Try again! ", Snackbar.LENGTH_LONG)
                            .show();
                    Log.e(LOG_TAG, "Exception at login " + exception.getMessage());
                } else {
                    JSONObject accountJson = new JSONObject(s);
                    if (accountJson.getString(data_status).equals("success")) {
                        Snackbar.make(findViewById(android.R.id.content), "Service is created ", Snackbar.LENGTH_LONG)
                                .show();
                    } else {
                        AlertDialog.Builder netbuilder = new AlertDialog.Builder(AddService.this);
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
