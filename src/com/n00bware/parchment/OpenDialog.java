
package com.n00bware.parchment;

import android.app.Activity;
import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TextView;

import java.io.File;

public class OpenDialog extends ListActivity {
    private final String TAG = "Parchment";
    private final String OPEN_FILENAME = "FILE_TO_BE_OPENED:";
    private final String CURRENT_PATH = "/";
    private SharedPreferences mSharedPrefs;
    private TextView mTextView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // When clicked, show a toast with the TextView text
                Toast.makeText(getApplicationContext(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();

                String path = new String(mTextView.getText().toString());
                updatePath(path);
            }
        });
    }
    private void updatePath(String selection) {
        SharedPreferences.Editor openFilename = mSharedPrefs.edit();

        File file_path = new File(CURRENT_PATH, selection);
        if (file_path.isDirectory()) {
            Log.d(TAG, String.format("Directory Selected: %s", file_path.getAbsolutePath()));

            String[] ls = file_path.list();

            setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ls));
        }
        if (file_path.isFile()) {
            String filename = new String(file_path.getAbsolutePath());
            Log.d(TAG, String.format("File selected: %s", filename));
            openFilename.putString(OPEN_FILENAME, filename);
            openFilename.commit();
        }

    }
}
