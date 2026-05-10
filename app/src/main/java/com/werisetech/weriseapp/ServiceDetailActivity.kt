package com.werisetech.weriseapp

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.werisetech.weriseapp.data.AppDatabase
import com.werisetech.weriseapp.data.Service
import com.werisetech.weriseapp.i18n.TranslatorFactory
import com.werisetech.weriseapp.util.HoursParser
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_service_detail)

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

    private suspend fun loadAndBind(id: String) {
        val raw = withContext(Dispatchers.IO) {
            AppDatabase.get(this@ServiceDetailActivity).services()
                .all()
                .firstOrNull { it.id == id }
        }
        if (raw == null) {
            Toast.makeText(this, "Service not found", Toast.LENGTH_SHORT).show()
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

    private fun bind(service: Service) {
        bindHeader(service)
        bindDescription(service)
        bindAddressBlock(service)
        bindPhoneBlock(service)
        bindWebsiteBlock(service)
    }

    private fun bindHeader(service: Service) {
        findViewById<TextView>(R.id.detailName).text = service.name

        val isOpen = HoursParser.isOpenAt(service.hours)
        findViewById<ImageView>(R.id.detailStatusDot).setImageResource(
            if (isOpen) R.drawable.ic_dot_open else R.drawable.ic_dot_closed
        )
        findViewById<TextView>(R.id.detailStatusLabel).text =
            getString(if (isOpen) R.string.open_now else R.string.closed)

        findViewById<ImageView>(R.id.detailFaithBadge).visibility =
            if (service.faithBased) View.VISIBLE else View.GONE
    }

    private fun bindDescription(service: Service) {
        findViewById<TextView>(R.id.detailBlurb).text = service.blurb
        findViewById<TextView>(R.id.detailHours).text = HoursParser.pretty(service.hours)
    }

    private fun bindAddressBlock(service: Service) {
        val addressView = findViewById<TextView>(R.id.detailAddress)
        addressView.text = service.address
        addressView.setOnClickListener { openMap(service) }
        findViewById<MaterialButton>(R.id.buttonDirections).setOnClickListener { openMap(service) }
    }

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

    private fun bindWebsiteBlock(service: Service) {
        val websiteView = findViewById<TextView>(R.id.detailWebsite)
        val websiteLabel = findViewById<TextView>(R.id.detailWebsiteLabel)
        val websiteButton = findViewById<MaterialButton>(R.id.buttonWebsite)

        if (service.website.isNullOrBlank()) {
            websiteView.visibility = View.GONE
            websiteLabel.visibility = View.GONE
            websiteButton.visibility = View.GONE
            return
        }
        websiteView.text = service.website
        val click = View.OnClickListener { openUrl(service.website) }
        websiteView.setOnClickListener(click)
        websiteButton.setOnClickListener(click)
    }

    // -------------------------------------------------------------------
    // Outbound intents
    // -------------------------------------------------------------------

    /** Open the user's default dialer with the provider's number pre-filled. */
    private fun dial(phone: String) {
        val tel = "tel:" + phone.filter { it.isDigit() || it == '+' }
        try {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(tel)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No phone app installed", Toast.LENGTH_SHORT).show()
        }
    }

    /** Open the user's default browser to the provider's website. */
    private fun openUrl(url: String) {
        val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url
                          else "https://$url"
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(normalized)))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No browser installed", Toast.LENGTH_SHORT).show()
        }
    }

    /** Open the user's default map app at the provider's coordinates. */
    private fun openMap(service: Service) {
        val q = Uri.encode(service.address)
        val geoUri = Uri.parse("geo:${service.latitude},${service.longitude}?q=$q")
        try {
            startActivity(Intent(Intent.ACTION_VIEW, geoUri))
        } catch (e: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$q")))
        }
    }

    companion object {
        const val EXTRA_SERVICE_ID = "service_id"
    }
}
