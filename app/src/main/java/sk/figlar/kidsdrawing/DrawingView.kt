package sk.figlar.kidsdrawing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private lateinit var mDrawPath: CustomPath
    private lateinit var mCanvasBitmap: Bitmap
    private lateinit var mDrawPaint: Paint
    private lateinit var mCanvasPaint: Paint
    private var mBrushSize: Float = 0f
    private var mColor = Color.BLACK
    private lateinit var mCanvas: Canvas
    private val mPaths: MutableList<CustomPath> = emptyList<CustomPath>().toMutableList()
    private val mUndoPaths: MutableList<CustomPath> = emptyList<CustomPath>().toMutableList()

    init {
        setUpDrawing()
    }

    fun onClickUndo() {
        if (mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPaint.color = mColor
        mDrawPaint.style = Paint.Style.STROKE
        mDrawPaint.strokeJoin = Paint.Join.ROUND
        mDrawPaint.strokeCap = Paint.Cap.ROUND
        mDrawPath = CustomPath(mColor, mBrushSize)
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap, 0f, 0f, mCanvasPaint)

        for (path in mPaths) {
            if (!path.isEmpty) {
                mDrawPaint.strokeWidth = path.brushThickness
                mDrawPaint.color = path.color
                canvas.drawPath(path, mDrawPaint)
            }
        }
        if (!mDrawPath.isEmpty) {
            mDrawPaint.strokeWidth = mDrawPath.brushThickness
            mDrawPaint.color = mDrawPath.color
            canvas.drawPath(mDrawPath, mDrawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when (event?.action) {
            MotionEvent.ACTION_DOWN -> {
                mDrawPath.color = mColor
                mDrawPath.brushThickness = mBrushSize

                mDrawPath.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath.moveTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath.lineTo(touchX, touchY)
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                mPaths += mDrawPath
                mDrawPath = CustomPath(mColor, mBrushSize)
            }
            else -> return false
        }
        invalidate()

        return true
    }

    fun setBrushSize(newSize: Float) {
        mBrushSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            newSize,
            resources.displayMetrics
        )
        mDrawPaint.strokeWidth = mBrushSize
    }

    fun setColor(newColor: String) {
        mColor = Color.parseColor(newColor)
        mDrawPaint.color = mColor
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float) : Path()
}