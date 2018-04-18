package rw.limitless.limitlessapps.ussd;

import android.content.Intent;

import com.github.clans.fab.FloatingActionButton;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CallService extends AppCompatActivity {

    ImageButton back;
    String fullussd;
    LinearLayout showcollingcode_layout;
    TextView fullussdcode;
    String final_replacedString;
    String final_ussd_url;
    List<EditText> allEds = new ArrayList<EditText>();
    String[] strings;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_service);

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDatas();
            }
        });

        showcollingcode_layout = (LinearLayout) findViewById(R.id.showcollingcode_layout);
        fullussdcode = (TextView) findViewById(R.id.fullussdcode);

        final Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.containsKey("fullussd")) {
                fullussd = extras.getString("fullussd");
            } else {
                fullussd = "";
            }
        }

        String[] splited = fullussd.split("\\(");
        StringBuilder system_field = new StringBuilder();

        for (int i = 1; i < splited.length; i++) {
            system_field.append(splited[i]);
        }

        String new_system_field = String.valueOf(system_field);
        String replacedStringNumber;
        String replacedStringUser_info;
        String semi_final_replacedString, other_final_replacedString;

        if (new_system_field != null && new_system_field.length() > 0 && new_system_field.charAt(new_system_field.length() - 1) == '*') {

            replacedStringNumber = new_system_field.replace("System field)", "*");
            replacedStringUser_info = replacedStringNumber.replace("User info)", "*User info ");
            semi_final_replacedString = replacedStringUser_info.replace("Pin)", "*Pin ");
            other_final_replacedString = semi_final_replacedString.replace("Amount)", "*Amount ");

            final_ussd_url = other_final_replacedString;

            getSemiFinal(other_final_replacedString);

        } else {
            replacedStringNumber = new_system_field.replace("System field)", "*");
            replacedStringUser_info = replacedStringNumber.replace("User info)", "*User info ");
            semi_final_replacedString = replacedStringUser_info.replace("Pin)", "*Pin ");
            other_final_replacedString = semi_final_replacedString.replace("Amount)", "*Amount ");

            final_ussd_url = other_final_replacedString;

            getSemiFinal(other_final_replacedString);
        }
    }

    private void getSemiFinal(String semi_final_replacedString) {
        final_replacedString = semi_final_replacedString.replace("**", "*");

        if (!fullussd.isEmpty()) {
            showcollingcode_layout.setVisibility(View.VISIBLE);
            fullussdcode.setText(final_replacedString+Uri.encode("#"));
        }

        String[] split_final = final_replacedString.split("\\*");
        StringBuilder buld_final_ussd = new StringBuilder();

        String user_info = "User info";
        String user_pin = "Pin";
        String user_amount = "Amount";

        //find system field
        for (int i = 0; i < split_final.length; i++) {
            buld_final_ussd.append(split_final[i]);
            if (split_final[i].startsWith(user_info)) {
                add_text_field(i, user_info, Integer.parseInt(split_final[i].substring(10)), "User info");
            } else {
                System.out.println("not found");
            }

            if (split_final[i].startsWith(user_pin)) {
                add_text_field(i, user_pin, Integer.parseInt(split_final[i].substring(4)), "Pin");
            } else {
                System.out.println("not found");
            }

            if (split_final[i].startsWith(user_amount)) {
                add_text_field(i, user_amount, Integer.parseInt(split_final[i].substring(7)), "Amount");
            } else {
                System.out.println("not found");
            }
        }
    }

    private void add_text_field(int numberOfLines, String placeholder, int maxLength, String input_type) {
        LinearLayout ll = (LinearLayout)
                findViewById(R.id.dynamic_layout);

        // add edittext
        EditText et = new EditText(this);
        allEds.add(et);
        et.setId(numberOfLines + 1);
        et.setHint(input_type.equals("User info") ? "Enter Phone / Meter number etc..." : input_type.equals("Pin") ? "Enter Pin (max length " + maxLength + ")" : input_type.equals("Amount") ? "Enter amount (min" + maxLength + ")" : "");
        et.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        et.setInputType(input_type.equals("Pin") ? InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD : input_type.equals("Amount") ? InputType.TYPE_CLASS_NUMBER : InputType.TYPE_CLASS_TEXT);
        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        ll.addView(et);
        numberOfLines++;
        System.out.println(numberOfLines);
    }

    private void getDatas() {
        strings = new String[]{String.valueOf(allEds.size())};

        System.out.println(final_ussd_url);

        String[] split_final = final_replacedString.split("\\*");
        StringBuilder buld_final_ussd = new StringBuilder();

        String user_info = "User info";
        String user_pin = "Pin";
        String user_amount = "Amount";
        String replace_field, replace_amount, replace_user_info = "", replace_pin;
        StringBuilder sb = new StringBuilder();

        for (EditText i : allEds) {
            String newstrings = i.getText().toString();
            CharSequence ids = i.getHint();

            if (newstrings.isEmpty()) {
                Toast.makeText(getApplicationContext(), "All fields is required", Toast.LENGTH_LONG).show();
                i.setError("This field is required");
            } else {
                //find system field
                for (int t = 0; t < split_final.length; t++) {
                    buld_final_ussd.append(split_final[t]);
                    if (split_final[t].startsWith(user_info)) {
                        int max_length = Integer.parseInt(split_final[t].substring(10));
                        if (ids.toString().startsWith("Enter Phone")) {
                            replace_user_info = fullussdcode.getText().toString().replace(user_info + " " + max_length, newstrings);
                            fullussdcode.setText(replace_user_info);
                        }
                    } else {
                        System.out.println("not found");
                    }
                    if (split_final[t].startsWith(user_amount)) {
                        int max_length = Integer.parseInt(split_final[t].substring(7));
                        if (ids.toString().startsWith("Enter amount")) {
                            replace_amount = fullussdcode.getText().toString().replace(user_amount + " " + max_length, newstrings);
                            fullussdcode.setText(replace_amount);
                        }
                    }

                    if (split_final[t].startsWith(user_pin)) {
                        int max_length = Integer.parseInt(split_final[t].substring(4));
                        if (ids.toString().startsWith("Enter Pin")) {
                            replace_field = fullussdcode.getText().toString().replace(user_pin + " " + max_length, newstrings);
                            fullussdcode.setText(replace_field);
                        }
                    }
                }
            }
        }
        System.out.println(fullussdcode.getText().toString());
        Intent callIntent = new Intent(Intent.ACTION_CALL, ussdToCallableUri(fullussdcode.getText().toString()));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        startActivity(callIntent);
    }

    private Uri ussdToCallableUri(String ussd) {

        String uriString = "";

        if(!ussd.startsWith("tel:"))
            uriString += "tel:";

        for(char c : ussd.toCharArray()) {

            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }

        return Uri.parse(uriString);
    }
}
