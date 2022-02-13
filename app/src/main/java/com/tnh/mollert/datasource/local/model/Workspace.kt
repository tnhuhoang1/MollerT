package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class represent workspace (team)
 */
@Entity
class Workspace(
    @PrimaryKey
    val workspaceId: String,
    val workspaceName: String,
    var workspaceDesc: String? = null,
    val workspaceType: String = TYPE_OTHER
) {
    companion object{
        const val TYPE_EDUCATION = "education"
        const val TYPE_MARKETING = "marketing"
        const val TYPE_HUMAN_RESOURCES = "human_resources"
        const val TYPE_SALES_CRM = "sales_crm"
        const val TYPE_ENGINEERING_IT = "engineering_it"
        const val TYPE_SMALL_BUSINESS = "small_business"
        const val TYPE_OTHER = "other"
    }
}