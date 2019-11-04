package com.test.draw

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_room.*

class EnterActivity : AppCompatActivity(){
    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_room)

        //canvasView = findViewById(R.id.canvas)

//        clearCanvas.setOnClickListener(ClearCanvas(canvasView) as View.OnClickListener)

        create_room_button.setOnClickListener{
            if(!findViewById<EditText>(R.id.RoomText).text.isNullOrEmpty()) {
                val intent = Intent(this, CanvasActivity::class.java)
                intent.putExtra("ROOMNUMBER", findViewById<EditText>(R.id.RoomText).text.toString())
                startActivity(intent)
            }else{
                Toast.makeText(this,"방 번호를 입력하세요",Toast.LENGTH_SHORT).show()
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
