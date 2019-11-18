package com.test.draw

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_canvas.*
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.UriMatcher
import android.net.Uri
import android.nfc.Tag
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_canvas.view.*
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*


class CanvasActivity : AppCompatActivity() {

    private var filePath : Uri? = null
    val storage : FirebaseStorage = FirebaseStorage.getInstance()
    var filename : String = ""

    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)

        canvasView = findViewById(R.id.canvas)

        if(!intent.getStringExtra("ROOMNUMBER").isNullOrBlank()) {
            canvasView.RoomNumber = intent.getStringExtra("ROOMNUMBER")
            Toast.makeText(this,intent.getStringExtra("ROOMNUMBER")+"번 방 입장",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"실패",Toast.LENGTH_SHORT).show()
            return
        }


        //image_button click
        image_button.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permissions, PERMISSION_CODE)
                }
                else{
                    pickImageFromGallery()
                }
            }
            else{
                pickImageFromGallery()
            }
        }

//        clearCanvas.setOnClickListener(ClearCanvas(canvasView) as View.OnClickListener)

    }

    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    companion object{
        private val IMAGE_PICK_CODE = 1000
        private val PERMISSION_CODE = 1001
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
                else{
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){
            if (data != null) {
                filePath = data.data
            }
            uploadFile()

            image_view.setImageURI(data?.data)
        }
    }

    private fun uploadFile(){
        if (filePath != null){

            val formatter : SimpleDateFormat = SimpleDateFormat("yyyyMMHH_mmss")
            val now : Date = Date()

            filename = formatter.format(now)
            val storageRef : StorageReference = storage.getReferenceFromUrl("gs://fir-c771c.appspot.com/").child(filename)
            storageRef.putFile(filePath!!)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot>() { downloadFile() })
        }
    }
    private fun downloadFile(){


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