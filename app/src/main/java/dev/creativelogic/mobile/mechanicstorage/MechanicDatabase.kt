package dev.creativelogic.mobile.mechanicstorage

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import dev.creativelogic.model.MechanicDocumentCodec
import dev.creativelogic.model.MechanicDocumentV1
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
    override fun observeAll(): Flow<List<MechanicDocumentV1>> = dao.observeAll().map { entities ->
        entities.map { MechanicDocumentCodec.decode(it.documentJson) }
    }

    override suspend fun save(document: MechanicDocumentV1) = dao.save(
        MechanicEntity(
            document.mechanicMetadata.id,
            document.mechanicMetadata.modifiedAtEpochMillis,
            MechanicDocumentCodec.encode(document),
        ),
    )
}
