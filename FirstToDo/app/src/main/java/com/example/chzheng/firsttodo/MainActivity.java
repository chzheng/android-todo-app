package com.example.chzheng.firsttodo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;
    private EditText etEditText;

    private final int REQUST_CODE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        items = new ArrayList<String>();
        readItems(this);
//        items.add("dog");
//        items.add("cat");
//        items.add("mosue");

        itemsAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        lvItems = (ListView)findViewById(R.id.lvItems);
        lvItems.setAdapter(itemsAdapter);

        etEditText = (EditText) findViewById(R.id.etEditText);

        lvItems.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String item = items.remove(position);
                itemsAdapter.notifyDataSetChanged();
//                writeItems();
                deleteItem(MainActivity.this, item);
                return true;
            }
        });

        lvItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // navigate to EditItem
                Intent i = new Intent(MainActivity.this, EditItemActivity.class);
                i.putExtra("item", items.get(position));
                i.putExtra("position", position);
                startActivityForResult(i, REQUST_CODE);
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == REQUST_CODE) {
            String item = data.getExtras().getString("item");
            int position = data.getExtras().getInt("position");
            String oldItem = items.get(position);
            if (! oldItem.equals(item)) {
                items.set(position, item);
                itemsAdapter.notifyDataSetChanged();
//            writeItems();
                deleteItem(this, oldItem);
                addOrUpdateItem(this, item);
            }
        }
    }

    private void readItems(Activity activity) {
//        readItemsFromFile();
        items = ToDoDatabaseHelper.getInstance(activity.getApplication()).getAllToDos();
    }

    private void deleteItem(Activity activity, String item) {
        ToDoDatabaseHelper.getInstance(activity.getApplication()).deleteToDo(item);
    }

    private void addOrUpdateItem(Activity activity, String item) {
        ToDoDatabaseHelper.getInstance(activity.getApplication()).addorUpdateToDo(item);
    }

    private void writeItems() {
        File fileDir = getFilesDir();
        File file = new File(fileDir, "todo.txt");
        try {
            FileUtils.writeLines(file, items);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readItemsFromFile() {
        File fileDir = getFilesDir();
        File file = new File(fileDir, "todo.txt");
        try {
            items = new ArrayList<String>(FileUtils.readLines(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onAddItem(View view) {
        String item = etEditText.getText().toString();
        itemsAdapter.add(item);
        etEditText.setText("");
//        writeItems();
        addOrUpdateItem(this, item);
    }

}
