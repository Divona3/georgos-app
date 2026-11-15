package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GrievousDao {

    @Query("SELECT * FROM farmer_passport WHERE id = 1")
    fun observeFarmerPassport(): Flow<FarmerPassportEntity?>

    @Query("SELECT * FROM farmer_passport WHERE id = 1")
    suspend fun getFarmerPassportOnce(): FarmerPassportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFarmerPassport(passport: FarmerPassportEntity)

    @Query("SELECT * FROM land_passports ORDER BY isSelected DESC, id ASC")
    fun observeLands(): Flow<List<LandPassportEntity>>

    @Query("SELECT * FROM land_passports ORDER BY isSelected DESC, id ASC")
    suspend fun getLandsOnce(): List<LandPassportEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLand(land: LandPassportEntity): Long

    @Update
    suspend fun updateLand(land: LandPassportEntity)

    @Query("UPDATE land_passports SET isSelected = (id = :landId)")
    suspend fun selectLand(landId: Long)

    @Query("SELECT * FROM climate_snapshots WHERE landId = :landId")
    fun observeClimateForLand(landId: Long): Flow<ClimateSnapshotEntity?>

    @Query("SELECT * FROM climate_snapshots")
    fun observeAllClimates(): Flow<List<ClimateSnapshotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertClimate(snapshot: ClimateSnapshotEntity)

    @Query("SELECT * FROM farming_reminders ORDER BY isCompleted ASC, dayOfMonth ASC, id DESC")
    fun observeReminders(): Flow<List<FarmingReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: FarmingReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: FarmingReminderEntity)

    @Query("DELETE FROM farming_reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)

    @Query("SELECT * FROM leaf_transactions ORDER BY timestampMillis DESC")
    fun observeLeafTransactions(): Flow<List<LeafTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeafTransaction(tx: LeafTransactionEntity)

    @Query("SELECT * FROM redeemed_prizes ORDER BY redeemedAtMillis DESC")
    fun observeRedeemedPrizes(): Flow<List<RedeemedPrizeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedeemedPrize(prize: RedeemedPrizeEntity)

    @Query("SELECT * FROM land_simulations ORDER BY createdAtMillis DESC")
    fun observeSimulations(): Flow<List<LandSimulationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSimulation(simulation: LandSimulationEntity)

    @Query("SELECT * FROM offline_dispatch_logs ORDER BY dispatchedAtMillis DESC LIMIT 20")
    fun observeDispatchLogs(): Flow<List<OfflineDispatchLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDispatchLog(log: OfflineDispatchLogEntity)

    @Transaction
    suspend fun completeReminderWithReward(reminder: FarmingReminderEntity) {
        if (!reminder.isCompleted) {
            updateReminder(
                reminder.copy(
                    isCompleted = true,
                    completedAtMillis = System.currentTimeMillis()
                )
            )
            insertLeafTransaction(
                LeafTransactionEntity(
                    title = "Task Completed: ${reminder.title}",
                    sourceType = "REMINDER",
                    leavesDelta = reminder.leavesReward
                )
            )
        }
    }
}
