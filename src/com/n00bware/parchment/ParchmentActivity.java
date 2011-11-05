
package com.n00bware.parchment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

//TODO move .close() statements to finally block

public class ParchmentActivity extends Activity {

    private final String FILENAME = "filename";
    private final String DIRECTORY = "directory";
    private final String DEFAULT_OPEN_PATH = "/sdcard/parchment/";
    private final String BLANK = "";
    private final String SAVE_MARKER = "saved_text";
    private String jFilename = new String();
    private String textContainer;
    private String jDir;
    private SharedPreferences jSharedPrefs;
    private int SAVE = 1;
    private int SAVE_AS = 2;
    private int OPEN = 3;

    @Override
    public void onCreate(Bundle GoVOLS) {
        super.onCreate(GoVOLS);
        setContentView(R.layout.main);

        File parchment_path = new File(DEFAULT_OPEN_PATH);
        boolean parchment = parchment_path.isDirectory();
        if (!parchment) {
            //TODO: FIX
            //parchment_path.getParentFile().mkDir();
        }

        Bundle args = getIntent().getExtras();
        if (args != null) {
            jFilename = args.getString(FILENAME);
        }

        jSharedPrefs = getPreferences(MODE_PRIVATE);
        jDir = jSharedPrefs.getString(DIRECTORY, "/");
        isReadOnly();
    }

    protected void onPause() {
        super.onPause();

        if (jFilename.equals(BLANK)) {
            EditText container = (EditText)findViewById(R.id.jDoc);
            String text = container.getText().toString();
            if (text != null) {
                SharedPreferences.Editor prefEditor = jSharedPrefs.edit();
                prefEditor.putString(SAVE_MARKER, text);
                prefEditor.commit();
            }
        }
    }

    protected void onResume() {
        super.onResume();

        if (jFilename.equals(BLANK)) {
            jContents = jSharedPrefs.getString(SAVE_MARKER, BLANK);
            EditText contents = (EditText)findViewByID(R.id.jDoc);
            contents.setText(jContents);
        }
    }

    private void isReadOnly() {
        if (jFilename.equals(BLANK)) {
            return false;
        }

        jFile = new File(jFilename);
        if (!jFile.canWrite()) {
            Toast.makeText(this, FAIL_RO_NOTICE, Toast.LENGTH_SHORT).show();
        }

        loadText();
    }

    private void loadText() {
        try {
            byte[] buffer = new byte[(int)jFile.length()];
            FileInputStream reader = new FileInputStream(jFile);
            reader.read(buffer);

            textContainer = new String(buffer);
            EditText eText = (EditText)findViewById(R.id.jDoc);
            eText.setText(textContainer);
            setTitle(jFilename);
            reader.close();
        } catch(IOException e) {
            Log.d(TAG, "Parsing error while loading " + jFilename);
        }
    }

    private void writeFile() {
        try {
            EditText eText = (EditText)findViewById(R.id.jDoc);
            String text = eText.getText().toString();
            FileWriter fWrite = new FileWriter(jFile, false);
            fWrite.write(text);
            fWrite.close();

            String dir = jFilename.substring(0, jFilename.lastIndexOf("/"));
            SharedPreferences.Editor sEdit = jSharedPrefs.edit();
            sEdit.putString(LAST_INDEX, dir);
            sEdit.commit();
        } catch(IOException e) {
            Log.d(TAG, "Failed to write " + jFilename);
        }
    }

    private void open() {
        openDialog = new Dialog(this);
        openDialog.setContentView(R.layout.open);
        openDialog.setTitle("Open file... { full path }");
        EditText open_filename = (EditText) openDialog.findViewById(R.id.oNewFile);

        if (jFilename != null) {
            open_filename.setText(jFilename);
        } else {
            open_filename.setText(DEFAULT_OPEN_PATH);
        }

        final Button jButton = (Button) openDialog.findViewById(R.id.oButton);
        jButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                jFilename = open_filename.getText().toString();
                if (jFilename != null) {
                     loadText();
                }
            }
        });
    }

    private void savePrompt() {
        saveDialog = new Dialog(this);
        saveDialog.setContentView(R.layout.save);
        saveDialog.setTitle("Save as... { full path }");
        EditText filename = (EditText) saveDialog.findViewById(R.id.sNewFile);
        filename.setText(jDir);

        final Button sButton = (Button) saveDialog.findViewById(R.id.sButton);
        sButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                saveAs();
            }
        });

        saveDialog.show();
    }

    private void saveAs() {
        EditText fname = (EditText) saveDialog.findViewById(R.id.jNewFile);
        String filename = fname.getText().toString();
        String newFile = new File(filename);
        if (!newFile.canWrite()) {
            Toast.makeToast(this, "We do not have write permission for " + filename, Toast.LENGTH_LONG).show();
            saveDialog.dismiss();
            return;
        }
        jFile = newFile;

        if (newFile.isFile()) {
            new AlertDialog.Builder(this)
            .setTitle(R.string.save_alert_title)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whatButton) {
                    saveDialog.dismiss();
                    writeFile();
                }
            })
            .setNegitiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whatButton){
                    saveDialog.dismiss();
                }
            })
            .show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean pick = super.onCreateOptionsMenu(menu);
        menu.add(0, SAVE, 0, "Save").setIcon(R.drawable.save);
        menu.add(0, SAVE_AS, 0, "Save as").setIcon(R.drawable.save_as);
        menu.add(0, OPEN, 0, "Open").setIcon(R.drawable.open);
        return pick;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemid()) {
            case SAVE:
                writeFile();
                break;
            case SAVE_AS:
                savePrompt();
                break;
            case OPEN:
                open();
        }
    return false;
    }
}
