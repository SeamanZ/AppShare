package com.example.seamanz.appshare

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
import android.content.pm.LabeledIntent
import android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            shareUrl()
        }
    }

    private fun shareUrl() {

        val action = Intent.ACTION_SEND
        val mimeType = "text/plain"
        val text = "https://github.com"
        val subject = "this is a subject"
        val title = "Share to your friends"

        //查询系统有哪些APP支持文本内容分享
        val queryIntent = Intent().apply {
            this.action = action
            this.type = mimeType
            this.putExtra(Intent.EXTRA_TEXT, text)
            this.putExtra(Intent.EXTRA_SUBJECT, subject)

        }
        val resolveInfoList = packageManager.queryIntentActivities(queryIntent, MATCH_DEFAULT_ONLY)
        //可选的分享应用Intent
        val initialIntents = mutableListOf<LabeledIntent>()
        for (info in resolveInfoList) {

            val appName = info.loadLabel(packageManager)
            val activityInfo = info.activityInfo
            val targetPackageName = activityInfo.packageName
            val activityName = activityInfo.name

            Log.v(TAG, "targetPackageName = $targetPackageName  appName = $appName activityName = $activityName")

            //过滤掉APP自己（在API18的模拟器上发现有BUG，这样写也不能过虑）
            if (!this.packageName!!.contentEquals(targetPackageName)) {

                val intent = Intent().apply {
                    this.component = ComponentName(targetPackageName, activityName)
                    this.action = action
                    this.type = mimeType
                    this.addFlags(FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                    this.putExtra(Intent.EXTRA_TEXT, text)
                    this.putExtra(Intent.EXTRA_SUBJECT, subject)
                }

                initialIntents.add(LabeledIntent(intent, targetPackageName, appName, info.icon))
            }

        }

        if (initialIntents.size > 0) {
            //以APP名字来排序
            initialIntents.sortBy { it.nonLocalizedLabel.toString() }

            //用来创建chooserIntent的Intent的APP会被系统追加到分享选择列表的最后一项
            val intent = initialIntents.removeAt(initialIntents.lastIndex)

            val chooserIntent = if (Build.VERSION.SDK_INT >= 22) {

                //用户选择目标应用的时候，系统会发广播到MyReceiver
                val receiver = Intent(this, MyReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    this, 0, receiver, PendingIntent.FLAG_UPDATE_CURRENT
                )

                Intent.createChooser(intent, title, pendingIntent.intentSender)
            } else {
                Intent.createChooser(intent, title)
            }

            chooserIntent.apply {
                this.putExtra(Intent.EXTRA_INITIAL_INTENTS, initialIntents.toTypedArray())
            }

            startActivity(chooserIntent)

        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
