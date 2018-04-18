package rw.limitless.limitlessapps.ussd;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.text.Text;

import de.hdodenhof.circleimageview.CircleImageView;

public class SingleContact extends AppCompatActivity {

    ImageButton back;
    ImageView services, homesimcard, phone;
    Intent intent;
    String contact_name, contact_number, contact_profile;
    CircleImageView contactimage;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 100;
    TextView fullname, phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_contact);

        back = (ImageButton) findViewById(R.id.back);
        contactimage = (CircleImageView) findViewById(R.id.contactimage);
        fullname = (TextView) findViewById(R.id.fullname);
        phone_number = (TextView) findViewById(R.id.phone_number);
        phone = (ImageView) findViewById(R.id.phone);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        services = (ImageView) findViewById(R.id.services);
        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(SingleContact.this, ClientServices.class);
                startActivity(intent);
            }
        });

        homesimcard = (ImageView) findViewById(R.id.homesimcard);
        homesimcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(SingleContact.this, ClientServices.class);
                startActivity(intent);
            }
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null) {
            if (extras.containsKey("fullname")) {
                contact_name = extras.getString("fullname");
                contact_profile = extras.getString("contact_profile");
                contact_number = extras.getString("contact_number");
            } else {
                contact_name = "";
                contact_profile = "";
                contact_number = "";
            }
        }

        fullname.setText(contact_name);
        phone_number.setText(contact_number);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!contact_number.isEmpty()){
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + contact_number));

                    // Here, thisActivity is the current activity
                    if (ContextCompat.checkSelfPermission(SingleContact.this,
                            android.Manifest.permission.CALL_PHONE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(SingleContact.this,
                                new String[]{android.Manifest.permission.CALL_PHONE},
                                MY_PERMISSIONS_REQUEST_CALL_PHONE);

                        // MY_PERMISSIONS_REQUEST_CALL_PHONE is an
                        // app-defined int constant. The callback method gets the
                        // result of the request.
                    } else {
                        //You already have permission
                        try {
                            startActivity(intent);
                        } catch(SecurityException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"Phone number is not available", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the phone call
                    Intent intent = new Intent(Intent.ACTION_CALL);
                    intent.setData(Uri.parse("tel:" + contact_number));
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(),"Calling is disabled", Toast.LENGTH_LONG).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
