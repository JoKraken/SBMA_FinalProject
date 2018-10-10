@file:Suppress("DEPRECATION")

package com.example.johanna.runis

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View

open class OnSwipeTouchListener: View.OnTouchListener {

    private val gestureDetector = GestureDetector(GestureListener())

    fun onTouch(event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {

        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100
        protected var mLastOnDownEvent: MotionEvent? = null

        override fun onDown(e: MotionEvent): Boolean {
            mLastOnDownEvent = e
            return true
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            onTouch(e)
            return true
        }


        override fun onFling(e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val result = false
            var e1_1 : MotionEvent? = null
            if(e1 == null){
                Log.d("DEBUG_onSwipeListener_onFling", "e1 == null")
                e1_1 = mLastOnDownEvent
            }else e1_1 = e1

            if(e1_1 != null && e2 != null){
                Log.d("DEBUG_onSwipeListener_onFling", "e1 & e2 != null")
                try {
                    val diffY = e2.y - e1_1.y
                    val diffX = e2.x - e1_1.x
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                    } else {
                        //onTouch(e);
                    }
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }

            return result
        }
    }

    override fun onTouch(v: View, event: MotionEvent?): Boolean {
        Log.d("DEBUG_onswipe_onTouch", v.toString())
        Log.d("DEBUG_onswipe_onTouch", event.toString())
        Log.d("DEBUG_onswipe_onTouch", gestureDetector.onTouchEvent(event).toString())
        val bool = gestureDetector.onTouchEvent(event)
        return bool
    }

    open fun onSwipeRight() {}

    open fun onSwipeLeft() {}
}