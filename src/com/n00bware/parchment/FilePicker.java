
package com.n00bware.parchment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FilePicker extends ListActivity {

    private Button saveButton;
    private EditText saveFilename;
    private Intent intent;
    private List<String> item = null;
    private List<String> path = null;
    private String BLANK = "";
    private String DIR_MARKER = "/";
    private String root="/";
    private final String OPEN_FILENAME = "open_filepath";
    private final String OPEN = "open";
    private final String PARENT_DIR = "../";
    private final String SAVE = "save";
    private final String SAVE_FILENAME = "save_filepath";
    private final String TAG = "Parchment";
    private SharedPreferences mSharedPrefs;
    private TextView myPath;

    private final String prompt_title = "[ %s ]";
    private final String file_selection_error_write = "We can't write to: %s";
    private final String file_selection_error_read = "We can\'t read: %s";
    private final String save_prompt = "Save as?";
    private final String save_message = "Overwrite: %s ?";
    private final String open_message = "Open: %s";
    private final String open_prompt = "Open?";
    private final String no_filename_save_error = "No filename detected ...Please enter a filename to save";
    private final String location_tracker = "Location: %s";

    private String mPrompt;
    private String mFileError;
    private String mMessage;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_picker);

        myPath = (TextView)findViewById(R.id.path);
        saveFilename = (EditText)findViewById(R.id.save_filename);

        //decide if we want to save or just open file
        LinearLayout save_layout = (LinearLayout)findViewById(R.id.save_layout);
        if (!Global.SAVEABLE) {
            save_layout.setVisibility(View.GONE);
        }
        setStrings();

        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename_entered = saveFilename.getText().toString();
                Log.d(TAG, String.format("path detected: %s", filename_entered));
                if (!filename_entered.equals(BLANK)) {
                    File path_track = new File (filename_entered);
                    intent = getIntent();
                    intent.putExtra(SAVE_FILENAME, filename_entered);
                    setResult(RESULT_OK, intent);
                    Global.SAVEABLE = true;
                    Global.PREV_PATH = path_track.getParent();
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), no_filename_save_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
        Log.d(TAG, String.format("Previous path %s", Global.PREV_PATH));
        getDir(Global.PREV_PATH);
    }
    
    private void getDir(String dirPath) {

        myPath.setText(String.format(location_tracker, dirPath));
        if (!dirPath.equals(DIR_MARKER)) {
            saveFilename.setText(dirPath +DIR_MARKER);
            Global.PREV_PATH = dirPath;
        }

        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if (!dirPath.equals(root)) {
            item.add(root);
            path.add(root);
            item.add(PARENT_DIR);
            path.add(f.getParent());
        }

        try {
            for (int i=0; i < files.length; i++) {
                File file = files[i];
                path.add(file.getPath());

                //put list in alphabetic order
                Collections.sort(item, String.CASE_INSENSITIVE_ORDER);
                Collections.sort(path, String.CASE_INSENSITIVE_ORDER);

                if (file.isDirectory()) {
                    item.add(file.getName() + DIR_MARKER);
                } else {
                    item.add(file.getName());
                }
            }

            ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
            setListAdapter(fileList);
        } catch (NullPointerException npe) {
            Log.d(TAG, "we experienced a problem with the path");
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	
        final File file = new File(path.get(position));
        final String title = String.format(mFileError, file.getAbsolutePath());

        if (file.isDirectory()) {
            if(file.canWrite()) {
                getDir(path.get(position));
            } else {
                new AlertDialog.Builder(this)
                .setIcon(R.drawable.open)
                .setTitle(title)
                .setPositiveButton("OK", 
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }
        } else {
            new AlertDialog.Builder(this)
            .setIcon(R.drawable.files)
            .setTitle(String.format(mMessage, file.getAbsolutePath()))
            .setPositiveButton(mPrompt,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    intent = getIntent();

                    String filename = new String(file.getAbsolutePath());
                    Log.d(TAG, String.format("File selected: %s", filename));

                    if (!Global.SAVEABLE) {
                        intent.putExtra(OPEN_FILENAME, filename);
                    } else {
                        intent.putExtra(SAVE_FILENAME, filename);
                    }

                    setResult(RESULT_OK, intent);
                    Global.SAVEABLE = true;
                    Global.PREV_PATH = file.getParent();
                    finish();
                }
            })
            .setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
        }
    }

    public void setStrings() {
        //decide what strings to show
        if (!Global.SAVEABLE) {
            mPrompt = open_prompt;
            mFileError = file_selection_error_read;
            mMessage = open_message;
        } else {
            mPrompt = save_prompt;
            mFileError = file_selection_error_write;
            mMessage = save_message;
        }

        //because we don't want null pointers
        if ((Global.PREV_PATH == null) || (Global.PREV_PATH.equals(BLANK))) {
            Global.PREV_PATH = root;
            Log.d(TAG, String.format("Previous path %s", Global.PREV_PATH));
        }
    }
}
