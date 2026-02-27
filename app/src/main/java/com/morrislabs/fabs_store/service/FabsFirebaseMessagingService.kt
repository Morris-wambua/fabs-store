package com.morrislabs.fabs_store.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.morrislabs.fabs_store.util.NotificationHelper
import com.morrislabs.fabs_store.util.TokenManager

class FabsFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FabsFirebaseMsgService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token received")
        updateFcmTokenInFirestore(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from: ${message.from}")

        val data = message.data
        val type = data["type"] ?: ""

        when (type) {
            "chat" -> handleChatNotification(data)
            "reservation_new" -> handleNewReservation(data)
            "reservation_update" -> handleReservationUpdate(data)
            else -> handleGenericNotification(message)
        }
    }

    private fun handleChatNotification(data: Map<String, String>) {
        val senderName = data["senderName"] ?: "New Message"
        val messageText = data["messageText"] ?: ""

        NotificationHelper.showNotification(
            context = this,
            title = senderName,
            body = messageText,
            channelId = NotificationHelper.CHANNEL_CHAT,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    private fun handleNewReservation(data: Map<String, String>) {
        val customerName = data["customerName"] ?: "A customer"
        val serviceName = data["serviceName"] ?: "a service"

        NotificationHelper.showNotification(
            context = this,
            title = "New Reservation",
            body = "$customerName booked $serviceName",
            channelId = NotificationHelper.CHANNEL_RESERVATIONS,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    private fun handleReservationUpdate(data: Map<String, String>) {
        val status = data["status"] ?: ""
        val serviceName = data["serviceName"] ?: "Reservation"

        NotificationHelper.showNotification(
            context = this,
            title = "Reservation Updated",
            body = "$serviceName status changed to $status",
            channelId = NotificationHelper.CHANNEL_RESERVATIONS,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    private fun handleGenericNotification(message: RemoteMessage) {
        val title = message.notification?.title ?: "Fabs Store"
        val body = message.notification?.body ?: ""

        NotificationHelper.showNotification(
            context = this,
            title = title,
            body = body,
            channelId = NotificationHelper.CHANNEL_CHAT,
            notificationId = System.currentTimeMillis().toInt()
        )
    }

    private fun updateFcmTokenInFirestore(token: String) {
        val userId = TokenManager.getInstance(applicationContext).getUserId() ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("stores")
            .whereEqualTo("ownerId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.firstOrNull()?.reference?.update("fcmToken", token)
                    ?.addOnSuccessListener { Log.d(TAG, "Store FCM token updated") }
                    ?.addOnFailureListener { Log.e(TAG, "Failed to update store FCM token", it) }
            }
            .addOnFailureListener { Log.e(TAG, "Failed to find store for FCM update", it) }
    }
}
