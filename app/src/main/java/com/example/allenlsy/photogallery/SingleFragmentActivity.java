package com.example.allenlsy.photogallery;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public abstract class SingleFragmentActivity extends FragmentActivity {

    protected abstract Fragment createFragment();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_fragment); // activity_fragment is the generic layout for single fragment activity
        setContentView(getLayoutResId());

        // add new fragment to contain Crime
        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragmentContainer);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }

    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }
}
