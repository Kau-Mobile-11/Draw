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
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_create_room.*

class MakeRoomActivity : AppCompatActivity(){
    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        val RoomText = findViewById<TextView>(R.id.MakeRoomText)
        val PasswordText = findViewById<EditText>(R.id.MakePassword)
        //val NicknameText = findViewById<EditText>(R.id.MakeNickName)
        val database : FirebaseDatabase = FirebaseDatabase.getInstance()
        var RoomNumber = 1L;

        RoomText.text = ""

        //canvasView = findViewById(R.id.canvas)

//        clearCanvas.setOnClickListener(ClearCanvas(canvasView) as View.OnClickListener)

        database.getReference("ROOMSNUMBER").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.value == null) RoomNumber = 1
                else RoomNumber = p0.value as Long + 1

                RoomText.text = RoomNumber.toString()

                database.getReference("ROOMSNUMBER").setValue(RoomNumber)
            }

        })

        create_room_button.setOnClickListener{
            if(!RoomText.text.isNullOrEmpty()) {
                database.getReference("ROOMSINFO").child("" + RoomNumber).setValue(PasswordText.text.toString())
                database.getReference(RoomText.text.toString()).child("PEOPLENUMBER").setValue(0)
                database.getReference(RoomText.text.toString()).child("imageName").setValue("")
                val intent = Intent(this, CanvasActivity::class.java)
                intent.putExtra("ROOMNUMBER", RoomText.text.toString())
                startActivity(intent)
            }else{
                if(RoomText.text.isNullOrBlank()) Toast.makeText(this,"방 번호를 입력하세요.",Toast.LENGTH_SHORT).show()
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
