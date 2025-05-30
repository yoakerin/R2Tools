package com.yoake.r2base.obj

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

/**
 * ViewPager2的FragmentAdapter
 */
class R2VP2FragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    val fragmentList: MutableList<Fragment> = ArrayList()

    constructor(fragmentActivity: FragmentActivity) : this(
        fragmentActivity.supportFragmentManager,
        fragmentActivity.lifecycle,

        )

    constructor(fragment: Fragment) : this(
        fragment.childFragmentManager,
        fragment.lifecycle,

        )

    override fun getItemCount(): Int {
        return fragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragmentList[position]
    }
}