package com.test.draw

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {

    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        canvasView = findViewById(R.id.canvas)
// hi
//        clearCanvas.setOnClickListener(ClearCanvas(canvasView) as View.OnClickListener)

    }

//    fun ClearCanvas(view: View) {
//        canvasView.ClearCanvas()
//    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when(item?.itemId) {
            R.id.erase -> canvasView.ClearCanvas()
        }

        return super.onOptionsItemSelected(item)

    }

}