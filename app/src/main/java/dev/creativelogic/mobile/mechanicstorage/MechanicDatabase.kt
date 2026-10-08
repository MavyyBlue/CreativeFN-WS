package dev.creativelogic.mobile.mechanicstorage

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import dev.creativelogic.graph.MechanicGraphCodec
import dev.creativelogic.catalog.InitialDeviceCatalog
import dev.creativelogic.model.SavedMechanic
import dev.creativelogic.model.MechanicRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Entity(tableName = "mechanics")
data class MechanicEntity(
    @PrimaryKey val id: String,
    val modifiedAtEpochMillis: Long,
    val documentJson: String,
)

@Dao
interface MechanicDao {
    @Query("SELECT * FROM mechanics ORDER BY modifiedAtEpochMillis DESC, id ASC")
    fun observeAll(): Flow<List<MechanicEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: MechanicEntity)
}

@Database(entities = [MechanicEntity::class], version = 1, exportSchema = true)
abstract class MechanicDatabase : RoomDatabase() {
    abstract fun mechanics(): MechanicDao
}

class RoomMechanicRepository(private val dao: MechanicDao) : MechanicRepository {
    private val codec = MechanicGraphCodec(InitialDeviceCatalog.catalog)
    override fun observeAll(): Flow<List<SavedMechanic>> = dao.observeAll().map { entities ->
        entities.map { codec.decode(it.documentJson) }
    }

    override suspend fun save(document: SavedMechanic) = dao.save(
        MechanicEntity(
            document.mechanicMetadata.id,
            document.mechanicMetadata.modifiedAtEpochMillis,
            codec.encode(document),
        ),
    )
}
