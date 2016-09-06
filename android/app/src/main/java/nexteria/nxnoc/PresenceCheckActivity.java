package nexteria.nxnoc;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import static nexteria.nxnoc.Constants.EVENT_CAPACITY_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_END_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_NAME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_PRESENT_ATTENDEES_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_ROOM_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_START_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_TYPE_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_UUID_COLUMN;


public class PresenceCheckActivity extends AppCompatActivity {

    private Firebase dbRef;

    public class CustomComparator implements Comparator<HashMap<String,String>> {
        @Override
        public int compare(HashMap<String,String> o1, HashMap<String,String> o2) {
            SimpleDateFormat formater = new SimpleDateFormat("HH:mm");
            Date date1 = null;
            try {
                date1 = formater.parse(o1.get(EVENT_START_TIME_COLUMN));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Date date2 = null;
            try {
                date2 = formater.parse(o2.get(EVENT_START_TIME_COLUMN));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            return date1.compareTo(date2);
        }
    }

    private ArrayList<HashMap<String,String>> events_list = new ArrayList<HashMap<String,String>>();
    private EventsListViewAdapter events_adapter = new EventsListViewAdapter(this, events_list);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_presence_check);
        Firebase.setAndroidContext(this.getApplication());

        ImageView img = (ImageView)findViewById(R.id.nexteria_loading_presence_checkimg);
        img.setBackgroundResource(R.drawable.nexteria_loading);
        AnimationDrawable frameAnimation = (AnimationDrawable) img.getBackground();
        frameAnimation.start();

        ListView listView = (ListView) findViewById(R.id.events_presence_check_list);
        listView.setAdapter(events_adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                HashMap<String, String> event = (HashMap<String, String>) parent.getAdapter().getItem(position);
                Intent intent = new Intent(view.getContext(), EventMonitoring.class);
                intent.putExtra("event_uuid", event.get(EVENT_UUID_COLUMN));
                startActivity(intent);
            }

        });

        dbRef = new Firebase("https://nxnoc.firebaseio.com/events/");
        dbRef = dbRef.orderByChild("attendees").startAt(true).getRef();
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot events) {
                events_list.clear();
                if (events.exists()) {
                    for (DataSnapshot event : events.getChildren()) {
                        HashMap<String, String> temp2 = new HashMap<String, String>();
                        Date start_time = new Date(Long.parseLong((String) event.child("start_time").getValue()) * 1000);
                        Date end_time = new Date(Long.parseLong((String) event.child("end_time").getValue()) * 1000);

                        DateFormat format = new SimpleDateFormat("HH:mm");
                        temp2.put(EVENT_START_TIME_COLUMN, format.format(start_time));
                        temp2.put(EVENT_END_TIME_COLUMN, format.format(end_time));
                        temp2.put(EVENT_TYPE_COLUMN, (String) event.child("type").getValue());
                        temp2.put(EVENT_ROOM_COLUMN, (String) event.child("room").getValue());
                        temp2.put(EVENT_NAME_COLUMN, (String) event.child("name").getValue());
                        temp2.put(EVENT_UUID_COLUMN, event.getKey());
                        temp2.put(EVENT_PRESENT_ATTENDEES_COLUMN, String.valueOf(event.child("present_attendees").getChildrenCount()));
                        temp2.put(EVENT_CAPACITY_COLUMN, String.valueOf(event.child("preselected_attendees_count").getValue()));
                        events_list.add(temp2);
                    }

                    Collections.sort(events_list, new CustomComparator());
                    events_adapter.notifyDataSetChanged();

                    LinearLayout loading_view = (LinearLayout) findViewById(R.id.presence_check_loading_layout);
                    loading_view.setVisibility(View.GONE);

                    ListView menu_view = (ListView) findViewById(R.id.events_presence_check_list);
                    menu_view.setVisibility(View.VISIBLE);
                }
            }
            @Override public void onCancelled(FirebaseError error)
            {
                new AlertDialog.Builder(PresenceCheckActivity.this)
                        .setTitle("Error")
                        .setCancelable(false)
                        .setMessage("There was an error during fetching data!")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });
    }
}
