package com.inchtechs.opentechnology.otiot;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements SensorEventListener {
    public static final String TURN_ON_BULB = "xyz12345";
    public static final String TURN_OFF_BULB = "xyz12347";
    public static final String TURN_ON_FAN = "xyz12348";
    public static final String TURN_OFF_FAN = "xyz12346";
    public String ipaddport = "192.168.43.52:1998";
    ImageView imageView;
    TextView textView;
    SeekBar sb;
    SensorManager sensorManager;
    Sensor proximity, acc;
    ProximityListener proximityListener;
    //                            {"water":12,"humidity":87,"temperature":23,"light":0,"sound":-79,"flame":0,"ir":1,"flameanalog":234}
//                            taking the two values of flame ... digital and analog
    TextView watertv, humiditytv, temperaturetv, lighttv, soundtv, flametv, irtv;
    ColorStateList defaultcolor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(layoutParams);
        builder.setMessage("Input ip address:port");
        builder.setView(editText);
        builder.setTitle("IP Address");
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ipaddport = editText.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while(true){
                            sendSocketMessage("data",null);
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            }
        });
        builder.setCancelable(false);
        builder.create().show();
        imageView = (ImageView) findViewById(R.id.bulbimage);
        sb = (SeekBar) findViewById(R.id.seekbar);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                sendSocketMessage("llll"+i,null);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ToggleButton toggleButton = (ToggleButton) findViewById(R.id.togglebutton);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(advanced){
                    advanced = false;
                }else{
                    advanced = true;
                }
            }
        });

//        watertv,humiditytv,temperaturetv,lighttv,soundtv,flametv,irtv;
        watertv=(TextView) findViewById(R.id.water);
        humiditytv=(TextView) findViewById(R.id.humidity);
        temperaturetv=(TextView) findViewById(R.id.temperature);
        lighttv=(TextView) findViewById(R.id.light);
        soundtv=(TextView) findViewById(R.id.sound);
        flametv=(TextView) findViewById(R.id.flame);
        irtv=(TextView) findViewById(R.id.ir);
        defaultcolor = flametv.getTextColors();
        sensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        acc =sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proximityListener = new ProximityListener();
