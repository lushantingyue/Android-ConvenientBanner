package com.bigkoo.convenientbanner

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.support.v4.view.ViewPager
import android.support.v4.view.ViewPager.PageTransformer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout

import com.bigkoo.convenientbanner.adapter.CBPageAdapter
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator
import com.bigkoo.convenientbanner.listener.CBPageChangeListener
import com.bigkoo.convenientbanner.listener.OnItemClickListener
import com.bigkoo.convenientbanner.view.CBLoopViewPager

import java.lang.ref.WeakReference
import java.util.ArrayList
import kotlin.reflect.KClass

/**
 * 页面翻转控件，极方便的广告栏
 * 支持无限循环，自动翻页，翻页特效
 * @author Sai 支持自动翻页
 */
class ConvenientBanner<T> : LinearLayout {
    private var mDatas: List<T>? = null
    private var page_indicatorId: IntArray? = null
    private val mPointViews = ArrayList<ImageView>()
    private var pageChangeListener: CBPageChangeListener? = null
    private var onPageChangeListener: ViewPager.OnPageChangeListener? = null
    private var pageAdapter: CBPageAdapter<*>? = null
    var viewPager: CBLoopViewPager? = null
    private var scroller: ViewPagerScroller? = null
    private var loPageTurningPoint: ViewGroup? = null
    private var autoTurningTime: Long = 0
    /***
     * 是否开启了翻页
     * @return
     */
    var isTurning: Boolean = false
        private set
    private var canTurn = false
    private val manualPageable = true
    private var canLoop = true
    private var adSwitchTask: AdSwitchTask? = null

    var isManualPageable: Boolean
        get() = viewPager!!.isCanScroll
        set(manualPageable) {
            viewPager!!.isCanScroll = manualPageable
        }

    //获取当前的页面index
    val currentItem: Int
        get() = if (viewPager != null) {
            viewPager!!.realItem
        } else -1

    var isCanLoop: Boolean
        get() = viewPager!!.isCanLoop
        set(canLoop) {
            this.canLoop = canLoop
            viewPager!!.isCanLoop = canLoop
        }

    /**
     * 设置ViewPager的滚动速度
     * @param scrollDuration
     */
    var scrollDuration: Int
        get() = scroller!!.scrollDuration
        set(scrollDuration) {
            scroller!!.scrollDuration = scrollDuration
        }

