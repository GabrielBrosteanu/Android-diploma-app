package com.example.simplebluetooth;
import android.app.Activity;
import java.util.UUID;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends Activity{

    Button incrHome, decrHome, incrGuest, decrGuest, stopBtn, homeToBtn,guestToBtn;
    static TextView homeTeamName, guestTeamName, scoreHomeTeam, scoreGuestTeam;

    static public Button startPauseBtn;
    static public Button  resetGame;


    static public TextView clockString;
    static public TextView clockTO;

    static public boolean pauseEnabled;

    protected static String address;
    private int newScore;
    public int hTeamShotsScored;
    public int gTeamShotsScored;
    public long gMisses;
    public int hMisses;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    BluetoothService mService;
    protected static boolean mBound = false;


    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, BluetoothService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        incrHome = (Button) findViewById(R.id.incrHomeScore);
        decrHome = (Button) findViewById(R.id.decrHomeScore);
        incrGuest = (Button) findViewById(R.id.incrGuestScore);
        decrGuest = (Button) findViewById(R.id.decrGuestScore);
        stopBtn = (Button) findViewById(R.id.stopButton);
        startPauseBtn = (Button) findViewById(R.id.startPauseButton);
        homeToBtn = findViewById(R.id.homeToButton);
        guestToBtn = findViewById(R.id.guestToButton);
        resetGame = findViewById(R.id.resetStats);

        clockString = (TextView) findViewById(R.id.clock);
        clockTO = (TextView) findViewById(R.id.toClock);
        homeTeamName = (TextView) findViewById(R.id.homeTeam);
        guestTeamName = (TextView) findViewById(R.id.guestTeam);
        scoreHomeTeam = (TextView) findViewById(R.id.homeTeamScore);
        scoreGuestTeam = (TextView) findViewById(R.id.guestTeamScore);

        Intent intent = getIntent();
        Intent intentChart = new Intent(this,ChartActivity.class);
        address = intent.getStringExtra(DeviceList.EXTRA_DEVICE_ADDRESS);


        homeToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound && mService.btSocket.isConnected() && mService.guestToActive == false && mService.homeToActive == false){
                    mService.startToTimer();
                    mService.pauseTimer();
                    mService.homeToActive = true;
                    Toast.makeText(getApplicationContext(), "Home team TO started!",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "The game has not started yet",Toast.LENGTH_SHORT).show();
                }
            }
        });

        guestToBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBound && mService.btSocket.isConnected() && mService.homeToActive == false &&  mService.guestToActive == false){
                    mService.startToTimer();
                    mService.pauseTimer();
                    mService.guestToActive = true;
                    Toast.makeText(getApplicationContext(), "Home team TO started!",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "The game has not started yet",Toast.LENGTH_SHORT).show();
                }
            }
        });

        startPauseBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBound) {
                    mService.sendData();

                    if(mService.timerRunnnig && mService.homeToActive == false &&  mService.guestToActive == false &&  mService.timerToRunnnig == false){
                        mService.pauseTimer();
                        pauseEnabled = true;
                        Toast.makeText(getApplicationContext(), "Bluetooth service stopped!",Toast.LENGTH_SHORT).show();
                    }
                    else if (mBound && mService.btSocket.isConnected() && mService.homeToActive == false &&  mService.guestToActive == false &&  mService.timerToRunnnig == false){
                        mService.startTimer();
                        Toast.makeText(getApplicationContext(), "Bluetooth service started!",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Check the bluetooth's module integrity",Toast.LENGTH_SHORT).show();
                    }
                }

                }

        });

        incrGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("proxSensor/Scored");

                myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            mService.value = (Long) task.getResult().getValue();
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                            if (task.getResult().getValue() !=null && mService.timerRunnnig && mService.value >= 0) {

                                mService.value++;
                                myRef.setValue(mService.value);
                                MainActivity.scoreGuestTeam.setText(String.valueOf(mService.value));
                            }
                        }
                    }
                });
            }
        });

        decrGuest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("proxSensor/Scored");

                myRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.e("firebase", "Error getting data", task.getException());
                        }
                        else {
                            mService.value = (Long) task.getResult().getValue();
                            Log.d("firebase", String.valueOf(task.getResult().getValue()));
                            if (task.getResult().getValue() !=null && mService.timerRunnnig && mService.value > 0 ) {

                                mService.value--;
                                myRef.setValue(mService.value);
                                MainActivity.scoreGuestTeam.setText(String.valueOf(mService.value));
                            }
                        }
                    }
                });

            }
        });

        incrHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound && mService.timerRunnnig){
                    mService.points++;
                    newScore = mService.points;
                    scoreHomeTeam.setText(String.valueOf(newScore));
                }

            }
        });

        decrHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mBound && mService.timerRunnnig && mService.points>0){
                    mService.points--;
                    mService.misses++;
                    newScore = mService.points;
                    scoreHomeTeam.setText(String.valueOf(newScore));
                }
            }
        });

        resetGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.resetTimer();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService();
                hTeamShotsScored = Integer.parseInt(MainActivity.scoreHomeTeam.getText().toString());
                gTeamShotsScored = Integer.parseInt(MainActivity.scoreGuestTeam.getText().toString());
                hMisses = mService.misses;
                gMisses = mService.missedValue;
                intentChart.putExtra("homeTeamShotsScored",hTeamShotsScored);
                intentChart.putExtra("guestTeamShotsScored",gTeamShotsScored);
                intentChart.putExtra("homeTeamMisses",hMisses);
                intentChart.putExtra("guestTeamMisses",gMisses);
                startActivity(intentChart);

            }
        });

    }


    public void stopService(){
        if(mBound ==true){
        unbindService(connection);
        if(mService.timerRunnnig){
            mService.pauseTimer();
        }
        mBound = false;
        }
        else{
            Toast.makeText(getApplicationContext(), "Bluetooth service not active!",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause()

    {
        super.onPause();
    }


    protected void onStop() {
        super.onStop();
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };


}
