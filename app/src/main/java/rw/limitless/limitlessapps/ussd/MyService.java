package rw.limitless.limitlessapps.ussd;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by limitlessapps on 04/01/2018.
 */

public class MyService extends android.support.v4.app.Fragment {

    ImageView services,homesimcard;
    Intent intent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.myservice, container, false);

        services = (ImageView) view.findViewById(R.id.services);
        services.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getContext(), ClientServices.class);
                startActivity(intent);
            }
        });

        homesimcard = (ImageView) view.findViewById(R.id.homesimcard);
        homesimcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(getContext(), ClientServices.class);
                startActivity(intent);
            }
        });

        return view;
    }
}
