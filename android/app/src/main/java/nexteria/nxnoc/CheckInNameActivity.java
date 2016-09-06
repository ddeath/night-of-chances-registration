package nexteria.nxnoc;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static nexteria.nxnoc.Constants.EVENT_END_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_NAME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_ROOM_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_START_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_TYPE_COLUMN;

/**
 * Created by ddeath on 4/9/16.
 */
public class CheckInNameActivity extends AppCompatActivity {
    private Firebase dbRef;
    protected HashMap<String, HashMap<String, String>> attendees_list = new HashMap<>();
    protected List<HashMap<String, String>> attendees_search_results = new ArrayList<>();
    private AttendeesSearchListAdapter attendees_search_adapter;

    private ArrayList<HashMap<String,String>> attendee_events = new ArrayList<HashMap<String,String>>();
    private EventsListViewAdapter attendee_events_adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_name);

        attendees_list = (HashMap<String, HashMap<String,String>>) getIntent().getSerializableExtra("attendees_list");

        ListView search_results_view = (ListView) findViewById(R.id.attendees_search_results);
        attendees_search_adapter = new AttendeesSearchListAdapter(this, attendees_search_results);
        search_results_view.setAdapter(attendees_search_adapter);

        ListView listView = (ListView) findViewById(R.id.attendee_name_events_list);
        attendee_events_adapter = new EventsListViewAdapter(this, attendee_events);
        listView.setAdapter(attendee_events_adapter);

        initSearchBar();
    }

    private void initSearchBar()
    {
        final EditText search_bar = (EditText) findViewById(R.id.search_bar);

        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                attendees_search_results.clear();
                for(String key : attendees_list.keySet()) {
                    if(key.toLowerCase().contains(s.toString().toLowerCase())){
                        attendees_search_results.add(attendees_list.get(key));
                    }
                }

                attendees_search_adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        ImageButton search_reset_button = (ImageButton) findViewById(R.id.clear_searchbar);
        search_reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search_bar.setText("");
                attendees_search_results.clear();
                attendees_search_adapter.notifyDataSetChanged();
            }
        });

    }

    public void showAttendeeInfo(DataSnapshot attendee, String checked_in)
    {
        final TextView loading_text = (TextView) findViewById(R.id.loading_attendee_events_text);
        loading_text.setVisibility(View.VISIBLE);

        TextView attendee_name = (TextView) findViewById(R.id.attendee_name_text);
        attendee_name.setText((String) attendee.child("name").getValue());

        TextView ticket_type = (TextView) findViewById(R.id.ticket_type_text);
        ticket_type.setText((String) attendee.child("ticket_type").getValue());

        TextView attendee_note = (TextView) findViewById(R.id.attendee_note_text);
        String note_text = "";

        for (DataSnapshot note : attendee.child("notes").getChildren())
        {
            note_text = note_text + " " + (String) note.getValue();
        }

        attendee_note.setText(note_text);

        LinearLayout attendee_info_layout = (LinearLayout) findViewById(R.id.attendee_checkin_info_view);
        if (checked_in == "")
        {
            // show green background
            attendee_info_layout.setBackgroundColor(Color.parseColor("#3dbd4e"));
        }
        else
        {
            // show red background
            attendee_info_layout.setBackgroundColor(Color.parseColor("#FF4081"));
        }

        attendee_events.clear();

        for (DataSnapshot event_uuid : attendee.child("events").getChildren())
        {
            dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid.getKey());
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot event) {
                    if (event.exists())
                    {
                        HashMap<String,String> temp2 = new HashMap<String, String>();
                        Date start_time = new Date(Long.parseLong((String) event.child("start_time").getValue()));
                        Date end_time = new Date(Long.parseLong((String) event.child("end_time").getValue()));

                        DateFormat format = new SimpleDateFormat("HH:mm");
                        temp2.put(EVENT_START_TIME_COLUMN, format.format(start_time));
                        temp2.put(EVENT_END_TIME_COLUMN, format.format(end_time));
                        temp2.put(EVENT_TYPE_COLUMN, (String)  event.child("type").getValue());
                        temp2.put(EVENT_ROOM_COLUMN, (String)  event.child("room").getValue());
                        temp2.put(EVENT_NAME_COLUMN, (String)  event.child("name").getValue());
                        attendee_events.add(temp2);
                        attendee_events_adapter.notifyDataSetChanged();
                        loading_text.setVisibility(View.INVISIBLE);
                    }
                }
                @Override public void onCancelled(FirebaseError error)
                {
                    showAlertDialog("Error", "The event read failed: " + error.getMessage() + "\n\n rescan ticket again.");
                }
            });

            registerAttendee(event_uuid.getKey(), attendee.getKey(), checked_in);
        }

        for (DataSnapshot event_uuid : attendee.child("standin_events").getChildren())
        {
            dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid.getKey());
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot event) {
                    if (event.exists())
                    {
                        HashMap<String,String> temp2 = new HashMap<String, String>();
                        Date start_time = new Date(Long.parseLong((String) event.child("start_time").getValue()));
                        Date end_time = new Date(Long.parseLong((String) event.child("end_time").getValue()));

                        DateFormat format = new SimpleDateFormat("HH:mm");
                        temp2.put(EVENT_START_TIME_COLUMN, format.format(start_time));
                        temp2.put(EVENT_END_TIME_COLUMN, format.format(end_time));
                        temp2.put(EVENT_TYPE_COLUMN, (String)  event.child("type").getValue());
                        temp2.put(EVENT_ROOM_COLUMN, (String)  event.child("room").getValue());
                        temp2.put(EVENT_NAME_COLUMN, "NÁHRADNÍK " +(String)  event.child("name").getValue());
                        attendee_events.add(temp2);
                        attendee_events_adapter.notifyDataSetChanged();
                        loading_text.setVisibility(View.INVISIBLE);
                    }
                }
                @Override public void onCancelled(FirebaseError error)
                {
                    showAlertDialog("Error", "The event read failed: " + error.getMessage() + "\n\n rescan ticket again.");
                }
            });

            registerStandinAttendee(event_uuid.getKey(), attendee.getKey(), checked_in);
        }

        LinearLayout attendee_info_view = (LinearLayout) findViewById(R.id.attendee_checkin_info_view);
        attendee_info_view.setVisibility(View.VISIBLE);

        Button close_button = (Button) findViewById(R.id.attendee_info_close_button);

        close_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout attendee_info_view = (LinearLayout) findViewById(R.id.attendee_checkin_info_view);
                attendee_info_view.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void registerAttendee(final String event_uuid, final String attendee_key, final String checked_in)
    {
        dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/attendees/" + attendee_key);
        dbRef.setValue(checked_in, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null)
                {
                    new AlertDialog.Builder(CheckInNameActivity.this)
                            .setTitle("Error")
                            .setCancelable(false)
                            .setMessage("Data was not saved.\nTry again or contact responsible person.\n\n Attendee number:" + attendee_key)
                            .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    registerAttendee(event_uuid, attendee_key, checked_in);
                                }
                            })
                            .show();
                }
            }
        });
    }

    private void registerStandinAttendee(final String event_uuid, final String attendee_key, final String checked_in)
    {
        dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/standin_attendees/" + attendee_key);
        dbRef.setValue(checked_in, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null)
                {
                    new AlertDialog.Builder(CheckInNameActivity.this)
                            .setTitle("Error")
                            .setCancelable(false)
                            .setMessage("Data was not saved.\nTry again or contact responsible person.\n\n Attendee number:" + attendee_key)
                            .setPositiveButton("Try again", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    registerAttendee(event_uuid, attendee_key, checked_in);
                                }
                            })
                            .show();
                }
            }
        });
    }

    public void showAlertDialog(String title, String message)
    {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(false)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
