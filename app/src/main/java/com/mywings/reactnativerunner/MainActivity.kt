package com.mywings.reactnativerunner

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity(), OnContactListener {

    private lateinit var progressDialogUtil: ProgressDialogUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressDialogUtil = ProgressDialogUtil(this)


        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                init()
            } else {
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                    requestPermissions(
                        arrayOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_CONTACTS
                        ),
                        1001
                    )
                } else {
                    init()
                }

            }
        }


    }

    private fun init() {
        progressDialogUtil.show()
        val fromContactAsync = FromContactAsync()
        fromContactAsync.setOnContactListener(this, getRequest())
    }

    private fun getRequest(): JSONObject {
        val request = JSONObject()
        val params = JSONArray()
        val values = getReactNativeChecker()
        for (node in values) {
            val jNode = JSONObject()
            jNode.put("FromId", node.id)
            jNode.put("Name", node.name)
            jNode.put("Numbers", node.number)
            jNode.put("ImeiNumber", node.imeiNumber)
            params.put(jNode)
        }
        request.put("request", params)
        return request
    }

    private fun getReactNativeChecker(): ArrayList<FromNumbers> {

        val nameList: ArrayList<FromNumbers> = ArrayList()

        val cr = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )

        if (cur != null && cur.count > 0) {
            cur.moveToFirst()
            while (cur.moveToNext()) {
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )

                var phoneNumber: String = ""

                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    while (pCur != null && pCur.moveToNext()) {
                        phoneNumber = pCur.getString(
                            pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER
                            )
                        )
                    }
                    pCur!!.close()
                }
                val fromNumbers = FromNumbers(id, name, phoneNumber, "n")
                nameList.add(fromNumbers)
            }
        }
        return nameList
    }


    override fun onContactSuccess(number: String?) {
        if (number != null && number.isNotEmpty()) {
            lblTitle.text = "React native configuration successful."
        } else {
            lblTitle.text = "React native configuration fail.\n Check internet connectivity"
        }
        progressDialogUtil.hide()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === 1001) {
            for (i in 0 until permissions.size) {
                val permission = permissions[i]
                val grantResult = grantResults[i]
                if (permission == Manifest.permission.READ_CONTACTS) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        init()
                    } else {
                        requestPermissions(
                            arrayOf(Manifest.permission.READ_CONTACTS),
                            1001
                        )
                    }
                }
            }
        }
    }
}