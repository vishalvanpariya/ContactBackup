package com.vishal.contactbackup


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.CallLog
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_contacts.view.*
import java.text.SimpleDateFormat
import java.time.Duration
import java.util.*


/**
 * A simple [Fragment] subclass.
 */
class recent : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var v=inflater.inflate(R.layout.fragment_recent, container, false)

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

        Thread(Runnable {
            activity!!.runOnUiThread(Runnable {
                var recentlist=getrecents()
                var adapter= RecentAdepter(context,recentlist)
                v.recycler.adapter=adapter
                v.recycler.layoutManager= LinearLayoutManager(context)
            })
        }).start()

        return v
    }


    fun getrecents():LinkedList<Recent>{
        val allCalls = Uri.parse("content://call_log/calls")
        val c = activity!!.managedQuery(allCalls, null, null, null, "date DESC")

        var temp=0
        var recentlist=LinkedList<Recent>()
        while (c.moveToNext()) {
            temp++
            if (temp==101)break
            val num = c.getString(c.getColumnIndex(CallLog.Calls.NUMBER))// for  number
            val name = c.getString(c.getColumnIndex(CallLog.Calls.CACHED_NAME))// for name
            val duration = c.getInt(c.getColumnIndex(CallLog.Calls.DURATION))// for duration
            val type =
                Integer.parseInt(c.getString(c.getColumnIndex(CallLog.Calls.TYPE)))// for call type, Incoming or out going.
            val date =c.getString(c.getColumnIndex(CallLog.Calls.DATE))

            recentlist.add(
                Recent(
                    num,
                    name,
                    "$duration",
                    "$type",
                    date
                )
            )
        }

        return recentlist
    }


    class Recent(val num:String,val name:String?,var duration: String,var type:String,var datemain: String){
        init {
            var time=(datemain.toLong())
            val date = Date(time)
            val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            format.timeZone = TimeZone.getTimeZone("Asia/Calcutta")
            var formatted = format.format(date)
            var month=getmonth(formatted.substring(5,7).toInt())
            var d=formatted.substring(8,10).toInt()
            var period="AM"
            if (formatted.substring(11,13).toInt()>12){
                period="PM"
                var hr="${formatted.substring(11,13).toInt()-12}"
                if (hr.toInt()<10)
                    hr="0$hr"
                formatted=formatted.replaceRange(11,13,"$hr")
            }
            datemain="$month $d ${formatted.substring(11)} $period"

            if (type.toInt()==1){
                type="Incoming"
            }
            else if (type.toInt()==2){
                type="Outgoing"
            }
            else if (type.toInt()==3){
                type="Missed"
            }

            var h=(duration.toInt()/3600)
            var m=(duration.toInt()-h*3600)/60
            var s = duration.toInt()-h*3600-m*60
            if (h==0)
                duration="${m}m ${s}s"
            else
                duration="${h}h ${m}m ${s}s"
        }

        private fun getmonth(month: Int): String {
            when(month){
                1-> return "Jan"
                2-> return "Feb"
                3-> return "Mar"
                4-> return "Apr"
                5-> return "May"
                6-> return "June"
                7-> return "July"
                8-> return "Aug"
                9-> return "Sept"
                10-> return "Oct"
                11-> return "Nov"
                12-> return "Dec"
            }
            return "Jan"
        }
    }


    class RecentAdepter(
        var context: Context?,
        var list :LinkedList<Recent>
    ) : RecyclerView.Adapter<RecentAdepter.MyHolder>() {


        class MyHolder (itemView:View): RecyclerView.ViewHolder(itemView){
            lateinit var name: TextView
            lateinit var type: TextView
            lateinit var duration: TextView
            lateinit var date: TextView
            lateinit var missedtext: TextView
            lateinit var incoming:ImageView
            lateinit var outgoing:ImageView
            lateinit var missed:ImageView
            init {
                name=itemView.findViewById(R.id.name)
                type=itemView.findViewById(R.id.type)
                duration=itemView.findViewById(R.id.duration)
                date=itemView.findViewById(R.id.date)
                incoming=itemView.findViewById(R.id.incoming)
                outgoing=itemView.findViewById(R.id.outgoing)
                missed=itemView.findViewById(R.id.missed)
                missedtext=itemView.findViewById(R.id.missedtext)
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentAdepter.MyHolder {
            return MyHolder(LayoutInflater.from(context).inflate(R.layout.recentitem,parent,false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: RecentAdepter.MyHolder, position: Int) {

            holder.itemView.setOnClickListener {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:${list[position].num}")
                context!!.startActivity(intent)
            }

            if (list[position].name==null || list[position].name.equals(""))
                holder.name.text=list[position].num
            else
                holder.name.text=list[position].name
            holder.duration.text=list[position].duration
            holder.date.text=list[position].datemain
            holder.type.text=list[position].type
            if (list[position].type.equals("Incoming")){
                holder.incoming.visibility=View.VISIBLE
                holder.outgoing.visibility=View.VISIBLE
                holder.missed.visibility=View.GONE
                holder.missedtext.visibility=View.GONE
                holder.type.visibility=View.VISIBLE
            }
            else if (list[position].type.equals("Outgoing")){
                holder.incoming.visibility=View.GONE
                holder.outgoing.visibility=View.VISIBLE
                holder.missed.visibility=View.GONE
                holder.missedtext.visibility=View.GONE
                holder.type.visibility=View.VISIBLE
            }
            else if (list[position].type.equals("Missed")){
                holder.incoming.visibility=View.GONE
                holder.outgoing.visibility=View.GONE
                holder.missed.visibility=View.VISIBLE
                holder.missedtext.visibility=View.VISIBLE
                holder.type.visibility=View.GONE
            }
        }

    }

}
