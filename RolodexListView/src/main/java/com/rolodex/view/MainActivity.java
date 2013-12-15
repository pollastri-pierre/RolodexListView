package com.rolodex.view;

import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.util.Random;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RolodexListView listView = new RolodexListView(this, null);
        listView.setPadding(20, 20, 20, 20);
        setContentView(listView);
        listView.setAdapter(new SampleAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this, "Click on " + i, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class SampleAdapter extends BaseAdapter {

        Random r = new Random();

        @Override
        public int getCount() {
            return 100;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ImageView v = new ImageView(MainActivity.this);

            v.setImageDrawable(getResources().getDrawable(getResources()
                    .getIdentifier("test_" + (r.nextInt(7) + 1), "drawable", getPackageName())));
            v.setScaleType(ImageView.ScaleType.FIT_XY);
            //View v = new View(MainActivity.this);
            //v.setBackgroundColor(Color.argb(250, r.nextInt(255), r.nextInt(255), r.nextInt(255)));
            //Button v = new Button(MainActivity.this);
            //v.setText("Click");
            return v;
        }
    }

}
