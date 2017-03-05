package com.example.dakin.quicktest;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.LayoutInflater;

import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    public String chosenId;
    public String[] saberSoundFiles;
    public int numOfFile = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soundselector);


        RelativeLayout rLayout = (RelativeLayout) this.findViewById(R.id.relayout);
        RadioGroup radiobuttons = (RadioGroup) this.findViewById(R.id.radiobuttons);
        //LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View view = layoutInflater.inflate(R.layout.activity_soundselector, null);



        for (int i = 0; i < numOfFile; i++) {
            TextView tv = new TextView(this);

            RadioButton rb = new RadioButton(this);


            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);


            lp.setMargins(0, 8, 0, 8);
            rb.setLayoutParams(lp);
            radiobuttons.addView(rb);

            lp.setMargins(24, 0, 0 ,0);
            lp.addRule(RelativeLayout.ALIGN_BASELINE, rb.getId());
            tv.setLayoutParams(lp);
            tv.setText("test");
            rLayout.addView(tv);







        }
    }


    public String[] getSaberSounds(){
        return saberSoundFiles;
    }
    public boolean downloadFile(final String path) {

        try {
            URL url = new URL(path);
            String fileName = URLUtil.guessFileName(path, null, "video/3gpp");

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(1000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(getFilesDir() + fileName);

            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();

            FileOutputStream outStream = new FileOutputStream(file);
            byte[] buff = new byte[5 * 1024];

            int len;
            while ((len = inStream.read(buff)) != -1) {
                outStream.write(buff, 0, len);
            }

            outStream.flush();
            outStream.close();
            inStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void onRadioButtonClicked(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        String chosenId = getResources().getResourceEntryName(view.getId());

    }



}

