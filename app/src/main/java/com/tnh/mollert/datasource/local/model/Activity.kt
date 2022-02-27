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


    fun getDecodedMessage(encodedMessage: String): String{
        return encodedMessage.split("***").joinToString("") { s ->
            if (s.startsWith("[*") && s.endsWith("*]")) {
                s.removePrefix("[*").removeSuffix("*]").split("/*").getOrNull(2) ?: ""
            } else {
                s
            }
        }
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

    fun getCreateBoardMessage(userEmail: String, userName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} created board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangeBoardDescMessage(userEmail: String, userName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} changed description of board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getLeaveBoardMessage(userEmail: String, userName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} left board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangeBoardVisMessage(userEmail: String, userName: String, boardId: String, boardName: String, vis: String): String{
        return "${getEncodedMember(userEmail, userName)} changed board ${getEncodedBoard(boardId, boardName)} visibility to $vis"
    }

    fun getCreateListMessage(userEmail: String, userName: String, boardId: String, boardName: String, listName: String): String{
        return "${getEncodedMember(userEmail, userName)} added list \"$listName\" to board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getCreateCardMessage(userEmail: String, userName: String, boardId: String, boardName: String, cardId: String, cardName: String): String{
        return "${getEncodedMember(userEmail, userName)} added card ${getEncodedCard(cardId, cardName)} to board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedLabelMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} changed label on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedCardDescMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} changed card description on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedCardNameMessage(userEmail: String, userName: String,  cardId: String, cardName: String, newName: String,  boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} rename card ${getEncodedCard(cardId, cardName)} to ${getEncodedCard(cardId, newName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangedCardCoverMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} changed cover on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAttachedImageMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} attached an image on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAttachedLinkMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} attached a link on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getCardAddDateMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} set date on card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getCardAchievedMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} achieved card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getActiveCardMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} activate card ${getEncodedCard(cardId, cardName)} to board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getJoinedCardMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} joined card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getLeftCardMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} left card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelCommendMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} deleted comment from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelAttachmentMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} deleted attachment from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelCardMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} deleted card ${getEncodedCard(cardId, cardName)} from board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAddWorkMessage(userEmail: String, userName: String, workName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} added work \"$workName\" to card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelTaskMessage(userEmail: String, userName: String, taskName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} deleted task \"$taskName\" from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getDelWorkMessage(userEmail: String, userName: String, workName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} deleted work \"$workName\" from card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getAddTaskMessage(userEmail: String, userName: String, taskName: String, cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} added task \"$taskName\" to card ${getEncodedCard(cardId, cardName)} in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getMarkCheckedDateMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} marked due date on card ${getEncodedCard(cardId, cardName)} complete in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getMarkUncheckedDateMessage(userEmail: String, userName: String,  cardId: String, cardName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} set due date on card ${getEncodedCard(cardId, cardName)} incomplete in board ${getEncodedBoard(boardId, boardName)}"
    }

    fun getChangeBoardBackgroundMessage(userEmail: String, userName: String, boardId: String, boardName: String): String{
        return "${getEncodedMember(userEmail, userName)} changed background of board ${getEncodedBoard(boardId, boardName)}"
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