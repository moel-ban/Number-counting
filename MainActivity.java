package com.motkine.numbercounter;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Typeface;
import android.graphics.Color;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final long LIMIT = 8L * 60L * 60L * 1000L;
    private SharedPreferences prefs;
    private long total = 0, startedAt = 0, endedAt = 0;
    private int entries = 0;
    private boolean active = false;
    private TextView totalView, statusView, timerView, entriesView, historyView;
    private EditText numberInput;
    private Button startButton, addButton, stopButton, resetButton;
    private final Handler handler = new Handler();
    private final Runnable ticker = new Runnable() {
        @Override public void run() { checkExpiry(); refresh(); handler.postDelayed(this, 1000); }
    };

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("counter", MODE_PRIVATE);
        load();
        buildUi();
        checkExpiry(); refresh();
    }

    private void load() {
        total = prefs.getLong("total", 0); startedAt = prefs.getLong("started", 0);
        endedAt = prefs.getLong("ended", 0); entries = prefs.getInt("entries", 0);
        active = prefs.getBoolean("active", false);
    }
    private void save() {
        prefs.edit().putLong("total", total).putLong("started", startedAt)
            .putLong("ended", endedAt).putInt("entries", entries).putBoolean("active", active).apply();
    }
    private void checkExpiry() {
        if (active && System.currentTimeMillis() >= startedAt + LIMIT) {
            active = false; endedAt = startedAt + LIMIT; save();
            if (statusView != null) Toast.makeText(this, "8 hours completed. Session finished.", Toast.LENGTH_LONG).show();
        }
    }
    private String time(long ms) { return new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date(ms)); }
    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(24, 34, 24, 24); root.setBackgroundColor(Color.rgb(247,248,250));
        scroll.addView(root);
        TextView title = new TextView(this); title.setText("NUMBER COUNTER"); title.setTextSize(23); title.setTypeface(null, Typeface.BOLD); title.setTextColor(Color.rgb(24,32,48)); title.setGravity(Gravity.CENTER); root.addView(title);
        TextView sub = new TextView(this); sub.setText("8-hour counting session"); sub.setTextSize(14); sub.setTextColor(Color.GRAY); sub.setGravity(Gravity.CENTER); addSpace(root, 6); root.addView(sub);
        addSpace(root, 28);
        totalView = new TextView(this); totalView.setTextSize(48); totalView.setTypeface(null, Typeface.BOLD); totalView.setTextColor(Color.rgb(20,130,85)); totalView.setGravity(Gravity.CENTER); root.addView(totalView);
        TextView totalLabel = new TextView(this); totalLabel.setText("TOTAL"); totalLabel.setTextSize(12); totalLabel.setTextColor(Color.GRAY); totalLabel.setGravity(Gravity.CENTER); root.addView(totalLabel);
        addSpace(root, 20);
        statusView = new TextView(this); statusView.setTextSize(15); statusView.setGravity(Gravity.CENTER); root.addView(statusView);
        timerView = new TextView(this); timerView.setTextSize(16); timerView.setTypeface(null, Typeface.BOLD); timerView.setGravity(Gravity.CENTER); timerView.setTextColor(Color.DKGRAY); addSpace(root, 8); root.addView(timerView);
        entriesView = new TextView(this); entriesView.setTextSize(13); entriesView.setTextColor(Color.GRAY); entriesView.setGravity(Gravity.CENTER); root.addView(entriesView);
        addSpace(root, 24);
        startButton = button("START SESSION", 0xFF16865B); root.addView(startButton);
        startButton.setOnClickListener(v -> { if (!active) { total=0; entries=0; startedAt=System.currentTimeMillis(); endedAt=0; active=true; save(); refresh(); } });
        addSpace(root, 16);
        numberInput = new EditText(this); numberInput.setSingleLine(true); numberInput.setTextSize(18); numberInput.setHint("Enter a number, e.g. 9"); numberInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED); root.addView(numberInput);
        addSpace(root, 8);
        addButton = button("ADD NUMBER", 0xFF263B59); root.addView(addButton);
        addButton.setOnClickListener(v -> addNumber());
        addSpace(root, 12);
        stopButton = button("STOP & SHOW TOTAL", 0xFFC44848); root.addView(stopButton);
        stopButton.setOnClickListener(v -> { if (active) { active=false; endedAt=System.currentTimeMillis(); save(); refresh(); new AlertDialogHelper(this).show("Session finished", "Final total: " + total + "\nEntries: " + entries + "\nDuration: " + duration()); } });
        addSpace(root, 8);
        resetButton = button("RESET / NEW SESSION", 0xFF777777); root.addView(resetButton);
        resetButton.setOnClickListener(v -> { active=false; total=0; entries=0; startedAt=0; endedAt=0; save(); refresh(); });
        addSpace(root, 22);
        historyView = new TextView(this); historyView.setTextSize(12); historyView.setTextColor(Color.GRAY); root.addView(historyView);
        setContentView(scroll);
    }
    private void addNumber() {
        if (!active) { Toast.makeText(this, "Press START first", Toast.LENGTH_SHORT).show(); return; }
        checkExpiry(); if (!active) { refresh(); return; }
        String s = numberInput.getText().toString().trim();
        if (s.isEmpty()) { numberInput.setError("Enter a number"); return; }
        try { long value = Long.parseLong(s); total += value; entries++; numberInput.setText(""); save(); refresh(); }
        catch (Exception e) { numberInput.setError("Invalid number"); }
    }
    private String duration() { if (startedAt == 0) return "00:00:00"; long end = endedAt > 0 ? endedAt : System.currentTimeMillis(); long sec=Math.max(0,(end-startedAt)/1000); return String.format(Locale.getDefault(), "%02d:%02d:%02d", sec/3600,(sec%3600)/60,sec%60); }
    private void refresh() {
        if (totalView == null) return;
        totalView.setText(String.valueOf(total)); statusView.setText(active ? "● SESSION ACTIVE" : (endedAt > 0 ? "SESSION FINISHED" : "NOT STARTED"));
        statusView.setTextColor(active ? Color.rgb(20,130,85) : Color.GRAY);
        timerView.setText(active ? "Elapsed: " + duration() + " / 08:00:00" : "Duration: " + duration());
        entriesView.setText("Numbers added: " + entries);
        startButton.setEnabled(!active); addButton.setEnabled(active); stopButton.setEnabled(active);
        historyView.setText(endedAt > 0 ? "Last session: " + time(startedAt) + " – " + time(endedAt) + "\nFinal total: " + total : (startedAt > 0 ? "Started: " + time(startedAt) : ""));
    }
    private Button button(String label, int color) { Button b=new Button(this); b.setText(label); b.setTextColor(Color.WHITE); b.setTextSize(14); b.setAllCaps(false); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(color)); return b; }
    private void addSpace(LinearLayout l, int h) { View v=new View(this); l.addView(v,new LinearLayout.LayoutParams(1,h)); }
    @Override protected void onResume() { super.onResume(); handler.post(ticker); }
    @Override protected void onPause() { super.onPause(); handler.removeCallbacks(ticker); }

    private static class AlertDialogHelper {
        private final Activity a; AlertDialogHelper(Activity a) { this.a=a; }
        void show(String title, String message) { new android.app.AlertDialog.Builder(a).setTitle(title).setMessage(message).setPositiveButton("OK", null).show(); }
    }
}