    enum class PageIndicatorAlign {
        ALIGN_PARENT_LEFT, ALIGN_PARENT_RIGHT, CENTER_HORIZONTAL
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ConvenientBanner)
        canLoop = a.getBoolean(R.styleable.ConvenientBanner_canLoop, true)
        a.recycle()
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ConvenientBanner)
        canLoop = a.getBoolean(R.styleable.ConvenientBanner_canLoop, true)
        a.recycle()
        init(context)
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ConvenientBanner)
        canLoop = a.getBoolean(R.styleable.ConvenientBanner_canLoop, true)
        a.recycle()
        init(context)
    }

    private fun init(context: Context) {
        val hView = LayoutInflater.from(context).inflate(
                R.layout.include_viewpager, this, true)
        viewPager = hView.findViewById(R.id.cbLoopViewPager) as CBLoopViewPager
        loPageTurningPoint = hView
                .findViewById(R.id.loPageTurningPoint) as ViewGroup
        initViewPagerScroll()

        adSwitchTask = AdSwitchTask(this)
    }

    internal class AdSwitchTask(convenientBanner: ConvenientBanner<*>) : Runnable {

        private val reference: WeakReference<ConvenientBanner<*>>

        init {
            this.reference = WeakReference(convenientBanner)
        }

        override fun run() {
            val convenientBanner = reference.get()

            if (convenientBanner != null) {
                if (convenientBanner.viewPager != null && convenientBanner.isTurning) {
                    val page = convenientBanner.viewPager!!.currentItem + 1
                    convenientBanner.viewPager!!.currentItem = page
                    convenientBanner.postDelayed(convenientBanner.adSwitchTask, convenientBanner.autoTurningTime)
                }
            }
        }
    }

    fun setPages(holderCreator: CBViewHolderCreator<*>, datas: List<T>): ConvenientBanner<*> {
        this.mDatas = datas
        pageAdapter = CBPageAdapter(holderCreator, mDatas)
        viewPager!!.setAdapter(pageAdapter!!, canLoop)

        if (page_indicatorId != null)
            setPageIndicator(page_indicatorId!!)
        return this
    }

    /**
     * 通知数据变化
     * 如果只是增加数据建议使用 notifyDataSetAdd()
     */
    fun notifyDataSetChanged() {
        viewPager!!.adapter!!.notifyDataSetChanged()
        if (page_indicatorId != null)
            setPageIndicator(page_indicatorId!!)
    }

    /**
     * 设置底部指示器是否可见
     *
     * @param visible
     */
    fun setPointViewVisible(visible: Boolean): ConvenientBanner<*> {
        loPageTurningPoint!!.visibility = if (visible) View.VISIBLE else View.GONE
        return this
    }

    /**
     * 底部指示器资源图片
     *
     * @param page_indicatorId
     */
    fun setPageIndicator(page_indicatorId: IntArray): ConvenientBanner<*> {
        loPageTurningPoint!!.removeAllViews()
        mPointViews.clear()
        this.page_indicatorId = page_indicatorId
        if (mDatas == null) return this
//        for (count in mDatas.size!!.indices) {

        for ( count in 0..(mDatas!!.size-1)) {
            // 翻页指示的点
            val pointView = ImageView(context)
            pointView.setPadding(5, 0, 5, 0)
            if (mPointViews.isEmpty())
                pointView.setImageResource(page_indicatorId[1])
            else
                pointView.setImageResource(page_indicatorId[0])
            mPointViews.add(pointView)
            loPageTurningPoint!!.addView(pointView)
        }
        pageChangeListener = CBPageChangeListener(mPointViews,
                page_indicatorId)
        viewPager!!.setOnPageChangeListener(pageChangeListener!!)
        pageChangeListener!!.onPageSelected(viewPager!!.realItem)
        if (onPageChangeListener != null) pageChangeListener!!.setOnPageChangeListener(onPageChangeListener!!)

        return this
    }

    /**
     * 指示器的方向
     * @param align  三个方向：居左 （RelativeLayout.ALIGN_PARENT_LEFT），居中 （RelativeLayout.CENTER_HORIZONTAL），居右 （RelativeLayout.ALIGN_PARENT_RIGHT）
     * @return
     */
    fun setPageIndicatorAlign(align: PageIndicatorAlign): ConvenientBanner<*> {
        val layoutParams = loPageTurningPoint!!.layoutParams as RelativeLayout.LayoutParams
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, if (align == PageIndicatorAlign.ALIGN_PARENT_LEFT) RelativeLayout.TRUE else 0)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, if (align == PageIndicatorAlign.ALIGN_PARENT_RIGHT) RelativeLayout.TRUE else 0)
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL, if (align == PageIndicatorAlign.CENTER_HORIZONTAL) RelativeLayout.TRUE else 0)
        loPageTurningPoint!!.layoutParams = layoutParams
        return this
    }

    /***
     * 开始翻页
     * @param autoTurningTime 自动翻页时间
     * @return
     */
    fun startTurning(autoTurningTime: Long): ConvenientBanner<*> {
        //如果是正在翻页的话先停掉
        if (isTurning) {
            stopTurning()
        }
        //设置可以翻页并开启翻页
        canTurn = true
        this.autoTurningTime = autoTurningTime
        isTurning = true
        postDelayed(adSwitchTask, autoTurningTime)
        return this
    }

    fun stopTurning() {
        isTurning = false
        removeCallbacks(adSwitchTask)
    }

    /**
     * 自定义翻页动画效果
     *
     * @param transformer
     * @return
     */
    fun setPageTransformer(transformer: PageTransformer): ConvenientBanner<*> {
        viewPager!!.setPageTransformer(true, transformer)
        return this
    }


    /**
     * 设置ViewPager的滑动速度
     */
    private fun initViewPagerScroll() {
        try {
            val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.isAccessible = true
//            var mScroller = getMember(ViewPager.class.java, scroller,"mScroller")

            scroller = ViewPagerScroller(
                    viewPager!!.context)
            mScroller.set(viewPager, scroller)

        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    fun getMember(cls: Class<T>, target: Any, member: String): Any? {
        var result: Any? = null
        try {
            val memberField = cls.getDeclaredField(member)
            memberField.isAccessible = true
            result = memberField.get(target)

        } catch (e: Exception) {

        }

        return result
    }

    //触碰控件的时候，翻页应该停止，离开的时候如果之前是开启了翻页的话则重新启动翻页
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {

        val action = ev.action
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_OUTSIDE) {
            // 开始翻页
            if (canTurn) startTurning(autoTurningTime)
        } else if (action == MotionEvent.ACTION_DOWN) {
            // 停止翻页
            if (canTurn) stopTurning()
        }
        return super.dispatchTouchEvent(ev)
    }

    //设置当前的页面index
    fun setcurrentitem(index: Int) {
        if (viewPager != null) {
            viewPager!!.currentItem = index
        }
    }

    fun getOnPageChangeListener(): ViewPager.OnPageChangeListener? {
        return onPageChangeListener
    }

    /**
     * 设置翻页监听器
     * @param onPageChangeListener
     * @return
     */
    fun setOnPageChangeListener(onPageChangeListener: ViewPager.OnPageChangeListener): ConvenientBanner<*> {
        this.onPageChangeListener = onPageChangeListener
        //如果有默认的监听器（即是使用了默认的翻页指示器）则把用户设置的依附到默认的上面，否则就直接设置
        if (pageChangeListener != null)
            pageChangeListener!!.setOnPageChangeListener(onPageChangeListener)
        else
            viewPager!!.setOnPageChangeListener(onPageChangeListener)
        return this
    }

    /**
     * 监听item点击
     * @param onItemClickListener
     */
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener?): ConvenientBanner<*> {
        if (onItemClickListener == null) {
            viewPager!!.setOnItemClickListener(null!!)
            return this
        }
        viewPager!!.setOnItemClickListener(onItemClickListener)
        return this
    }
}
