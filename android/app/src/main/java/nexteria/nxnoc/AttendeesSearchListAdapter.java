package nexteria.nxnoc;

import static nexteria.nxnoc.Constants.ATTENDEE_SEARCH_NAME;
import static nexteria.nxnoc.Constants.ATTENDEE_SEARCH_EMAIL;
import static nexteria.nxnoc.Constants.ATTENDEE_SEARCH_ID;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import nexteria.nxnoc.CheckInNameActivity;

/**
 * Created by ddeath on 3/26/16.
 */
public class AttendeesSearchListAdapter extends BaseAdapter {

    public class CheckInOnClick implements View.OnClickListener
    {
        private String attendee_id;
        private Firebase dbRef;

        public CheckInOnClick(String attendee_id) {
            this.attendee_id = attendee_id;
        }

        @Override
        public void onClick(View v)
        {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee_id);
            dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (snapshot.exists())
                    {
                        String checked_in = (String) snapshot.child("checked_in").getValue();
                        final String attendee_name = (String) snapshot.child("name").getValue();
                        Date current_time = new Date();
                        dbRef.child("checked_in").setValue(String.valueOf(current_time.getTime()), new Firebase.CompletionListener() {
                            @Override
                            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                if (firebaseError != null)
                                {
                                    activity.showAlertDialog("Error", "Data was not saved.\nTry again or write name on paper.\n\n" + attendee_name);
                                }
                            }
                        });

                        activity.showAttendeeInfo(snapshot, checked_in);
                    }
                    else
                    {

                    }
                }
                @Override public void onCancelled(FirebaseError error)
                {
                    activity.showAlertDialog("Error", "The read failed: " + error.getMessage());
                }
            });
        }

    }

    public List<HashMap<String, String>> attendees_list;
    CheckInNameActivity activity;
    Button checkin_button;
    TextView txt_attendee_name;

    public AttendeesSearchListAdapter(Activity activity, List<HashMap<String, String>> attendees_list){
        super();
        this.activity = (CheckInNameActivity) activity;
        this.attendees_list = attendees_list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return attendees_list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return attendees_list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        HashMap<String, String> map = attendees_list.get(position);

        if(convertView == null){
            LayoutInflater inflater=activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.search_result_attendee, null);

            txt_attendee_name = (TextView) convertView.findViewById(R.id.attendee_result_name);
            checkin_button = (Button) convertView.findViewById(R.id.attendee_result_checkin_button);

            convertView.setTag(R.id.attendee_result_checkin_button, checkin_button);
            convertView.setTag(R.id.attendee_result_name, txt_attendee_name);

        }
        else
        {
            txt_attendee_name = (TextView) convertView.getTag(R.id.attendee_result_name);
            checkin_button = (Button) convertView.getTag(R.id.attendee_result_checkin_button);
        }

        txt_attendee_name.setText(map.get(ATTENDEE_SEARCH_NAME) + " (" + map.get(ATTENDEE_SEARCH_EMAIL) + ")");
        checkin_button.setOnClickListener(new CheckInOnClick(map.get(ATTENDEE_SEARCH_ID)));

        return convertView;
    }

}