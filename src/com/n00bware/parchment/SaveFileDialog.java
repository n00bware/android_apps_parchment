
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SaveFileDialog extends ListActivity {

    private Button saveButton;
    private EditText saveFilename;
    private Intent intent;
    private List<String> item = null;
    private List<String> path = null;
    private String BLANK = "";
    private String root="/";
    private final String SAVE_FILENAME = "save_filepath";
    private final String TAG = "Parchment";
    private SharedPreferences mSharedPrefs;
    private TextView myPath;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.save_file);

        myPath = (TextView)findViewById(R.id.spath);
        saveFilename = (EditText)findViewById(R.id.save_filename);

        saveButton = (Button)findViewById(R.id.save_button);
        saveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String filename_entered = saveFilename.getText().toString();
                Log.d(TAG, String.format("path detected: %s", filename_entered));
                if (!filename_entered.equals(BLANK)) {
                    intent = getIntent();
                    intent.putExtra(SAVE_FILENAME, filename_entered);
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "No filename detected ...Please enter a filename to save", Toast.LENGTH_SHORT).show();
                }
            }
        });
        getDir(root);
    }
    
    private void getDir(String dirPath) {

        myPath.setText("Location: " + dirPath);
        if (!dirPath.equals("/")) {
            saveFilename.setText(dirPath +"/");
        }

        item = new ArrayList<String>();
        path = new ArrayList<String>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if (!dirPath.equals(root)) {
            item.add(root);
            path.add(root);
            item.add("../");
            path.add(f.getParent());
        }

        for (int i=0; i < files.length; i++) {
            File file = files[i];
            path.add(file.getPath());

            //put list in alphabetic order
            Collections.sort(item, String.CASE_INSENSITIVE_ORDER);
            Collections.sort(path, String.CASE_INSENSITIVE_ORDER);

            if (file.isDirectory()) {
                item.add(file.getName() + "/");
            } else {
                item.add(file.getName());
            }
    	}

        ArrayAdapter<String> fileList = new ArrayAdapter<String>(this, R.layout.row, item);
        setListAdapter(fileList);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	
        final File file = new File(path.get(position));
        if (file.isDirectory()) {
            if(file.canWrite()) {
                getDir(path.get(position));
            } else {
                new AlertDialog.Builder(this)
                .setIcon(R.drawable.open)
                .setTitle("We can't write to [" + file.getName() + "] !")
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
            .setTitle("[" + file.getName() + "]")
            .setPositiveButton("Save as?", 
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String filename = new String(file.getAbsolutePath());
                    Log.d(TAG, String.format("File selected: %s", filename));
                    intent = getIntent();
                    intent.putExtra(SAVE_FILENAME, filename);
                    setResult(RESULT_OK, intent);
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
}
