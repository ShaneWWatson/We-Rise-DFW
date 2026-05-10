package com.werisetech.weriseapp

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.werisetech.weriseapp.data.AppDatabase
import com.werisetech.weriseapp.data.Category
import com.werisetech.weriseapp.data.DfwBounds
import com.werisetech.weriseapp.data.SeedData
import com.werisetech.weriseapp.data.Service
import com.werisetech.weriseapp.i18n.TranslatorFactory
import com.werisetech.weriseapp.location.LocationProvider
import com.werisetech.weriseapp.online.OverpassRefresher
import com.werisetech.weriseapp.ui.PreferencesActivity
import com.werisetech.weriseapp.ui.ServicePagerAdapter
import com.werisetech.weriseapp.util.DistanceUtil
import com.werisetech.weriseapp.util.HoursParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Main screen.
 *
 * Layout: a map fills the top half; three tabs (Food / Clothing / Shelter)
 * and a list of providers fill the bottom half. Two floating action buttons
 * sit above the footer:
 *  - **Search** runs the local pipeline (seed + cached online results),
 *    re-reading the user's location once.
 *  - **Find more online** queries the OpenStreetMap Overpass API for
 *    additional nearby providers and merges them into the local cache.
 *
 * Privacy: location is read only at the moment one of the two buttons is
 * tapped, used in-memory to filter results, and never persisted.
 */
class MainActivity : AppCompatActivity() {

    // -------------------------------------------------------------------
    // View references
    // -------------------------------------------------------------------

    private lateinit var mapView: MapView
    private lateinit var outOfRangeBox: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var searchButton: MaterialButton
    private lateinit var onlineSearchButton: MaterialButton
    private lateinit var pagerAdapter: ServicePagerAdapter
    private lateinit var prefs: SharedPreferences

    // -------------------------------------------------------------------
    // Transient state (in-memory only — never persisted)
    // -------------------------------------------------------------------

    /** Last computed (service, distance) pairs grouped by category. */
    private var lastResults: Map<Category, List<Pair<Service, Double>>> = emptyMap()
    private var hasShownResults: Boolean = false

