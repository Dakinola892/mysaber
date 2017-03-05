package com.adamditchfield.uploadtest;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.os.Environment;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    //SOUND VARIABLES
    private SoundPool soundPool;
    private AudioManager audioManager;
    //maximum streams
    private static final int MAX_STREAMS = 5;
    //stream type
    private static final int streamType = AudioManager.STREAM_MUSIC;
    private boolean loaded;
    private float volume;
    private int chosenSoundId;

    //SENSOR VARIABLES
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    void SoundSetup() {
        //audioManager to adjust volume
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        //get current volume index of stream type
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);
        //get max volume index of stream type
        float maxVolumeIndex = (float) audioManager.getStreamMaxVolume(streamType);
        //volume from 0 to 1
        this.volume = currentVolumeIndex/maxVolumeIndex;
        //"suggest a stream whose volume should be changed by volume control"
        this.setVolumeControlStream(streamType);

        //different SoundPool setup depending on Android SDK
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setAudioAttributes(aa).setMaxStreams(MAX_STREAMS);

            this.soundPool = builder.build();
        } else {
            this.soundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        }

        //when SoundPool load complete
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                loaded = true;
            }
        });
    }

    void LoadSoundFile(String fileName) {
        //load sound file into SoundPool
        this.chosenSoundId = this.soundPool.load(getCacheDir() + "/" + fileName, 1);

        //String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        //String fileName = "Song.mp3";

        //String fileName = "Environment.getExternalStorageDirectory().getPath()/Music/Song.mp3";
        /*try {
            FileInputStream fis = openFileInput(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

        /*File f = new File(baseDir + File.separator + fileName);
        try {
            FileInputStream fiStream = new FileInputStream(f);
            try {
                FileDescriptor df = fiStream.getFD();

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }

    void SensorSetup() {
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer, senSensorManager.SENSOR_DELAY_NORMAL);
    }

    //override onPause so device unregisters the sensor when app hibernates
    protected void OnPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    //when app resumes register sensor again
    protected void OnResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, senSensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //current values
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            //if 100 milliseconds have passed since last update
            if ((curTime - lastUpdate) > 100) {
                long diffTime = curTime - lastUpdate;
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

                //if speed higher than threshold, play sound
                if (speed > SHAKE_THRESHOLD && loaded) {


                    //play sound
                    float leftVolume = volume;
                    float rightVolume = volume;
                    int streamId = this.soundPool.play(this.chosenSoundId, leftVolume, rightVolume, 1, 0, 1f);
                }

                last_x = x;
                last_y = y;
                last_z = z;
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    public void ButtonHit(View view){
        GrabAsync update=new GrabAsync("http://192.168.43.6:5000");
        update.execute("http://192.168.43.6:5000");

    }


    public void recordpls(View view) {
        Intent intent = new Intent(this, Record.class);
        startActivity(intent);
    }
    public void importpls(View view) {
        Intent intent = new Intent(this, Import.class);
        startActivity(intent);
    }

    public class GrabAsync extends AsyncTask<String,Integer,String>{

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String existingFileName = "lighthouse.jpg";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String url;


        GrabAsync(String address){
            this.url=address;
        }

        @Override
        protected String doInBackground(String... address) {
            URL url = null;
            try {
                url = new URL(address[0]);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            //File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    //"yourfile");
            String ans;

            try {
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                AssetFileDescriptor fileDescriptor = getAssets().openFd("lighthouse.jpg");
                FileInputStream fileInputStream = fileDescriptor.createInputStream();

                //FileInputStream fileInputStream = (FileInputStream) getBaseContext().getAssets().open("lighthouse.jpg");
                conn.setDoOutput(true);
                // Don't use a cached copy.
                conn.setUseCaches(false);
                // Use a post method.
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"file\";filename=\"" + existingFileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                // create a buffer of maximum size
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                // close streams
                Log.e("Debug", "File is written");
                fileInputStream.close();


                BufferedReader in = new BufferedReader(new InputStreamReader( conn.getInputStream()));
                ans = in.readLine();
                conn.disconnect();
                dos.flush();
                dos.close();


            } catch (Exception e) {
                ans = e.toString();
            }
            return ans;
        }

        @Override
        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);
            TextView textView = (TextView)findViewById(R.id.textView2);
            textView.setText(aString);
        }
    }
    public void setupsound(View view){
        SoundSetup();

        LoadSoundFile("sabsound.3gp");

        SensorSetup();
    }


    private void doFileUpload() {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        DataInputStream inStream = null;
        String existingFileName = "src/main/assets/lighthouse.jpg";
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        String responseFromServer = "";
        String urlString = "192.168.43.6:5000";

        try {

            //------------------ CLIENT REQUEST
            FileInputStream fileInputStream = new FileInputStream(new File(existingFileName));
            // open a URL connection to the Servlet
            URL url = new URL(urlString);
            // Open a HTTP connection to the URL
            conn = (HttpURLConnection) url.openConnection();
            // Allow Inputs
            conn.setDoInput(true);
            // Allow Outputs
            conn.setDoOutput(true);
            // Don't use a cached copy.
            conn.setUseCaches(false);
            // Use a post method.
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            dos = new DataOutputStream(conn.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + existingFileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);
            // create a buffer of maximum size
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            // read file and write it into form...
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {

                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            }

            // send multipart form data necesssary after file data...
            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
            // close streams
            Log.e("Debug", "File is written");
            fileInputStream.close();
            dos.flush();
            dos.close();

        } catch (MalformedURLException ex) {
            Log.e("Debug", "error: " + ex.getMessage(), ex);
        } catch (IOException ioe) {
            Log.e("Debug", "error: " + ioe.getMessage(), ioe);
        }

        //------------------ read the SERVER RESPONSE
        try {

            inStream = new DataInputStream(conn.getInputStream());
            String str;

            while ((str = inStream.readLine()) != null) {

                Log.e("Debug", "Server Response " + str);

            }

            inStream.close();

        } catch (IOException ioex) {
            Log.e("Debug", "error: " + ioex.getMessage(), ioex);
        }
    }
}
