package com.droidsentinel.app.di

import android.content.Context
import androidx.room.Room
import com.droidsentinel.app.data.apk.ApkInspector
import com.droidsentinel.app.data.apps.InstalledAppRepository
import com.droidsentinel.app.data.history.ScanHistoryDatabase
import com.droidsentinel.app.data.hypatia.HypatiaDatabaseRepository
import com.droidsentinel.app.data.hypatia.HypatiaSignatureScanner
import com.droidsentinel.app.domain.advisor.LocalSecurityAdvisor
import com.droidsentinel.app.domain.analysis.MetadataRiskEngine
import com.droidsentinel.app.domain.scan.ScanCoordinator

class AppContainer(context: Context) {
    val scanner = HypatiaSignatureScanner()
    val databaseRepository = HypatiaDatabaseRepository(context, scanner)
    val installedApps = InstalledAppRepository(context)
    val apkInspector = ApkInspector(context)
    val metadataEngine = MetadataRiskEngine()
    val coordinator = ScanCoordinator(installedApps, scanner, metadataEngine, apkInspector)
    val advisor = LocalSecurityAdvisor()
    val history = Room.databaseBuilder(context, ScanHistoryDatabase::class.java, "scan-history.db")
        .fallbackToDestructiveMigration()
        .build()
    val preferences = context.getSharedPreferences("droidsentinel", Context.MODE_PRIVATE)
}
