package com.vishal.contactbackup

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.provider.ContactsContract
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val transaction =supportFragmentManager.beginTransaction()
        transaction.add(R.id.frag, main_frag())
        transaction.commit()


        mybackupicon.setImageResource(R.drawable.backupblue)
        mybackuptext.setTextColor(resources.getColor(R.color.colorPrimary))


        mybackupicon.setOnClickListener {

            val transaction =supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frag, main_frag())
            transaction.commit()

            mybackupicon.setImageResource(R.drawable.backupblue)
            mybackuptext.setTextColor(resources.getColor(R.color.colorPrimary))

            contactsicon.setImageResource(R.drawable.contacs)
            contactstext.setTextColor(resources.getColor(android.R.color.black))

            recenticon.setImageResource(R.drawable.recent)
            recenttext.setTextColor(resources.getColor(android.R.color.black))
        }

        recenticon.setOnClickListener {

            val transaction =supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frag, recent())
            transaction.commit()

            recenticon.setImageResource(R.drawable.recentblue)
            recenttext.setTextColor(resources.getColor(R.color.colorPrimary))

            contactsicon.setImageResource(R.drawable.contacs)
            contactstext.setTextColor(resources.getColor(android.R.color.black))

            mybackupicon.setImageResource(R.drawable.backup)
            mybackuptext.setTextColor(resources.getColor(android.R.color.black))
        }

        contactsicon.setOnClickListener {

            var dialog= ProgressDialog(this)
            dialog.setTitle("")
            dialog.setMessage("Loading...")
            dialog.show()

            val h = Handler()
            h.postDelayed(object : Runnable {
                override fun run() {
                    dialog.dismiss()
                }
            }, 3000)

            contactsicon.setImageResource(R.drawable.contacsblue)
            contactstext.setTextColor(resources.getColor(R.color.colorPrimary))

            mybackupicon.setImageResource(R.drawable.backup)
            mybackuptext.setTextColor(resources.getColor(android.R.color.black))

            recenticon.setImageResource(R.drawable.recent)
            recenttext.setTextColor(resources.getColor(android.R.color.black))


            val transaction =supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frag, contacts())
            transaction.commit()

        }

        keypadicon.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:")
            startActivity(intent)
        }

        newicon.setOnClickListener {
            val intent = Intent(
                ContactsContract.Intents.SHOW_OR_CREATE_CONTACT,
                Uri.parse("tel: ")
            )
            intent.putExtra(ContactsContract.Intents.EXTRA_FORCE_CREATE, true)
            startActivity(intent)
        }
    }


}
