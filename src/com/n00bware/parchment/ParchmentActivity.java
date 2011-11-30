
package com.n00bware.parchment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
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

/* 
 * TODO move .close() statements to finally blocks
 * nomenclature object prefixes are changing; to claify
 *p ParchmentActivity objects; Content from layout/main.xml
 *s Content from layout/save.xml
 *o Content from layout/open.xml
 */

public class ParchmentActivity extends Activity {

    private final String TAG = "Parchment";
    private final String FILENAME = "filename";
    private final String DIRECTORY = "directory";
    private final String DEFAULT_OPEN_PATH = "/sdcard/parchment/parchment_test";
    private final String BLANK = "";
    private final String ROOT_DIR = "/";
    private final String OPEN_FILENAME = "open_filepath";
    private final String SAVE_FILENAME = "save_filepath";
    private final String SAVE_MARKER = "saved_text";
    private final String LAST_INDEX = "last_index";
    private final String SAVE_ALERT_TITLE = "Save as %s";
    private final String PARSING_ERROR = "Parsing error while loading ";
    private String pFilename = new String();
    private String textContainer;
    private String pDir;
    private String pContents;

    private final int NEW = 1;
    private final int SAVE = 2;
    private final int SAVE_AS = 3;
    private final int OPEN = 4;

    private boolean mIsNewFile = false;

    private Button saveButton;
    private File pFile;
    private File root = Environment.getExternalStorageDirectory();
    private Intent intent;
    private SharedPreferences pSharedPrefs;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        //we want to be sure Global.SAVEABLE =TRUE
        //then set false when just opening
        Global.SAVEABLE = true;

        Bundle args = getIntent().getExtras();
        if (args != null) {
            pFilename = args.getString(FILENAME);
            setTitle(pFilename);
            Global.PREV_PATH = pFilename;
            Log.d(TAG, "args: " + pFilename);
        } else {
            Log.d(TAG, "args: null");
        }

