
package com.n00bware.parchment;

import java.io.File;
import java.util.ArrayList;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OpenFileDialog extends ListActivity {

    private Intent intent;
    private List<String> item = null;
    private List<String> path = null;
    private final String OPEN_FILENAME = "open_filepath";
    private String root="/";
    private final String TAG = "Parchment";
    private SharedPreferences mSharedPrefs;
    private TextView myPath;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.open_file);
        myPath = (TextView)findViewById(R.id.path);
        getDir(root);
    }
    
    private void getDir(String dirPath) {

        myPath.setText("Location: " + dirPath);

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
            if(file.canRead()) {
                getDir(path.get(position));
            } else {
                new AlertDialog.Builder(this)
                .setIcon(R.drawable.open)
                .setTitle("[" + file.getName() + "] folder can't be read!")
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
            .setPositiveButton("Open?", 
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String filename = new String(file.getAbsolutePath());
                    Log.d(TAG, String.format("File selected: %s", filename));
                    intent = getIntent();
                    intent.putExtra(OPEN_FILENAME, filename);
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
