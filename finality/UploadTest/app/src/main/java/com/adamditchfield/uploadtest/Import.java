package com.adamditchfield.uploadtest;

        import android.content.Context;
        import android.os.AsyncTask;
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
        import java.io.BufferedReader;
        import java.io.DataOutputStream;
        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.MalformedURLException;
        import java.net.URL;
        import java.net.URLConnection;

public class Import extends AppCompatActivity {

    public String chosenId;
    public String[] saberSoundFiles;
    public int numOfFile = 3;
    public RelativeLayout rLayout;
    public RadioGroup radiobuttons;
    public Context c = this;
    public String[] sounds;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);


        rLayout = (RelativeLayout) this.findViewById(R.id.relayout);
        radiobuttons = (RadioGroup) this.findViewById(R.id.radiobuttons);

        //LayoutInflater layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //View view = layoutInflater.inflate(R.layout.activity_soundselector, null);

        /*
        for (int i = 0; i < numOfFile; i++) {
            TextView tv = new TextView(this);

            RadioButton rb = new RadioButton(this);


            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);


            lp.setMargins(0, 8, 0, 8);
            rb.setLayoutParams(lp);
            radiobuttons.addView(rb);

            lp.setMargins(24, 0, 0, 0);
            lp.addRule(RelativeLayout.ALIGN_BASELINE, rb.getId());
            tv.setLayoutParams(lp);
            tv.setText("test");
            rLayout.addView(tv);


        }*/
        GrabAsync g = new GrabAsync("http://192.168.43.6:5000/pls");
        g.execute("http://192.168.43.6:5000/pls");
    }


    public String[] getSaberSounds() {
        return saberSoundFiles;
    }

    public boolean downloadFile(final String path) {

        try {
            URL url = new URL(path);
            String fileName = "/sabsound.3gp";

            URLConnection ucon = url.openConnection();
            ucon.setReadTimeout(5000);
            ucon.setConnectTimeout(1000);

            InputStream is = ucon.getInputStream();
            BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);

            File file = new File(getCacheDir() + fileName);

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

    public class GrabAsync extends AsyncTask<String, Integer, String> {

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String url;


        GrabAsync(String address) {
            this.url = address;
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
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                ans = in.readLine();
                conn.disconnect();


            } catch (Exception e) {
                ans = e.toString();
            }
            return ans;
        }

        protected void onPostExecute(String aString) {
            super.onPostExecute(aString);


            ////////////////////////
            sounds = aString.split(",");
            for (int i = 0; i < sounds.length-1; i++) {
                TextView tv = new TextView(c);

                RadioButton rb = new RadioButton(c);


                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                );


                lp.setMargins(0, 8, 0, 8);
                rb.setLayoutParams(lp);
                tv.setLayoutParams(lp);
                radiobuttons.addView(rb);
                rb.setClickable(true);
                rb.setOnClickListener(clicky);
                lp.setMargins(24, 0, 0, 0);
                lp.addRule(RelativeLayout.ALIGN_BASELINE, rb.getId());
                tv.setLayoutParams(lp);
                tv.setText(sounds[i]);
                rLayout.addView(tv);


            }
        }
        public class Downloadasync extends AsyncTask<String,Integer,Integer> {
            @Override
            protected Integer doInBackground(String... addr) {
                String toDL = addr[0];
                downloadFile("http://192.168.43.6:5000/upload/"+toDL);
                return 1;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
            }
        }

        View.OnClickListener clicky = new View.OnClickListener() {
            public void onClick(View v) {
                String toDL=sounds[radiobuttons.getCheckedRadioButtonId()];
                Downloadasync d = new Downloadasync();
                d.execute(toDL);

            }
        };

    }
}


