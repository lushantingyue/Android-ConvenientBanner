package com.bigkoo.convenientbanner.adapter

import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup

import com.bigkoo.convenientbanner.R
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator
import com.bigkoo.convenientbanner.holder.Holder
import com.bigkoo.convenientbanner.view.CBLoopViewPager

/**
 * Created by Sai on 15/7/29.
 */
class CBPageAdapter<T>(protected var holderCreator: CBViewHolderCreator<*>, protected var mDatas: List<T>?) : PagerAdapter() {
    //    private View.OnClickListener onItemClickListener;
    private var canLoop = true
    private var viewPager: CBLoopViewPager? = null
    private val MULTIPLE_COUNT = 300

    val realCount: Int
        get() = if (mDatas == null) 0 else mDatas!!.size

    fun toRealPosition(position: Int): Int {
        val realCount = realCount
        if (realCount == 0)
            return 0
        return position % realCount
    }

    override fun getCount(): Int {
        return if (canLoop) realCount * MULTIPLE_COUNT else realCount
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val realPosition = toRealPosition(position)

        val view = getView(realPosition, null, container)
        //        if(onItemClickListener != null) view.setOnClickListener(onItemClickListener);
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        val view = `object` as View
        container.removeView(view)
    }

    override fun finishUpdate(container: ViewGroup) {
        var position = viewPager!!.currentItem
        if (position == 0) {
            position = viewPager!!.fristItem
        } else if (position == count - 1) {
            position = viewPager!!.lastItem
        }
        try {
            viewPager!!.setCurrentItem(position, false)
        } catch (e: IllegalStateException) {
        }

    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    fun setCanLoop(canLoop: Boolean) {
        this.canLoop = canLoop
    }

    fun setViewPager(viewPager: CBLoopViewPager) {
        this.viewPager = viewPager
    }

    fun getView(position: Int, view: View?, container: ViewGroup): View {
        var view = view
        var holder: Holder<T>?
        if (view == null) {
            holder = holderCreator.createHolder() as Holder<T>
            view = holder.createView(container.context)
            view.setTag(R.id.cb_item_tag, holder)
        } else {
            holder = view.getTag(R.id.cb_item_tag) as Holder<T>
        }
        holder.UpdateUI(container.context, position, mDatas!![position])

        return view
    }

    //    public void setOnItemClickListener(View.OnClickListener onItemClickListener) {
    //        this.onItemClickListener = onItemClickListener;
    //    }
}
