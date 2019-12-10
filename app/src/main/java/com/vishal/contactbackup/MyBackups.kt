package com.vishal.contactbackup

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.activity_my_backups.*
import kotlinx.android.synthetic.main.activity_my_backups.recycler
import java.io.File
import java.util.*

class MyBackups : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_backups)

        loadrecycler()

        var back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            onBackPressed()
        }

        dustbin.setOnClickListener {
            var alertDialog=AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Are you sure you want to delete backups?")
                .setPositiveButton("YES", object :DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        var list=getbackuplist()
                        if (list!=null) {
                            for (obj in list) {
                                var file=File("${Environment.getExternalStorageDirectory()}${obj.file}")
                                file.delete()
                            }
                        }
                        val sharedPref: SharedPreferences = getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
                        val editor=sharedPref.edit()
                        editor.putString("backuplist","empty")
                        editor.apply()
                        loadrecycler()
                        p0!!.dismiss()
                    }
                }).setNegativeButton("No",object :DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0!!.dismiss()
                    }
                })

            alertDialog.show()
        }
    }

    fun loadrecycler(){
        var list =getbackuplist()
        if (list==null){
            dustbin.isEnabled=false
        }
        if (list!=null && list.size==0){
            dustbin.isEnabled=false
        }
        if (list!=null){
            recycler.visibility=View.VISIBLE
            backupmsg.visibility=View.GONE
            recycler.adapter=BackupAdepter(this,list)
            recycler.layoutManager= LinearLayoutManager(this)
        }
        else{
            recycler.visibility=View.GONE
            backupmsg.visibility=View.VISIBLE
        }
    }
    fun getbackuplist():LinkedList<main_frag.BackUpData>?{
        val sharedPref: SharedPreferences = getSharedPreferences("favorite.txt", Context.MODE_PRIVATE)
        val gson= Gson()
        var backupliststr=sharedPref.getString("backuplist","empty")
        var backuplist=LinkedList<main_frag.BackUpData>()
        if (!backupliststr.equals("empty")) {
            backuplist = gson.fromJson<LinkedList<main_frag.BackUpData>>(
                backupliststr,
                object : TypeToken<LinkedList<main_frag.BackUpData>>() {}.type
            )
            var templist=LinkedList<main_frag.BackUpData>()
            for (obj in backuplist){
                var file = File("${Environment.getExternalStorageDirectory()}${obj.file}")
                if (file.exists())
                    templist.add(obj)
            }
            if (templist.size==0)
                return null
            else
                return templist
        }
        else
            return null
    }

    class BackupAdepter(
        var context: Context?,
        var list: LinkedList<main_frag.BackUpData>
    ) : RecyclerView.Adapter<BackupAdepter.MyHolder>() {


        class MyHolder (itemView: View): RecyclerView.ViewHolder(itemView){
            lateinit var time: TextView
            lateinit var size: TextView
            init {
                time=itemView.findViewById(R.id.time)
                size=itemView.findViewById(R.id.size)
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackupAdepter.MyHolder {
            return MyHolder(LayoutInflater.from(context).inflate(R.layout.backupitem,parent,false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: BackupAdepter.MyHolder, position: Int) {
            holder.time.text=list[position].time
            holder.size.text=list[position].numofnum.toString()

            holder.itemView.setOnClickListener {
                context!!.startActivity(Intent(context,BackupSaved::class.java).putExtra("file",list[position].file).putExtra("size",list[position].numofnum).putExtra("time",list[position].time))
            }
        }

    }

}
