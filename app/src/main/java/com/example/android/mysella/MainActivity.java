package com.example.android.mysella;
import android.app.Activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;

import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity  {
    Button b_btOn, b_btOff, btConnect;
    TextView txtString;

    Handler bluetoothIn;
    final int handlerState = 0;     //used to identify handler message
    private BluetoothAdapter m_BluetoothAdapter = null;

    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private ConnectedThread mConnectedThread;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //private static final UUID BTMODULEUUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*
        b_btOn = (Button) findViewById(R.id.btOn);
        b_btOn.setOnClickListener(btOnListener);

        b_btOff=(Button)findViewById(R.id.btOff);
        b_btOff.setOnClickListener(btOffListener);
        txtString = (TextView) findViewById(R.id.txtString);
        */

        //Load WebView
        //Enable in page web navigation
        //Enable JavaScript
        WebView myWebView = (WebView) findViewById(R.id.webview);
        myWebView.setWebViewClient(new WebViewClient());
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        myWebView.loadUrl("https://mysella-170709.appspot.com/");

        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {

                WebView myWebView2 = (WebView) findViewById(R.id.webview);
                if (msg.arg1==1) {
                    myWebView2.setBackgroundColor(Color.GREEN);
                } else if (msg.arg1==2){
                    myWebView2.setBackgroundColor(Color.RED);
                }
                if (msg.what == handlerState) {										//if message is what we want
                    String readMessage = (String) msg.obj;                                                            // msg.arg1 = bytes from connect thread
                    /*
                    WebView myWebView2 = (WebView) findViewById(R.id.webview);
                    if (readMessage.trim() == "GOOD") {
                        myWebView2.setBackgroundColor(Color.GREEN);
                    } else if (readMessage.trim() == "BAD"){
                        myWebView2.setBackgroundColor(Color.RED);
                    }*/
                    //sendData(readMessage);
                    recDataString.append(readMessage);      								//keep appending to string until ~
                    int endOfLineIndex = recDataString.indexOf("~");                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);    // extract string
                        txtString.setText("Data = " + dataInPrint);
                        int dataLength = dataInPrint.length();							//get length of data received
                        //txtStringLength.setText("String Size = " + String.valueOf(dataLength));

                        /*
                        if (recDataString.charAt(0) == '#')								//if it starts with # we know it is what we are looking for
                        {
                            String sensor0 = recDataString.substring(1, 5);             //get sensor value from string between indices 1-5
                            String sensor1 = recDataString.substring(6, 10);            //same again...
                            String sensor2 = recDataString.substring(11, 15);
                            String sensor3 = recDataString.substring(16, 20);

                            if(sensor0.equals("1.00"))
                                sensorView0.setText("Switched on");	//update the textviews with sensor values
                            else
                                sensorView0.setText("Off");	//update the textviews with sensor values
                            sensorView1.setText(sensor1);
                            sensorView2.setText(sensor2);
                            sensorView3.setText(sensor3);
                            //sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V");
                        }*/
                        recDataString.delete(0, recDataString.length()); 					//clear all string data
                        // strIncom =" ";
                        dataInPrint = " ";
                    }
                }
            }
        };

        //Bluetooth
        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (m_BluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        checkBTState();
    }

    /*
    //Create an anonymous implementation of OnClickListener
    private OnClickListener btOnListener = new OnClickListener() {
        public void onClick(View v) {
            on(v);
            //mConnectedThread.write("2");
        }
    };

    //Create an anonymous implementation of OnClickListener
    private OnClickListener btOffListener = new OnClickListener() {
        public void onClick(View v) {
            off(v);
        }
    };
    */

    private void sendData(String data){
        String text = "";
        BufferedReader reader=null;
        // Send data
        try
        {

            // Defined URL  where to send data
            URL url = new URL("https://mysella-170709.appspot.com/api?posture=");

            // Send POST data request

            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write( data );
            wr.flush();

            // Get the server response

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line = null;

            // Read Server Response
            while((line = reader.readLine()) != null)
            {
                // Append server response in string
                sb.append(line + "\n");
            }


            text = sb.toString();
        }
        catch(Exception ex)
        {

        }
        finally
        {
            try
            {

                reader.close();
            }

            catch(Exception ex) {}
        }

        // Show response on activity
        //content.setText( text  );

    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates insecure outgoing connecetion with BT device using UUID
        //return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();

        /*
        //Get MAC address from DeviceListActivity via intent
        Intent intent = getIntent();

        //Get the MAC address from the DeviceListActivty via EXTRA
        address = intent.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        */
        //create device and set the MAC address
        //Log.i("ramiro", "adress : " + address);
        //BluetoothDevice device = m_BluetoothAdapter.getRemoteDevice("9C:1D:58:A3:BF:C5");
        BluetoothDevice device = m_BluetoothAdapter.getRemoteDevice("98:D3:32:10:C8:79");

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "Create Socket Failed", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        } catch (IOException e) {
            try
            {
                btSocket.close();
            } catch (IOException e2)
            {
                //insert code to deal with this
                 Toast.makeText(getBaseContext(), "Socket Connect Failed", Toast.LENGTH_LONG).show();
            }
        }
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        //mConnectedThread.write("x");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if(m_BluetoothAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (m_BluetoothAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    public void on(View v){
        if (!m_BluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 0);
            Toast.makeText(getApplicationContext(), "Bluetooth turned on",Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getApplicationContext(), "Bluetooth is already on", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        m_BluetoothAdapter.disable();
        Toast.makeText(getApplicationContext(), "Bluetooth turned off" ,Toast.LENGTH_LONG).show();
    }

    public  void visible(View v){
        //Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //startActivityForResult(getVisible, 0);
    }


    public void list(View v){
        /*
        pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt : pairedDevices) list.add(bt.getName());
        Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();

        final ArrayAdapter adapter = new  ArrayAdapter(this,android.R.layout.simple_list_item_1, list);

        lv.setAdapter(adapter);*/
    }


    //create new class for connect thread
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //creation of the connect thread
        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }


        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    bytes = mmInStream.read(buffer);        	//read bytes from input buffer
                    String readMessage = new String(buffer, 0, bytes);
                    System.out.println(readMessage);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                Toast.makeText(getBaseContext(), "Connection Failure", Toast.LENGTH_LONG).show();
                finish();

            }
        }
    }
}

