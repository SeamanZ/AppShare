package com.example.seamanz.appshare

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log


class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.v(TAG, "onReceive = $intent")
        intent.extras?.let { bundle ->
            for (key in bundle.keySet()) {
                Log.v(TAG, "onReceive key = $key")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {

                val componentName = bundle[Intent.EXTRA_CHOSEN_COMPONENT] as ComponentName
                Log.v(TAG, "onReceive componentName = $componentName")

                val pm = context.packageManager

                //取得用户选中选的应用名字
                val applicationLabel = pm.getApplicationLabel(pm.getApplicationInfo(componentName.packageName, 0))
                Log.v(TAG, "onReceive applicationLabel = $applicationLabel")
            }


        }

    }

    companion object {
        const val TAG = "MyReceiver"
    }
}
