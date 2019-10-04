package com.example.vrinda.myapplication;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.REQUEST_COMPANION_RUN_IN_BACKGROUND;
import static android.Manifest.permission.SEND_SMS;
import static android.os.Build.VERSION_CODES.P;

public class Home extends AppCompatActivity  {

    private FusedLocationProviderClient client;
    private final static int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    private LocationRequest locationRequest;
    private static final String TAG = "Home";
    Button listen, listDevices, button1, button2;
    ListView listView;
    TextView status, msg_box;

    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] btArray;

    SendRecieve sendRecieve;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;

    int REQUEST_ENABLE_BLUETOOTH = 1;

    private static final String APP_NAME = "HandyHelmet";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //askPermission();
        requestPermission();

        //client = new FusedLocationProviderClient(Home.this);
        //locationRequest = new LocationRequest();
        //locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        client = LocationServices.getFusedLocationProviderClient(this );

        Button button1 = findViewById(R.id.getLocation);
        final Button button2 = findViewById(R.id.alert);
        Button button3 = findViewById(R.id.logout);
        listen = findViewById(R.id.listen);
        listView = findViewById(R.id.listView);
        listDevices = findViewById(R.id.listDevices);
        status = findViewById(R.id.status);
        msg_box = findViewById(R.id.msg_box);

        bluetoothAdapter= BluetoothAdapter.getDefaultAdapter();

        if(!bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }

        implementListeners();

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(Home.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(Home.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
                client.getLastLocation().addOnSuccessListener(Home.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location!=null)
                        {
                            TextView textView = findViewById(R.id.location);
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            double altitude = location.getAltitude();
                            double accuracy = location.getAccuracy();
                            String lat = String.valueOf(latitude);
                            String lon = String.valueOf(longitude);
                            String alt = String.valueOf(altitude);
                            String acc = String.valueOf(accuracy);
                            String loc = "Latitude: " + lat + " Longitude: " +  lon + " Altitude: " + alt + " Error " + acc;
                            String latkey = "Latitude";
                            String lonkey = "Longitude";

                            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                            if(firebaseUser != null)
                            {
                                DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
                                DatabaseReference Ref = myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location");
                                Ref.setValue(loc);

                            }

                            textView.setText(loc);
                            //textView.setText(location.toString());


                        }
                    }
                });
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ActivityCompat.checkSelfPermission(Home.this, SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }

                final TextView textView = findViewById(R.id.userInfo);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if(firebaseUser != null)
                {
                    final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            String emgphone = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("emgphone").getValue(String.class);
                            String altemphone = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("altemgphone").getValue(String.class);
                            String loc = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location").getValue(String.class);
                            String name = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("username").getValue(String.class);
                            String info = "Name " + name + " Emg Phone: " + emgphone + " Alternate emg phone: " + altemphone + " Location: " + loc;
                            textView.setText(info);
                            SmsManager smsManager = SmsManager.getDefault();
                            smsManager.sendTextMessage(emgphone, null, name + " has faced an accident. The following is the location " +loc,
                                    null, null);
                            smsManager.sendTextMessage(altemphone, null, name + " has faced an accident. The following is the location " +loc,
                                    null, null);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            String error = "Not possible";
                            textView.setText(error);

                        }
                    });
                }
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(Home.this, MainActivity.class));
            }
        });
    }

    private void implementListeners()
    {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                if(bt.size()>0)
                {
                    for(BluetoothDevice device : bt)
                    {
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, strings);
                    listView.setAdapter(arrayAdapter);
                }
            }
        });

        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                ClientClass clientClass = new ClientClass(btArray[i]);
                clientClass.start();

                status.setText("Connecting");
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what)
            {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    msg_box.setText(tempMsg);
                    String str = "1,1,1";
                    if(tempMsg.contains(str))
                    {
                        if(ActivityCompat.checkSelfPermission(Home.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(Home.this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                        {
                            break;
                        }
                        client.getLastLocation().addOnSuccessListener(Home.this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if(location!=null)
                                {
                                    TextView textView = findViewById(R.id.location);
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    double altitude = location.getAltitude();
                                    double accuracy = location.getAccuracy();
                                    String lat = String.valueOf(latitude);
                                    String lon = String.valueOf(longitude);
                                    String alt = String.valueOf(altitude);
                                    String acc = String.valueOf(accuracy);
                                    String loc = "Latitude: " + lat + " Longitude: " +  lon + " Altitude: " + alt + " Error " + acc;
                                    String latkey = "Latitude";
                                    String lonkey = "Longitude";

                                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                    if(firebaseUser != null)
                                    {
                                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
                                        DatabaseReference Ref = myRef.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location");
                                        Ref.setValue(loc);

                                    }

                                    textView.setText(loc);
                                    //textView.setText(location.toString());


                                }
                            }
                        });
                        if(ActivityCompat.checkSelfPermission(Home.this, SEND_SMS) != PackageManager.PERMISSION_GRANTED)
                        {
                           break;
                        }

                        final TextView textview = findViewById(R.id.userInfo);
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if(firebaseUser != null)
                        {
                            final DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users");
                            myRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String emgphone = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("emgphone").getValue(String.class);
                                    String altemphone = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("altemgphone").getValue(String.class);
                                    String loc = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("location").getValue(String.class);
                                    String name = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("username").getValue(String.class);
                                    String info = "Name " + name + " Emg Phone: " + emgphone + " Alternate emg phone: " + altemphone + " Location: " + loc;
                                    textview.setText(info);
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(emgphone, null, name + " has faced an accident. The following is the location " +loc, null, null);
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    String error = "Not possible";
                                    textview.setText(error);

                                }
                            });
                        }
                    }
                    break;
            }
            return true;

        }
    });

    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass()
        {
            try
            {
                serverSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, MY_UUID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket = null;
            while(socket==null)
            {
                try
                {
                    Message message= Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                    Message message= Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }
                if(socket != null)
                {
                    Message message= Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendRecieve = new SendRecieve(socket);
                    sendRecieve.start();

                    break;
                }

            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1)
        {
            device = device1;
            try
            {
                socket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try
            {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendRecieve = new SendRecieve(socket);
                sendRecieve.start();

            }
            catch (IOException e)
            {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendRecieve extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendRecieve (BluetoothSocket socket)
        {
            bluetoothSocket=socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try
            {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true)
            {
                try
                {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        public void write()
        {

        }
    }
    //private void askPermission ()
    //{
        //ActivityCompat.requestPermissions(Home.this, new String[] {Manifest.permission.SEND_SMS}, SEND_SMS_PERMISSION_REQUEST_CODE);
    //}

    private void requestPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, 1);
    }


}

