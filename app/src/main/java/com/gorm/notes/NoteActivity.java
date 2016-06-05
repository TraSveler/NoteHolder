package com.gorm.notes;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class NoteActivity extends AppCompatActivity {
    private EditText nEditText;
    private TextView nTextView;
    private DBHelper dbHelper;
    private SharedPreferences pref;
    private SharedPreferences.Editor nEd;
    private MenuItem itemP, itemM;
    private int tSize;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(getIntent().getStringExtra("name"));
        pref = getPreferences(MODE_PRIVATE);
        nEd = pref.edit();
        tSize = pref.getInt("TEXT_SIZE", 15);

        dbHelper = new DBHelper(this);
        nEditText = (EditText) findViewById(R.id.editTextN);
        nTextView = (TextView) findViewById(R.id.textViewN);
        nEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
        nEditText.setText(dbHelper.getNote(getIntent().getStringExtra("id")));
        nTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
        nTextView.setText(dbHelper.getNote(getIntent().getStringExtra("id")));
        if (getIntent().getBooleanExtra("read", false)) {
            nEditText.setVisibility(View.GONE);
            nTextView.setVisibility(View.VISIBLE);
        } else {
            nEditText.setVisibility(View.VISIBLE);
            nTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        itemP = menu.findItem(R.id.action_plus);
        itemM = menu.findItem(R.id.action_minus);
        itemP.setVisible(getIntent().getBooleanExtra("size", false));
        itemM.setVisible(getIntent().getBooleanExtra("size", false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_plus:
                if (tSize < 30) {
                    tSize += 2;
                    nEd.putInt("TEXT_SIZE", tSize);
                    nEd.commit();
                    nEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
                    nTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
                }
                return true;
            case R.id.action_minus:
                if (tSize > 10) {
                    tSize -= 2;
                    nEd.putInt("TEXT_SIZE", tSize);
                    nEd.commit();
                    nEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
                    nTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, tSize);
                }
                return true;
            case android.R.id.home:
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (inputMethodManager.isAcceptingText())
                    inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                showSaveDialog();
                return true;
            default:
                return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showSaveDialog();
        }
        return false;
    }

    private void showSaveDialog() {
        if (getIntent().getBooleanExtra("read", false)) {
            onBackPressed();
        } else {
            LayoutInflater layoutInflater = LayoutInflater.from(NoteActivity.this);
            View promptView = layoutInflater.inflate(R.layout.save_dialog, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(NoteActivity.this);
            alertDialogBuilder.setView(promptView);

            alertDialogBuilder.setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dbHelper.saveNote(getIntent().getStringExtra("id"), nEditText.getText().toString());
                            onBackPressed();

                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            onBackPressed();
                        }
                    })
                    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
    }
}
