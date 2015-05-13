package com.sayit.vipulmittal.sayit.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

import com.sayit.vipulmittal.sayit.R;
import com.sayit.vipulmittal.sayit.listenerModule.ListenerService;


public class MainActivity extends FragmentActivity {
    private MainFragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getActionBar() != null)
            getActionBar().hide();
        if (savedInstanceState == null) {
            fragment = new MainFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, fragment)
                    .commit();
        }
        startService(new Intent(this, ListenerService.class));

    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ListenerService.LOADED);
        filter.addAction(ListenerService.RUNNING);
        filter.addAction(ListenerService.STOPPED);
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, ListenerService.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(ListenerService.LOADED)) {
                fragment.statusChanged(MainFragment.Status.LOADED);
            } else if (action.equals(ListenerService.RUNNING)) {
                fragment.statusChanged(MainFragment.Status.RUNNING);
            } else if (action.equals(ListenerService.STOPPED)) {
                fragment.statusChanged(MainFragment.Status.LOADED);
            }
        }
    };
}
