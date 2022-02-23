package com.tnh.mollert.datasource.local.model

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tnh.mollert.utils.UserWrapper

@Entity
data class Activity(
    @PrimaryKey
    val activityId: String,
    val actor: String,
    val boardId: String? = null,
    val cardId: String? = null,
    var message: String = "",
    var seen: Boolean? = false,
    var activityType: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {


    fun getMessageAsText(){

    }


    fun setMessage(){


    }





    companion object{
        const val TYPE_INFO = "info"
        const val TYPE_INVITATION = "invitation"
        const val TYPE_COMMENT = "comment"
        const val TYPE_ACTION = "action"
    }

}

object MessageMaker{

    fun getEncodedBoard(boardId: String, boardName: String): String{
        return getEncoded(HEADER_BOARD, boardId, boardName)
    }

    fun getEncodedMember(memberEmail: String, memberName: String): String{
        return getEncoded(HEADER_MEMBER, memberEmail, memberName)
    }

    fun getEncodedWorkspace(workspaceId: String, workspaceName: String): String{
        return getEncoded(HEADER_WORKSPACE, workspaceId, workspaceName)
    }

    fun getEncodedCard(cardId: String, cardName: String): String{
        return getEncoded(HEADER_CARD, cardId, cardName)
    }

    fun getEncodedComment(commentContent: String): String{
        return getEncoded(HEADER_COMMENT, " ", commentContent)
    }

    /**
     * @return pair of email? and workspace id
     *
     *
     */
    fun getInvitationParams(eMessage: String): Pair<String, String>{
        var first: String = ""
        var second: String = ""
        eMessage.split("***").forEach { s->
            if(s.startsWith("[*") && s.endsWith("*]")){
                val l = s.removePrefix("[*").removeSuffix("*]").split("/*")
                if(l.getOrNull(0) == HEADER_MEMBER){
                    first = l.getOrElse(1){""}
                }else if(l.getOrNull(0) == HEADER_WORKSPACE){
                    second = l.getOrElse(1){""}
                }
            }
        }
        return Pair(first, second)
    }


    fun getDecodedMessage(encodedMessage: String): String{
        return encodedMessage.split("***").joinToString("") { s ->
            if (s.startsWith("[*") && s.endsWith("*]")) {
                s.removePrefix("[*").removeSuffix("*]").split("/*").getOrNull(2) ?: ""
            } else {
                s
            }
        }
    }

    fun getDecodedSpannable(encodedMessage: String): Spannable{
        val builder = SpannableStringBuilder("")
        var start = 0
        var end = 0
        encodedMessage.split("***").forEach { s->
            if (s.startsWith("[*") && s.endsWith("*]")) {
                val p = s.removePrefix("[*").removeSuffix("*]").split("/*").getOrNull(2) ?: ""
                start = end
                end += p.length
                builder.append(p)
                builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            } else {
                start = end
                end += s.length
                builder.append(s)
            }
        }
        return builder
    }

    fun getWorkspaceInvitationSenderMessage(workspaceId: String, workspaceName: String, otherEmail: String, otherName: String): String{
        return "invited ${getEncodedMember(otherEmail, otherName)} to ${getEncodedWorkspace(workspaceId, workspaceName)} workspace"
    }

    fun getWorkspaceInvitationReceiverMessage(workspaceId: String, workspaceName: String, otherEmail: String, otherName: String): String{
        return "is invited by ${getEncodedMember(otherEmail, otherName)} to ${getEncodedWorkspace(workspaceId, workspaceName)} workspace"
    }

    fun getCommentMessage(userEmail: String, userName: String, cardId: String, cardName: String, comment: String): String{
        return "${getEncodedMember(userEmail, userName)} comments ${getEncodedComment(comment)} on card ${getEncodedCard(cardId, cardName)}"
    }

    fun getCommentContent(encodedMessage: String): String{
        encodedMessage.split("***").forEach{ s ->
            if (s.startsWith("[*") && s.endsWith("*]")) {
                val list = s.removePrefix("[*").removeSuffix("*]").split("/*")
                if(list.getOrNull(0) == HEADER_COMMENT){
                    return list[2]
                }
            }
        }
        return ""
    }

    fun getEncoded(header: String, ref: String, content: String): String{
        return "***${getHeader(header)}${getRef(ref)}${getContent(content)}***"
    }

    fun getHeader(c: String) = "[*$c"
    fun getRef(c: String) = "/*$c/*"
    fun getContent(c: String) = "$c*]"
    const val HEADER_BOARD = "board"
    const val HEADER_WORKSPACE = "workspace"
    const val HEADER_MEMBER = "member"
    const val HEADER_COMMENT = "comment"
    const val HEADER_CARD = "card"

}