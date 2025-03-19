package com.d3133558.IntMk1

import java.nio.ByteBuffer

enum class ActionType(val id: Byte) {
    MOVE(1);

    companion object{
        fun fromId(id: Byte) = values().first { it.id == id }
    }
}

data class GameMessage (
    val senderId: String,  //n+1 Bytes
    val x: Float,          //4 Bytes
    val y: Float,          //4 Bytes
    val action: ActionType //1 Byte
)

fun GameMessage.toByteArray(): ByteArray {
    val senderBytes = senderId.toByteArray(Charsets.UTF_8)
    val buffer = ByteBuffer.allocate(1+senderBytes.size+4+4+1)
    //Store the senderID length (1 Byte)
    buffer.put(senderBytes.size.toByte())
    //Store the senderID data (n bytes)
    buffer.put(senderBytes)
    //store x and y positions (4 Bytes + 4 Bytes)
    buffer.putFloat(x)
    buffer.putFloat(y)
    //store action (1 Byte)
    buffer.put(action.id)
    return buffer.array()
}

fun ByteArray.toGameMessage(): GameMessage {
    val buffer = ByteBuffer.wrap(this)
    //Read sender length (1 byte)
    val senderLength = buffer.get().toInt()
    val senderBytes = ByteArray(senderLength)
    //Read sender bytes
    val senderId = String(senderBytes, Charsets.UTF_8)
    val x = buffer.float
    val y = buffer.float
    //Read action (1 Byte)
    val action = ActionType.fromId(buffer.get())
    return GameMessage(senderId, x, y, action)
}
