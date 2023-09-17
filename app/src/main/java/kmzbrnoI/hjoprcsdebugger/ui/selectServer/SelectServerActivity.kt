package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.content.Intent
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ServerPagerActivityBinding.inflate(layoutInflater)
        setContentView(R.layout.server_pager_activity)
        mPager = findViewById(R.id.servers_pager)
        setTitle(getString(R.string.app_name) + " v"+ BuildConfig.VERSION_NAME)

        tabTitles = arrayListOf(getString(R.string.discovered_servers), getString(R.string.saved_servers))

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        binding.serversPager.adapter = pagerAdapter

        binding.serversTabs.setupWithViewPager(binding.serversPager)

        binding.createNewServerButton.setOnClickListener{
            val intent = Intent(this, CreateServerActivity::class.java)
            startActivityForResult(intent, STORED_SERVERS_RELOAD)
        }
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            mPager.currentItem = mPager.currentItem - 1
        }
    }

    /**
     * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
     * sequence.
     */
    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return if (position == 1) StoredServers() else FoundServers()
        }

        override fun getPageTitle(position: Int): CharSequence {
            return tabTitles[position]
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        for (fragment in supportFragmentManager.fragments) {
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }
}
