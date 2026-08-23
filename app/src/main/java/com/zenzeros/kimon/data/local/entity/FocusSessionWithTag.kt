package com.zenzeros.kimon.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class FocusSessionWithTag(
    @Embedded
    val session: FocusSessionEntity,
    @Relation(
        parentColumn = "tagId",
        entityColumn = "id"
    )
    val tag: TagEntity?
)
