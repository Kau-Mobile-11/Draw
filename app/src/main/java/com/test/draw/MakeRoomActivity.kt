package com.test.draw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_create_room.*

class MakeRoomActivity : AppCompatActivity(){
    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val RoomText = findViewById<TextView>(R.id.MakeRoomText)
        val PasswordText = findViewById<EditText>(R.id.MakePassword)
        val NicknameText = findViewById<EditText>(R.id.MakeNickName)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        var RoomNumber = 1L;

        RoomText.text = ""

        //canvasView = findViewById(R.id.canvas)

//        clearCanvas.setOnClickListener(ClearCanvas(canvasView) as View.OnClickListener)

        database.getReference("RoomsInfo").addChildEventListener(object : ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val data = p0.value as Map<String, String>
                val temp = data["RoomNumber"]?.toLong()

                if(temp != null && RoomNumber <= temp) RoomNumber = temp + 1

                RoomText.text = "" + RoomNumber
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        create_room_button.setOnClickListener{
            if(!RoomText.text.isNullOrEmpty() && !NicknameText.text.isNullOrEmpty()) {
                database.getReference("RoomsInfo").push().setValue(mapOf("RoomNumber" to ""+RoomNumber, "Password" to PasswordText.text.toString()))
                database.getReference(RoomText.text.toString()).child("PEOPLENUMBER").setValue(0)
                val intent = Intent(this, CanvasActivity::class.java)
                intent.putExtra("ROOMNUMBER", RoomText.text.toString())
                startActivity(intent)
            }else{
                if(RoomText.text.isNullOrBlank()) Toast.makeText(this,"방 번호를 입력하세요.",Toast.LENGTH_SHORT).show()
                else if(NicknameText.text.isNullOrBlank()) Toast.makeText(this,"닉네임을 입력하세요.",Toast.LENGTH_SHORT).show()
            }
        }

    }

//    fun ClearCanvas(view: View) {
//        canvasView.ClearCanvas()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId) {
            R.id.erase -> canvasView.ClearCanvas()
        }

        return super.onOptionsItemSelected(item)

    }
}
