package com.mobile.project1Sensors

import android.content.Context
import android.util.Log
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase


@Entity(tableName = "user_table")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val heartRate: String,
    val respiratoryRate: String,
    val nausea: Int,
    val headache: Int,
    val diarrhea: Int,
    val soreThroat: Int,
    val fever: Int,
    val muscleAche: Int,
    val lossOfSmellOrTaste: Int,
    val cough: Int,
    val shortnessOfBreath: Int,
    val feelingTired: Int
)


@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(user: User)
}


@Database(entities = [User::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            Log.d("Get db", "database")
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "signs_symptoms_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
