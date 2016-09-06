package nexteria.nxnoc;

        import android.app.Activity;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.graphics.Color;
        import android.graphics.Typeface;
        import android.net.Uri;
        import android.support.v7.app.AlertDialog;
        import android.util.Log;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.BaseAdapter;
        import android.widget.BaseExpandableListAdapter;
        import android.widget.CheckBox;
        import android.widget.CompoundButton;
        import android.widget.ImageButton;
        import android.widget.TextView;

        import com.firebase.client.DataSnapshot;
        import com.firebase.client.Firebase;
        import com.firebase.client.FirebaseError;
        import com.firebase.client.ValueEventListener;

        import java.util.ArrayList;
        import java.util.Date;
        import java.util.HashMap;
        import java.util.List;

        import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_CHECKED_IN;
        import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_PHONE_COLUMN;
        import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_NAME_COLUMN;
        import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_PRESENT;
        import static nexteria.nxnoc.Constants.EVENT_UUID_COLUMN;
        import static nexteria.nxnoc.Constants.MONITORING_EVENT_ATTENDEE_ID;



/**
 * Created by ddeath on 3/28/16.
 */
public class AttendeesExapndableListViewAdapter extends BaseExpandableListAdapter {

    private Context _context;
    private List<String> group_labels;
    private HashMap<String, List<HashMap<String, String>>> groups_data;

    public class CallOnClickListener implements View.OnClickListener
    {

        String phone_number;
        public CallOnClickListener(String phone_number) {
            this.phone_number = phone_number;
        }

        @Override
        public void onClick(View v)
        {
            Intent call = new Intent(Intent.ACTION_DIAL);
            call.setData(Uri.parse("tel:" + phone_number));
            v.getContext().startActivity(call);
        }

    }

    public class PresentOnClickListener implements CheckBox.OnClickListener
    {

        private String attendee_id;
        private String event_uuid;
        private String checkin_time;

        public PresentOnClickListener(String attendee_id, String event_uuid, String checkin_time) {
            this.attendee_id = attendee_id;
            this.event_uuid = event_uuid;
            this.checkin_time = checkin_time;
        }

        @Override
        public void onClick(final View v) {
            CheckBox checkbox = (CheckBox) v;

            if (checkbox.isChecked())
            {
                final Date current_time = new Date();
                Firebase dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/present_attendees/" + attendee_id);
                dbRef.setValue(String.valueOf(current_time.getTime()), new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        if (firebaseError != null)
                        {
                            new AlertDialog.Builder(v.getContext())
                                    .setTitle("Error")
                                    .setCancelable(false)
                                    .setMessage("Data was not saved.\nTry again or contact responsible person.\n\n Attendee number:" + attendee_id)
                                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();
                        }
                        else
                        {
                            Firebase dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/attendees/" + attendee_id);
                            dbRef.removeValue();

                            dbRef = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/standin_attendees/" + attendee_id);
                            dbRef.removeValue();
                        }
                    }
                });
            }
            else
            {
                Firebase dbRef3 = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/present_attendees/" + attendee_id);
                dbRef3.removeValue();

                Firebase dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees/" + attendee_id + "/events/" + event_uuid);
                dbRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot event) {
                        if (event.exists()) {
                            Firebase dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/attendees/" + attendee_id);
                            dbRef2.setValue(checkin_time, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null)
                                    {
                                        new AlertDialog.Builder(v.getContext())
                                                .setTitle("Error")
                                                .setCancelable(false)
                                                .setMessage("Data was not saved.\nTry again or contact responsible person.\n\n Attendee number:" + attendee_id)
                                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            Firebase dbRef2 = new Firebase("https://nxnoc.firebaseio.com/events/" + event_uuid + "/standin_attendees/" + attendee_id);
                            dbRef2.setValue(checkin_time, new Firebase.CompletionListener() {
                                @Override
                                public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                                    if (firebaseError != null)
                                    {
                                        new AlertDialog.Builder(v.getContext())
                                                .setTitle("Error")
                                                .setCancelable(false)
                                                .setMessage("Data was not saved.\nTry again or contact responsible person.\n\n Attendee number:" + attendee_id)
                                                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                })
                                                .show();
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {

                    }
                });
            }
        }
    }

    public AttendeesExapndableListViewAdapter(Context context, List<String> group_labels,
                                              HashMap<String, List<HashMap<String, String>>> groups_data) {
        this._context = context;
        this.group_labels = group_labels;
        this.groups_data = groups_data;
    }

    @Override
    public int getGroupCount() {
        return group_labels.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.groups_data.get(this.group_labels.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return group_labels.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.groups_data.get(this.group_labels.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition) + " - " + this.groups_data.get(this.group_labels.get(groupPosition)).size();
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.monitorint_event_group, null);
        }

        TextView header = (TextView) convertView
                .findViewById(R.id.monitoring_event_group_label);
        header.setTypeface(null, Typeface.BOLD);
        header.setText(headerTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        HashMap<String, String> map = (HashMap<String, String>) getChild(groupPosition, childPosition);

        TextView txt_attendee_name;
        ImageButton button_call;
        CheckBox attendee_present;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) this._context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.monitoring_event, null);

            txt_attendee_name = (TextView) convertView.findViewById(R.id.monitoring_attendee_name);
            button_call = (ImageButton) convertView.findViewById(R.id.monitoring_attendee_call_button);
            attendee_present = (CheckBox) convertView.findViewById(R.id.attendee_present_checkbox);

            convertView.setTag(R.id.monitoring_attendee_name, txt_attendee_name);
            convertView.setTag(R.id.monitoring_attendee_call_button, button_call);
            convertView.setTag(R.id.attendee_present_checkbox, attendee_present);

        }
        else
        {
            txt_attendee_name = (TextView) convertView.getTag(R.id.monitoring_attendee_name);
            button_call = (ImageButton) convertView.getTag(R.id.monitoring_attendee_call_button);
            attendee_present = (CheckBox) convertView.getTag(R.id.attendee_present_checkbox);
        }

        txt_attendee_name.setText(map.get(MONITORING_EVENT_ATTENDEE_NAME_COLUMN));
        if (map.get(MONITORING_EVENT_ATTENDEE_CHECKED_IN) != "")
        {
            txt_attendee_name.setTextColor(Color.GREEN);
        }
        else
        {
            txt_attendee_name.setTextColor(Color.RED);
        }

        attendee_present.setChecked(Boolean.valueOf(map.get(MONITORING_EVENT_ATTENDEE_PRESENT)));
        button_call.setOnClickListener(new CallOnClickListener(map.get(MONITORING_EVENT_ATTENDEE_PHONE_COLUMN)));
        attendee_present.setOnClickListener(new PresentOnClickListener(map.get(MONITORING_EVENT_ATTENDEE_ID), map.get(EVENT_UUID_COLUMN), map.get(MONITORING_EVENT_ATTENDEE_CHECKED_IN)));

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
