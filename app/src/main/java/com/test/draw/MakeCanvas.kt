package com.test.draw

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.firebase.database.*
import android.graphics.RectF

class CanvasView(internal var context: Context, attrs : AttributeSet?) : View(context, attrs) {

    var RoomNumber : String = ""
    private var mbitmap : Bitmap? = null
    private var mCanvas : Canvas? = null
    var mPath = ArrayList<Path>()
    private var mPaint: Paint = Paint()
    var mX = ArrayList<Float>()
    var mY = ArrayList<Float>()
    var Finished = ArrayList<Boolean>()
    var lineNum = "0"
    private var eraseX = 0f
    private var eraseY = 0f
    private var myNum = "0"
    var penOption = 0
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
        for(i in mPath) {
            canvas!!.drawPath(i, mPaint)
        }

    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        mbitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mbitmap)

    }

    fun onStartTouchEvent(x: Float, y:Float, num:Int) {
        mPath[num].moveTo(x,y)
        mX[num]=x
        mY[num]=y
        Finished[num] = false
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

    fun upTouchEvent(num:Int){
        mPath[num].lineTo(mX[num],mY[num])
        Finished[num] = true
    }

    fun removeAll(){
        for(i in mPath){
            i.reset()
        }
        invalidate()
    }

    fun ClearCanvas() {
        database.getReference(RoomNumber).child("ERASE").push().setValue(-1)
        for(i in 0..(Integer.parseInt(lineNum)-1)){
            database.getReference(RoomNumber).child("PATHS").child("" + i).removeValue()
        }
    }

    fun ErasePath(pathIndex : Int) {
        mPath[pathIndex].reset()
        invalidate()
    }

    fun findEraseLine(x:Float, y:Float) {
        val padding = 10f
        val touchPoint = RectF(x, y, x + padding, y + padding)
        val touchPointPath = Path()
        touchPointPath.addRect(touchPoint, Path.Direction.CW)
        for (i in 0..(mPath.size - 1)) {
            val hourPath = mPath[i]
            touchPointPath.addCircle(x, y, padding, Path.Direction.CW)
            touchPointPath.close()
            val hourPathCopy = Path(hourPath)
            touchPointPath.reset()
            val bounds = RectF()
            hourPathCopy.computeBounds(bounds, true)
            if (bounds.left.toDouble() != 0.0 && bounds.top.toDouble() != 0.0 && bounds.right.toDouble() != 0.0 && bounds.bottom.toDouble() != 0.0) {
                database.getReference(RoomNumber).child("ERASE").push().setValue(i)
                database.getReference(RoomNumber).child("PATHS").child("" + i).removeValue()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        if(penOption == 0){
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    myNum = lineNum
                    database.getReference(RoomNumber).child("PEOPLENUMBER").setValue(Integer.parseInt(myNum).toLong() + 1)
                    database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "F", "START" to "T"))
                }
                MotionEvent.ACTION_MOVE -> {
                    database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "F", "START" to "F"))
                }
                MotionEvent.ACTION_UP -> {
                    database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "T", "START" to "F"))
                }

            }
        }else if(penOption == 1){
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    eraseX = x
                    eraseY = y
                }
            }

            var dx = (x.toFloat() - eraseX)
            var dy = (y.toFloat() - eraseY)

            val len = Math.sqrt(1.0*dx*dx + dy*dy)

            dx /= len.toFloat()
            dy /= len.toFloat()

            while(Math.abs(x - eraseX) > 6 || Math.abs(y - eraseY) > 6){
                findEraseLine(eraseX, eraseY)
                eraseX += 6 * dx
                eraseY += 6 * dy
            }
        }

        return true
    }


    companion object {
        private val TOLERANCE = 5f
    }
}

