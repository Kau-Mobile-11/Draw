package com.test.draw

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Path
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_canvas.*
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CanvasActivity : AppCompatActivity(){
    /////녹화
    private val TAG : String = "MainActivity"
    private val REQUEST_CODE : Int= 1002
    private var mScreenDensity: Int = 0
    private var mProjectionManager: MediaProjectionManager? = null
    private val DISPLAY_WIDTH : Int = 720
    private val DISPLAY_HEIGHT : Int = 1280
    private var mMediaProjection: MediaProjection? = null
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaProjectionCallback: MediaProjectionCallback? = null
    private var mMediaRecorder: MediaRecorder? = null
    private val REQUEST_PERMISSION_KEY : Int = 1
    private val thisContext = this
    internal var isRecording = false
    var recordItem : MenuItem? = null
    /////

    var RoomNumber : String = ""
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
                var value = p0.value as Map<String, String>

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
        }
    }

    var PointListener = object : ValueEventListener{
        override fun onCancelled(p0: DatabaseError) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun onDataChange(p0: DataSnapshot) {
            try {
                val value = p0.value as Map<String, String>
                val x = value.getValue("X").toFloat()
                val y = value.getValue("Y").toFloat()
                val pointerNumber = Integer.parseInt(value.getValue("NUM"))

                canvasView.setPointer(pointerNumber, x, y)
            }catch(e : Exception){}
        }
    }
    
    lateinit var canvasView: CanvasView

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = ( View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        //네비게이션바, 상단바 숨김
        setContentView(R.layout.activity_canvas)

        RoomNumber = intent.getStringExtra("ROOMNUMBER")

        canvasView = findViewById(R.id.canvas)

        database.getReference(RoomNumber).child("PEOPLENUMBER").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.value as Long

                canvasView.lineNum = "" + value.toInt()

                for(i : Int in 0..value.toInt()){
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

                canvasView.lineNum = "" + value.toInt()
                database.getReference(RoomNumber).child("PATHS").child("" + value.toInt()).addChildEventListener(childlistener)
                canvasView.mPath.add(Path())
                canvasView.mX.add(0.toFloat())
                canvasView.mY.add(0.toFloat())
                canvasView.Finished.add(false)
            }

        })

        database.getReference(RoomNumber).child("POINTERNUM").addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                try {
                    val value = p0.value as Long

                    canvasView.PointerNum = "" + value

                    for (i in 0..(value-1)) {
                        database.getReference(RoomNumber).child("POINTERS").child("" + (i - 1)).addValueEventListener(PointListener)
                        canvas.mPointer.add(Path())
                        canvasView.PointerX.add(0.toFloat())
                        canvasView.PointerY.add(0.toFloat())
                        canvasView.PointerVisible.add(true)
                    }
                }catch(e : Exception){}
            }

        })

        database.getReference(RoomNumber).child("POINTERNUM").addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                try {
                    val value = p0.value as Long

                    canvasView.PointerNum = "" + value

                    database.getReference(RoomNumber).child("POINTERS").child("" + (value - 1))
                        .addValueEventListener(PointListener)
                    canvas.mPointer.add(Path())
                    canvasView.PointerX.add(0.toFloat())
                    canvasView.PointerY.add(0.toFloat())
                    canvasView.PointerVisible.add(true)
                }catch(e : Exception){}
            }
        })

        database.getReference(RoomNumber).child("imageName").addChildEventListener(object : ChildEventListener{     //DB에 업로드할 이미지의 이름을 추가
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                filename = p0.value as String
                Log.i("filename",filename)
                if(filename != "") {
                    downloadFile()
                }
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                filename = p0.value as String
                Log.i("filename",filename)
                if(filename != "") {
                    downloadFile()
                }
            }
            //DB에 이미지의 이름이 추가되거나 바뀌면 downloadFile 함수 호출
            override fun onChildRemoved(p0: DataSnapshot) {
                image_view.setImageResource(0)
            }

        })

        database.getReference(RoomNumber).child("ERASE").addChildEventListener(object : ChildEventListener{
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
                val option = p0.value as Long

                if(option == -1L){
                    canvasView.removeAll()
                }else if(option >= 10000000) {
                    for(i in 10000000..option){
                        canvasView.clearPointer((i - 10000000).toInt())
                        database.getReference(RoomNumber).child("POINTERS").child((i - 10000000).toString()).removeValue()
                    }
                }else {
                    canvasView.ErasePath(option.toInt())
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })


        if(!intent.getStringExtra("ROOMNUMBER").isNullOrBlank()) {
            canvasView.RoomNumber = intent.getStringExtra("ROOMNUMBER")
            Toast.makeText(this,intent.getStringExtra("ROOMNUMBER")+"번 방 입장",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this,"실패",Toast.LENGTH_SHORT).show()
            return
        }

        ////녹화
        val PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
        )
        if (!Function().hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_KEY)
        }
        //권한 확인
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        mScreenDensity = metrics.densityDpi

        mMediaRecorder = MediaRecorder()

        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            //getSystemService를 통해 서비스를 생성
        ////
    }


    ///////// 녹화
    private fun onToggleScreenShare() {
        if(!isRecording){
            mMediaRecorder?.reset()
            initRecorder()
            shareScreen()
        }else{
            mMediaRecorder?.stop()
            mMediaRecorder?.reset()
            stopScreenSharing()
        }
    }
    //녹화를 위한 세팅
    private fun initRecorder(){
        try{
            val formatter : SimpleDateFormat = SimpleDateFormat("yyyyMMHH_mmss")
            val now : Date = Date()
            var Videoname = formatter.format(now) + ".mp4"

            mMediaRecorder?.apply{
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile("${externalCacheDir.absolutePath}"+Videoname)
                setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncodingBitRate(512 * 1000)
                setVideoFrameRate(16) // 30
                setVideoEncodingBitRate(3000000)
            }
            mMediaRecorder?.prepare()   //위 set들이 모두 정상적으로 성공하면 녹화 준비
        }catch (e : IOException){
            e.printStackTrace()
        }
    }
    private fun shareScreen(){
        if(mMediaProjection == null){
            startActivityForResult(mProjectionManager?.createScreenCaptureIntent(), REQUEST_CODE)
            return
        }//권한요청
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder?.start() //녹화 시작
        isRecording = true
        recordItem?.title = "녹화 중지"
        Toast.makeText(thisContext,"녹화를 시작합니다.", Toast.LENGTH_SHORT).show()
    }
    private fun createVirtualDisplay(): VirtualDisplay? {
        return mMediaProjection?.createVirtualDisplay(
            "MainActivity", DISPLAY_WIDTH, DISPLAY_HEIGHT, mScreenDensity,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mMediaRecorder?.surface, null, null
        )
    }//화면 녹화를 위해 surface를 받아와 virtualDisplay 생성
    private fun stopScreenSharing(){
        if (mVirtualDisplay == null){
            return
        }
        mVirtualDisplay!!.release() //객체와 관련된 리소스 해제
        destroyMediaProjection()
        isRecording = false
        recordItem?.title = "녹화 시작"
        Toast.makeText(thisContext,"녹화를 중지합니다.", Toast.LENGTH_SHORT).show()
    }
    private fun destroyMediaProjection(){   //media projection 해제, 정지, null
        if(mMediaProjection != null){
            mMediaProjection!!.unregisterCallback(mMediaProjectionCallback) //해제
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
        Log.i(TAG, "MediaProjection Stopped")
    }
    //media projection 세션이 더 이상 유효하지 않을 때 onStop() 호출
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            if (isRecording) {
                isRecording = false
                recordItem?.title = "녹화 시작"
                Toast.makeText(thisContext,"녹화를 중지합니다.", Toast.LENGTH_SHORT).show()
                mMediaRecorder?.stop()
                mMediaRecorder?.reset()
            }
            mMediaProjection = null
            stopScreenSharing()
        }
    }
    override fun onDestroy(){
        super.onDestroy()
        destroyMediaProjection()
    }
    //녹화 도중 뒤로가기를 하면 녹화 중지를 한다
    override fun onBackPressed() {
        if (isRecording){
            Snackbar.make(findViewById(android.R.id.content), "녹화를 중지하고 나가시겠습니까?",
                Snackbar.LENGTH_INDEFINITE).setAction("중지") {
                mMediaRecorder?.stop()
                mMediaRecorder?.reset()
                Log.v(TAG, "Stopping Recording")
                stopScreenSharing()
                finish()
            }.show()
        }else{
            finish()
        }
    }
    /////////


    ///////// 사진 첨부
    private fun pickImageFromGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }
    //static
    companion object{
        private const val IMAGE_PICK_CODE = 1000
        private const val PERMISSION_CODE = 1001
    }
    private fun uploadFile(){
        if (filePath != null){
            val formatter : SimpleDateFormat = SimpleDateFormat("yyyyMMHH_mmss")
            val now : Date = Date()
            filename = formatter.format(now) + ".png"
            //업로드할 사진의 고유 이름 생성
            val storageRef : StorageReference = storage.getReferenceFromUrl("gs://fir-c771c.appspot.com/").child("images/" + filename)
            storageRef.putFile(filePath!!)
                .addOnSuccessListener(OnSuccessListener<UploadTask.TaskSnapshot> { database.getReference(canvasView.RoomNumber).child("imageName").child("image").setValue(filename)})
                .addOnProgressListener(OnProgressListener { Toast.makeText(thisContext, "사진을 업로드 중입니다.", Toast.LENGTH_SHORT).show() })
        }
    }
    private fun downloadFile(){
        val storageRef : StorageReference = storage.reference.child("images").child(filename)
        var ONE_MEGABYTE: Long = 1024 * 1024
        storageRef.getBytes(ONE_MEGABYTE).addOnCompleteListener {
            image_view.setImageBitmap(byteArrayToBitmap(it.result!!))       //storage에서 사진을 받아와 set
            Toast.makeText(this,"사진 공유 성공", Toast.LENGTH_SHORT).show()
        }
    }
    private fun byteArrayToBitmap(byteArry: ByteArray): Bitmap? {
        var bitmap: Bitmap? = BitmapFactory.decodeByteArray(byteArry,0,byteArry.size)
        return bitmap
    }
    /////////


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            //사진 첨부
            PERMISSION_CODE -> {
                if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    pickImageFromGallery()
                }
                else{
                    Toast.makeText(this, "권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
            //녹화
            REQUEST_PERMISSION_KEY -> {
                if (grantResults.size > 0 && grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    isRecording = false
                    recordItem?.title = "녹화 시작"
                    Toast.makeText(thisContext,"녹화를 취소합니다.", Toast.LENGTH_SHORT).show()
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "마이크와 저장소 권한을 허용해주세요.",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction("ENABLE"
                    ) {
                        val intent = Intent().apply{
                            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            addCategory(Intent.CATEGORY_DEFAULT)
                            data = Uri.parse("package:$packageName")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        }
                        startActivity(intent)
                    }.show()
                }
                return
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when(requestCode) {
            //사진 첨부
            IMAGE_PICK_CODE -> {
                if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE) {
                    if (data != null) {
                        filePath = data.data
                    }
                    uploadFile()
                }
            }
            // 녹화
            REQUEST_CODE -> {
                if (requestCode != REQUEST_CODE) {
                    Log.e(TAG, "Unknown request code: $requestCode")
                    return
                }
                if (resultCode != RESULT_OK) {
                    Toast.makeText(this, "녹화 권한이 거부되었습니다.", Toast.LENGTH_SHORT).show()
                    isRecording = false
                    recordItem?.title = "녹화 시작"
                    Toast.makeText(thisContext,"녹화를 취소합니다.", Toast.LENGTH_SHORT).show()
                    return
                }
                mMediaProjectionCallback = MediaProjectionCallback()
                mMediaProjection = mProjectionManager?.getMediaProjection(resultCode, data)
                mMediaProjection?.registerCallback(mMediaProjectionCallback, null)
                mVirtualDisplay = createVirtualDisplay()
                mMediaRecorder?.start()
                isRecording = true
                recordItem?.title = "녹화 중지"
                Toast.makeText(thisContext,"녹화를 시작합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId){
            R.id.erase_all -> {
                canvasView.ClearCanvas()
                Toast.makeText(this,"지우기 성공", Toast.LENGTH_SHORT).show()
            }

            R.id.input_image -> {
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

            R.id.image_delete ->{
                database.getReference(RoomNumber).child("imageName").removeValue()
                Toast.makeText(this,"이미지 지우기 성공", Toast.LENGTH_SHORT).show()
            }

            R.id.recording -> {
                recordItem = item
                onToggleScreenShare()
            }

            R.id.pen -> {
                canvasView.penOption = 0
                Toast.makeText(this,"펜 선택", Toast.LENGTH_SHORT).show()
            }
            R.id.erase -> {
                canvasView.penOption = 1
                Toast.makeText(this,"지우개 선택", Toast.LENGTH_SHORT).show()
            }
            R.id.pointer -> {
                canvasView.penOption = 2;
                canvasView.myPointer = canvasView.PointerNum
                database.getReference(RoomNumber).child("POINTERNUM").setValue(Integer.parseInt(canvasView.PointerNum) + 1)
                Toast.makeText(this,"마우스 포인터 선택", Toast.LENGTH_SHORT).show()
            }
            R.id.erase_pointer -> {
                if(Integer.parseInt(canvas.PointerNum) != 0)
                    database.getReference(RoomNumber).child("ERASE").push().setValue(Integer.parseInt(canvas.PointerNum) + 10000000 - 1)
                Toast.makeText(this,"포인터 지우기 성공", Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}