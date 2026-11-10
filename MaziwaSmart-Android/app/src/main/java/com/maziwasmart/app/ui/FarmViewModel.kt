
package com.maziwasmart.app.ui

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maziwasmart.app.data.AuthRepository
import com.maziwasmart.app.data.Cow
import com.maziwasmart.app.data.Expense
import com.maziwasmart.app.data.FarmRepository
import com.maziwasmart.app.data.FeedingRecord
import com.maziwasmart.app.data.MilkRecord
import com.maziwasmart.app.data.MilkSale
import com.maziwasmart.app.data.UserProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Holds all the farm data for the logged-in farmer.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FarmViewModel : ViewModel() {

    private val repo = FarmRepository()
    private val auth = AuthRepository()

    private val userId = MutableStateFlow(auth.currentUserId)

    val error = MutableStateFlow<String?>(null)

    private fun <T> load(
        source: (String) -> Flow<List<T>>
    ): StateFlow<List<T>> =
        userId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(emptyList())
            } else {
                source(uid).catch { e ->
                    error.value = e.message
                    emit(emptyList())
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            emptyList()
        )

    val cows: StateFlow<List<Cow>> = load { repo.cows(it) }

    val feedings: StateFlow<List<FeedingRecord>> =
        load { repo.feedings(it) }

    val milk: StateFlow<List<MilkRecord>> =
        load { repo.milk(it) }

    val expenses: StateFlow<List<Expense>> =
        load { repo.expenses(it) }

    val sales: StateFlow<List<MilkSale>> =
        load { repo.sales(it) }

    val profile: StateFlow<UserProfile?> =
        userId.flatMapLatest { uid ->
            if (uid == null) {
                flowOf(null)
            } else {
                repo.profile(uid).catch {
                    emit(null)
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    /**
     * Call after logging in so the new user's data loads.
     */
    fun refreshUser() {
        userId.value = auth.currentUserId
    }

    // Logout
    fun logout(activity: Activity, onLoggedOut: () -> Unit) {
        viewModelScope.launch {
            try {
                auth.logout(activity)

                userId.value = null
                onLoggedOut()

            } catch (e: Exception) {
                error.value = e.message ?: "Could not log out"
            }
        }
    }

    /**
     * Runs a save/delete operation in the background.
     */
    private fun save(
        onDone: (String?) -> Unit,
        action: suspend (uid: String) -> Unit
    ) {
        val uid = userId.value

        if (uid == null) {
            onDone("You are not logged in")
            return
        }

        viewModelScope.launch {
            try {
                action(uid)
                onDone(null)
            } catch (e: Exception) {
                onDone(e.message ?: "Something went wrong")
            }
        }
    }

    private fun now() = System.currentTimeMillis()

    fun addCow(cow: Cow, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.add(it, "cows", cow.copy(createdAt = now()))
        }

    fun deleteCow(id: String, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.delete(it, "cows", id)
        }

    fun addFeeding(record: FeedingRecord, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.add(it, "feedings", record.copy(createdAt = now()))
        }

    fun deleteFeeding(id: String, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.delete(it, "feedings", id)
        }

    fun addMilk(record: MilkRecord, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.add(it, "milk", record.copy(createdAt = now()))
        }

    fun deleteMilk(id: String, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.delete(it, "milk", id)
        }

    fun addExpense(expense: Expense, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.add(it, "expenses", expense.copy(createdAt = now()))
        }

    fun deleteExpense(id: String, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.delete(it, "expenses", id)
        }

    fun addSale(sale: MilkSale, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.add(it, "sales", sale.copy(createdAt = now()))
        }

    fun deleteSale(id: String, onDone: (String?) -> Unit) =
        save(onDone) {
            repo.delete(it, "sales", id)
        }

    fun updateProfile(
        profile: UserProfile,
        onDone: (String?) -> Unit
    ) = save(onDone) {
        repo.updateProfile(it, profile)
    }
}
