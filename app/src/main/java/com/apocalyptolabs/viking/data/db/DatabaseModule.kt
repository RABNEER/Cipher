package com.apocalyptolabs.viking.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideVikingDatabase(
        @ApplicationContext context: Context
    ): VikingDatabase {
        val passphrase = DatabaseSecurityHelper.getOrGeneratePassphrase(context)
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(
            context,
            VikingDatabase::class.java,
            "viking_threat_db"
        )
            .openHelperFactory(factory)
            .addMigrations(VikingDatabase.MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideThreatLogDao(database: VikingDatabase): ThreatLogDao {
        return database.threatLogDao()
    }
}
