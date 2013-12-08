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
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

import java.util.Random;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RolodexListView listView = new RolodexListView(this, null);
        setContentView(listView);
        listView.setAdapter(new SampleAdapter());
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
            return v;
        }
    }

}
