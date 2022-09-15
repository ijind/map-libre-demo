package ai.txai.common.countrycode

import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnItemTouchListener

open class RecyclerItemClickListener(context: Context, mListener: OnItemClickListener?) :
    OnItemTouchListener {
    private var mGestureDetector: GestureDetector
    private var childView: View? = null
    private lateinit var touchView: RecyclerView

   interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
        fun onLongClick(view: View, position: Int) {

        }
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        mGestureDetector.onTouchEvent(e)
        childView = rv.findChildViewUnder(e.x, e.y)
        touchView = rv
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    init {
        mGestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(ev: MotionEvent): Boolean {
                    if (childView != null && mListener != null) {
                        val adapterPosition = touchView.getChildAdapterPosition(childView!!)
                        if (adapterPosition >= 0) {
                            mListener.onItemClick(childView!!, adapterPosition)
                        }
                    }
                    return true
                }

                override fun onLongPress(ev: MotionEvent) {
                    if (childView != null && mListener != null) {
                        val adapterPosition = touchView.getChildAdapterPosition(childView!!)
                        if (adapterPosition >= 0) {
                            mListener.onLongClick(childView!!, adapterPosition)
                        }
                    }
                }
            })
    }
}