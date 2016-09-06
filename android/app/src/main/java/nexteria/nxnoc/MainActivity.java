package nexteria.nxnoc;

import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;

import static nexteria.nxnoc.Constants.EVENT_UUID_COLUMN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_CHECKED_IN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_ID;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_NAME_COLUMN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_PHONE_COLUMN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_PRESENT;

public class MainActivity extends AppCompatActivity {

    protected HashMap<String, HashMap<String, String>> attendees_list = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);

        setContentView(R.layout.activity_main);

        ImageView img = (ImageView)findViewById(R.id.nexteria_loading_img);
        img.setBackgroundResource(R.drawable.nexteria_loading);
        AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
        frameAnimation.start();

        login();
        fetchData();

        ImageView nexteria_logo = (ImageView) findViewById(R.id.nexteria_logo_img);
        nexteria_logo.setVisibility(View.VISIBLE);

        Button checkin_scan_button = (Button) findViewById(R.id.checkin_scan_button);
        checkin_scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            Intent intent = new Intent(v.getContext(), CheckInScanActivity.class);
            startActivity(intent);
            }
        });

        Button  presence_check_button = (Button) findViewById(R.id.presence_check_button);
        presence_check_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PresenceCheckActivity.class);
                startActivity(intent);
            }
        });

        Button  check_in_by_name_button = (Button) findViewById(R.id.check_in_by_name_button);
        check_in_by_name_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CheckInNameActivity.class);
                intent.putExtra("attendees_list", attendees_list);
                startActivity(intent);
            }
        });
    }

    private void fetchData()
    {
        Firebase dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot attendees) {
                long count = 0;
                for (DataSnapshot attendee : attendees.getChildren())
                {
                    if ((String) attendee.child("checked_in").getValue() != "")
                    {
                        count++;
                    }
                }

                TextView number_of_attendees = (TextView) findViewById(R.id.number_of_attendees_onsite);
                number_of_attendees.setText(String.valueOf(count));
                LinearLayout attendees_info_count = (LinearLayout) findViewById(R.id.attendees_info_count);
                attendees_info_count.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                TextView number_of_attendees = (TextView) findViewById(R.id.number_of_attendees_onsite);
                number_of_attendees.setText("-E-");
                LinearLayout attendees_info_count = (LinearLayout) findViewById(R.id.attendees_info_count);
                attendees_info_count.setVisibility(View.VISIBLE);
            }
        });

        dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees");
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot attendees) {
                for(DataSnapshot attendee : attendees.getChildren())
                {
                    String name = (String) attendee.child("name").getValue();
                    String email = (String) attendee.child("email").getValue();
                    String id = (String) attendee.getKey();

                    HashMap<String, String> attendee_attributes = new HashMap<String, String>();
                    attendee_attributes.put("name", name);
                    attendee_attributes.put("email", email);
                    attendee_attributes.put("id", id);

                    attendees_list.put(name, attendee_attributes);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.app_name))
                        .setCancelable(false)
                        .setMessage(firebaseError.getMessage() + ". \n\nAttendees list fetch failed.")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fetchData();
                            }
                        })
                        .show();
            }
        });
    }

    private void login()
    {
        Firebase dbRef = new Firebase("https://nxnoc.firebaseio.com/");
        dbRef.authWithPassword("dusan.plavak@gmail.com", "google", new Firebase.AuthResultHandler() {
            @Override
            public void onAuthenticated(AuthData authData) {
                LinearLayout loading_view = (LinearLayout) findViewById(R.id.main_menu_loading_layout);
                loading_view.setVisibility(View.GONE);

                LinearLayout menu_view = (LinearLayout) findViewById(R.id.main_menu_layout);
                menu_view.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAuthenticationError(FirebaseError firebaseError) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.app_name))
                        .setCancelable(false)
                        .setMessage(firebaseError.getMessage() + ". \n\nPlease check your internet connection.")
                        .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                login();
                            }
                        })
                        .show();
            }
        });
    }
}
