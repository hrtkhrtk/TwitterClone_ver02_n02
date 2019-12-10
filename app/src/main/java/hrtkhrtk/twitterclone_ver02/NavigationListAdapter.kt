//package hrtkhrtk.twitterclone
package hrtkhrtk.twitterclone_ver02

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class NavigationListAdapter(context: Context) : BaseAdapter() {
    private var mLayoutInflater: LayoutInflater
    private var mNavigationItemArrayList = ArrayList<NavigationItem>()

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return mNavigationItemArrayList.size
    }

    override fun getItem(position: Int): Any {
        return mNavigationItemArrayList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.nav_item, parent, false)
        }

        val title = convertView!!.findViewById<View>(R.id.title) as TextView
        title.text = mNavigationItemArrayList[position].title

        return convertView
    }

    fun setNavigationItemArrayList(navigationItemArrayList: ArrayList<NavigationItem>) {
        mNavigationItemArrayList = navigationItemArrayList
    }
}