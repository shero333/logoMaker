package com.esport.logo.maker.unlimited.one_time_screens.on_boarding.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.esport.logo.maker.unlimited.R

class OnBoardingAdapter(private val context: Context) : PagerAdapter() {
    private val layouts = intArrayOf(
        R.layout.boarding_layout1,
        R.layout.boarding_layout2,
        R.layout.boarding_layout3,
        R.layout.boarding_layout4,
        R.layout.boarding_layout5
    )

    override fun getCount(): Int {
        return layouts.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val layoutInflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = layoutInflater.inflate(layouts[position], container, false)
        view.tag = position
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
//        super.destroyItem(container, position, object);
        container.removeView(`object` as ConstraintLayout)
    }
}