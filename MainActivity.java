package com.example.episodetrial;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainActivity extends Activity {
    // Publicly available sample media for demonstration. No DashReels content is included.
    private static final String[] TITLES = {"Big Buck Bunny", "Elephants Dream", "For Bigger Joyrides"};
    private static final String[] URLS = {
        "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"
    };
    private final Map<Integer, Long> downloads = new LinkedHashMap<>();
    private final Map<Integer, TextView> statuses = new LinkedHashMap<>();
    private DownloadManager manager;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView summary;
    private final int background = Color.rgb(15,16,23);
    private final int panel = Color.rgb(30,32,43);
    private final int accent = Color.rgb(235,67,116);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        for (int i = 0; i < TITLES.length; i++) {
            long saved = getPreferences(MODE_PRIVATE).getLong("download_" + i, -1);
            if (saved != -1) downloads.put(i, saved);
        }
        buildUi();
        handler.postDelayed(this::refresh, 1000);
    }

    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }
    private TextView text(String value, int size, int color) {
        TextView v = new TextView(this); v.setText(value); v.setTextSize(size); v.setTextColor(color); return v;
    }
    private void buildUi() {
        ScrollView scroll = new ScrollView(this); scroll.setBackgroundColor(background);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20),dp(35),dp(20),dp(25));
        scroll.addView(root); setContentView(scroll);
        TextView header = text("EPISODE DOWNLOAD TRIAL", 13, accent); header.setTypeface(null, Typeface.BOLD); root.addView(header);
        TextView title = text("Download episodes", 29, Color.WHITE); title.setTypeface(null,Typeface.BOLD); title.setPadding(0,dp(14),0,dp(10)); root.addView(title);
        TextView detail = text("Sample videos only • No DashReels account needed", 13, 0xffaaaaaf); root.addView(detail);
        Button all = button("↓  DOWNLOAD ALL  (3)");
        LinearLayout.LayoutParams allLp = new LinearLayout.LayoutParams(-1,dp(52)); allLp.topMargin=dp(25); root.addView(all,allLp);
        all.setOnClickListener(v -> { for(int i=0;i<TITLES.length;i++) enqueue(i); refresh(); });
        summary = text("0 of 3 completed", 14, Color.WHITE); summary.setPadding(0,dp(20),0,dp(12)); root.addView(summary);
        for(int i=0;i<TITLES.length;i++) {
            final int index=i;
            LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(14),dp(15),dp(14),dp(14)); card.setBackgroundColor(panel);
            LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(-1,-2); cp.bottomMargin=dp(12); root.addView(card,cp);
            TextView name=text("Episode "+(i+1)+"  ·  "+TITLES[i],17,Color.WHITE); name.setTypeface(null,Typeface.BOLD); card.addView(name);
            TextView status=text("Not downloaded",13,0xffb7b7c2); status.setPadding(0,dp(9),0,dp(10)); card.addView(status); statuses.put(i,status);
            LinearLayout controls=new LinearLayout(this); controls.setOrientation(LinearLayout.HORIZONTAL); card.addView(controls);
            Button download=button("Download"); controls.addView(download,new LinearLayout.LayoutParams(0,dp(42),1));
            download.setOnClickListener(v -> enqueue(index));
            Button open=button("Play offline"); LinearLayout.LayoutParams op=new LinearLayout.LayoutParams(0,dp(42),1); op.leftMargin=dp(10); controls.addView(open,op);
            open.setOnClickListener(v -> openEpisode(index));
        }
        TextView footer=text("Downloads use Android Download Manager and are stored in your device's Downloads folder. This prototype does not download protected or subscription video streams.",12,0xffa1a1a9);
        footer.setPadding(0,dp(10),0,0); root.addView(footer);
        refresh();
    }
    private Button button(String label) {
        Button b=new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(accent)); return b;
    }
    private void enqueue(int index) {
        Long id=downloads.get(index);
        if(id!=null && getStatus(id)!=DownloadManager.STATUS_FAILED) {
            Toast.makeText(this,"Episode "+(index+1)+" already queued or downloaded",Toast.LENGTH_SHORT).show(); return;
        }
        try {
            DownloadManager.Request req=new DownloadManager.Request(Uri.parse(URLS[index]));
            req.setTitle("Episode "+(index+1)+" - "+TITLES[index]);
            req.setDescription("Episode Download Trial sample video");
            req.setMimeType("video/mp4");
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS,"EpisodeTrial/episode_"+(index+1)+".mp4");
            long downloadId=manager.enqueue(req); downloads.put(index,downloadId);
            getPreferences(MODE_PRIVATE).edit().putLong("download_"+index,downloadId).apply();
            refresh();
        } catch(Exception e) { Toast.makeText(this,"Could not start download: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
    }
    private int getStatus(long id) {
        try(Cursor c=manager.query(new DownloadManager.Query().setFilterById(id))) {
            if(c!=null && c.moveToFirst()) return c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
        } catch(Exception ignored) { }
        return DownloadManager.STATUS_FAILED;
    }
    private void refresh() {
        int complete=0;
        for(int i=0;i<TITLES.length;i++) {
            TextView label=statuses.get(i); if(label==null) continue;
            Long id=downloads.get(i);
            if(id==null) { label.setText("Not downloaded"); continue; }
            try(Cursor c=manager.query(new DownloadManager.Query().setFilterById(id))) {
                if(c==null || !c.moveToFirst()) { label.setText("Download missing — tap Download to retry"); continue; }
                int status=c.getInt(c.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS));
                long total=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                long received=c.getLong(c.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                if(status==DownloadManager.STATUS_SUCCESSFUL) { complete++; label.setText("✓ Downloaded — ready for offline playback"); }
                else if(status==DownloadManager.STATUS_FAILED) label.setText("Download failed — tap Download to retry");
                else if(status==DownloadManager.STATUS_PAUSED) label.setText("Paused by Android Download Manager");
                else if(status==DownloadManager.STATUS_PENDING) label.setText("Waiting in download queue");
                else label.setText(total>0 ? "Downloading " + (received*100/total) + "%" : "Downloading…");
            } catch(Exception e) { label.setText("Unable to read download status"); }
        }
        if(summary!=null) summary.setText(complete+" of "+TITLES.length+" completed");
        handler.removeCallbacksAndMessages(null); handler.postDelayed(this::refresh,1500);
    }
    private void openEpisode(int index) {
        Long id=downloads.get(index);
        if(id==null || getStatus(id)!=DownloadManager.STATUS_SUCCESSFUL) { Toast.makeText(this,"Download this episode first",Toast.LENGTH_SHORT).show(); return; }
        Uri uri=manager.getUriForDownloadedFile(id);
        if(uri==null) { Toast.makeText(this,"Downloaded file not available",Toast.LENGTH_SHORT).show(); return; }
        try {
            Intent intent=new Intent(Intent.ACTION_VIEW); intent.setDataAndType(uri,"video/mp4"); intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); startActivity(intent);
        } catch(Exception e) { Toast.makeText(this,"No compatible video player installed",Toast.LENGTH_LONG).show(); }
    }
    @Override protected void onDestroy() { handler.removeCallbacksAndMessages(null); super.onDestroy(); }
}
