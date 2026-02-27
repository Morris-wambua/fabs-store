package com.morrislabs.fabs_store.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.morrislabs.fabs_store.data.model.ChatMessage
import com.morrislabs.fabs_store.data.model.Conversation
import com.morrislabs.fabs_store.data.model.SenderType
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository {

    private val db = FirebaseFirestore.getInstance()
    private val conversationsRef = db.collection("conversations")

    fun observeConversations(storeId: String): Flow<List<Conversation>> = callbackFlow {
        val listener = conversationsRef
            .whereArrayContains("participants", storeId)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val conversations = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Conversation::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(conversations)
            }
        awaitClose { listener.remove() }
    }

    fun observeMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = conversationsRef
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        text: String
    ) {
        val message = hashMapOf(
            "senderId" to senderId,
            "senderType" to SenderType.STORE,
            "text" to text,
            "timestamp" to Timestamp.now(),
            "read" to false
        )

        conversationsRef
            .document(conversationId)
            .collection("messages")
            .add(message)
            .await()

        conversationsRef
            .document(conversationId)
            .update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageTimestamp" to Timestamp.now()
                )
            )
            .await()
    }

    suspend fun getOrCreateConversation(
        storeId: String,
        storeName: String,
        userId: String,
        userName: String
    ): String {
        val existing = conversationsRef
            .whereArrayContains("participants", storeId)
            .get()
            .await()
            .documents
            .firstOrNull { doc ->
                val participants = doc.get("participants") as? List<*>
                participants?.contains(userId) == true
            }

        if (existing != null) return existing.id

        val conversation = hashMapOf(
            "participants" to listOf(userId, storeId),
            "participantNames" to mapOf(userId to userName, storeId to storeName),
            "lastMessage" to "",
            "lastMessageTimestamp" to Timestamp.now(),
            "unreadCount" to mapOf(userId to 0, storeId to 0)
        )

        return conversationsRef.add(conversation).await().id
    }

    suspend fun markMessagesAsRead(conversationId: String, storeId: String) {
        val unreadMessages = conversationsRef
            .document(conversationId)
            .collection("messages")
            .whereEqualTo("read", false)
            .whereNotEqualTo("senderId", storeId)
            .get()
            .await()

        val batch = db.batch()
        unreadMessages.documents.forEach { doc ->
            batch.update(doc.reference, "read", true)
        }
        batch.commit().await()

        conversationsRef
            .document(conversationId)
            .update("unreadCount.$storeId", 0)
            .await()
    }

    suspend fun registerStoreFcmToken(storeId: String, ownerId: String, storeName: String, token: String) {
        val storeData = hashMapOf(
            "fcmToken" to token,
            "ownerId" to ownerId,
            "name" to storeName
        )
        db.collection("stores").document(storeId).set(storeData).await()
    }
}
