package com.vishal.contactbackup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import java.util.*

class Favorite : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        back.setOnClickListener {
            onBackPressed()
        }

        val sharedPref: SharedPreferences = getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)

        val (namelist,numlist)=getfavlist()
        if (numlist.size==0){
            emptytext.visibility=View.VISIBLE
            recycler.visibility=View.GONE
        }
        else{

            var adapter= contacts.ContactAdepter(this, namelist, numlist)
            recycler.adapter=adapter
            recycler.layoutManager= LinearLayoutManager(this)
        }
    }

    fun getfavlist():Pair<LinkedList<String>, LinkedList<Long>>{
        var namelist= LinkedList<String>()
        var numlist= LinkedList<Long>()
        val sharedPref: SharedPreferences = getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val gson= Gson()
        var namestr=sharedPref.getString("namelist","empty")
        var numstr=sharedPref.getString("numlist","empty")
        if (!namestr.equals("empty")) {
            namelist = gson.fromJson<LinkedList<String>>(namestr,
                object : TypeToken<LinkedList<String>>() {}.type)
            numlist = gson.fromJson<LinkedList<Long>>(numstr,
                object : TypeToken<LinkedList<Long>>() {}.type)
        }
        return Pair(namelist,numlist)
    }

    override fun onRestart() {
        super.onRestart()
        val (namelist,numlist)=getfavlist()
        if (numlist.size==0){
            emptytext.visibility=View.VISIBLE
            recycler.visibility=View.GONE
        }
        else{

            var adapter= contacts.ContactAdepter(this, namelist, numlist)
            recycler.adapter=adapter
            recycler.layoutManager= LinearLayoutManager(this)
        }
    }
}
