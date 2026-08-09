package com.apocalyptolabs.viking.domain.usecase

import android.content.Context
import android.content.pm.PackageManager
import com.apocalyptolabs.viking.core.ai.ThreatClassifier
import com.apocalyptolabs.viking.data.repository.ThreatRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class AuditPermissionsUseCaseTest {

    private lateinit var context: Context
    private lateinit var packageManager: PackageManager
    private lateinit var classifier: ThreatClassifier
    private lateinit var repository: ThreatRepository
    private lateinit var useCase: AuditPermissionsUseCase

    @Before
    fun setUp() {
        context = mock(Context::class.java)
        packageManager = mock(PackageManager::class.java)
        classifier = mock(ThreatClassifier::class.java)
        repository = mock(ThreatRepository::class.java)

        `when`(context.packageManager).thenReturn(packageManager)
        `when`(packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS)).thenReturn(emptyList())

        useCase = AuditPermissionsUseCase(context, classifier, repository)
    }

    @Test
    fun testAuditPermissions_emptyPackages_returnsEmptyList() = runBlocking {
        val results = useCase()
        assertNotNull(results)
        org.junit.Assert.assertTrue(results.isEmpty())
    }
}
