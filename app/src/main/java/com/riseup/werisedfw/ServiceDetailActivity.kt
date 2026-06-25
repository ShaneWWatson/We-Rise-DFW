package com.riseup.werisedfw

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.riseup.werisedfw.ServiceDetailActivity.Companion.EXTRA_SERVICE_ID
import com.riseup.werisedfw.data.AppDatabase
import com.riseup.werisedfw.data.Service
import com.riseup.werisedfw.i18n.TranslatorFactory
import com.riseup.werisedfw.util.HoursParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Detail screen for a single provider.
 *
 * Loads the [Service] from the local cache, translates it on the fly if a
 * non-English language is selected, and renders the full set of fields with
 * actionable buttons for **Directions**, **Call**, and **Visit website**.
 */
class ServiceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.detailRoot)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.updatePadding(
                left = systemBars.left,
                top = systemBars.top,
                right = systemBars.right,
                bottom = systemBars.bottom
                           )
            insets
        }

        findViewById<MaterialToolbar>(R.id.detailToolbar)
            .setNavigationOnClickListener { finish() }

        val id = intent.getStringExtra(EXTRA_SERVICE_ID)
        if (id == null) {
            finish(); return
        }

        lifecycleScope.launch { loadAndBind(id) }
    }

    // -------------------------------------------------------------------
    // Data loading + translation
    // -------------------------------------------------------------------

    /**
     * Loads the [Service] with [id] from the local database, optionally
     * translates its fields, then calls [bind].
     *
     * @param id The service identifier passed via [EXTRA_SERVICE_ID].
     */
    private suspend fun loadAndBind(id: String) {
        val raw = withContext(Dispatchers.IO) {
            AppDatabase.get(this@ServiceDetailActivity).services()
                .getById(id)
        }
        if (raw == null) {
            Toast.makeText(this, R.string.service_not_found, Toast.LENGTH_SHORT).show()
            finish(); return
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val langCode = prefs.getString(getString(R.string.pref_language_key), "en") ?: "en"
        val translator = TranslatorFactory.get(this)
        val service = if (langCode == "en") raw else translator.translateService(raw, langCode)

        bind(service)
    }

    // -------------------------------------------------------------------
    // View binding
    // -------------------------------------------------------------------

    /** Populates all view groups with data from [service]. */
    private fun bind(service: Service) {
        bindHeader(service)
        bindDescription(service)
        bindAddressBlock(service)
        bindPhoneBlock(service)
        bindWebsiteBlock(service)
    }

    /** Renders the name, open/closed status, and faith-based badge. */
    private fun bindHeader(service: Service) {
        findViewById<TextView>(R.id.detailName).text = service.name

        val isOpen = HoursParser.isOpenAt(service.hours)
        findViewById<ImageView>(R.id.detailStatusDot).setImageResource(
            if (isOpen) R.drawable.ic_dot_open else R.drawable.ic_dot_closed,
        )
        findViewById<TextView>(R.id.detailStatusLabel).text =
            getString(if (isOpen) R.string.open_now else R.string.closed)

        findViewById<ImageView>(R.id.detailFaithBadge).visibility =
            if (service.faithBased) View.VISIBLE else View.GONE
    }

    /** Renders the blurb and the formatted weekly hours. */
    private fun bindDescription(service: Service) {
        findViewById<TextView>(R.id.detailBlurb).text = service.blurb
        findViewById<TextView>(R.id.detailHours).text = HoursParser.pretty(service.hours)
    }

    /** Renders the address and wires the Directions actions to [openMap]. */
    private fun bindAddressBlock(service: Service) {
        val addressView = findViewById<TextView>(R.id.detailAddress)
        addressView.text = service.address
        addressView.setOnClickListener { openMap(service) }
        findViewById<MaterialButton>(R.id.buttonDirections).setOnClickListener { openMap(service) }
    }

    /** Renders the phone row, or hides it when no number is listed. */
    private fun bindPhoneBlock(service: Service) {
        val phoneView = findViewById<TextView>(R.id.detailPhone)
        val callButton = findViewById<MaterialButton>(R.id.buttonCall)
        val phoneLabel = findViewById<TextView>(R.id.detailPhoneLabel)

        if (service.phone.isNullOrBlank()) {
            phoneView.visibility = View.GONE
            callButton.visibility = View.GONE
            phoneLabel.visibility = View.GONE
            return
        }
        phoneView.text = service.phone
        phoneView.setOnClickListener { dial(service.phone) }
        callButton.setOnClickListener { dial(service.phone) }
    }

    /** Renders the website row, or hides it when no URL is listed. */
    private fun bindWebsiteBlock(service: Service) {
        val websiteView = findViewById<TextView>(R.id.detailWebsite)
        val websiteLabel = findViewById<TextView>(R.id.detailWebsiteLabel)
        val websiteButton: MaterialButton? = findViewById(R.id.buttonWebsite)

        if (service.website.isNullOrBlank()) {
            websiteView.visibility = View.GONE
            websiteLabel.visibility = View.GONE
            websiteButton?.visibility = View.GONE
            return
        }
        websiteView.text = service.website
        val click = View.OnClickListener { openUrl(service.website) }
        websiteView.setOnClickListener(click)
        websiteButton?.setOnClickListener(click)
    }

    // -------------------------------------------------------------------
    // Outbound intents
    // -------------------------------------------------------------------

    /** Open the user's default dialer with the provider's number pre-filled. */
    private fun dial(phone: String) {
        val tel = "tel:" + phone.filter { it.isDigit() || (it == '+') }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, tel.toUri()))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_phone_app, Toast.LENGTH_SHORT).show()
        }
    }

    /** Open the user's default browser to the provider's website. */
    private fun openUrl(url: String) {
        // Normalise the scheme using a case-insensitive check so that URLs like
        // "HTTPS://example.com" are not incorrectly double-prefixed.
        val uri = url.toUri()
        val safe = when (uri.scheme?.lowercase()) {
            "http", "https" -> uri
            else -> "https://$url".toUri()
        }
        // Final guard: only fire the intent if the resolved scheme is a web scheme.
        // This prevents non-http(s) URIs that could originate from untrusted OSM data
        // from being dispatched to arbitrary app handlers.
        if (safe.scheme?.lowercase() !in setOf("http", "https")) {
            Toast.makeText(this, R.string.no_browser_app, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            startActivity(Intent(Intent.ACTION_VIEW, safe))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, R.string.no_browser_app, Toast.LENGTH_SHORT).show()
        }
    }

    /** Open the user's default map app at the provider's coordinates. */
    private fun openMap(service: Service) {
        val q = Uri.encode(service.address)
        val geoUri = "geo:${service.latitude},${service.longitude}?q=$q"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, geoUri.toUri()))
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://maps.google.com/?q=$q".toUri()))
        }
    }

    companion object {
        /** Intent extra key for the service ID string. */
        const val EXTRA_SERVICE_ID = "service_id"
    }
}

