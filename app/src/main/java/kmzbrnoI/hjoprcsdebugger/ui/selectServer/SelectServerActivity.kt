package kmzbrnoI.hjoprcsdebugger.ui.selectServer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import kmzbrnoI.hjoprcsdebugger.R
import kotlinx.android.synthetic.main.server_pager_activity.*

/**
 * The number of pages (wizard steps) to show in this demo.
 */
private const val NUM_PAGES = 2


class SelectServerActivity : AppCompatActivity() {
    private lateinit var tabTitles: ArrayList<String>

    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_pager_activity)

        tabTitles = arrayListOf(getString(R.string.discovered_servers), getString(R.string.saved_servers))

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        servers_pager.adapter = pagerAdapter

        servers_tabs.setupWithViewPager(servers_pager)
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
            when (position) {
                0 -> {
                     FoundServers()
                }
                1 -> {
                    return StoredServers()
                }
            }
            return FoundServers()
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return tabTitles[position]
        }
    }
}
