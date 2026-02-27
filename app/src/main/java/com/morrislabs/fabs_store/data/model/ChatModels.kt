package com.morrislabs.fabs_store.data.model

import com.google.firebase.Timestamp

/**
 * Firestore Schema:
 *
 * conversations/{conversationId}
 *   - participants: List<String>          // [userId, storeId]
 *   - participantNames: Map<String, String> // {userId: "John Doe", storeId: "Salon X"}
 *   - lastMessage: String
 *   - lastMessageTimestamp: Timestamp
 *   - unreadCount: Map<String, Int>       // {userId: 0, storeId: 1}
 *
 *   messages/{messageId}
 *     - senderId: String
 *     - senderType: String                // "USER" or "STORE"
 *     - text: String
 *     - timestamp: Timestamp
 *     - read: Boolean
 *
 * users/{userId}
 *   - fcmToken: String
 *   - role: String                        // "CUSTOMER" or "STORE_OWNER"
 *   - displayName: String
 *
 * stores/{storeId}
 *   - fcmToken: String
 *   - ownerId: String
 *   - name: String
 *
 * reservations/{reservationId}
 *   - userId: String
 *   - storeId: String
 *   - storeName: String
 *   - serviceName: String
 *   - status: String                      // "BOOKED_PENDING_ACCEPTANCE", "BOOKED_ACCEPTED", etc.
 *   - reservationDate: String
 *   - startTime: String
 *   - endTime: String
 */

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Map<String, Int> = emptyMap()
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderType: String = "",
    val text: String = "",
    val timestamp: Timestamp? = null,
    val read: Boolean = false
)

object SenderType {
    const val USER = "USER"
    const val STORE = "STORE"
}
