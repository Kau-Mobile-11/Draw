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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.net.Uri
import android.nfc.Tag
import android.renderscript.Sampler
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
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
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()  // firebase db의 인스턴스를 가져옴
    val childlistener = object : ChildEventListener{
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
            try {
                var value = p0.value as Map<String, String>;

                if (value.getValue("FIN").equals("T")) {
                    canvasView.upTouchEvent(Integer.parseInt(value.getValue("NUM")))
                    canvasView.invalidate()
                } else if (value.getValue("START").equals("T")) {
                    canvasView.onStartTouchEvent(
                        value.getValue("X").toFloat(),
                        value.getValue("Y").toFloat(),
                        Integer.parseInt(value.getValue("NUM"))
                    )
                    canvasView.invalidate()
                } else {
                    canvasView.onMoveTouchEvent(
                        value.getValue("X").toFloat(),
                        value.getValue("Y").toFloat(),
                        Integer.parseInt(value.getValue("NUM"))
                    )
                    canvasView.invalidate()
                }
            }catch(e : Exception){
            }
        }

        override fun onChildRemoved(p0: DataSnapshot) {
            canvasView.removeAll()
            canvasView.invalidate()
        }
    }

    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canvas)
        val RoomNumber = intent.getStringExtra("ROOMNUMBER")

        canvasView = findViewById(R.id.canvas)

        database.getReference(RoomNumber).child("PEOPLENUMBER").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.value as Long

                canvasView.myNum = "" + (value.toInt() + 1)

                database.getReference(RoomNumber).child("PEOPLENUMBER").setValue(value + 1)

                canvasView.mPath.add(Path())
                canvasView.mX.add(0.toFloat())
                canvasView.mY.add(0.toFloat())
                canvasView.Finished.add(false)
                for(i : Int in 1..value.toInt()){
                    database.getReference(RoomNumber).child("PATHS").child(""+i).addChildEventListener(childlistener)
                    canvasView.mPath.add(Path())
                    canvasView.mX.add(0.toFloat())
                    canvasView.mY.add(0.toFloat())
                    canvasView.Finished.add(false)
                }
            }
        })

        database.getReference(RoomNumber).child("PEOPLENUMBER").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.value as Long

                database.getReference(RoomNumber).child("PATHS").child("" + value.toInt()).addChildEventListener(childlistener)
                canvasView.mPath.add(Path())
                canvasView.mX.add(0.toFloat())
                canvasView.mY.add(0.toFloat())
                canvasView.Finished.add(false)
            }

        })

        database.getReference(RoomNumber).child("imageName").addChildEventListener(object : ChildEventListener{
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
                filename = p0.value as String
                Log.i("filename",filename)
                if(filename != "") {
                    downloadFile()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
                image_view.setImageResource(0)
            }

        })

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

        image_clear.setOnClickListener{
            database.getReference(RoomNumber).child("imageName").removeValue()
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

            //image_view.setImageURI(data?.data)
        }
    }

    private fun uploadFile(){
        if (filePath != null){

            val formatter : SimpleDateFormat = SimpleDateFormat("yyyyMMHH_mmss")
            val now : Date = Date()

            filename = formatter.format(now) + ".png"
            val storageRef : StorageReference = storage.getReferenceFromUrl("gs://fir-c771c.appspot.com/").child("images/" + filename)
            storageRef.putFile(filePath!!)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot>() { database.getReference(canvasView.RoomNumber).child("imageName").push().setValue(filename)})
        }
    }
    private fun downloadFile(){
        val storageRef : StorageReference = storage.reference.child("images").child(filename)
        var ONE_MEGABYTE: Long = 1024 * 1024
        storageRef?.getBytes(ONE_MEGABYTE).addOnCompleteListener {
            image_view.setImageBitmap(byteArrayToBitmap(it.result!!))
        }
        //GlideApp.with(this).load(storageRef).into(image_view)
    }
    private fun byteArrayToBitmap(byteArry: ByteArray): Bitmap {
        var bitmap:Bitmap?=null
        bitmap = BitmapFactory.decodeByteArray(byteArry,0,byteArry.size)
        return bitmap
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