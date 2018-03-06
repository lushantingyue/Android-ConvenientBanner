package com.bigkoo.convenientbanner.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.bigkoo.convenientbanner.adapter.CBPageAdapter

import com.bigkoo.convenientbanner.listener.OnItemClickListener


class CBLoopViewPager : ViewPager {
    internal var mOuterPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var onItemClickListener: OnItemClickListener? = null
    private var mAdapter: CBPageAdapter<*>? = null

    var isCanScroll = true
    var isCanLoop = true
        set(canLoop) {
            field = canLoop
            if (canLoop == false) {
                setCurrentItem(realItem, false)
            }
            if (mAdapter == null) return
            mAdapter!!.setCanLoop(canLoop)
            mAdapter!!.notifyDataSetChanged()
        }

    val fristItem: Int
        get() = if (isCanLoop) mAdapter!!.realCount else 0

    val lastItem: Int
        get() = mAdapter!!.realCount - 1

    private var oldX = 0f
    private var newX = 0f

    val realItem: Int
        get() = if (mAdapter != null) mAdapter!!.toRealPosition(super.getCurrentItem()) else 0

    private val onPageChangeListener = object : ViewPager.OnPageChangeListener {
        private var mPreviousPosition = -1f

        override fun onPageSelected(position: Int) {
            val realPosition = mAdapter!!.toRealPosition(position)
            if (mPreviousPosition != realPosition.toFloat()) {
                mPreviousPosition = realPosition.toFloat()
                if (mOuterPageChangeListener != null) {
                    mOuterPageChangeListener!!.onPageSelected(realPosition)
                }
            }
        }

        override fun onPageScrolled(position: Int, positionOffset: Float,
                                    positionOffsetPixels: Int) {

            if (mOuterPageChangeListener != null) {
                if (position != mAdapter!!.realCount - 1) {
                    mOuterPageChangeListener!!.onPageScrolled(position,
                            positionOffset, positionOffsetPixels)
                } else {
                    if (positionOffset > .5) {
                        mOuterPageChangeListener!!.onPageScrolled(0, 0f, 0)
                    } else {
                        mOuterPageChangeListener!!.onPageScrolled(position,
                                0f, 0)
                    }
                }
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            if (mOuterPageChangeListener != null) {
                mOuterPageChangeListener!!.onPageScrollStateChanged(state)
            }
        }
    }

    fun setAdapter(adapter: PagerAdapter, canLoop: Boolean) {
        mAdapter = adapter as CBPageAdapter<*>
        mAdapter!!.setCanLoop(canLoop)
        mAdapter!!.setViewPager(this)
        super.setAdapter(mAdapter)

        setCurrentItem(fristItem, false)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isCanScroll) {
            if (onItemClickListener != null) {
                when (ev.action) {
                    MotionEvent.ACTION_DOWN -> oldX = ev.x

                    MotionEvent.ACTION_UP -> {
                        newX = ev.x
                        if (Math.abs(oldX - newX) < sens) {
                            onItemClickListener!!.onItemClick(realItem)
                        }
                        oldX = 0f
                        newX = 0f
                    }
                }
            }
            return super.onTouchEvent(ev)
        } else
            return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isCanScroll)
            super.onInterceptTouchEvent(ev)
        else
            false
    }

    override fun getAdapter(): CBPageAdapter<*>? {
        return mAdapter
    }

    override fun setOnPageChangeListener(listener: ViewPager.OnPageChangeListener) {
        mOuterPageChangeListener = listener
    }


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    private fun init() {
        super.setOnPageChangeListener(onPageChangeListener)
    }

    fun setOnItemClickListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    companion object {
        private val sens = 5f
    }
}
