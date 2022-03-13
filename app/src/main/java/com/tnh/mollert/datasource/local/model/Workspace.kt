package com.tnh.mollert.datasource.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Class represent workspace (team)
 */
@Entity
data class Workspace(
    @PrimaryKey
    val workspaceId: String,
    val workspaceName: String,
    var workspaceDesc: String? = null,
    val workspaceType: String = TYPE_OTHER
) {
    companion object{
        const val TYPE_EDUCATION = "Education"
        const val TYPE_MARKETING = "Marketing"
        const val TYPE_HUMAN_RESOURCES = "Human Resources"
        const val TYPE_SALES_CRM = "Sales CRM"
        const val TYPE_ENGINEERING_IT = "Engineering IT"
        const val TYPE_SMALL_BUSINESS = "Small Business"
        const val TYPE_OTHER = "Other"
    }
}