        if ((Global.PREV_PATH == null) || (Global.PREV_PATH.equals(ROOT_DIR))) {
            setTitle(R.string.default_title);
            Global.PREV_PATH = ROOT_DIR;
        }

        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.d(TAG, "sdcard mounted");
            if (!root.canWrite()) {
                Log.d(TAG, "however sdcard is not writable");
            } else {
                Log.d(TAG, "sdcard is also writable");
            }
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            Log.d(TAG, "sdcard readonly");
        } else {
            Log.d(TAG, "epic failure");
        }

        pSharedPrefs = getPreferences(MODE_WORLD_WRITEABLE);
        pDir = pSharedPrefs.getString(DIRECTORY, "/");
        /*
         * TODO: read only is important but we should look into
         * allowing su save abilities
         */
        isReadOnly();
    }

    /* onStart should be called when orentation changes hopefully now we won't lose data from TextEdits this way */
    @Override
    public void onStart() {
        super.onStart();
        setContentView(R.layout.main);

        if ((Global.PREV_PATH == null) || (Global.PREV_PATH.equals(ROOT_DIR))) {
            setTitle(R.string.default_title);
            Global.PREV_PATH = ROOT_DIR;
        }

        pContents = pSharedPrefs.getString(SAVE_MARKER, BLANK);
        if (!pContents.equals(BLANK)) {
            EditText contents = (EditText)findViewById(R.id.pDoc);
            contents.setText(pContents);
        }
    }

    protected void onPause() {
        super.onPause();

        if (pFilename.equals(BLANK)) {
            EditText container = (EditText)findViewById(R.id.pDoc);
            String text = container.getText().toString();
            if (text != null) {
                SharedPreferences.Editor prefEditor = pSharedPrefs.edit();
                prefEditor.putString(SAVE_MARKER, text);
                prefEditor.commit();
            }
        }
    }

    protected void onResume() {
        super.onResume();

        if (pFilename.equals(BLANK)) {
            pContents = pSharedPrefs.getString(SAVE_MARKER, BLANK);
            EditText contents = (EditText)findViewById(R.id.pDoc);
            contents.setText(pContents);
        }
    }

    private void isReadOnly() {
        pFile = new File(pFilename);

        String file_path = pFile.getAbsolutePath();
        Log.d(TAG, String.format("file path { %s }", file_path));
        if (!file_path.equals(ROOT_DIR)) {
            setTitle(file_path);
            Log.d(TAG, String.format("Title should be %s", file_path));
        }

        loadText();
    }

    private void loadText() {
        try {
            byte[] buffer = new byte[(int)pFile.length()];
            Log.d(TAG, pFile.toString());
            FileInputStream reader = new FileInputStream(pFile);
            reader.read(buffer);

            textContainer = new String(buffer);
            EditText eText = (EditText)findViewById(R.id.pDoc);
            eText.setText(textContainer);
            String text = eText.getText().toString();
            if (text != null) {
                SharedPreferences.Editor prefEditor = pSharedPrefs.edit();
                prefEditor.putString(SAVE_MARKER, text);
                prefEditor.commit();
            }
            Log.d(TAG, "Text found: " + eText.getText().toString());
            reader.close();
        } catch(IOException ioe) {
            Log.d(TAG, PARSING_ERROR + pFilename);
            Toast.makeText(getApplicationContext(), PARSING_ERROR + pFilename, Toast.LENGTH_SHORT).show();
            //swallowed exception
        } catch (Exception e) {
            //swallowed exception
        }
    }

    private void writeFile() {
        try {
            EditText eText = (EditText)findViewById(R.id.pDoc);
            String text = eText.getText().toString();
            FileWriter fWrite = new FileWriter(pFile);
            Log.d(TAG, String.format("text we are attempting to write { %s }", text));
            String titleSet = pFile.getAbsolutePath();
            setTitle(titleSet);
            try {
                fWrite.write(text);
                fWrite.flush();
                fWrite.close();
                String dir = LAST_INDEX;
                SharedPreferences.Editor sEdit = pSharedPrefs.edit();
                sEdit.putString(LAST_INDEX, dir);
                sEdit.commit();
            } catch (IOException IOe) {
                Log.d(TAG, "IOException while FileWriter was writing file");
                //swallowed exception
            }

        } catch(IOException IOE) {
            Log.d(TAG, "Failed to write " + pFilename);
            //swallowed exception
        } catch (Exception e) {
            //swallowed exception
        }
    trustButVerify();
    }

    private void newFile() {
        final EditText old_text = (EditText)findViewById(R.id.pDoc);
        String pdoc_txt = old_text.getText().toString();

        if (!pdoc_txt.equals(BLANK)) {
            new AlertDialog.Builder(this)
            .setTitle(R.string.new_file_title)
            .setPositiveButton(R.string.save_button_text, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whatButton) {
                    mIsNewFile = true;
                    savePrompt();
                }
            })
            .setNegativeButton(R.string.clear, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whatButton) {
                    old_text.setText(BLANK);
                    setTitle(R.string.default_title);
                }
            }).show();
        }
    }

    private void open() {
        Global.SAVEABLE = false;
        Intent open_file = new Intent(this, FilePicker.class);
        open_file.putExtra(OPEN_FILENAME, BLANK);
        startActivityForResult(open_file, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            try {
                String open_data_string = data.getStringExtra(OPEN_FILENAME);
                Log.d(TAG, String.format("extra open data found: %s", open_data_string));
                pFilename = open_data_string;
                isReadOnly();
            } catch (NullPointerException npe) {
                Toast.makeText(getApplicationContext(), "no file was returned", Toast.LENGTH_SHORT).show();
                //swallowed exception
            }

        } else if (requestCode == 4) {
            try {
                String save_data_string = data.getStringExtra(SAVE_FILENAME);
                Log.d(TAG, String.format("extra save data found: %s", save_data_string));
                pFilename = save_data_string;
                pFile = new File(save_data_string);
                saveAs();
            } catch (NullPointerException npe) {
                Toast.makeText(getApplicationContext(), "no file was returned", Toast.LENGTH_SHORT).show();
                //swallowed return
            }
        } else {
            Log.wtf(TAG, "This shouldn't ever happen ...shit is fucked up");
        }

    }

    private void savePrompt() {
        EditText et = (EditText)findViewById(R.id.pDoc);
        String txt = et.getText().toString();

        if (!txt.equals(BLANK)) {
            Global.SAVEABLE = true;
            Intent save_file = new Intent(this, FilePicker.class);
            save_file.putExtra(SAVE_FILENAME, BLANK);
            startActivityForResult(save_file, 4);
        } else {
            Toast.makeText(getApplicationContext(), R.string.save_blank, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAs() {
        Log.d(TAG, pFile.getAbsolutePath());

        if (!pFile.isFile()) {
            new AlertDialog.Builder(this)
            .setTitle(String.format(SAVE_ALERT_TITLE, pFile.getAbsolutePath()))
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whatButton) {
                    mIsNewFile = false;
                    writeFile();
                }
            })
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whatButton) {
                    mIsNewFile = false;
                }
            })
            .show();
        } else {
            Log.d(TAG, "if not a file already");
            mIsNewFile = false;
            writeFile();
        }
    }

    private void trustButVerify() {
        EditText reset_text = (EditText)findViewById(R.id.pDoc);
        String pFile_path = pFile.getAbsolutePath();
        File trust = new File(pFile_path);
        Log.d(TAG, String.format("pFile {%s} pFilename {%s}", pFile_path, pFilename));

        if (trust.exists() && trust.canWrite() && trust.canRead()) {
            Log.d(TAG, "Trust but verify ... all good here");
            Toast.makeText(getApplicationContext(), String.format("%s has been saved", pFile_path), Toast.LENGTH_SHORT).show();
            setTitle(pFile_path);

            if (mIsNewFile) {
                reset_text.setText(BLANK);
            }
            mIsNewFile = false;
        } else {
            mIsNewFile = false;
            Log.d(TAG, "Trust but verify ...failed combined checks");
            Toast.makeText(getApplicationContext(), String.format("%s has not been save", pFile_path), Toast.LENGTH_SHORT).show();
            setTitle(R.string.default_title);

            if (trust.exists()) {
                Log.d(TAG, String.format("Trust but verify ... '%s' does in fact exist", pFile_path));
                if (trust.canRead()) {
                    Log.d(TAG, "Trust but verify ... we can read " + pFile_path);
                } else {
                    Log.d(TAG, "Trust but verify ...failed canRead()");
                }
                if (trust.canWrite()) {
                    Log.d(TAG, "Trust but verify ... we can write " + pFile_path);
                } else {
                    Log.d(TAG, "Trust but verify ... failed canWrite()");
                }
            } else {
                Log.d(TAG, "Trust but verify ... failed exists()");
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        boolean pick = super.onCreateOptionsMenu(menu);
        menu.add(0, NEW, 0, "New").setIcon(R.drawable.new_file);
        menu.add(0, SAVE, 0, "Save").setIcon(R.drawable.save);
        menu.add(0, SAVE_AS, 0, "Save as").setIcon(R.drawable.save_as);
        menu.add(0, OPEN, 0, "Open").setIcon(R.drawable.open);
        return pick;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case NEW:
                newFile();
                break;
            case SAVE:
                writeFile();
                break;
            case SAVE_AS:
                savePrompt();
                break;
            case OPEN:
                open();
                break;
        }
    return false;
    }
}
