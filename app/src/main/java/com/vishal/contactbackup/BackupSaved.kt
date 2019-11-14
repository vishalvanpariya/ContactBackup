package com.vishal.contactbackup

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_backup_saved.*
import android.content.Intent
import android.net.Uri
import android.os.Environment
import java.io.File


class BackupSaved : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_backup_saved)

        var file=intent.getStringExtra("file")
        var size=intent.getIntExtra("size",5555)
        var t=intent.getStringExtra("time")

        back.setOnClickListener {
            onBackPressed()
        }

        time.text=t
        numofnum.text="$size Contacts"

        restore.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(
                Uri.fromFile(File("${Environment.getExternalStorageDirectory()}$file")),
                "text/x-vcard"
            )
            startActivity(intent)
        }

        share.setOnClickListener {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            val screenshotUri = Uri.parse("${Environment.getExternalStorageDirectory()}$file")
            sharingIntent.type = "*/*"
            sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri)
            startActivity(Intent.createChooser(sharingIntent, "Share image using"))
        }
    }
}
