/**
 * This file is part of Sveriges Radio Play for Android
 *
 * Sveriges Radio Play for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as published by
 * the Free Software Foundation.
 *
 * Sveriges Radio Play for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Sveriges Radio Play for Android.  If not, see <http://www.gnu.org/licenses/>.
 */
package dk.nordfalk.netradio;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import org.fpl.media.MediaPlayer;

/**
 *
 * @author j
 */
public class Afspiller_akt extends Activity implements OnClickListener {


  private Button startStopButton;
  private TextView tv;
  private Button sendKnap;
  /*
  private Button spilKnap;
  private Button stopKnap;*/
  Log log = new Log();
  private CheckBox scrollCb;
  //private Spinner kanalSpinner;
  private String[][] kanaler = {
//    {"P1 rtsp LQ", "rtsp://live-rtsp.dr.dk/rtplive/_definst_/Channel3_LQ.stream"},
    {"P3 rtsp LQ", "rtsp://live-rtsp.dr.dk/rtplive/_definst_/Channel5_LQ.stream"},
//    {"Sverige P1 rtsp ", "rtsp://mobil-live.sr.se/mobilradio/kanaler/p1-aac-96"},
    {"Sverige P3 rtsp", "rtsp://mobil-live.sr.se/mobilradio/kanaler/p3-aac-96"},
    {"P3 mp3 ICE LQ", "http://live-icy.gss.dr.dk:8000/Channel5_LQ.mp3"},
    {"P3 httplive", "httplive://live-http.gss.dr.dk/streaming/audio/channel5.m3u8"},
    {"P3 http(live)2", "http://live-http.gss.dr.dk/streaming/audio/channel5.m3u8"},
  };
  private int[] afspilningskvalitet = new int[kanaler.length];
  String[] afspilningskvalitetNavn = { "-", "Godt", "Afbrydelser", "Virker ikke!" };

  private TextView statusTv;
  private TextView virkerTv;
  private Spinner kanalSpinner;
  private boolean spiller;
  private String url;
  private MediaPlayer mp;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Volumen op/ned skal styre lydstyrken af medieafspilleren, uanset som noget spilles lige nu eller ej
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    if (savedInstanceState == null) {
      // skru op til 2/3 styrke hvis volumen er lavere end det
      AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
      int max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
      int nu = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
      if (nu < 2 * max / 3) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 2 * max / 3, AudioManager.FLAG_SHOW_UI);
      }
    }

    tv = new TextView(this);
    tv.setId(100701);
    log.systemOutToTextView(tv);

    TableLayout tl = new TableLayout(this);

    ArrayList<String> elem = new ArrayList<String>();
    for (String[] e : kanaler) {
      elem.add(e[0]);
    }

    kanalSpinner = new Spinner(this);
    kanalSpinner.setId(1008);
    kanalSpinner.setAdapter(new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1, elem.toArray(new String[0])));
    tl.addView(kanalSpinner);
    //række.addView(kanalSpinner);


    LinearLayout række = new LinearLayout(this);
    virkerTv = new TextView(this);
    virkerTv.setText("\n..virker..\n(prøv i 2 min)");
    række.addView(virkerTv);
    ((LinearLayout.LayoutParams) virkerTv.getLayoutParams()).weight = 1;


    tl.addView(række);


    række = new LinearLayout(this);
    scrollCb = new CheckBox(this);
    scrollCb.setText("Scroll");
    scrollCb.setEnabled(Log.scroll_tv_til_bund);
    scrollCb.setOnClickListener(new OnClickListener() {
      public void onClick(View arg0) {
        Log.scroll_tv_til_bund = scrollCb.isChecked();
      }
    });
    scrollCb.setId(10012);
    //række.addView(scrollCb);
    /*
    spilKnap = new Button(this);
    spilKnap.setText("Spil");
    spilKnap.setOnClickListener(this);
    række.addView(spilKnap);

    stopKnap = new Button(this);
    stopKnap.setText("Stop");
    stopKnap.setOnClickListener(this);
    række.addView(stopKnap);
     */


    sendKnap = new Button(this);
    sendKnap.setText("Send\nrapport");
    sendKnap.setOnClickListener(this);
    række.addView(sendKnap);


    statusTv = new TextView(this);
    række.addView(statusTv);
    ((LinearLayout.LayoutParams) statusTv.getLayoutParams()).weight = 1;

    tl.addView(række);

    startStopButton = new Button(this);
    startStopButton.setId(1010);
    tl.addView(startStopButton);
    startStopButton.setText("Play");
    startStopButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        try {
          if (mp==null || !mp.isPlaying()) {
            int kanalNr = kanalSpinner.getSelectedItemPosition();
            String navn = kanaler[kanalNr][0];
            url = kanaler[kanalNr][1];
            Log.d("Afspiller " + navn + " med URL:\n" + url);
            Toast.makeText(Afspiller_akt.this, "Spiller " + navn, Toast.LENGTH_LONG).show();
            Toast.makeText(Afspiller_akt.this, "Lad den køre 2 minutter før du bedømmer den", Toast.LENGTH_LONG).show();

            visStatus(url);
        		mp = MediaPlayer.create(Afspiller_akt.this, Uri.parse(url));
            mp.start();
            startStopButton.setText("Stop");
          } else {
            mp.stop();
            startStopButton.setText("Play");
          }
        } catch (Exception e) {
          Log.e(e);
        }
      }
    });

    ScrollView sv = new ScrollView(this);
    sv.setId(10011);
    sv.addView(tv);
    tl.addView(sv);
    setContentView(tl);
  }



  void åbnSendEpost(String emne, String txt) {
    Intent postIntent=new Intent(android.content.Intent.ACTION_SEND);
    postIntent.setType("plain/text");
    postIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"jacob.nordfalk@gmail.com"});
    postIntent.putExtra(Intent.EXTRA_CC, new String[] {"MIKP@dr.dk", "pappons@gmail.com"});
    postIntent.putExtra(Intent.EXTRA_SUBJECT, emne);
    postIntent.putExtra(Intent.EXTRA_TEXT, txt);
    startActivity(Intent.createChooser(postIntent, "Send mail..."));
  }


  public void onClick(View v) {
  }

  @Override
  protected void onDestroy() {
    Log.d("onDestroy");
    super.onDestroy();
  }

  private void visStatus(String txt) {
    statusTv.setText(txt);
    Log.d(txt);
  }
}