/*        sensorManager.registerListener(this,acc,SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(proximityListener,proximity,SensorManager.SENSOR_DELAY_NORMAL);*/
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i("Hello",location.getLatitude()+":::"+location.getLongitude()+"");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });

    }

    boolean advanced = false;
    public void turnOnLed(View v){
        toggleOnOff(v);
    }
    boolean on =false;
    private void toggleOnOff(View v) {
        if(on){
            sendSocketMessage(TURN_OFF_BULB,v);

        }else{
            sendSocketMessage(TURN_ON_BULB,v);

        }
    }
    boolean fanon =false;
    public void toggleFanOnOff(View v) {
        if(fanon){
            sendSocketMessage(TURN_OFF_FAN,v);

        }else{
            sendSocketMessage(TURN_ON_FAN,v);

        }
    }
    boolean firsttimeentry=true;
    private void sendSocketMessage(final String message,final @Nullable  View v) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] st = ipaddport.split(":");
                    Socket socket = new Socket(st[0],Integer.parseInt(st[1]));
                    OutputStream outputStream = socket.getOutputStream();
                    outputStream.write(message.getBytes());
                    if(message.equals("data")){
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String ddd = "";
                        while(bufferedReader.ready()||ddd==""){
                          ddd += bufferedReader.readLine();
                        }
                        final String d  =ddd;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try{

//                            {"water":12,"humidity":87,"temperature":23,"light":0,"sound":-79,"flame":0,"ir":1,"flameanalog":234}
//                            taking the two values of flame ... digital and analog
                                    JSONObject jsonObject =  new JSONObject(d);
                                    watertv.setText(jsonObject.getString("water"));
                                    humiditytv.setText(jsonObject.getString("humidity"));
                                    temperaturetv.setText(jsonObject.getString("temperature")+"°");
                                    soundtv.setText(jsonObject.getString("sound"));
                                    if(jsonObject.getInt("flameanalog") == 1){
                                        flametv.setTextColor(Color.RED);
                                        flametv.setText(jsonObject.getString("flameanalog"));
                                    }else{
                                        flametv.setTextColor(defaultcolor);
                                        flametv.setText(jsonObject.getString("flameanalog"));
                                    }
                                    if(jsonObject.getInt("light") == 1){
                                        lighttv.setTextColor(Color.RED);
                                        lighttv.setText(jsonObject.getString("light"));
                                    }else{
                                        lighttv.setTextColor(defaultcolor);
                                        lighttv.setText(jsonObject.getString("light"));
                                    }
                                    irtv.setText(jsonObject.getString("ir"));
                                    /*if(jsonObject.getInt("ir")==1){
                                        if(firsttimeentry){
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    final TextView textView = new TextView(MainActivity.this);
                                                    textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                                    alertDialog.setTitle("Authentify yourself")
                                                            .setCancelable(false).setView(textView);
                                                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            if(textView.getText().toString().equals("tkc")){
                                                                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
                                                            }else{
                                                                finish();
                                                            }
                                                        }
                                                    });
                                                    firsttimeentry=false;
                                                }
                                            });

                                        }
                                    }*/
                                }catch (NumberFormatException | JSONException e){
                                    e.printStackTrace();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(MainActivity.this, "wrong response data", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        });
                    }
                    outputStream.close();
                    switch (message){
                        case TURN_OFF_BULB:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelativeLayout rl =  (RelativeLayout) v;
                                    rl.setBackgroundResource(R.drawable.bordered_circle);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        imageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                    }
                                    on=false;
                                }
                            });break;

                        case TURN_ON_BULB:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelativeLayout rl =  (RelativeLayout) v;
                                    rl.setBackgroundResource(R.drawable.bordered_circle2);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        imageView.setImageTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                                    }
                                    on=true;
                                }
                            });break;
                        case TURN_OFF_FAN:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelativeLayout rl =  (RelativeLayout) v;
                                    rl.setBackgroundResource(R.drawable.bordered_circle);
                                    fanon=false;
                                }
                            });break;

                        case TURN_ON_FAN:
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    RelativeLayout rl =  (RelativeLayout) v;
                                    rl.setBackgroundResource(R.drawable.bordered_circle2);
                                    fanon=true;
                                }
                            });break;

                    }
                } catch (IOException e) {
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Toast.makeText(MainActivity.this, "Cant reach server", Toast.LENGTH_SHORT).show();
                       }
                   });
                    e.printStackTrace();
                }
                catch (NumberFormatException | ArrayIndexOutOfBoundsException e){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(MainActivity.this, "Format ip:port is wrong", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorManager!=null){
            sensorManager.unregisterListener(this);
            sensorManager.unregisterListener(proximityListener);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if(sensorManager!=null){
            sensorManager.registerListener(this,acc,SensorManager.SENSOR_DELAY_NORMAL);
            sensorManager.registerListener(proximityListener,proximity,SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float[] floats= sensorEvent.values;
//            Toast.makeText(this, floats[0]+""+floats[1]+""+floats[2], Toast.LENGTH_SHORT).show();
            int ax = (int)(Math.atan2(floats[0],floats[1])/(Math.PI/180));
            int ay = (int)(Math.atan2(floats[1],floats[2])/(Math.PI/180));
            int az = (int)(Math.atan2(floats[0],floats[2])/(Math.PI/180));
//            textView.setText("X: "+ax+" Y: "+ay+" Z: "+az);
            if(advanced){
                int val = (10*((ax+360)/360));
                sendSocketMessage("llll" +val,null);
                sb.setProgress(val);textView.setText(val+"");
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
    Camera camera;
    boolean cameraon=false;
    public void cameraAction(boolean action){
//        if(action){
//            if(!cameraon){
//                camera = Camera.open();
//                Camera.Parameters parameters = camera.getParameters();
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
//                camera.setParameters(parameters);
//                camera.startPreview();
//            }
//        }else{
//            if(camera!=null){
//                if(cameraon){
//                    camera.stopPreview();
//                    camera.release();
//                }
//            }
//        }
    }
    private class ProximityListener implements   SensorEventListener{
        boolean bulbon = false;
        boolean candoaction = true;
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if(sensorEvent.sensor.getType() == Sensor.TYPE_PROXIMITY){
                float[] floats= sensorEvent.values;
                if(floats[0]<sensorEvent.sensor.getMaximumRange()){
                    if (bulbon & candoaction){
                        sendSocketMessage(TURN_OFF_BULB,findViewById(R.id.cc));
                        bulbon = false;
                    }else if(!bulbon & candoaction){
                        sendSocketMessage(TURN_ON_BULB,findViewById(R.id.cc));
                        bulbon = true;
                    }
                    candoaction = false;
                }else{
                    candoaction = true;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }

}
