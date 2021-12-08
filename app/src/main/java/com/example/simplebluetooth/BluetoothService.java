package com.example.simplebluetooth;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;
import java.util.UUID;
import static android.content.ContentValues.TAG;

public class BluetoothService extends Service {

    private static final long START_TIME_IN_MILLIS = 600000;
    private static final long START_TIME_IN_MILLIS2 = 10000;
    private String timeStringFormatted;


    private CountDownTimer cdTimer;
    private CountDownTimer cdToTimer;

    protected boolean timerRunnnig;
    protected boolean timerToRunnnig;

    protected boolean homeToActive;
    protected boolean guestToActive;

    private long timeLeftMillis = START_TIME_IN_MILLIS;
    private long timeLeftMillis2 = START_TIME_IN_MILLIS2;

    final int handlerState = 0;                         //used to identify handler message
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothSocket btSocket;

    private StringBuilder recDataString = new StringBuilder();
    private static String pts;
    public Long value;
    public Long missedValue;




    public int points = 0;
    public int misses = 0;


    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final String CHANNEL_ID = "bluetoothServiceChannel";

    private ConnectedThread mConnectedThread;

    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }


    public void sendData() {
        try {
            BluetoothDevice device = btAdapter.getRemoteDevice(MainActivity.address);
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_LONG).show();
        }
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                Log.d("catch","Nu am reusit sa inchid socketul");
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("proxSensor/Scored");
        DatabaseReference myMissedRef = database.getReference("proxSensor/Miss");
        myRef.setValue(0);
        myMissedRef.setValue(0);

        myMissedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                missedValue = dataSnapshot.getValue(Long.class);
                Log.d(TAG, "Missed Value is: " + missedValue);
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if(timerRunnnig) {
                    value = dataSnapshot.getValue(Long.class);
                    MainActivity.scoreGuestTeam.setText(value.toString());
                    Log.d(TAG, "Value is: " + value);
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }




    Handler bluetoothIn = new Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")

        public void handleMessage(android.os.Message msg) {
            if (msg.what == handlerState && timerRunnnig ==true){
                String readMessage = (String) msg.obj;
                recDataString.append(readMessage);
                int endOfLineIndex = recDataString.indexOf("~");
                if (endOfLineIndex > 0) {
                    String dataInPrint = recDataString.substring(0, endOfLineIndex);
                    if (recDataString.charAt(0) == '#')
                    {
                        char ch1 = '2';
                        char ch2 = '1';
                        char sensor0 = recDataString.charAt(1);
                        if (sensor0 == ch1) {
                            points++;
                            pts = String.valueOf(points);
                            Log.d("scor", pts);
                        } else if (sensor0 == ch2) {
                            misses++;
                        } else {
                            MainActivity.scoreHomeTeam.setText("pl");
                        }
                    }
                    recDataString.delete(0, recDataString.length());
                }
            }
            MainActivity.scoreHomeTeam.setText(pts);
        }
    };


    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;


        // comentat/ era ca parametru pt ConnectThread
        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            while (true) {
                try {

                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

    }
        protected void startTimer() {
            cdTimer = new CountDownTimer(timeLeftMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timeLeftMillis = millisUntilFinished;
                    updateCdText();

                }

                @Override
                public void onFinish() {
                    timerRunnnig = false;
                    MainActivity.startPauseBtn.setText("START");
                    MainActivity.startPauseBtn.setVisibility(View.INVISIBLE);
                    MainActivity.resetGame.setVisibility(View.VISIBLE);
                }

            }.start();
            timerRunnnig = true;
            MainActivity.startPauseBtn.setText("PAUSE");
            MainActivity.resetGame.setVisibility(View.INVISIBLE);

        }

        protected void pauseTimer() {
            cdTimer.cancel();
            timerRunnnig = false;
                MainActivity.startPauseBtn.setText("START");
                MainActivity.resetGame.setVisibility(View.VISIBLE);


        }

        protected void resetTimer() {
            timeLeftMillis = START_TIME_IN_MILLIS;
            updateCdText();
            MainActivity.resetGame.setVisibility(View.INVISIBLE);
            MainActivity.startPauseBtn.setVisibility(View.VISIBLE);
        }

        protected void updateCdText() {
            int minutes = (int) (timeLeftMillis / 1000) / 60;
            int seconds = (int) (timeLeftMillis / 1000) % 60;

            String timeStringFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
            MainActivity.clockString.setText(timeStringFormatted);

        }

    protected void startToTimer() {
        cdToTimer = new CountDownTimer(timeLeftMillis2, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftMillis2 = millisUntilFinished;
                updateCdToText();

            }

            @Override
            public void onFinish() {
                timerToRunnnig = false;
                if(homeToActive == true){
                    homeToActive = false;
                }
                else{
                    guestToActive = false;
                }
                int minutes = (int) (START_TIME_IN_MILLIS2 / 1000) / 60;
                int seconds = (int) (START_TIME_IN_MILLIS2 / 1000) % 60;

                String timeStringFormatted2 = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
                MainActivity.clockTO.setText(timeStringFormatted2);
                timeLeftMillis2 = START_TIME_IN_MILLIS2;

                startTimer();

            }

        }.start();
        timerToRunnnig = true;
    }
    protected void updateCdToText() {
        int minutes = (int) (timeLeftMillis2 / 1000) / 60;
        int seconds = (int) (timeLeftMillis2 / 1000) % 60;

        String timeStringFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        MainActivity.clockTO.setText(timeStringFormatted);

    }



}





