package com.gorm.notes;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private boolean delMode = false;
    private ArrayList<String> delId;
    private ArrayList<Boolean> delFlag;
    private FloatingActionButton fab;
    private LinearLayout mainLay;
    private Vibrator vibr;
    private DBHelper dbHelper;
    private Intent noteIntent;
    private SharedPreferences pref;
    private SharedPreferences.Editor mEd;
    private MenuItem itemA, itemU;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noteIntent = new Intent(MainActivity.this, NoteActivity.class);
        pref = getPreferences(MODE_PRIVATE);
        mEd = pref.edit();

        dbHelper = new DBHelper(this);
        vibr = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        mainLay = (LinearLayout) findViewById(R.id.lin);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (delMode) {
                    showDelDialog();
                } else
                    showInputDialog();
            }
        });
        if (pref.getBoolean("FAB", false)) {
            fab.hide();
        } else {
            fab.show();
        }
        delId = new ArrayList<>();
        delFlag = new ArrayList<>();
        createTable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        itemA = menu.findItem(R.id.action_authors);
        itemA.setVisible(pref.getBoolean("AUTHORS", false));
        itemU = menu.findItem(R.id.action_upload);
        itemU.setVisible(!pref.getBoolean("OFF", false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_authors:
                message("Dima Black & GMO - 2016");
                return true;
            case R.id.action_upload:
                ConnectivityManager cu = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if (cu.getActiveNetworkInfo() != null && cu.getActiveNetworkInfo().isConnectedOrConnecting())
                    dbHelper.sendDB(this);
                else
                    message("Sorry, you are offline");
                return true;
            case R.id.action_settings:
                ConnectivityManager cs = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                if ((cs.getActiveNetworkInfo() != null && cs.getActiveNetworkInfo().isConnectedOrConnecting()) || pref.getBoolean("OFF", false))
                    showSetDialog();
                else
                    message("Sorry, you are offline");
                return true;
            default:
                return true;
        }
    }

    private void showInputDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (editText.getText().toString().isEmpty() || editText.getText().toString().substring(0, 1).equals(" ")) {
                            dialog.cancel();
                            message("Notename cannot be empty or started from space");
                        } else if (editText.getText().toString().length() > 20) {
                            dialog.cancel();
                            message("Notename cannot have more than 20 characters");
                        } else {
                            dbHelper.createNote(editText.getText().toString());
                            createTable();
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = alertDialogBuilder.create();
        alert.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }

    private void showDelDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.del_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        delNotes();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void showSetDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.set_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final Switch switchT = (Switch) promptView.findViewById(R.id.switchT);
        switchT.setChecked(pref.getBoolean("TOAST", false));
        switchT.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEd.putBoolean("TOAST", isChecked);
                mEd.commit();
            }
        });

        final Switch switchA = (Switch) promptView.findViewById(R.id.switchA);
        switchA.setChecked(pref.getBoolean("AUTHORS", false));
        switchA.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                itemA.setVisible(isChecked);
                mEd.putBoolean("AUTHORS", isChecked);
                mEd.commit();
            }
        });

        final Switch switchS = (Switch) promptView.findViewById(R.id.switchS);
        switchS.setChecked(pref.getBoolean("SIZE", false));
        switchS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEd.putBoolean("SIZE", isChecked);
                mEd.commit();
            }
        });

        final Switch switchR = (Switch) promptView.findViewById(R.id.switchR);
        switchR.setChecked(pref.getBoolean("READ", false));
        switchR.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEd.putBoolean("READ", isChecked);
                mEd.commit();
            }
        });

        final Switch switchO = (Switch) promptView.findViewById(R.id.switchO);
        switchO.setChecked(pref.getBoolean("OFF", false));
        switchO.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEd.putBoolean("OFF", isChecked);
                mEd.commit();
                itemU.setVisible(!pref.getBoolean("OFF", false));
            }
        });

        final Switch switchD = (Switch) promptView.findViewById(R.id.switchD);
        switchD.setChecked(pref.getBoolean("DATA", false));
        switchD.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEd.putBoolean("DATA", isChecked);
                mEd.commit();
                if (delMode) {
                    fab.setImageResource(android.R.drawable.ic_input_add);
                    for (Boolean b : delFlag) {
                        b = false;
                    }
                    delId.clear();
                    delMode = false;
                }
                createTable();
            }
        });

        final Switch switchF = (Switch) promptView.findViewById(R.id.switchF);
        switchF.setChecked(pref.getBoolean("FAB", false));
        switchF.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEd.putBoolean("FAB", isChecked);
                mEd.commit();
                if (pref.getBoolean("FAB", false)) {
                    fab.hide();
                } else {
                    fab.show();
                }
                if (delMode) {
                    fab.setImageResource(android.R.drawable.ic_input_add);
                    for (Boolean b : delFlag) {
                        b = false;
                    }
                    delId.clear();
                    delMode = false;
                    createTable();
                }
            }
        });

        alertDialogBuilder.setCancelable(true);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void delNotes() {
        dbHelper.delNotes(delId);
        for (Boolean b : delFlag) {
            b = false;
        }
        delId.clear();
        delMode = false;
        fab.setImageResource(android.R.drawable.ic_input_add);
        createTable();
    }

    private void createTable() {
        mainLay.removeAllViews();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("NoteTable", null, null, null, null, null, null);
        if (c.moveToFirst()) {
            do {
                createRow(c.getString(c.getColumnIndex("id")), c.getString(c.getColumnIndex("name")), c.getString(c.getColumnIndex("time")), c.getString(c.getColumnIndex("data")));
            } while (c.moveToNext());
        } else
            message("You have not any notes");
        dbHelper.close();
        c.close();
    }

    private void createRow(final String id, final String nm, String tm, String dt) {
        delFlag.add(false);
        final RelativeLayout relLay = new RelativeLayout(this);
        relLay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        relLay.setPadding((int) (5 * getResources().getDisplayMetrics().density + 0.5f), 0, (int) (10 * getResources().getDisplayMetrics().density + 0.5f), 0);

        View top = new View(this);
        RelativeLayout.LayoutParams topp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 1);
        top.setLayoutParams(topp);
        top.setBackgroundColor(Color.parseColor("#FF5722"));
        relLay.addView(top);

        relLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (delMode) {
                    for (int i = 0; i < mainLay.getChildCount(); i++) {
                        if (relLay.equals(mainLay.getChildAt(i))) {
                            if (delFlag.get(i)) {
                                relLay.setBackgroundColor(Color.TRANSPARENT);
                                delFlag.set(i, false);
                                delId.remove(id);
                            } else {
                                relLay.setBackgroundColor(Color.parseColor("#5C6BC0"));
                                delFlag.set(i, true);
                                delId.add(id);
                            }
                        }
                    }
                    if (delId.isEmpty()) {
                        delMode = false;
                        fab.setImageResource(android.R.drawable.ic_input_add);
                    }
                } else {
                    noteIntent.putExtra("name", nm);
                    noteIntent.putExtra("id", id);
                    noteIntent.putExtra("size", pref.getBoolean("SIZE", false));
                    noteIntent.putExtra("read", pref.getBoolean("READ", false));
                    startActivity(noteIntent);
                }
            }
        });

        relLay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!delMode && !pref.getBoolean("FAB", false)) {
                    delMode = true;
                    fab.setImageResource(android.R.drawable.ic_delete);
                    for (int i = 0; i < mainLay.getChildCount(); i++) {
                        if (relLay.equals(mainLay.getChildAt(i))) {
                            relLay.setBackgroundColor(Color.parseColor("#5C6BC0"));
                            delFlag.set(i, true);
                            delId.add(id);
                        }
                    }
                    vibr.vibrate(200);
                }
                return true;
            }
        });

        TextView txt = new TextView(this);
        RelativeLayout.LayoutParams txp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        txp.addRule(RelativeLayout.CENTER_VERTICAL);
        txt.setLayoutParams(txp);
        txt.setText(nm);
        txt.setTextAppearance(this, android.R.style.TextAppearance_Large);

        RelativeLayout relData = new RelativeLayout(this);
        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        relData.setLayoutParams(rlp);
        if (!pref.getBoolean("DATA", false)) {
            LinearLayout linData = new LinearLayout(this);
            linData.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            linData.setOrientation(LinearLayout.VERTICAL);

            TextView time = new TextView(this);
            time.setText(tm);
            time.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            time.setGravity(Gravity.CENTER_HORIZONTAL);
            time.setTextAppearance(this, android.R.style.TextAppearance_Large);
            time.setTextColor(Color.parseColor("#999999"));

            TextView data = new TextView(this);
            data.setText(dt);
            data.setLayoutParams(new LinearLayoutCompat.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            data.setTextAppearance(this, android.R.style.TextAppearance_Large);
            data.setTextColor(Color.parseColor("#999999"));

            linData.addView(time);
            linData.addView(data);
            relData.addView(linData);
        }
        relLay.addView(txt);
        relLay.addView(relData);
        mainLay.addView(relLay, 0);
    }

    private void message(String s) {
        if (pref.getBoolean("TOAST", false)) {
            Toast toast = Toast.makeText(getApplicationContext(),
                    s, Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Snackbar.make(getWindow().getDecorView(), s, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }
}
