package com.test.draw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_create_room.*

class EnterActivity : AppCompatActivity(){
    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_room)

        val RoomText = findViewById<EditText>(R.id.EnterRoomText)
        val PasswordText = findViewById<EditText>(R.id.EnterPassword)
        val NicknameText = findViewById<EditText>(R.id.EnterNickName)
        val ThisClass = this

        //canvasView = findViewById(R.id.canvas)

//        clearCanvas.setOnClickListener(ClearCanvas(canvasView) as View.OnClickListener)

        fun Check() {
            val database : FirebaseDatabase = FirebaseDatabase.getInstance()

            database.getReference("ROOMSINFO").child(""+RoomText.text.toString()).addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.value == null){
                        Toast.makeText(ThisClass, "없는 방입니다.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    val password = p0.value as String

                    if(password.equals(PasswordText.text.toString())){
                        val intent = Intent(ThisClass, CanvasActivity::class.java)
                        intent.putExtra("ROOMNUMBER", RoomText.text.toString())
                        startActivity(intent)
                    }else{
                        Toast.makeText(ThisClass, "비밀번호를 잘못 입력하셨습니다.", Toast.LENGTH_SHORT).show()
                    }
                }

            })

            return
        }

        create_room_button.setOnClickListener{
            if(!RoomText.text.isNullOrEmpty() && !NicknameText.text.isNullOrEmpty()) {
                Check()
            }else{
                if(RoomText.text.isNullOrEmpty()) Toast.makeText(this,"방 번호를 입력하세요.",Toast.LENGTH_SHORT).show()
                else if(NicknameText.text.isNullOrEmpty()) Toast.makeText(this,"닉네임을 입력하세요.",Toast.LENGTH_SHORT).show()
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
