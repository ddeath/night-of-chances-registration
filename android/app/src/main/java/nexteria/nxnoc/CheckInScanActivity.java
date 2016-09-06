package nexteria.nxnoc;

import static nexteria.nxnoc.Constants.EVENT_NAME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_ROOM_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_START_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_END_TIME_COLUMN;
import static nexteria.nxnoc.Constants.EVENT_TYPE_COLUMN;


import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


public class CheckInScanActivity extends AppCompatActivity implements View.OnTouchListener {

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;

    private ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;
    private boolean invalid_code = false;

    private ArrayList<HashMap<String,String>> attendee_events = new ArrayList<HashMap<String,String>>();
    private EventsListViewAdapter attendee_events_adapter;

    private Firebase dbRef;

    static {
        System.loadLibrary("iconv");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        ListView listView = (ListView) findViewById(R.id.attendee_events_list);
        attendee_events_adapter = new EventsListViewAdapter(this, attendee_events);
        listView.setAdapter(attendee_events_adapter);

        RelativeLayout camera_container = (RelativeLayout) findViewById(R.id.cameraContainer);
        camera_container.setOnTouchListener(this);

        initControls();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        FrameLayout cameraPreview = (FrameLayout) findViewById(R.id.cameraPreview);
        cameraPreview.requestFocus();

        if (invalid_code)
        {
            TextView scanner_status = (TextView) findViewById(R.id.scanner_status_text);
            scanner_status.setText("Scanning...");

            ImageView scanner_image = (ImageView) findViewById(R.id.scanner_transparent_logo);
            scanner_image.setImageResource(R.drawable.nexteria_transparent_gray);
            restartScanner();
        }

        return true;
    }

    private void initControls()
    {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        // Instance barcode scanner
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        mPreview = new CameraPreview(CheckInScanActivity.this, mCamera, previewCb,
                autoFocusCB);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            releaseCamera();
        }

        return super.onKeyDown(keyCode, event);
    }


    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance()
    {
        Camera c = null;
        try
        {
            c = Camera.open();
        } catch (Exception e)
        {
        }

        return c;
    }

    private void releaseCamera()
    {
        if (mCamera != null)
        {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
                public void onPreviewFrame(byte[] data, Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPreviewSize();

        Image barcode = new Image(size.width, size.height, "Y800");
        barcode.setData(data);

        int result = scanner.scanImage(barcode);

        if (result != 0) {
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            SymbolSet syms = scanner.getResults();
            for (Symbol sym : syms) {
                Log.i("<<<<<<Asset Code>>>>> ",
                        "<<<<Bar Code>>> " + sym.getData());
                String scanResult = sym.getData().trim();

                processScanResult(scanResult);
                barcodeScanned = true;

                break;
            }
        }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };


    private void processScanResult(String message)
    {
        dbRef = new Firebase("https://nxnoc.firebaseio.com/attendees/" + message);
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
                                showAlertDialog("Error", "Data was not saved.\nTry again or write name on paper.\n\n" + attendee_name);
                            }
                        }
                    });

                    showAttendeeInfo(snapshot, checked_in);
                }
                else
                {
                    TextView scanner_status = (TextView) findViewById(R.id.scanner_status_text);
                    scanner_status.setText("Invalid code");

                    ImageView scanner_image = (ImageView) findViewById(R.id.scanner_transparent_logo);
                    scanner_image.setImageResource(R.drawable.nexteria_transparent_red);

                    invalid_code = true;
                }
            }
            @Override public void onCancelled(FirebaseError error)
            {
                showAlertDialog("Error", "The read failed: " + error.getMessage());
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
                restartScanner();
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
                    new AlertDialog.Builder(CheckInScanActivity.this)
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
                    new AlertDialog.Builder(CheckInScanActivity.this)
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
                        restartScanner();
                    }
                })
                .show();
    }

    private void restartScanner()
    {
        if (barcodeScanned) {

            barcodeScanned = false;
            mCamera.setPreviewCallback(previewCb);
            mCamera.startPreview();
            previewing = true;
            mCamera.autoFocus(autoFocusCB);
        }
    }
}
