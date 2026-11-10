
package com.maziwasmart.app.data

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * All reading and writing of farm data in Cloud Firestore.
 * Reading uses snapshot listeners, so the screens update automatically.
 */
class FarmRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private fun userDoc(uid: String) =
        db.collection("users").document(uid)

    private fun collection(
        uid: String,
        name: String
    ): CollectionReference = userDoc(uid).collection(name)

    /**
     * Listens to a whole collection and turns each document into an object.
     */
    private fun <T : Any> listen(
        uid: String,
        name: String,
        type: Class<T>,
        withId: (T, String) -> T
    ): Flow<List<T>> = callbackFlow {

        val registration = collection(uid, name)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        doc.toObject(type)
                    }.getOrNull()?.let {
                        withId(it, doc.id)
                    }
                } ?: emptyList()

                trySend(items)
            }

        awaitClose {
            registration.remove()
        }
    }

    fun profile(uid: String): Flow<UserProfile?> = callbackFlow {

        val registration = userDoc(uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                trySend(
                    runCatching {
                        snapshot?.toObject(UserProfile::class.java)
                    }.getOrNull()
                )
            }

        awaitClose {
            registration.remove()
        }
    }

    fun cows(uid: String) =
        listen(uid, "cows", Cow::class.java) { item, id ->
            item.copy(id = id)
        }

    fun feedings(uid: String) =
        listen(uid, "feedings", FeedingRecord::class.java) { item, id ->
            item.copy(id = id)
        }

    fun milk(uid: String) =
        listen(uid, "milk", MilkRecord::class.java) { item, id ->
            item.copy(id = id)
        }

    fun expenses(uid: String) =
        listen(uid, "expenses", Expense::class.java) { item, id ->
            item.copy(id = id)
        }

    fun sales(uid: String) =
        listen(uid, "sales", MilkSale::class.java) { item, id ->
            item.copy(id = id)
        }

    suspend fun add(
        uid: String,
        collectionName: String,
        item: Any
    ) {
        collection(uid, collectionName).add(item).await()
    }

    suspend fun delete(
        uid: String,
        collectionName: String,
        id: String
    ) {
        collection(uid, collectionName).document(id).delete().await()
    }

    /**
     * Updates profile fields without deleting other fields
     * that may already exist in the user document.
     */
    suspend fun updateProfile(
        uid: String,
        profile: UserProfile
    ) {
        userDoc(uid)
            .set(profile, SetOptions.merge())
            .await()
    }
}

