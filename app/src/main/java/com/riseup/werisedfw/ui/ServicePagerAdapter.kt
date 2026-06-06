package com.riseup.werisedfw.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.riseup.werisedfw.data.Category
import com.riseup.werisedfw.data.Service

/**
 * Backs the bottom-half [androidx.viewpager2.widget.ViewPager2] with one
 * [ServiceListFragment] per category.
 */
class ServicePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val fragments = HashMap<Int, ServiceListFragment>()

    override fun getItemCount(): Int = TAB_ORDER.size

    override fun createFragment(position: Int): Fragment {
        val fragment = ServiceListFragment.newInstance(TAB_ORDER[position])
        fragments[position] = fragment
        return fragment
    }

    /** Push a fresh per-category result map into all live fragments. */
    fun updateAll(byCategory: Map<Category, List<Pair<Service, Double>>>) {
        fragments.forEach { (position, fragment) ->
            fragment.update(byCategory[TAB_ORDER[position]].orEmpty())
        }
    }

    private companion object {
        /** Fixed order of categories matching the tab positions 0, 1, 2. */
        val TAB_ORDER = listOf(Category.FOOD, Category.CLOTHING, Category.SHELTER)
    }
}

