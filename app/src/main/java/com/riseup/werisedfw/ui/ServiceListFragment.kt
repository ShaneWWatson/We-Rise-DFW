package com.riseup.werisedfw.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.riseup.werisedfw.R
import com.riseup.werisedfw.ServiceDetailActivity
import com.riseup.werisedfw.data.Category
import com.riseup.werisedfw.data.Service

/**
 * One tab's worth of provider list. Renders a [RecyclerView] with cards or
 * an empty-state message depending on whether there are results.
 */
class ServiceListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var adapter: ServiceListAdapter

    /** Items pushed before the view was inflated; applied in [onViewCreated]. */
    private var pendingItems: List<Pair<Service, Double>> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_service_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recyclerView)
        emptyState = view.findViewById(R.id.emptyState)
        adapter = ServiceListAdapter(::openDetail)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        applyItems(pendingItems)
    }

    /** Public entry point for the parent activity to feed in new data. */
    fun update(items: List<Pair<Service, Double>>) {
        pendingItems = items
        if (::adapter.isInitialized) applyItems(items)
    }

    // -------------------------------------------------------------------
    // Internals
    // -------------------------------------------------------------------

    private fun applyItems(items: List<Pair<Service, Double>>) {
        adapter.submitList(items)
        emptyState.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun openDetail(service: Service) {
        val intent = Intent(requireContext(), ServiceDetailActivity::class.java)
            .putExtra(ServiceDetailActivity.EXTRA_SERVICE_ID, service.id)
        startActivity(intent)
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: Category): ServiceListFragment =
            ServiceListFragment().apply {
                arguments = Bundle().apply { putString(ARG_CATEGORY, category.name) }
            }
    }
}

