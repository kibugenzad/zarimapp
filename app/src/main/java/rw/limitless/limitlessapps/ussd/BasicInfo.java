package rw.limitless.limitlessapps.ussd;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.awesomedialog.blennersilva.awesomedialoglibrary.AwesomeInfoDialog;
import com.mukesh.countrypicker.fragments.CountryPicker;
import com.mukesh.countrypicker.interfaces.CountryPickerListener;

public class BasicInfo extends AppCompatActivity {

    ImageButton back;
    SharedPreferences sharedpreferences;
    public static final String mypreference = "USSD_data";
    TextView phone,carrier,country;
    LinearLayout countrylayout;
    CountryPicker picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_info);

        sharedpreferences = getSharedPreferences(mypreference, Context.MODE_PRIVATE);

        back = (ImageButton) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        phone = (TextView) findViewById(R.id.phone);
        carrier = (TextView) findViewById(R.id.carrier);
        countrylayout = (LinearLayout) findViewById(R.id.countrylayout);
        country= (TextView) findViewById(R.id.country);

        countrylayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                picker = CountryPicker.newInstance("Select Country");
                picker.show(getSupportFragmentManager(), "COUNTRY_PICKER");
                picker.setListener(new CountryPickerListener() {
                    @Override
                    public void onSelectCountry(String name, String code, String dialCode, int flagDrawableResID) {
                        // Implement your code here
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putString("dial_code", dialCode);
                        editor.putString("country", name);
                        editor.commit();

                        country.setText(sharedpreferences.getString("country",""));
                    }
                });
            }
        });

        //populate
        getPhoneData();
    }

    public void getPhoneData(){
        if (!sharedpreferences.getString("sim_operator","").isEmpty() || !sharedpreferences.getString("phone","").isEmpty()){
            phone.setText(sharedpreferences.getString("phone",""));
            carrier.setText(sharedpreferences.getString("sim_operator",""));
            country.setText(sharedpreferences.getString("country",""));
        }
    }
}
