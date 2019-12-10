package com.vishal.contactbackup


import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import java.util.*
import kotlin.collections.LinkedHashSet
import android.content.res.AssetFileDescriptor
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.suke.widget.SwitchButton
import kotlinx.android.synthetic.main.fragment_main_frag.*
import kotlinx.android.synthetic.main.fragment_main_frag.view.*
import java.io.*
import java.lang.Exception
import java.text.SimpleDateFormat
import kotlin.random.Random


/**
 * A simple [Fragment] subclass.
 */
class main_frag : Fragment() {
    lateinit var dialog:ProgressDialog
    lateinit var v:View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v= inflater.inflate(R.layout.fragment_main_frag, container, false)

        val sharedPref: SharedPreferences = context!!.getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)


        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.LOLLIPOP) {
            if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context!!,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context!!,Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context!!,Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_CALL_LOG
                    ), 200
                )
            }
        }

        var back = v.findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            var alertDialog= AlertDialog.Builder(context!!)
                .setTitle("Exit")
                .setMessage("Do you want to Exit?")
                .setPositiveButton("Yes",object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0!!.dismiss()
                        activity!!.finish()
                    }
                }).setNegativeButton("No",object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0!!.dismiss()
                    }
                })
            alertDialog.show()
        }

        var fav =v.findViewById<ImageView>(R.id.fav)
        fav.setOnClickListener {
            startActivity(Intent(activity,Favorite::class.java))
        }

        var switch=v.findViewById<SwitchButton>(R.id.toggle)
        switch.isChecked=sharedPref.getBoolean("auto",false)
        switch.setOnCheckedChangeListener(object :SwitchButton.OnCheckedChangeListener{
            override fun onCheckedChanged(view: SwitchButton?, isChecked: Boolean) {
                if (isChecked)
                    setautobackup()
                else{
                    stopauto()
                }

            }
        })

        var mainbackupimage=v.findViewById<ImageView>(R.id.mainbackupimage)
        mainbackupimage.setOnClickListener {
            dialog=ProgressDialog(activity)
            dialog.setTitle("")
            dialog.setMessage("Loading...")
            dialog.setCancelable(false)
            dialog.show()
            contactbackup()
        }

        v.mybackupcard.setOnClickListener {
            context!!.startActivity(Intent(context,MyBackups::class.java))
        }

        displaytime()



        return v
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode==200){
            if (grantResults[0]!=PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context,"Sorry without permission you can not use this app", Toast.LENGTH_SHORT).show()
                var alertDialog= AlertDialog.Builder(context!!)
                    .setTitle("")
                    .setMessage("Sorry without permission you can not use this app")
                    .setPositiveButton("Ok",object :DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            p0!!.dismiss()
                            activity!!.finish()
                        }
                    })
                alertDialog.show()
            }
        }
    }

    private fun displaytime() {
        val sharedPref: SharedPreferences = activity!!.getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val editor=sharedPref.edit()
        val gson= Gson()
        var backupliststr=sharedPref.getString("backuplist","empty")
        var backuplist=LinkedList<BackUpData>()
        if (!backupliststr.equals("empty")) {
            backuplist = gson.fromJson<LinkedList<BackUpData>>(
                backupliststr,
                object : TypeToken<LinkedList<BackUpData>>() {}.type
            )
            var mainHandler = Handler(context!!.getMainLooper())
            var runnable= Runnable {
                kotlin.run {
                    var file=File("${Environment.getExternalStorageDirectory()}${backuplist.last.file}")
                    if (file.exists()) {
                        v.backuptime.text =
                            "Keep your contacts in safe area\nLast backup : ${backuplist.last.time}"
                        v.backuptime.visibility = View.VISIBLE
                    }
                }
            }
            mainHandler.post(runnable)

        }
    }


    fun setautobackup(){
        val sharedPref: SharedPreferences = context!!.getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val editor=sharedPref.edit()
        editor.putBoolean("auto",true)
        editor.apply()
        var intent=Intent(activity,AutoBackup_Receiver::class.java)
        var pendingintent= PendingIntent.getBroadcast(activity,101,intent,0)
        var alarmManager=activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+1800000,pendingintent)
    }

    fun stopauto(){
        val sharedPref: SharedPreferences = context!!.getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val editor=sharedPref.edit()
        editor.putBoolean("auto",false)
        editor.apply()
        var intent=Intent(activity,AutoBackup_Receiver::class.java)
        var pendingintent= PendingIntent.getBroadcast(activity,101,intent, 0)
        var alarmManager=activity!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingintent)
    }

    fun contactbackup(){

        var thread=Thread(){
            kotlin.run {

                var folder = File("${Environment.getExternalStorageDirectory()}/Contact Backup")
                if (!folder.exists())
                    folder.mkdir()
                var timelong=System.currentTimeMillis()
                var filename="/Contact Backup/Contacts_${timelong}.vcf"
                val date = Date(timelong)
                val format = SimpleDateFormat("HH:mm:ss dd-MM-yyyy")
                format.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
                var time = format.format(date)




                val cr = activity!!.getContentResolver()
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
                                        val lookupKey =cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                                        val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
                                        var fd :AssetFileDescriptor
                                        var fis:FileInputStream
                                        var buf:ByteArray
                                        Log.d("xxxx","${uri}")
                                        fd= activity!!.applicationContext.contentResolver.openAssetFileDescriptor(uri, "r")!!
                                        fis = fd.createInputStream()
//                                        buf = ByteArray(fd.getDeclaredLength().toInt())
//                                        fd.close()
//                                        fis.read(buf)
                                        buf=readfile(fis)
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
                val sharedPref: SharedPreferences = activity!!.getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
                val editor=sharedPref.edit()
                val gson= Gson()
                var backupliststr=sharedPref.getString("backuplist","empty")
                var backuplist=LinkedList<BackUpData>()
                if (!backupliststr.equals("empty")) {
                    backuplist = gson.fromJson<LinkedList<BackUpData>>(
                        backupliststr,
                        object : TypeToken<LinkedList<BackUpData>>() {}.type
                    )
                }
                backuplist.add(BackUpData(filename,time,namelist.size))
                backupliststr=gson.toJson(backuplist)
                editor.putString("backuplist",backupliststr)
                editor.apply()
                dialog.dismiss()
                displaytime()
            }
        }
        thread.start()

    }

    class BackUpData(var file:String,var time:String,var numofnum:Int)

    @Throws(IOException::class)
    fun readfile(file: FileInputStream): ByteArray {
        var ous: ByteArrayOutputStream? = null
        var ios: InputStream? = null
        try {
            val buffer = ByteArray(4096)
            ous = ByteArrayOutputStream()
            ios = file
            var read = 0
            read = ios.read(buffer)
            while (read != -1) {
                ous.write(buffer, 0, read)
                read=ios.read(buffer)
            }
        } finally {
            try {
                ous?.close()
            } catch (e: IOException) {
            }

            try {
                ios?.close()
            } catch (e: IOException) {
            }

        }
        return ous!!.toByteArray()
    }


}
