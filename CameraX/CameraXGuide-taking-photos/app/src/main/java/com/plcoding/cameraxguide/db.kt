import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    fun insertUser(
        heartRate: String,
        respiratoryRate: String,
        nausea: Int,
        headache: Int,
        diarrhea: Int,
        soreThroat: Int,
        fever: Int,
        muscleAche: Int,
        lossOfSmellOrTaste: Int,
        cough: Int,
        shortnessOfBreath: Int,
        feelingTired: Int
    ) {
        viewModelScope.launch {
            val user = User(
                heartRate = heartRate,
                respiratoryRate = respiratoryRate,
                nausea = nausea,
                headache = headache,
                diarrhea = diarrhea,
                soreThroat = soreThroat,
                fever = fever,
                muscleAche = muscleAche,
                lossOfSmellOrTaste = lossOfSmellOrTaste,
                cough = cough,
                shortnessOfBreath = shortnessOfBreath,
                feelingTired = feelingTired
            )
            repository.insert(user)
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            val users = repository.getAllUsers()
            // Handle the list of users, e.g., update UI
        }
    }
}


class UserRepository(application: Application) {
    private val userDao: UserDao

    init {
        val db = UserDatabase.getDatabase(application)
        userDao = db.userDao()
    }

    suspend fun insert(user: User) {
        withContext(Dispatchers.IO) {
            userDao.insert(user)
        }
    }

    suspend fun getAllUsers(): List<User> {
        return withContext(Dispatchers.IO) {
            userDao.getAllUsers()
        }
    }
}


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
    suspend fun insert(user: User)

    @Query("SELECT * FROM user_table")
    suspend fun getAllUsers(): List<User>

    @Delete
    suspend fun deleteUser(user: User)
}


@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: UserDatabase? = null

        fun getDatabase(context: Context): UserDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UserDatabase::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
