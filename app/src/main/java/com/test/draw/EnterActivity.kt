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
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
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

            database.getReference("RoomsInfo").addChildEventListener(object : ChildEventListener{
                override fun onCancelled(p0: DatabaseError) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                    val data = p0.value as Map<String, String>
                    val RoomNumber = data["RoomNumber"]
                    val str = data["Password"]
                    if(RoomText.text.toString().equals(RoomNumber) && PasswordText.text.toString().equals(str)){
                        val intent = Intent(ThisClass, CanvasActivity::class.java)
                        intent.putExtra("ROOMNUMBER", RoomText.text.toString())
                        startActivity(intent)
                    }
                }

                override fun onChildRemoved(p0: DataSnapshot) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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
