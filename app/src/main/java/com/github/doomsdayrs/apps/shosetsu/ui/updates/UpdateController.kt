package com.github.doomsdayrs.apps.shosetsu.ui.updates

import android.os.Bundle
import android.util.Log
import android.view.View
import com.github.doomsdayrs.apps.shosetsu.backend.controllers.RecyclerController
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database
import com.github.doomsdayrs.apps.shosetsu.ui.updates.adapters.UpdatedNovelsAdapter
import com.github.doomsdayrs.apps.shosetsu.variables.Update
import java.util.*

/*
 * This file is part of shosetsu.
 *
 * shosetsu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * shosetsu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with shosetsu.  If not, see <https://www.gnu.org/licenses/>.
 * ====================================================================
 */ /**
 * shosetsu
 * 20 / 08 / 2019
 *
 * @author github.com/doomsdayrs
 */
class UpdateController : RecyclerController<UpdatedNovelsAdapter>() {
    var date: Long = -1
    private val novels = ArrayList<Int>()
    private var updates = ArrayList<Update>()

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong("date", date)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        date = savedInstanceState.getLong("date")
    }


    override fun onViewCreated(view: View) {
        updates = Database.DatabaseUpdates.getTimeBetween(date, date + 86399999)
        updates.forEach { if (!novels.contains(it.novelID)) novels.add(it.novelID) }
        adapter = UpdatedNovelsAdapter(novels, updates, activity!!)
        //UpdatedChaptersAdapter updatersAdapter = new UpdatedChaptersAdapter(updates, getActivity());
        recyclerView!!.post { adapter?.notifyDataSetChanged() }
        Log.d("Updates on this day: ", updates.size.toString())
    }
}