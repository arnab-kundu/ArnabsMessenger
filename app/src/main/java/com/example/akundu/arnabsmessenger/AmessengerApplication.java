package com.example.akundu.arnabsmessenger;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.leakcanary.LeakCanary;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by akundu on 23-Dec-16.
 */

public class AmessengerApplication extends Application implements Application.ActivityLifecycleCallbacks {
    public static FirebaseAuth appfirebaseAuth;
    public static FirebaseUser appfirebaseUser;
    public static String username;

    public AmessengerApplication() {
        registerActivityLifecycleCallbacks(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        // Normal app init code...
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("msg", "onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Log.d("msg", "onTrimMemory");
        if (AmessengerApplication.appfirebaseUser != null ) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo").child(AmessengerApplication.appfirebaseAuth.getCurrentUser().getUid());
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("online", "away");
            databaseReference.updateChildren(hashMap);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        Log.d("msg", "onTerminate");
        AmessengerApplication.appfirebaseUser = null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d("msg", "onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Log.d("msg", "onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.d("msg", "onActivityResumed"+activity.getLocalClassName());
        if (AmessengerApplication.appfirebaseUser != null && activity.getLocalClassName().equals("FriendListActivity")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo").child(AmessengerApplication.appfirebaseAuth.getCurrentUser().getUid());
            Map<String, Object> hasMap = new HashMap<>();
            hasMap.put("online", "online");
            databaseReference.updateChildren(hasMap);
        }


    }

    @Override
    public void onActivityPaused(Activity activity) {
        Log.d("msg", "onActivityPaused");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Log.d("msg", "onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d("msg", "onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Log.d("msg", "onActivityDestroyed");
        if (AmessengerApplication.appfirebaseUser != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo").child(AmessengerApplication.appfirebaseAuth.getCurrentUser().getUid());
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("online", "offline");
            //databaseReference.updateChildren(hashMap);
        }
    }
}
