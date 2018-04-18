package rw.limitless.limitlessapps.ussd;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.regex.Pattern;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

import static rw.limitless.limitlessapps.ussd.Utility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;

public class ProductInformation extends AppCompatActivity {

    ImageButton back;
    Button next;
    TextView savecontinue;
    EditText shortcode,nameregulator,email,uploadfile,serviceprovider,star,hash;
    JSONObject data;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    View shortcode_view,nameregulator_view,email_view,proof_view;
    private int MAX_ATTACHMENT_COUNT = 10;
    private ArrayList<String> photoPaths = new ArrayList<>();
    private ArrayList<String> docPaths = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_information);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        trackUserBahaviours();

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        shortcode = (EditText) findViewById(R.id.shortcode);
        email = (EditText) findViewById(R.id.email);

        shortcode = (EditText) findViewById(R.id.shortcode);
        nameregulator = (EditText) findViewById(R.id.nameregulator);
        email = (EditText) findViewById(R.id.email);
        uploadfile = (EditText) findViewById(R.id.uploadfile);
        serviceprovider = (EditText) findViewById(R.id.serviceprovider);
        shortcode_view = (View) findViewById(R.id.shortcode_view);
        email_view = (View) findViewById(R.id.email_view);
        nameregulator_view = (View) findViewById(R.id.nameregulator_view);
        email_view = (View) findViewById(R.id.email_view);
        proof_view = (View) findViewById(R.id.proof_view);
        star = (EditText) findViewById(R.id.star);
        hash = (EditText) findViewById(R.id.hash);

        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
            }
        });

        uploadfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        // Should we show an explanation?
                        if (shouldShowRequestPermissionRationale(
                                android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            // Explain to the user why we need to read the contacts
                        }

                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                        // MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
                        // app-defined int constant

                        return;
                    } else {
                        makeChoice();
                    }
                } else {
                    makeChoice();
                }
            }
        });
    }

    public void trackUserBahaviours(){
        if(sharedpreferences.contains("product_info")) {
            if (sharedpreferences.getBoolean("product_info", true)) {
                Intent StepTwoIntent = new Intent(ProductInformation.this, Dashboard.class);
                StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(StepTwoIntent);
            }
        }
    }

    private void makeChoice() {
        final CharSequence[] items = { "Choose from Image gallery","Choose from document gallery",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ProductInformation.this);
        builder.setTitle("Add Proof of payment!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Choose from Image gallery")) {
                    galleryIntent();
                } else if (items[item].equals("Choose from document gallery")) {
                    documentGallery();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void documentGallery() {
        FilePickerBuilder.getInstance().setMaxCount(10)
                .setSelectedFiles(docPaths)
                .setActivityTheme(R.style.MyActionBar)
                .pickFile(this);
    }

    private void galleryIntent() {
        FilePickerBuilder.getInstance().setMaxCount(5)
                .setSelectedFiles(photoPaths)
                .setActivityTheme(R.style.MyActionBar)
                .pickPhoto(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case FilePickerConst.REQUEST_CODE_PHOTO:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    photoPaths = new ArrayList<>();
                    photoPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));
                }
                break;
            case FilePickerConst.REQUEST_CODE_DOC:
                if(resultCode== Activity.RESULT_OK && data!=null)
                {
                    docPaths = new ArrayList<>();
                    docPaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_DOCS));
                }
                break;
        }
        addThemToView(photoPaths,docPaths);
    }

    private void addThemToView(ArrayList<String> photoPaths, ArrayList<String> docPaths) {
        ArrayList<String> filePaths = new ArrayList<>();
        if(photoPaths!=null)
            filePaths.addAll(photoPaths);

        if(docPaths!=null)
            filePaths.addAll(docPaths);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerview);

        if(recyclerView!=null) {
            StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, OrientationHelper.VERTICAL);
            layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS);
            recyclerView.setLayoutManager(layoutManager);

            ImageAdapter imageAdapter = new ImageAdapter(this, filePaths);

            recyclerView.setAdapter(imageAdapter);
            recyclerView.setItemAnimator(new DefaultItemAnimator());
        }

        Toast.makeText(this, "Num of files selected: "+ filePaths.size(), Toast.LENGTH_SHORT).show();
    }

    public void saveData(){
        if (shortcode.getText().toString().isEmpty()){
            shortcode.setError("Short code is required eg(*131#)");
            shortcode_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }

        if (nameregulator.getText().toString().isEmpty()){
            nameregulator.setError("Regulator name is required eg(RURA)");
            nameregulator_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }

        if (docPaths.isEmpty()){
            uploadfile.setError("Please give us a proof of payment to verify your account");
            proof_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }

        if (email.getText().toString().isEmpty()){
            email.setError("Email is required");
            email_view.setBackgroundColor(Color.parseColor("#dd4b39"));
        }

        if (shortcode.getText().toString().isEmpty() ||
                nameregulator.getText().toString().isEmpty() ||
                serviceprovider.getText().toString().isEmpty() ||
                email.getText().toString().isEmpty() ||
                docPaths.isEmpty()
                ){
            Snackbar.make(findViewById(android.R.id.content), "All field is required", Snackbar.LENGTH_LONG)
                    .show();
        }else{
            String checkemail = email.getText().toString().trim();
            // onClick of button perform this simplest code.
            if (isValidEmail(checkemail))
            {
                saveClientInfo();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Invalid email address", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static String encodeFileToBase64Binary(String fileName) throws IOException {
        File file = new File(fileName);
        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
        return new String(encoded, StandardCharsets.US_ASCII);
    }

    public void saveClientInfo(){
        if(isNetworkAvailable()){
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("shortcode", star.getText().toString()+shortcode.getText().toString()+hash.getText().toString());
                data.put("nameregulator", nameregulator.getText().toString());
                data.put("email", email.getText().toString());
                data.put("serviceprovider", serviceprovider.getText().toString());
                data.put("images_size", photoPaths.size());
                data.put("document_size", docPaths.size());
                data.put("user_key", sharedpreferences.getString("user_key", ""));

                //convert images or documents
                if (!docPaths.isEmpty()) {
                    for (int i = 0; i < photoPaths.size(); i++) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            data.put("images" + i, encodeFileToBase64Binary(photoPaths.get(i)));
                        }
                        String extension = photoPaths.get(i).substring(photoPaths.get(i).lastIndexOf("."));
                        data.put("image_type" + i, extension);
                    }
                }

                if (!photoPaths.isEmpty()){
                    for(int i = 0; i < docPaths.size(); i++){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                            try {
                                data.put("documents"+i, encodeFileToBase64Binary(docPaths.get(i)));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        String extension = photoPaths.get(i).substring(photoPaths.get(i).lastIndexOf("."));
                        data.put("document_type"+i, extension);
                    }
                }

                String URL = "http://159.89.33.228:3000/api/product_info";

                AsyncSaveClientData client = new AsyncSaveClientData();
                client.execute(URL);
                Log.d("Data", String.valueOf(data));

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
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

    private class AsyncSaveClientData extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(ProductInformation.this);
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

                        Snackbar.make(findViewById(android.R.id.content), "Information is recorded", Snackbar.LENGTH_LONG)
                                .show();

                        final CharSequence[] items = { "Yes","No" };
                        AlertDialog.Builder builder = new AlertDialog.Builder(ProductInformation.this);
                        builder.setTitle("Do you want to add new product");
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int item) {
                                if (items[item].equals("Yes")) {
                                    nameregulator.setText("");
                                    email.setText("");
                                    uploadfile.setText("");
                                    serviceprovider.setText("");
                                    shortcode.setText("");
                                } else if (items[item].equals("No")) {
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putBoolean("product_info", true);
                                    editor.commit();

                                    Intent StepTwoIntent = new Intent(ProductInformation.this,Dashboard.class);
                                    StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(StepTwoIntent);
                                }
                            }
                        });
                        builder.show();
                    } else {
                        AlertDialog.Builder netbuilder = new AlertDialog.Builder(ProductInformation.this);
                        netbuilder.setTitle("Oups");
                        netbuilder.setCancelable(true);
                        netbuilder.setMessage(accountJson.getString(data_status));
                        netbuilder.setPositiveButton("OK", null);
                        netbuilder.show();
                    }
                }

            } catch (JSONException e) {
                Log.e(LOG_TAG, "caught exception in PostExecute " + e.getMessage());
            }
        }

    }

    private class saveContinueClientInfo extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = "LoginActivity";
        ProgressDialog progress = new ProgressDialog(ProductInformation.this);
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
                        editor.putBoolean("product_info", true);
                        editor.commit();

                        nameregulator.setText("");
                        email.setText("");
                        uploadfile.setText("");
                        serviceprovider.setText("");

                    } else {
                        AlertDialog.Builder netbuilder = new AlertDialog.Builder(ProductInformation.this);
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
