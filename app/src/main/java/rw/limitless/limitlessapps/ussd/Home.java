package rw.limitless.limitlessapps.ussd;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeCustomDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.Closure;
import com.awesomedialog.blennersilva.awesomedialoglibrary.interfaces.ClosureEdit;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.credentials.Credential;
import com.google.android.gms.auth.api.credentials.CredentialPickerConfig;
import com.google.android.gms.auth.api.credentials.HintRequest;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;
import com.mukesh.countrypicker.models.Country;

import android.support.design.widget.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity {

    CountryPicker picker;
    ImageView buttonFlag;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    String dialCode = "";
    String countryName = "";
    TelephonyManager telephonyManager;
    TextView operatorname;
    ImageButton settings, imageaction;
    Intent intent;
    TabLayout tabLayout;
    ViewPager viewPager;
    final int limit = 5;
    TextView titleviewpage;
    JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        buttonFlag = (ImageView) findViewById(R.id.buttonFlag);

        operatorname = (TextView) findViewById(R.id.operatorname);

        settings = (ImageButton) findViewById(R.id.settings);

        titleviewpage = (TextView) findViewById(R.id.titleviewpage);

        imageaction = (ImageButton) findViewById(R.id.imageaction);

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(Home.this, Settings.class);
                startActivity(intent);
            }
        });

        readSimCard();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(getApplicationContext(), "Send SMS with Zarima is allowed", Toast.LENGTH_LONG);

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 10);

                Toast.makeText(getApplicationContext(), "Please allow Zarima to verify your phone number", Toast.LENGTH_LONG);
            }
        }

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new CustomAdapter(getSupportFragmentManager(), getApplicationContext()));
        viewPager.setOffscreenPageLimit(limit);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    titleviewpage.setText("Favorite");
                    imageaction.setImageResource(R.drawable.add);
                } else if (position == 1) {
                    titleviewpage.setText("Recommended");
                    imageaction.setImageResource(R.drawable.recent);
                } else if (position == 2) {
                    titleviewpage.setText("Contacts");
                    imageaction.setImageResource(R.drawable.account);
                } else if (position == 3) {
                    titleviewpage.setText("My service");
                    imageaction.setImageResource(R.drawable.service);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        readSimCard();
    }

    private class CustomAdapter extends FragmentPagerAdapter {
        private String fragments[] = {"Favorite", "Recent", "Contacts", "My Service"};

        private int[] imageResId = {

                R.drawable.start_border,
                R.drawable.recent,
                R.drawable.account,
                R.drawable.service
        };

        public CustomAdapter(FragmentManager supportFragmentManager, Context applicationContext) {
            super(supportFragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new Favorite();
                case 1:
                    return new Recent();
                case 2:
                    return new Contacts();
                case 3:
                    return new MyService();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        public CharSequence getPageTitle(int position) {
            //return fragments[position];
            Drawable image = ContextCompat.getDrawable(Home.this, imageResId[position]);
            image.setBounds(0, 0, image.getIntrinsicWidth(), image.getIntrinsicHeight());
            SpannableString sb = new SpannableString(" ");
            ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
            sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            return sb;
        }
    }

    public void readSimCard() {

        picker = CountryPicker.newInstance("Select Country");
        Country country = picker.getUserCountryInfo(Home.this); //Get user country based on sim

        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt("flag", country.getFlag());
        editor.putString("country", country.getName());
        editor.putString("dialCode", country.getDialCode());
        editor.commit();

        if (!sharedpreferences.getString("phone", "").isEmpty()) {
            telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
            String simOperatorName = telephonyManager.getSimOperatorName();
            //load data from sim and server
            operatorname.setText(simOperatorName);
        } else if (sharedpreferences.getString("sim_operator", "").isEmpty() && sharedpreferences.getString("phone", "").isEmpty()) {
            //read country from your SIM CARD
            dialCode = country.getDialCode();
            countryName = country.getName();

            if (sharedpreferences.getInt("flag", 0) != 0) {
                buttonFlag.setImageResource(sharedpreferences.getInt("flag", 0));
            } else {
                buttonFlag.setImageResource(country.getFlag());
            }

            if (!country.getName().isEmpty()) {
                new AwesomeInfoDialog(Home.this)
                        .setTitle(R.string.location_detection)
                        .setMessage("Do you leave in " + country.getName())
                        .setColoredCircle(R.color.button_action_color)
                        .setDialogIconAndColor(R.drawable.location, R.color.white)
                        .setCancelable(false)
                        .setPositiveButtonText(getString(R.string.dialog_yes_button))
                        .setPositiveButtonbackgroundColor(R.color.button_action_color)
                        .setPositiveButtonTextColor(R.color.white)
                        .setNegativeButtonText(getString(R.string.dialog_no_button))
                        .setNegativeButtonbackgroundColor(R.color.button_cancel)
                        .setNegativeButtonTextColor(R.color.white)
                        .setPositiveButtonClick(new Closure() {
                            @Override
                            public void exec() {
                                //click
                                ask_phone_number(false, sharedpreferences.getString("dialCode", ""));
                            }
                        })
                        .setNegativeButtonClick(new Closure() {
                            @Override
                            public void exec() {
                                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                                picker.setListener(new CountryPickerListener() {
                                    @Override
                                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                                        // Implement your code here
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putInt("flag", flagDrawableResID);
                                        editor.commit();

                                        buttonFlag.setImageResource(flagDrawableResID);

                                        // Refresh main activity upon close of dialog box
                                        Intent refresh = new Intent(Home.this, Home.class);
                                        startActivity(refresh);
                                        finish();
                                    }
                                });
                            }
                        })
                        .show();
            } else {
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        // Implement your code here
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putInt("flag", flagDrawableResID);
                        editor.commit();
                    }
                });
            }
        }
    }

    public void ask_phone_number(final boolean isOk, String phoneCode) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Enter your SIM card phone number");

        final EditText input = new EditText(Home.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_CLASS_PHONE);
        input.setText(phoneCode);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("SAVE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().equals("")) {
                            Toast.makeText(getApplicationContext(), "Please enter your phone number", Toast.LENGTH_LONG);
                        } else if (isValidPhoneNo(input.getText())) {
                            telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
                            String simOperatorName = telephonyManager.getSimOperatorName();

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("phone", input.getText().toString());
                            editor.putString("dial_code", dialCode);
                            editor.putString("country", countryName);
                            editor.putString("sim_operator", simOperatorName.isEmpty() ? "" : simOperatorName);
                            editor.commit();

                            String smsMsgVar = "Thank you for verifying "+input.getText().toString();

                            sendSmsMsgFnc(input.getText().toString(), smsMsgVar);

                            saveClientInfo(input.getText().toString(), countryName, simOperatorName.isEmpty() ? "" : simOperatorName);
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter a valid phone number",
                                    Toast.LENGTH_LONG).show();

                            handleErrorShowDialog(input.getText().toString(), countryName);
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

    public void handleErrorShowDialog(String phoneCode, String countryName) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Enter your SIM card phone number");

        final EditText input = new EditText(Home.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_CLASS_PHONE);
        input.setText(phoneCode);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("SAVE",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().equals("")) {
                            Toast.makeText(getApplicationContext(), "Please enter your phone number", Toast.LENGTH_LONG);
                        } else if (isValidPhoneNo(input.getText())) {
                            telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
                            String simOperatorName = telephonyManager.getSimOperatorName();

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("phone", input.getText().toString());
                            editor.putString("dial_code", dialCode);
                            editor.putString("country", sharedpreferences.getString("country",""));
                            editor.putString("sim_operator", simOperatorName.isEmpty() ? "" : simOperatorName);
                            editor.commit();

                            String smsMsgVar = "Thank you for verifying "+input.getText().toString();

                            sendSmsMsgFnc(input.getText().toString(), smsMsgVar);

                            saveClientInfo(input.getText().toString(), sharedpreferences.getString("country", ""), simOperatorName.isEmpty() ? "" : simOperatorName);
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter a valid phone number",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        alertDialog.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();
    }

    public static boolean isValidPhoneNo(CharSequence iPhoneNo) {
        return !TextUtils.isEmpty(iPhoneNo) && Patterns.PHONE.matcher(iPhoneNo).matches();
    }

    public void sendSmsMsgFnc(String mblNumVar, String smsMsgVar) {
        verifyingPhone(true, mblNumVar);

        try {
            SmsManager smsMgrVar = SmsManager.getDefault();
            smsMgrVar.sendTextMessage(mblNumVar, null, smsMsgVar, null, null);
            Toast.makeText(getApplicationContext(), "Your number (" + mblNumVar + ")" + " " + "is verified",
                    Toast.LENGTH_LONG).show();
            verifyingPhone(false, mblNumVar);
        } catch (Exception ErrVar) {
            Toast.makeText(getApplicationContext(), ErrVar.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
            verifyingPhone(false, mblNumVar);
            ErrVar.printStackTrace();
        }
    }

    public void verifyingPhone(boolean isVerifying, String phone) {
        ProgressDialog dialog = new ProgressDialog(Home.this);
        Log.d("isverifying", String.valueOf(isVerifying));
        if (isVerifying) {
            Toast.makeText(getApplicationContext(), "Verifying " + phone,
                    Toast.LENGTH_LONG).show();

            dialog.setMessage("Verifying " + phone + "...");
            dialog.show();
        }

        dialog.dismiss();
    }

    private void saveClientInfo(String phone_no, String countryName, String sim_operator) {
        if (isNetworkAvailable()) {
            try {
                data = new JSONObject();
                data.put("source", "rw.limitless.limitlessapps.ussd");
                data.put("phone_number", phone_no);
                data.put("country", countryName);
                data.put("sim_operator", sim_operator);

                String URL = "http://159.89.33.228:3000/api/phone_carrier_app_start";

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
        ProgressDialog progress = new ProgressDialog(Home.this);
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

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
                    alertDialog.setTitle("Enter your SIM card phone number");

                    final EditText input = new EditText(Home.this);
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);
                    input.setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_CLASS_PHONE);
                    input.setText(sharedpreferences.getString("dialCode", ""));
                    alertDialog.setView(input);

                    alertDialog.setPositiveButton("SAVE",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    if (input.getText().equals("")) {
                                        Toast.makeText(getApplicationContext(), "Please enter your phone number", Toast.LENGTH_LONG);
                                    } else if (isValidPhoneNo(input.getText())) {
                                        telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
                                        String simOperatorName = telephonyManager.getSimOperatorName();
                                        SharedPreferences.Editor editor = sharedpreferences.edit();
                                        editor.putString("phone", input.getText().toString());
                                        editor.putString("dial_code", dialCode);
                                        editor.putString("country", countryName);
                                        editor.putString("sim_operator", simOperatorName.isEmpty() ? "" : simOperatorName);
                                        editor.commit();

                                        String smsMsgVar = "Thank you for verifying "+input.getText().toString();

                                        sendSmsMsgFnc(input.getText().toString(), smsMsgVar);
                                        saveClientInfo(input.getText().toString(), countryName, simOperatorName.isEmpty() ? "" : simOperatorName);
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Please enter a valid phone number",
                                                Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                    alertDialog.setNegativeButton("NO",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });

                    alertDialog.show();

                } else {
                    JSONObject accountJson = new JSONObject(s);
                    if (accountJson.getString(data_status).equals("success")) {
                        Intent StepTwoIntent = new Intent(Home.this, Home.class);
                        StepTwoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(StepTwoIntent);
                    } else {
                        AlertDialog.Builder netbuilder = new AlertDialog.Builder(Home.this);
                        netbuilder.setTitle("Oups");
                        netbuilder.setCancelable(true);
                        netbuilder.setMessage(accountJson.getString(data_message));
                        netbuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
                                alertDialog.setTitle("Enter your SIM card phone number");

                                final EditText input = new EditText(Home.this);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);
                                input.setInputType(InputType.TYPE_CLASS_TEXT |
                                        InputType.TYPE_CLASS_PHONE);
                                input.setText(sharedpreferences.getString("dialCode",""));
                                alertDialog.setView(input);

                                alertDialog.setPositiveButton("SAVE",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (input.getText().equals("")) {
                                                    Toast.makeText(getApplicationContext(), "Please enter your phone number", Toast.LENGTH_LONG);
                                                } else if (isValidPhoneNo(input.getText())) {
                                                    telephonyManager = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));
                                                    String simOperatorName = telephonyManager.getSimOperatorName();

                                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                                    editor.putString("phone", input.getText().toString());
                                                    editor.putString("dial_code", dialCode);
                                                    editor.putString("country", sharedpreferences.getString("country",""));
                                                    editor.putString("sim_operator", simOperatorName.isEmpty() ? "" : simOperatorName);
                                                    editor.commit();

                                                    String smsMsgVar = "Thank you for verifying "+input.getText().toString();

                                                    sendSmsMsgFnc(input.getText().toString(), smsMsgVar);

                                                    saveClientInfo(input.getText().toString(), sharedpreferences.getString("country", ""), simOperatorName.isEmpty() ? "" : simOperatorName);
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Please enter a valid phone number",
                                                            Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });

                                alertDialog.setNegativeButton("NO",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.cancel();
                                            }
                                        });

                                alertDialog.show();
                            }
                        });
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
