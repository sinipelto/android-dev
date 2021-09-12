package fi.tuni.sinipelto.disqs.ui.adapter

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import fi.tuni.sinipelto.disqs.R
import fi.tuni.sinipelto.disqs.model.AppData
import fi.tuni.sinipelto.disqs.ui.fragment.MapFragment
import fi.tuni.sinipelto.disqs.ui.fragment.OwnpostsFragment
import fi.tuni.sinipelto.disqs.ui.fragment.WorldFragment

private val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2,
    R.string.tab_text_3
)

/**
 * that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
@Suppress("DEPRECATION")
class SectionsPagerAdapter(
    private val context: Context,
    private val appData: AppData,
    fm: FragmentManager,
) :
    androidx.fragment.app.FragmentStatePagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        val tab = position + 1

        //Log.d(TAG, "Position: $position")

        when (position) {
            0 -> {
                //Log.d(TAG, "Loading Tab WORLD..")
                return WorldFragment.newInstance(tab, appData)
            }
            1 -> {
                //Log.d(TAG, "Loading Tab OWNPOSTS..")
                return OwnpostsFragment.newInstance(tab, appData)
            }
            2 -> {
                //Log.d(TAG, "Loading Tab MAP..")
                return MapFragment.newInstance(tab)
            }
            else -> {
                Log.e(TAG, "ERROR: Requested tab item position out of bounds")
                throw IndexOutOfBoundsException("Position out of bounds!")
            }
        }
    }

    override fun getPageTitle(position: Int): CharSequence {
        return context.resources.getString(TAB_TITLES[position])
    }

    override fun getCount(): Int {
        // Show total pages.
        return TAB_TITLES.size
    }

    companion object {
        private const val TAG = "disqs.SECTIONS_PAGER"
    }
}