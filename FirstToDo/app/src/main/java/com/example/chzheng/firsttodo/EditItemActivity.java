package com.example.chzheng.firsttodo;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class EditItemActivity extends AppCompatActivity {
    private String item;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_item);

        item = getIntent().getStringExtra("item");
        EditText etItem = (EditText) findViewById(R.id.etItem);
        etItem.setText(item);

        position = getIntent().getIntExtra("position", 0);

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

    public void onSaveEdit(View view) {
        EditText etItem = (EditText) findViewById(R.id.etItem);
        item = etItem.getText().toString();
        Intent i = new Intent();
        i.putExtra("item", item);
        i.putExtra("position", position);
        setResult(RESULT_OK, i);
        this.finish();
    }
}
