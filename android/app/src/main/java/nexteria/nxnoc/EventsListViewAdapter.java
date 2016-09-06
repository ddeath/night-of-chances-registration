package nexteria.nxnoc;

import static nexteria.nxnoc.Constants.EVENT_START_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_END_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_TYPE_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_ROOM_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_NAME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_UUID_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_CAPACITY_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_PRESENT_ATTENDEES_COLUMN;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by ddeath on 3/26/16.
 */
public class EventsListViewAdapter extends BaseAdapter{

    public ArrayList<HashMap<String, String>> list;
    Activity activity;
    TextView txt_event_start_time;
    TextView txt_event_end_time;
    TextView txt_event_type;
    TextView txt_event_room;
    TextView txt_event_name;
    TextView txt_event_present_attendees_count;
    TextView txt_event_capacity;

    public EventsListViewAdapter(Activity activity, ArrayList<HashMap<String, String>> list){
        super();
        this.activity=activity;
        this.list=list;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        HashMap<String, String> map = list.get(position);

        if(convertView == null){
            LayoutInflater inflater=activity.getLayoutInflater();
            convertView = inflater.inflate(R.layout.event, null);

            txt_event_start_time = (TextView) convertView.findViewById(R.id.event_start_time);
            txt_event_end_time = (TextView) convertView.findViewById(R.id.event_end_time);
            txt_event_type = (TextView) convertView.findViewById(R.id.event_type);
            txt_event_room = (TextView) convertView.findViewById(R.id.event_room);
            txt_event_name = (TextView) convertView.findViewById(R.id.event_name);

            convertView.setTag(R.id.event_start_time, txt_event_start_time);
            convertView.setTag(R.id.event_end_time, txt_event_end_time);
            convertView.setTag(R.id.event_type, txt_event_type);
            convertView.setTag(R.id.event_room, txt_event_room);
            convertView.setTag(R.id.event_name, txt_event_name);

            if(map.containsKey(EVENT_UUID_COLUMN))
            {
                txt_event_present_attendees_count = (TextView) convertView.findViewById(R.id.present_attendees_count);
                txt_event_present_attendees_count.setVisibility(View.VISIBLE);

                txt_event_capacity = (TextView) convertView.findViewById(R.id.event_capacity);
                txt_event_capacity.setVisibility(View.VISIBLE);

                TextView separator = (TextView) convertView.findViewById(R.id.capacity_separator);
                separator.setVisibility(View.VISIBLE);

                convertView.setTag(R.id.present_attendees_count, txt_event_present_attendees_count);
                convertView.setTag(R.id.event_capacity, txt_event_capacity);
            }

        }
        else
        {
            txt_event_start_time = (TextView) convertView.getTag(R.id.event_start_time);
            txt_event_end_time = (TextView) convertView.getTag(R.id.event_end_time);
            txt_event_type = (TextView) convertView.getTag(R.id.event_type);
            txt_event_room = (TextView) convertView.getTag(R.id.event_room);
            txt_event_name = (TextView) convertView.getTag(R.id.event_name);

            if(map.containsKey(EVENT_UUID_COLUMN))
            {
                txt_event_present_attendees_count = (TextView) convertView.getTag(R.id.present_attendees_count);
                txt_event_capacity = (TextView) convertView.getTag(R.id.event_capacity);
            }
        }

        txt_event_start_time.setText(map.get(EVENT_START_TIME_COLUMN));
        txt_event_end_time.setText(map.get(EVENT_END_TIME_COLUMN));
        txt_event_type.setText(map.get(EVENT_TYPE_COLUMN));
        txt_event_room.setText(map.get(EVENT_ROOM_COLUMN));
        txt_event_name.setText(map.get(EVENT_NAME_COLUMN));

        if(map.containsKey(EVENT_UUID_COLUMN))
        {
            txt_event_present_attendees_count.setText(map.get(EVENT_PRESENT_ATTENDEES_COLUMN));
            txt_event_capacity.setText(map.get(EVENT_CAPACITY_COLUMN));
        }

        return convertView;
    }

}