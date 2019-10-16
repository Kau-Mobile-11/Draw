package com.test.draw

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.firebase.database.*


class CanvasView(internal var context: Context, attrs : AttributeSet?) : View(context, attrs) {

    private var mbitmap : Bitmap? = null
    private var mCanvas : Canvas? = null
    private var mPath : Path = Path()
    private var mPaint: Paint = Paint()
    private var mX : Float = 0.toFloat()
    private var mY : Float = 0.toFloat()
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
    }

    private fun onMoveTouchEvent(x:Float, y:Float) {
        val dx = Math.abs(x - mX)
        val dy = Math.abs(y - mY)
        if(dx >= TOLERANCE || dy >= TOLERANCE) {
            mPath.quadTo(mX,mY,(x+mX)/2,(y+mY)/2)
            mX = x
            mY = y
        }

    }

    private fun upTouchEvent(){
        mPath.lineTo(mX,mY)
    }

    fun ClearCanvas(){
        mPath.reset()
        invalidate()
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        database.getReference("x").setValue(x)  // db에 x라는 child를 만들고 x값을 set
        database.getReference("y").setValue(y)  // db에 y라는 child를 만들고 y값을 set

        //database.getReference("x").push().setValue(x) //  push()를 쓰면 누적 저장
        //database.getReference("y").push().setValue(y) //  위에 안쓴거는 계속 갱신


        //db에 x라는 child가 생기거나 변하거나 하면 자동호출
        database.getReference("x").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.value as Double      // x 라는 child의 값을 받아옴
                Log.d(TAG,"check x : $value")         // Float형은 안되고 Double, Long만 가능

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })
        //위와 같음
        database.getReference("y").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.value as Double
                Log.d(TAG,"check y : $value")

            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

        })

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onStartTouchEvent(x, y)
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                onMoveTouchEvent(x, y)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                upTouchEvent()
                invalidate()
            }
        }

        return true
    }


    companion object {
        private val TOLERANCE = 5f
    }

}

