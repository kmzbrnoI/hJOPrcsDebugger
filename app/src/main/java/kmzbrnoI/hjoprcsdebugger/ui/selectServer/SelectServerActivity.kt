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

    /**
     * The pager widget, which handles animation and allows swiping horizontally to access previous
     * and next wizard steps.
     */
    private lateinit var mPager: ViewPager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.server_pager_activity)

        if (savedInstanceState == null) {
            val fragment = FoundServers()
            supportFragmentManager.beginTransaction()
                .replace(R.id.servers_pager, fragment)
                .commit()

            // The pager adapter, which provides the pages to the view pager widget.
            val pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
            servers_pager.adapter = pagerAdapter
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
    }
}
