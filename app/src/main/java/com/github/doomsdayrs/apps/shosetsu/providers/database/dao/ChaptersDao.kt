package com.github.doomsdayrs.apps.shosetsu.providers.database.dao

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import app.shosetsu.lib.Novel
import com.github.doomsdayrs.apps.shosetsu.common.ext.entity
import com.github.doomsdayrs.apps.shosetsu.common.ext.logID
import com.github.doomsdayrs.apps.shosetsu.domain.model.local.BooleanChapterIDTuple
import com.github.doomsdayrs.apps.shosetsu.domain.model.local.ChapterEntity
import com.github.doomsdayrs.apps.shosetsu.domain.model.local.CountIDTuple
import com.github.doomsdayrs.apps.shosetsu.domain.model.local.NovelEntity
import com.github.doomsdayrs.apps.shosetsu.providers.database.dao.base.BaseDao

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
 */

/**
 * shosetsu
 * 23 / 04 / 2020
 *
 * @author github.com/doomsdayrs
 */
@Dao
interface ChaptersDao : BaseDao<ChapterEntity> {
	@Transaction
	suspend fun insertAndReturnChapterEntity(chapterEntity: ChapterEntity): ChapterEntity =
			loadChapter(insertReplace(chapterEntity))

	@Query("SELECT * FROM chapters")
	fun loadAllChapters(): Array<ChapterEntity>

	@Query("SELECT * FROM chapters WHERE novelID = :novelID")
	fun loadLiveChapters(novelID: Int): LiveData<List<ChapterEntity>>

	@Query("SELECT * FROM chapters WHERE novelID = :novelID")
	suspend fun loadChapters(novelID: Int): List<ChapterEntity>

	@Query("SELECT * FROM chapters WHERE id = :chapterID LIMIT 1")
	fun loadChapter(chapterID: Int): ChapterEntity

	@Query("SELECT * FROM chapters WHERE _rowid_ = :rowID LIMIT 1")
	fun loadChapter(rowID: Long): ChapterEntity

	@Query("SELECT COUNT(*),id FROM chapters WHERE url = :chapterURL")
	fun loadChapterCount(chapterURL: String): CountIDTuple

	fun hasChapter(chapterURL: String): BooleanChapterIDTuple {
		val c = loadChapterCount(chapterURL)
		return BooleanChapterIDTuple(c.count > 0, c.id)
	}

	@Query("SELECT COUNT(*) FROM chapters WHERE readingStatus != 2")
	fun loadChapterUnreadCount(): Int

	@Query("SELECT id FROM chapters WHERE novelID = :novelID AND readingStatus != 2 ORDER BY `order` DESC")
	fun findLastUnread(novelID: Int): Int

	@Query("UPDATE chapters SET isSaved = 1 AND savePath = :path WHERE id = :id")
	fun setChapterSavePath(id: Int, path: String)

	@Query("UPDATE chapters SET isSaved = 0 AND savePath = NULL WHERE id = :chapterID")
	suspend fun removeChapterSavePath(chapterID: Int)


	private suspend fun handleAbortInsert(novelChapter: Novel.Chapter, novelEntity: NovelEntity) {
		Log.d(logID(), "Chapter\t${novelChapter.link}\t\t\twas not found, inserting")
		insertAbort(novelChapter.entity(novelEntity))
	}

	private suspend fun handleUpdate(chapterEntity: ChapterEntity, novelChapter: Novel.Chapter) {
		Log.d(logID(), "Chapter\t${chapterEntity.url}\t\t\twas found, updating")
		suspendedUpdate(chapterEntity.copy(
				title = novelChapter.title,
				releaseDate = novelChapter.release,
				order = novelChapter.order
		))
	}

	@Transaction
	suspend fun handleChapters(novelEntity: NovelEntity, list: List<Novel.Chapter>) {
		Log.d(logID(), "Handling Chapters : ${novelEntity.url} ${novelEntity.id}")
		val databaseChapters: List<ChapterEntity> = loadChapters(novelEntity.id!!)
		Log.d(logID(), "Chapters received : ${list.size}")
		Log.d(logID(), "Chapters in data  : ${databaseChapters.size}")

		list.forEach { novelChapter: Novel.Chapter ->
			Log.d(logID(), "Processing ${novelChapter.link}")
			databaseChapters.find { it.url == novelChapter.link }?.let {
				handleUpdate(it, novelChapter)
			} ?: handleAbortInsert(novelChapter, novelEntity)
		}
	}
}