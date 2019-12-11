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
import android.R.attr.bottom
import android.R.attr.right
import android.R.attr.top
import android.R.attr.left
import android.graphics.RectF
import android.R.attr.y
import android.R.attr.x
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.icu.lang.UCharacter.GraphemeClusterBreak.T








class CanvasView(internal var context: Context, attrs : AttributeSet?) : View(context, attrs) {

    public var RoomNumber : String = ""
    private var mbitmap : Bitmap? = null
    private var mCanvas : Canvas? = null
    public var mPath = ArrayList<Path>()
    private var mPaint: Paint = Paint()
    public var mPointer = ArrayList<Path>()
    public var PointerX = ArrayList<Float>()
    public var PointerY = ArrayList<Float>()
    public var PointerVisible = ArrayList<Boolean>()
    public var mX = ArrayList<Float>()
    public var mY = ArrayList<Float>()
    public var Finished = ArrayList<Boolean>()
    public var lineNum = "0"
    public var PointerNum = "0"
    public var myPointer = "0"
    private var eraseX = 0f
    private var eraseY = 0f
    private var myNum = "0"
    public var penOption = 0
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
        for(i in mPointer){
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
        invalidate()
    }

    fun ClearCanvas() {
        database.getReference(RoomNumber).child("ERASE").push().setValue(-1)
        for(i in 0..(Integer.parseInt(lineNum)-1)){
            database.getReference(RoomNumber).child("PATHS").child("" + i).removeValue()
        }
    }

    public fun ErasePath(pathIndex : Int) {
        mPath[pathIndex].reset()
        invalidate()
    }

    fun clearPointer(pointerIndex : Int){
        PointerVisible[pointerIndex] = false
        mPointer[pointerIndex].reset()
        invalidate()
    }

    fun setPointer(pointerIndex : Int, x : Float, y : Float){
        if(!PointerVisible[pointerIndex]) return

        PointerX[pointerIndex] = x
        PointerY[pointerIndex] = y
        mPointer[pointerIndex].reset()
        mPointer[pointerIndex].addCircle(x, y, 50f, Path.Direction.CW)
        PointerVisible[pointerIndex] = true;
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
            val intersectResult = hourPathCopy.op(touchPointPath, Path.Op.INTERSECT)
            touchPointPath.reset()
            val bounds = RectF()
            hourPathCopy.computeBounds(bounds, true)
            //      Log.d(TAG, "intersectResult: " + intersectResult + " different?: " + bounds.left+","+bounds.top+","+bounds.right+","+bounds.bottom);
            if (bounds.left.toDouble() != 0.0 && bounds.top.toDouble() != 0.0 && bounds.right.toDouble() != 0.0 && bounds.bottom.toDouble() != 0.0) {
                database.getReference(RoomNumber).child("ERASE").push().setValue(i)
                database.getReference(RoomNumber).child("PATHS").child("" + i).removeValue()
            }
        }
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

        if(penOption == 0){
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
//                onStartTouchEvent(x, y)
//                invalidate()
                    myNum = lineNum
                    database.getReference(RoomNumber).child("PEOPLENUMBER").setValue(Integer.parseInt(myNum).toLong() + 1)
                    database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "F", "START" to "T"))
                }
                MotionEvent.ACTION_MOVE -> {
//                onMoveTouchEvent(x, y)
//                invalidate()

                    database.getReference(RoomNumber).child("PATHS").child(""+myNum).push().setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myNum, "FIN" to "F", "START" to "F"))
                }
                MotionEvent.ACTION_UP -> {
//                  upTouchEvent()
//                  invalidate()
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
        }else if(penOption == 2){
            database.getReference(RoomNumber).child("POINTERS").child(myPointer).setValue(mapOf("X" to x.toFloat().toString(), "Y" to y.toFloat().toString(), "NUM" to myPointer))
        }

        return true
    }


    companion object {
        private val TOLERANCE = 5f
    }
}