    // -------------------------------------------------------------------
    // Permission handling
    // -------------------------------------------------------------------

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) {
            doSearch()
        } else {
            Toast.makeText(this, R.string.permission_required_title, Toast.LENGTH_LONG).show()
        }
    }

    // -------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------

    override fun onCreate(savedInstanceState: Bundle?) {
        // OSMDroid requires Configuration.load before setContentView when the MapView
        // is declared in the inflated layout.
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        bindViews()
        setupMap()
        setupTabs()
        setupSearchButtons()

        // Pre-warm the local cache on first launch so the offline path has data
        // even before the user runs their first search.
        lifecycleScope.launch { primeCacheIfEmpty() }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    // -------------------------------------------------------------------
    // Setup
    // -------------------------------------------------------------------

    private fun bindViews() {
        setSupportActionBar(findViewById<MaterialToolbar>(R.id.toolbar))
        mapView = findViewById(R.id.mapView)
        outOfRangeBox = findViewById(R.id.outOfRangeBox)
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        searchButton = findViewById(R.id.searchButton)
        onlineSearchButton = findViewById(R.id.onlineSearchButton)
    }

    private fun setupMap() {
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)
        mapView.controller.setZoom(10.0)
        mapView.controller.setCenter(GeoPoint(DfwBounds.centerLat, DfwBounds.centerLon))
    }

    private fun setupTabs() {
        pagerAdapter = ServicePagerAdapter(this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.tab_food)
                1 -> getString(R.string.tab_clothing)
                else -> getString(R.string.tab_shelter)
            }
        }.attach()

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                redrawMapMarkers(positionToCategory(position))
            }
        })
    }

    private fun setupSearchButtons() {
        searchButton.setOnClickListener { onSearchClicked() }
        onlineSearchButton.setOnClickListener { onOnlineSearchClicked() }
    }

    // -------------------------------------------------------------------
    // Local search (button: "Search")
    // -------------------------------------------------------------------

    private fun onSearchClicked() {
        if (!LocationProvider.hasPermission(this)) {
            promptForLocationPermission()
            return
        }
        doSearch()
    }

    private fun promptForLocationPermission() {
        AlertDialog.Builder(this)
            .setTitle(R.string.permission_required_title)
            .setMessage(R.string.permission_required_message)
            .setPositiveButton(R.string.grant) { _, _ ->
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Reads the user's location once, then rebuilds the displayed results
     * from the seed data plus any online results already cached locally.
     * Translation, faith filter, and radius are applied here.
     */
    private fun doSearch() {
        searchButton.isEnabled = false
        searchButton.text = getString(R.string.searching)

        lifecycleScope.launch {
            val location = LocationProvider.fetchOnce(this@MainActivity)
            if (location == null) {
                showToast("Could not get location")
                resetSearchButton()
                return@launch
            }

            val lat = location.latitude
            val lon = location.longitude
            // CRITICAL: lat/lon stay in this scope — never written to disk, prefs, or logs.

            if (!DfwBounds.contains(lat, lon)) {
                outOfRangeBox.visibility = View.VISIBLE
                resetSearchButton()
                return@launch
            }
            outOfRangeBox.visibility = View.GONE

            val radiusMiles = prefs.getInt(getString(R.string.pref_radius_key), 5)
            val includeFaith = prefs.getBoolean(getString(R.string.pref_faith_key), true)
            val langCode = prefs.getString(getString(R.string.pref_language_key), "en") ?: "en"

            // 1. Combine the bundled seed list with anything previously imported
            //    from an online search (cached in the local DB).
            val cachedOnline = withContext(Dispatchers.IO) {
                AppDatabase.get(this@MainActivity).services().all()
                    .filter { it.source == "overpass" }
            }
            val combinedRaw = (SeedData.all() + cachedOnline).distinctBy { it.id }

            // 2. Translate user-facing fields (no-op when language == English).
            val translator = TranslatorFactory.get(this@MainActivity)
            val combined = if (langCode == "en") combinedRaw
                else combinedRaw.map { translator.translateService(it, langCode) }

            // 3. Persist to the local cache so the data survives an offline launch.
            withContext(Dispatchers.IO) {
                AppDatabase.get(this@MainActivity).services().replaceAll(combined)
            }

            // 4. Filter by faith + radius, partition by category, sort by distance.
            lastResults = computeResults(combined, lat, lon, radiusMiles, includeFaith)
            hasShownResults = true

            // 5. Render.
            pagerAdapter.updateAll(lastResults)
            redrawMapMarkers(positionToCategory(viewPager.currentItem))
            resetSearchButton()
        }
    }

    private fun resetSearchButton() {
        searchButton.isEnabled = true
        searchButton.text = getString(R.string.search)
    }

    // -------------------------------------------------------------------
    // Online search (button: "Find more online")
    // -------------------------------------------------------------------

    /**
     * Hits the OpenStreetMap Overpass API for additional providers near the
     * user, then merges them into the local cache and re-runs the local
     * filter so the new entries show up immediately.
     */
    private fun onOnlineSearchClicked() {
        if (!LocationProvider.hasPermission(this)) {
            promptForLocationPermission()
            return
        }

        onlineSearchButton.isEnabled = false
        onlineSearchButton.text = getString(R.string.online_search_progress)

        lifecycleScope.launch {
            val location = LocationProvider.fetchOnce(this@MainActivity)
            if (location == null || !DfwBounds.contains(location.latitude, location.longitude)) {
                showToast(getString(R.string.online_search_failed))
                resetOnlineSearchButton()
                return@launch
            }

            val radiusMiles = prefs.getInt(getString(R.string.pref_radius_key), 5)
            val newServices = OverpassRefresher.search(
                location.latitude,
                location.longitude,
                radiusMiles
            )

            if (newServices.isEmpty()) {
                showToast(getString(R.string.online_search_none))
                resetOnlineSearchButton()
                return@launch
            }

            // Merge with whatever's already cached, dedupe by id, persist.
            val merged = withContext(Dispatchers.IO) {
                val dao = AppDatabase.get(this@MainActivity).services()
                val existing = dao.all()
                val combined = (existing + newServices).distinctBy { it.id }
                dao.replaceAll(combined)
                combined
            }

            merged.size - merged.count { it.source != "overpass" }
            showToast(getString(R.string.online_search_added, newServices.size))

            // Re-run the local search pipeline so the new entries appear in the list.
            doSearch()
            resetOnlineSearchButton()
        }
    }

    private fun resetOnlineSearchButton() {
        onlineSearchButton.isEnabled = true
        onlineSearchButton.text = getString(R.string.online_search)
    }

    // -------------------------------------------------------------------
    // Result computation + rendering
    // -------------------------------------------------------------------

    /**
     * Pure function: given the raw service list and the user's location,
     * returns a per-category list of (service, distance) pairs sorted by
     * distance and filtered by radius / faith preference.
     */
    private fun computeResults(
        services: List<Service>,
        lat: Double,
        lon: Double,
        radiusMiles: Int,
        includeFaith: Boolean
    ): Map<Category, List<Pair<Service, Double>>> {
        val grouped = services.asSequence()
            .filter { includeFaith || !it.faithBased }
            .map { svc -> svc to DistanceUtil.milesBetween(lat, lon, svc.latitude, svc.longitude) }
            .filter { (_, miles) -> miles <= radiusMiles }
            .sortedBy { it.second }
            .toList()
            .groupBy { it.first.category }

        return mapOf(
            Category.FOOD to (grouped[Category.FOOD] ?: emptyList()),
            Category.CLOTHING to (grouped[Category.CLOTHING] ?: emptyList()),
            Category.SHELTER to (grouped[Category.SHELTER] ?: emptyList())
        )
    }

    /** Re-paints the map pins for the currently selected tab's category. */
    private fun redrawMapMarkers(category: Category) {
        mapView.overlays.removeAll { it is Marker }
        if (!hasShownResults) {
            mapView.invalidate()
            return
        }
        for ((svc, _) in lastResults[category].orEmpty()) {
            mapView.overlays.add(buildMarker(svc))
        }
        mapView.invalidate()
    }

    private fun buildMarker(svc: Service): Marker {
        val isOpen = HoursParser.isOpenAt(svc.hours)
        val iconRes = if (isOpen) R.drawable.ic_dot_open else R.drawable.ic_dot_closed
        return Marker(mapView).apply {
            position = GeoPoint(svc.latitude, svc.longitude)
            title = svc.name
            snippet = svc.address
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = ContextCompat.getDrawable(this@MainActivity, iconRes)
        }
    }

    // -------------------------------------------------------------------
    // Cache priming
    // -------------------------------------------------------------------

    /**
     * On a fresh install the DB is empty. Seed it once with the bundled
     * starter list so the user has data to look at before their first
     * search — without needing their location.
     */
    private suspend fun primeCacheIfEmpty() {
        val cached = withContext(Dispatchers.IO) {
            AppDatabase.get(this@MainActivity).services().all()
        }
        if (cached.isEmpty()) {
            withContext(Dispatchers.IO) {
                AppDatabase.get(this@MainActivity).services().replaceAll(SeedData.all())
            }
        }
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private fun positionToCategory(position: Int): Category = when (position) {
        0 -> Category.FOOD
        1 -> Category.CLOTHING
        else -> Category.SHELTER
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // -------------------------------------------------------------------
    // Options menu
    // -------------------------------------------------------------------

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, PreferencesActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
