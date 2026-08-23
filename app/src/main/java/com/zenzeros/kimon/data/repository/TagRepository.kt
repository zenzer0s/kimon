package com.zenzeros.kimon.data.repository

import com.zenzeros.kimon.data.local.dao.TagDao
import com.zenzeros.kimon.data.local.entity.TagEntity
import kotlinx.coroutines.flow.Flow

class TagRepository(private val tagDao: TagDao) {
    fun getAllActiveTags(): Flow<List<TagEntity>> = tagDao.getAllActiveTags()

    fun getTagById(id: Long): Flow<TagEntity?> = tagDao.getTagById(id)

    suspend fun insertTag(tag: TagEntity): Long = tagDao.insertTag(tag)

    suspend fun createTag(name: String, colorHex: String): Long = insertTag(TagEntity(name = name, colorHex = colorHex))

    suspend fun updateTag(tag: TagEntity) = tagDao.updateTag(tag)

    suspend fun deleteTag(tag: TagEntity) = tagDao.deleteTag(tag)
}
