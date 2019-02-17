package com.archit.myplayer.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView

import com.archit.myplayer.R
import com.archit.myplayer.activities.MainActivity
import com.archit.myplayer.fragments.AboutUsFragment
import com.archit.myplayer.fragments.FavouriteFragment
import com.archit.myplayer.fragments.MainScreenFragment
import com.archit.myplayer.fragments.SettingsFragment

import java.util.ArrayList

class NavigationDrawerAdapter(internal var contentList: ArrayList<String>, internal var getImages: IntArray, internal var mContext: Context) : RecyclerView.Adapter<NavigationDrawerAdapter.NavViewHolder>() {

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): NavViewHolder {
        val itemview = LayoutInflater.from(viewGroup.context).inflate(R.layout.row_custom_navigation_drawer, viewGroup, false)
        return NavViewHolder(itemview)
    }

    override fun onBindViewHolder(navViewHolder: NavViewHolder, i: Int) {
        navViewHolder.icon_GET.setBackgroundResource(getImages[i])
        navViewHolder.text_GET.text = contentList[i]
        navViewHolder.contentHolder.setOnClickListener {
            val x = navViewHolder.adapterPosition
            if (x == 0) {
                val mainScreenFragment = MainScreenFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, mainScreenFragment)
                        .commit()
            } else if (x == 1) {
                val favouriteFragment = FavouriteFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, favouriteFragment)
                        .commit()
            } else if (x == 2) {
                val settingsFragment = SettingsFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, settingsFragment)
                        .commit()
            } else {
                val aboutUsFragment = AboutUsFragment()
                (mContext as MainActivity).supportFragmentManager.beginTransaction().replace(R.id.details_fragment, aboutUsFragment)
                        .commit()
            }
            MainActivity.drawerLayout!!.closeDrawers()
        }
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    class NavViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var icon_GET: ImageView
        internal var text_GET: TextView
        internal var contentHolder: RelativeLayout

        init {
            this.icon_GET = itemView.findViewById(R.id.icon_navdrawer)
            this.text_GET = itemView.findViewById(R.id.text_navdrawer)
            this.contentHolder = itemView.findViewById(R.id.navdrawer_item_content_holder)
        }
    }

}
