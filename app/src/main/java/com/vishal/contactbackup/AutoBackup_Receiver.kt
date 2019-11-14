package com.vishal.contactbackup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Environment
import android.provider.ContactsContract
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.LinkedHashSet
import android.R.attr.start
import android.app.AlarmManager
import android.app.PendingIntent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService


class AutoBackup_Receiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        contactbackup(context)
        addnext(context)
    }

    private fun addnext(context: Context) {
        var intent=Intent(context,AutoBackup_Receiver::class.java)
        var pendingintent= PendingIntent.getBroadcast(context,101,intent, 0)
        var alarmManager=context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+43200000,pendingintent)
    }

    fun contactbackup(context:Context){

        var folder = File("${Environment.getExternalStorageDirectory()}/Contact Backup")
        if (!folder.exists())
            folder.mkdir()
        var timelong=System.currentTimeMillis()
        val date = Date(timelong)
        val format = SimpleDateFormat("HH:mm:ss dd-MM-yyyy")
        format.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
        var time = format.format(date)
        var filename="/Contact Backup/Contacts_${timelong}.vcf"




        val cr = context.getContentResolver()
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")

        var namelist= LinkedList<String>()
        var numchecklist=LinkedHashSet<Long>()
        var numlist= LinkedList<Long>()

        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {



                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )

                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id), null
                    )


                    while (pCur!!.moveToNext()) {
                        var phoneNo = pCur.getLong(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )


                        if (phoneNo>999999999) {
                            if (phoneNo.toString().length==12 && phoneNo.toString().startsWith("91")){
                                phoneNo = phoneNo.toString().substring(2, 12).toLong()
                            }
                            var a =numchecklist.add(phoneNo)
                            if (a) {
                                numlist.add(phoneNo)
                                namelist.add(name)
                                val lookupKey =
                                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                                val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
                                val fd: AssetFileDescriptor?
                                fd = context!!.getContentResolver()
                                    .openAssetFileDescriptor(uri, "r")
                                val fis = fd!!.createInputStream()
                                val buf = ByteArray(fd.getDeclaredLength().toInt())
                                fis.read(buf)
                                val vcardstring = String(buf)
                                val storage_path =
                                    Environment.getExternalStorageDirectory().toString() + filename
                                val mFileOutputStream = FileOutputStream(storage_path, true)
                                mFileOutputStream.write(vcardstring.toByteArray())
                            }
                        }
                    }
                    pCur.close()
                }

            }
        }
        if(cur!=null){
            cur.close()
        }
        val sharedPref: SharedPreferences = context.getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val editor=sharedPref.edit()
        val gson= Gson()
        var backupliststr=sharedPref.getString("backuplist","empty")
        var backuplist= LinkedList<main_frag.BackUpData>()
        if (!backupliststr.equals("empty")) {
            backuplist = gson.fromJson<LinkedList<main_frag.BackUpData>>(
                backupliststr,
                object : TypeToken<LinkedList<main_frag.BackUpData>>() {}.type
            )
        }
        backuplist.add(main_frag.BackUpData(filename, time, namelist.size))
        backupliststr=gson.toJson(backuplist)
        editor.putString("backuplist",backupliststr)
        editor.apply()

    }
}
