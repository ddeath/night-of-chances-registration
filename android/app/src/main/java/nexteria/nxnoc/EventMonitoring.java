package nexteria.nxnoc;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import static nexteria.nxnoc.Constants.EVENT_UUID_COLUMN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_CHECKED_IN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_ID;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_PRESENT;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_NAME_COLUMN;
import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_PHONE_COLUMN;

public class EventMonitoring extends AppCompatActivity {

    private AttendeesExapndableListViewAdapter attendees_adapter;
    private HashMap<String, List<HashMap<String, String>>> attendees_groups = new HashMap<String, List<HashMap<String,String>>>();
    private List<String> attendees_group_labels = new ArrayList<String>();


    private String event_uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_monitoring);

        attendees_group_labels.add(0, "Prítomní");
        attendees_group_labels.add(1, "Účastníci");
        attendees_group_labels.add(2, "Náhradníci");

        attendees_groups.put(attendees_group_labels.get(0), new ArrayList<HashMap<String, String>>());
        attendees_groups.put(attendees_group_labels.get(1), new ArrayList<HashMap<String, String>>());
        attendees_groups.put(attendees_group_labels.get(2), new ArrayList<HashMap<String, String>>());




        ExpandableListView listView = (ExpandableListView) findViewById(R.id.monitoring_attendee_list);
        attendees_adapter = new AttendeesExapndableListViewAdapter(this, attendees_group_labels, attendees_groups);
        listView.setAdapter(attendees_adapter);

        event_uuid = getIntent().getStringExtra("event_uuid");
        Firebase dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid);
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot event) {
                attendees_groups.get(attendees_group_labels.get(0)).clear();
                attendees_groups.get(attendees_group_labels.get(1)).clear();
                attendees_groups.get(attendees_group_labels.get(2)).clear();
                attendees_adapter.notifyDataSetChanged();

                if (event.exists())
                {
                    TextView event_name = (TextView) findViewById(R.id.monitoring_event_name);
                    event_name.setText((String) event.child("name").getValue());

                    TextView event_capacity = (TextView) findViewById(R.id.monitoring_event_capacity);
                    event_capacity.setText(String.valueOf(event.child("preselected_attendees_count").getValue()));

                    TextView event_min_attendees = (TextView) findViewById(R.id.monitoring_min_capacity);
                    event_min_attendees.setText(String.valueOf(event.child("min_capacity").getValue()));

                    TextView event_max_attendees = (TextView) findViewById(R.id.monitoring_max_capacity);
                    event_max_attendees.setText(String.valueOf(event.child("max_capacity").getValue()));

                    Firebase dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/attendees");
                    dbRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot attendees) {
                            attendees_groups.get(attendees_group_labels.get(1)).clear();
                            attendees_adapter.notifyDataSetChanged();

                            for(DataSnapshot attendee : attendees.getChildren())
                            {
                                Firebase ref = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee.getKey());
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot attendee) {
                                        if (attendee.exists()) {
                                            HashMap<String,String> temp2 = new HashMap<String, String>();

                                            temp2.put(MONITORING_EVENT_ATTENDEE_NAME_COLUMN, (String)  attendee.child("name").getValue());
                                            temp2.put(MONITORING_EVENT_ATTENDEE_PHONE_COLUMN, (String)  attendee.child("phone").getValue());
                                            temp2.put(MONITORING_EVENT_ATTENDEE_CHECKED_IN, String.valueOf(attendee.child("checked_in").getValue()));
                                            temp2.put(MONITORING_EVENT_ATTENDEE_ID, attendee.getKey());
                                            temp2.put(EVENT_UUID_COLUMN, event_uuid);
                                            temp2.put(MONITORING_EVENT_ATTENDEE_PRESENT, "false");
                                            attendees_groups.get(attendees_group_labels.get(1)).add(temp2);
                                            attendees_adapter.notifyDataSetChanged();
                                        }
                                    }
                                    @Override public void onCancelled(FirebaseError error)
                                    {
                                        new AlertDialog.Builder(EventMonitoring.this)
                                                .setTitle("Error")
                                                .setCancelable(false)
                                                .setMessage("Can not fetch attendee info. Try again. \n\n" + error.getMessage())
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }
                        }

                        @Override public void onCancelled(FirebaseError error)
                        {
                            new AlertDialog.Builder(EventMonitoring.this)
                                    .setTitle("Error")
                                    .setCancelable(false)
                                    .setMessage("Can not fetch attendee info. Try again. \n\n" + error.getMessage())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    });

                    dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/present_attendees");
                    dbRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot attendees) {
                            attendees_groups.get(attendees_group_labels.get(0)).clear();
                            attendees_adapter.notifyDataSetChanged();

                            TextView event_attendee_present_count = (TextView) findViewById(R.id.monitoring_present_attendee_count);
                            event_attendee_present_count.setText(String.valueOf(attendees.getChildrenCount()));

                            for(DataSnapshot attendee : attendees.getChildren())
                            {
                                Firebase ref = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee.getKey());
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot attendee) {
                                        if (attendee.exists()) {
                                            HashMap<String,String> temp2 = new HashMap<String, String>();

                                            if (attendee.child("standin_events").child(event_uuid).exists())
                                            {
                                                temp2.put(MONITORING_EVENT_ATTENDEE_NAME_COLUMN, (String)  attendee.child("name").getValue() + " (N)");
                                            }
                                            else
                                            {
                                                temp2.put(MONITORING_EVENT_ATTENDEE_NAME_COLUMN, (String)  attendee.child("name").getValue() + " (U)");
                                            }

                                            temp2.put(MONITORING_EVENT_ATTENDEE_PHONE_COLUMN, (String)  attendee.child("phone").getValue());
                                            temp2.put(MONITORING_EVENT_ATTENDEE_CHECKED_IN, String.valueOf(attendee.child("checked_in").getValue()));
                                            temp2.put(MONITORING_EVENT_ATTENDEE_ID, attendee.getKey());
                                            temp2.put(EVENT_UUID_COLUMN, event_uuid);
                                            temp2.put(MONITORING_EVENT_ATTENDEE_PRESENT, "true");
                                            attendees_groups.get(attendees_group_labels.get(0)).add(temp2);
                                            attendees_adapter.notifyDataSetChanged();
                                        }
                                    }
                                    @Override public void onCancelled(FirebaseError error)
                                    {
                                        new AlertDialog.Builder(EventMonitoring.this)
                                                .setTitle("Error")
                                                .setCancelable(false)
                                                .setMessage("Can not fetch attendee info. Try again. \n\n" + error.getMessage())
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }
                        }

                        @Override public void onCancelled(FirebaseError error)
                        {
                            new AlertDialog.Builder(EventMonitoring.this)
                                    .setTitle("Error")
                                    .setCancelable(false)
                                    .setMessage("Can not fetch attendee info. Try again. \n\n" + error.getMessage())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    });

                    dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/standin_attendees");
                    dbRef2.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot attendees) {
                            attendees_groups.get(attendees_group_labels.get(2)).clear();
                            attendees_adapter.notifyDataSetChanged();

                            for(DataSnapshot attendee : attendees.getChildren())
                            {
                                Firebase ref = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee.getKey());
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot attendee) {
                                        if (attendee.exists()) {
                                            HashMap<String,String> temp2 = new HashMap<String, String>();

                                            temp2.put(MONITORING_EVENT_ATTENDEE_NAME_COLUMN, (String)  attendee.child("name").getValue());
                                            temp2.put(MONITORING_EVENT_ATTENDEE_PHONE_COLUMN, (String)  attendee.child("phone").getValue());
                                            temp2.put(MONITORING_EVENT_ATTENDEE_CHECKED_IN, String.valueOf(attendee.child("checked_in").getValue()));
                                            temp2.put(MONITORING_EVENT_ATTENDEE_ID, attendee.getKey());
                                            temp2.put(EVENT_UUID_COLUMN, event_uuid);
                                            temp2.put(MONITORING_EVENT_ATTENDEE_PRESENT, "false");
                                            attendees_groups.get(attendees_group_labels.get(2)).add(temp2);
                                            attendees_adapter.notifyDataSetChanged();
                                        }
                                    }
                                    @Override public void onCancelled(FirebaseError error)
                                    {
                                        new AlertDialog.Builder(EventMonitoring.this)
                                                .setTitle("Error")
                                                .setCancelable(false)
                                                .setMessage("Can not fetch attendee info. Try again. \n\n" + error.getMessage())
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                    }
                                                })
                                                .show();
                                    }
                                });
                            }
                        }

                        @Override public void onCancelled(FirebaseError error)
                        {
                            new AlertDialog.Builder(EventMonitoring.this)
                                    .setTitle("Error")
                                    .setCancelable(false)
                                    .setMessage("Can not fetch attendee info. Try again. \n\n" + error.getMessage())
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    })
                                    .show();
                        }
                    });

                }
            }
            @Override public void onCancelled(FirebaseError error)
            {
                new AlertDialog.Builder(EventMonitoring.this)
                        .setTitle("Error")
                        .setCancelable(false)
                        .setMessage("The event read failed: " + error.getMessage())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
    }
}
