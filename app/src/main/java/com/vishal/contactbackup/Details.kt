package com.vishal.contactbackup

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.contactitem.name
import kotlinx.android.synthetic.main.contactitem.number
import java.util.*
import android.provider.ContactsContract.PhoneLookup
import android.net.Uri
import android.provider.ContactsContract
import android.content.ContentUris
import android.content.Intent








class Details : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        var num=intent.getStringExtra("number").toLong()
        var gson=Gson()
        var flag=false
        val sharedPref: SharedPreferences = getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val editor=sharedPref.edit()
        name.text=intent.getStringExtra("name")
        number.text=intent.getStringExtra("number")

        back.setOnClickListener {
            onBackPressed()
        }

        val (namelist,numlist)=getfavlist()
        if (numlist.contains(intent.getStringExtra("number").toLong())){
            flag=true
            fav.setImageResource(R.drawable.fullfavicon)
        }

        pen.setOnClickListener {
            val intent = Intent(Intent.ACTION_EDIT)
            var id=getcontctId(num)
            if (id!=0.toLong() && id !=null)  {
                intent.data = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id)
                startActivity(intent)
            }
        }

        msg.setOnClickListener {
            val number = "$num"
            startActivity(Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", number, null)))
        }
        card.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$num")
            startActivity(intent)
        }

        fav.setOnClickListener {
            if (flag){
                fav.setImageResource(R.drawable.favblueicon)
                flag=false
                val (namelist,numlist)=getfavlist()
                if (namelist.indexOf(intent.getStringExtra("name"))>-1){
                    var index=namelist.indexOf(intent.getStringExtra("name"))
                    namelist.removeAt(index)
                    numlist.removeAt(index)
                    var namestr=gson.toJson(namelist)
                    var numstr=gson.toJson(numlist)
                    editor.putString("namelist",namestr)
                    editor.putString("numlist",numstr)
                    editor.apply()
                }
            }
            else {
                fav.setImageResource(R.drawable.fullfavicon)
                flag=true
                val (namelist,numlist)=getfavlist()
                if (!numlist.contains(intent.getStringExtra("number").toLong())) {
                    namelist.add(intent.getStringExtra("name"))
                    numlist.add(intent.getStringExtra("number").toLong())
                    var namestr = gson.toJson(namelist)
                    var numstr = gson.toJson(numlist)
                    editor.putString("namelist", namestr)
                    editor.putString("numlist", numstr)
                    editor.apply()
                }
            }
        }
    }

    fun getfavlist():Pair<LinkedList<String>,LinkedList<Long>>{
        var namelist=LinkedList<String>()
        var numlist=LinkedList<Long>()
        val sharedPref: SharedPreferences = getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val gson=Gson()
        var namestr=sharedPref.getString("namelist","empty")
        var numstr=sharedPref.getString("numlist","empty")
        if (namestr!=null && !namestr.equals("empty")) {
            namelist = gson.fromJson<LinkedList<String>>(namestr,
                object : TypeToken<LinkedList<String>>() {}.type)
            numlist = gson.fromJson<LinkedList<Long>>(numstr,
                object : TypeToken<LinkedList<Long>>() {}.type)
        }
        return Pair(namelist,numlist)
    }

    fun getcontctId(phone:Long):Long{
        val lookupUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode("$phone"))
        val mcursor = contentResolver.query(
            lookupUri,
            arrayOf(PhoneLookup._ID),
            null,
            null,
            null
        )
        var idPhone: Long = 0
        try {
            if (mcursor != null) {
                if (mcursor.moveToFirst()) {
                    idPhone =mcursor.getString(mcursor.getColumnIndex(PhoneLookup._ID)).toLong()
                }
            }
        } finally {
            mcursor!!.close()
        }
        return idPhone
    }
}
