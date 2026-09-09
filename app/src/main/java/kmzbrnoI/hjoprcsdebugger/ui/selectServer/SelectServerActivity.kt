package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import kmzbrnoI.hjoprcsdebugger.BuildConfig
import kmzbrnoI.hjoprcsdebugger.R
import kmzbrnoI.hjoprcsdebugger.constants.STORED_SERVERS_RELOAD
import kmzbrnoI.hjoprcsdebugger.databinding.ServerPagerActivityBinding
import kmzbrnoI.hjoprcsdebugger.ui.createServer.CreateServerActivity


/**
 * The number of pages (wizard steps) to show in this demo.
 */
private const val NUM_PAGES = 2


class SelectServerActivity : AppCompatActivity() {
    private lateinit var tabTitles: ArrayList<String>

    private lateinit var mPager: ViewPager
    private lateinit var binding: ServerPagerActivityBinding

    private val createServerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(STORED_SERVERS_RELOAD, it.resultCode, it.data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ServerPagerActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        mPager = binding.serversPager
        title = getString(R.string.app_name) + " v"+ BuildConfig.VERSION_NAME

        tabTitles = arrayListOf(getString(R.string.discovered_servers), getString(R.string.saved_servers))

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        binding.serversPager.adapter = pagerAdapter

        binding.serversTabs.setupWithViewPager(binding.serversPager)

        binding.createNewServerButton.setOnClickListener{
            val intent = Intent(this, CreateServerActivity::class.java)
            createServerLauncher.launch(intent)
        }

        val callback = object : OnBackPressedCallback(mPager.currentItem > 0) {
            override fun handleOnBackPressed() {
                mPager.currentItem -= 1
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        mPager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                callback.isEnabled = position > 0
            }
        })
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> {
                    return FoundServers()
                }
                1 -> {
                    return StoredServers()
                }
            }
            return FoundServers()
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabTitles[position]
        }
    }
}
