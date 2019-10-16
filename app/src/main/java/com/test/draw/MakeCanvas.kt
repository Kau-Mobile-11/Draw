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
import com.google.firebase.database.*


class CanvasView(internal var context: Context, attrs : AttributeSet?) : View(context, attrs) {

    public var RoomNumber : String = ""
    private var mbitmap : Bitmap? = null
    private var mCanvas : Canvas? = null
    private var mPath : Path = Path()
    private var mPaint: Paint = Paint()
    private var mX : Float = 0.toFloat()
    private var mY : Float = 0.toFloat()
    private var Finished : Boolean = true
    private var arrivedX : Boolean = false
    private var arrivedY : Boolean = false
    val database : FirebaseDatabase = FirebaseDatabase.getInstance()  // firebase db의 인스턴스를 가져옴



    init {
        mPaint.isAntiAlias = true
        mPaint.color = Color.rgb(0,0,0)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeWidth = 4f
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawPath(mPath, mPaint)

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mbitmap)

    }

    private fun onStartTouchEvent(x: Float, y:Float) {
        mPath.moveTo(x,y)
        mX=x
        mY=y
        Finished = false;

        //Log.d(TAG,"START")
    }

    private fun onMoveTouchEvent(x:Float, y:Float) {
        if(Finished) return;

        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if(dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2)
            mX = x
            mY = y
        }
    }

    private fun upTouchEvent(){
        //mPath.lineTo(mX,mY)
        Finished = true
        arrivedX = false;
        arrivedY = false;

        //Log.d(TAG,"FINISH")
    }

    fun ClearCanvas(){
        mPath.reset()
        invalidate()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        //database.getReference("x").push().setValue(x) //  push()를 쓰면 누적 저장
        //database.getReference("y").push().setValue(y) //  위에 안쓴거는 계속 갱신

        database.getReference(RoomNumber).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                var value = p0.value as Map<String, String>;

                if(value.getValue("FIN").equals("T")) {
                    upTouchEvent()
                    invalidate()
                }else if(value.getValue("START").equals("T")){
                    onStartTouchEvent(value.getValue("X").toFloat(), value.getValue("Y").toFloat())
                    invalidate()
                }else{
                    onMoveTouchEvent(value.getValue("X").toFloat(), value.getValue("Y").toFloat())
                    invalidate()
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
//                onStartTouchEvent(x, y)
//                invalidate()
                database.getReference(RoomNumber).setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "FIN" to "F", "START" to "T"))
            }
            MotionEvent.ACTION_MOVE -> {
//                onMoveTouchEvent(x, y)
//                invalidate()

                database.getReference(RoomNumber).setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "FIN" to "F", "START" to "F"))
            }
            MotionEvent.ACTION_UP -> {
//                upTouchEvent()
//                invalidate()
                database.getReference(RoomNumber).setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "FIN" to "T", "START" to "F"))
            }
        }

        return true
    }


    companion object {
        private val TOLERANCE = 5f
    }

}

