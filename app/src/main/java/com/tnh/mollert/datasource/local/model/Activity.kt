package com.tnh.mollert.datasource.local.model

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import androidx.room.Entity
import androidx.room.PrimaryKey

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
        const val TYPE_INVITATION_WORKSPACE = "invitation_workspace"
        const val TYPE_INVITATION_BOARD = "invitation_board"
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
    fun getWorkspaceInvitationParams(eMessage: String): Pair<String, String>{
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

    /**
     * @return pair of board id and workspace id
     *
     *
     */
    fun getBoardInvitationParams(eMessage: String): Pair<String, String>{
        var first: String = ""
        var second: String = ""
        eMessage.split("***").forEach { s->
            if(s.startsWith("[*") && s.endsWith("*]")){
                val l = s.removePrefix("[*").removeSuffix("*]").split("/*")
                if(l.getOrNull(0) == HEADER_BOARD){
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

    fun getDecodedMessageWithMemberName(name: String, encodedMessage: String): String{
        return name + " " + getDecodedMessage(encodedMessage)
    }

    fun getEncodedContent(header: String, eMessage: String): String{
        eMessage.split("***").forEach { s->
            if(s.startsWith("[*") && s.endsWith("*]")){
                val l = s.removePrefix("[*").removeSuffix("*]").split("/*")
                if(l.getOrNull(0) == header){
                    return l.getOrElse(2){""}
                }
            }
        }
        return ""
    }

    fun getEncodedRef(header: String, eMessage: String): String{
        eMessage.split("***").forEach { s->
            if(s.startsWith("[*") && s.endsWith("*]")){
                val l = s.removePrefix("[*").removeSuffix("*]").split("/*")
                if(l.getOrNull(0) == header){
                    return l.getOrElse(1){""}
                }
            }
        }
        return ""
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

    fun getDecodedSpannableWithMemberName(name: String, encodedMessage: String): Spannable{
        val builder = SpannableStringBuilder("$name ")
        var start = 0
        var end = builder.length
        builder.setSpan(StyleSpan(android.graphics.Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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

    fun getBoardInvitationSenderMessage(boardId: String, boardName: String, otherEmail: String, otherName: String): String{
        return "invited ${getEncodedMember(otherEmail, otherName)} to ${getEncodedBoard(boardId, boardName)} board"
    }

    fun getBoardInvitationReceiverMessage(boardId: String, boardName: String, workspaceId: String, workspaceName: String, otherEmail: String, otherName: String): String{
        return "is invited by ${getEncodedMember(otherEmail, otherName)} to ${getEncodedBoard(boardId, boardName)} board${getEncodedWorkspace(workspaceId, workspaceName)}"
    }

    fun getCommentMessage(cardId: String, cardName: String, comment: String): String{
        return "commented ${getEncodedComment(comment)} on card ${getEncodedCard(cardId, cardName)}"
    }

    fun getCreateBoardMessage(boardId: String, boardName: String): String{
        return "created board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangeBoardDescMessage(boardId: String, boardName: String): String{
        return "changed description of board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getLeaveBoardMessage(boardId: String, boardName: String): String{
        return "left board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getJoinBoardMessage(boardId: String, boardName: String): String{
        return "joined board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangeBoardVisMessage(boardId: String, boardName: String, vis: String): String{
        return "changed board ${getEncodedBoard(boardId, boardName)} visibility to $vis"
    }

    fun getCreateListMessage(boardId: String, boardName: String, listName: String): String{
        return "added list \"$listName\" to board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getCreateCardMessage(boardId: String, boardName: String, cardId: String, cardName: String): String{
        return "added card ${getEncodedCard(cardId, cardName)} to board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedLabelMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "changed label on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedCardDescMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "changed card description on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedCardNameMessage(cardId: String, cardName: String, newName: String,  boardId: String, boardName: String): String{
        return "rename card ${getEncodedCard(cardId, cardName)} to ${getEncodedCard(cardId, newName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedCardCoverMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "changed cover on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAttachedImageMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "attached an image on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAttachedLinkMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "attached a link on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getCardAddDateMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "set date on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getCardAchievedMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "achieved card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getActiveCardMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "activate card ${getEncodedCard(cardId, cardName)} to board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getJoinedCardMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "joined card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getLeftCardMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "left card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelCommendMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "deleted comment from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelAttachmentMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "deleted attachment from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelCardMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "deleted card ${getEncodedCard(cardId, cardName)} from board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelListMessage(listName: String, boardId: String, boardName: String): String{
        return "deleted list \"$listName\" from board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAddWorkMessage(workName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "added work \"$workName\" to card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelTaskMessage(taskName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "deleted task \"$taskName\" from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelWorkMessage(workName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "deleted work \"$workName\" from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAddTaskMessage(taskName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "added task \"$taskName\" to card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getMarkCheckedDateMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "marked due date on card ${getEncodedCard(cardId, cardName)} complete in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getMarkUncheckedDateMessage(cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "set due date on card ${getEncodedCard(cardId, cardName)} incomplete in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangeBoardBackgroundMessage(boardId: String, boardName: String): String{
        return "changed background of board ${getEncodedBoard(boardId, boardName)}"
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