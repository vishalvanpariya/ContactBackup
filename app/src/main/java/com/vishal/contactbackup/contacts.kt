package com.vishal.contactbackup


import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.ContactsContract
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_contacts.*
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import java.util.*
import kotlin.collections.LinkedHashSet




/**
 * A simple [Fragment] subclass.
 */
class contacts : Fragment() {

    lateinit var v: View
    lateinit var adapter:ContactAdepter
    lateinit var namelist: LinkedList<String>
    lateinit var numlist: LinkedList<Long>

    @SuppressLint("MissingPermission")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        v=inflater.inflate(R.layout.fragment_contacts, container, false)


        Thread(Runnable {
            val (name,num)=getcontact()
            namelist=name
            numlist=num
            activity!!.runOnUiThread(Runnable {
                adapter=ContactAdepter(context,namelist,numlist)
                v.recycler.adapter=adapter
                v.recycler.layoutManager=LinearLayoutManager(context)
                var numofnum=v.findViewById<TextView>(R.id.numofnum)
                numofnum.text="Contacts(${namelist.size})"
            })
        }).start()

        var back = v.findViewById<ImageView>(R.id.back)
        back.setOnClickListener {
            var alertDialog= AlertDialog.Builder(context!!)
                .setTitle("Exit")
                .setMessage("Do you want to Exit?")
                .setPositiveButton("Yes",object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0!!.dismiss()
                        activity!!.finish()
                    }
                }).setNegativeButton("No",object : DialogInterface.OnClickListener{
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        p0!!.dismiss()
                    }
                })
            alertDialog.show()
        }

        return v
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)





        var fav =v.findViewById<ImageView>(R.id.fav)
        fav.setOnClickListener {
            startActivity(Intent(activity,Favorite::class.java))
        }

        var refresh=v.findViewById<ImageView>(R.id.refresh)
        refresh.setOnClickListener {
            var dialog=ProgressDialog(activity)
            dialog.setTitle("")
            dialog.setMessage("Loading...")
            dialog.setCancelable(false)
            dialog.show()

            val h = Handler()
            h.postDelayed(object : Runnable {
                override fun run() {
                    dialog.dismiss()
                }
            }, 3000)
        }

        var searchView=v.findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(text: String?): Boolean {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onQueryTextChange(text: String?): Boolean {
                var (name,num) =filterlist(text.toString(),namelist,numlist)
                adapter.updatelist(name,num)
                return true
            }
        })

    }

    fun filterlist(
        text:String,
        namelist:LinkedList<String>,
        numlist:LinkedList<Long>
    )
            :Pair<LinkedList<String>,LinkedList<Long>>{

        var namelist1=LinkedList<String>()
        var numlist1=LinkedList<Long>()
        for (d in namelist) {
            var temp=d.toLowerCase()
           if (temp.contains(text.toLowerCase())){
               numlist1.add(numlist[namelist.indexOf(d)])
               namelist1.add(d)
           }
        }
        return Pair(namelist1,numlist1)
    }


    fun getcontact():Pair<LinkedList<String>,LinkedList<Long>>{
        val cr = activity!!.getContentResolver()
        val cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC")

        var namelist=LinkedList<String>()
        var numchecklist=LinkedHashSet<Long>()
        var numlist=LinkedList<Long>()

        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {

                val lookupKey =
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_VCARD_URI, lookupKey)
                Log.d("xxxx lookup","${uri}")

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
                            }


                        }
                    }
                    pCur.close()
                }
            }
        }
        if(cur!=null){
            cur.close();
        }

        return Pair(namelist,numlist)
    }


    class ContactAdepter(
        var context: Context?,
        var namelist: LinkedList<String>,
        var numlist:LinkedList<Long>
    ) : RecyclerView.Adapter<ContactAdepter.MyHolder>() {


        class MyHolder (itemView:View):RecyclerView.ViewHolder(itemView){
            lateinit var name:TextView
            lateinit var num:TextView
            init {
                name=itemView.findViewById(R.id.name)
                num=itemView.findViewById(R.id.number)
            }
        }

        fun updatelist(name:LinkedList<String>,num:LinkedList<Long>){
            namelist=name
            numlist=num
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactAdepter.MyHolder {
            return MyHolder(LayoutInflater.from(context).inflate(R.layout.contactitem,parent,false))
        }

        override fun getItemCount(): Int {
            return namelist.size
        }

        override fun onBindViewHolder(holder: ContactAdepter.MyHolder, position: Int) {
            holder.num.setText("${numlist[position]}")
            holder.name.setText(namelist[position])
            holder.itemView.setOnClickListener {
                var intent=Intent(context,Details::class.java)
                intent.putExtra("name",namelist[position])
                intent.putExtra("number","${numlist[position]}")
                context!!.startActivity(intent)
            }
        }

    }


}
