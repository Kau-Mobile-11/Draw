package com.test.draw

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent.getIntent
import android.content.Intent.getIntentOld
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.google.firebase.database.*
import kotlin.reflect.typeOf


class CanvasView(internal var context: Context, attrs : AttributeSet?) : View(context, attrs) {

    public var RoomNumber : String = ""
    private var mbitmap : Bitmap? = null
    private var mCanvas : Canvas? = null
    public var mPath = ArrayList<Path>()
    private var mPaint: Paint = Paint()
    public var mX = ArrayList<Float>()
    public var mY = ArrayList<Float>()
    public var Finished = ArrayList<Boolean>()
    public var myNum = "0"
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()  // firebase db의 인스턴스를 가져옴

    var text_view : TextView? = null


    init {
        mPaint.isAntiAlias = true
        mPaint.color = Color.rgb(0,0,0)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        for(i in mPath) {
            canvas!!.drawPath(i, mPaint)
        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mbitmap)

    }

    public fun onStartTouchEvent(x: Float, y:Float, num:Int) {
        mPath[num].moveTo(x,y)
        mX[num]=x
        mY[num]=y
        Finished[num] = false;

        //Log.d(TAG,"START")
    }

    public fun onMoveTouchEvent(x:Float, y:Float, num:Int) {
        if(Finished[num]) return;

        val dx = Math.abs(x - mX[num])
        val dy = Math.abs(y - mY[num])
        if(dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath[num].quadTo(mX[num],mY[num],(x+mX[num])/2,(y+mY[num])/2)
            mX[num] = x
            mY[num] = y
        }
    }

    public fun upTouchEvent(num:Int){
        mPath[num].lineTo(mX[num],mY[num])
        Finished[num] = true

        //Log.d(TAG,"FINISH")
    }

    public fun removeAll(){
        for(i in mPath){
            i.reset()
        }
    }

    fun ClearCanvas() {
        database.getReference(RoomNumber).child("PATHS").removeValue()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        //database.getReference("x").push().setValue(x) //  push()를 쓰면 누적 저장
        //database.getReference("y").push().setValue(y) //  위에 안쓴거는 계속 갱신

//        database.getReference(RoomNumber).addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(p0: DataSnapshot) {
//                var value = p0.value as Map<String, String>;
//
//                if(value.getValue("FIN").equals("T")) {
//                    upTouchEvent()
//                    invalidate()
//                }else if(value.getValue("START").equals("T")){
//                    onStartTouchEvent(value.getValue("X").toFloat(), value.getValue("Y").toFloat())
//                    invalidate()
//                }else{
//                    onMoveTouchEvent(value.getValue("X").toFloat(), value.getValue("Y").toFloat())
//                    invalidate()
//                }
//            }
//
//            override fun onCancelled(p0: DatabaseError) {
//                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//            }
//
//        })

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                onStartTouchEvent(x, y)
//                invalidate()
                database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "F", "START" to "T"))
            }
            MotionEvent.ACTION_MOVE -> {
//                onMoveTouchEvent(x, y)
//                invalidate()

                database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "F", "START" to "F"))
            }
            MotionEvent.ACTION_UP -> {
//                upTouchEvent()
//                invalidate()
                database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "T", "START" to "F"))
            }

        }

        return true
    }


    companion object {
        private val TOLERANCE = 5f
    }
}

