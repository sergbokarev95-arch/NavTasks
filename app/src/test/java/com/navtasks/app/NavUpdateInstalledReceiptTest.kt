package com.navtasks.app

import org.junit.Assert.*
import org.junit.Test

class NavUpdateInstalledReceiptTest {
    @Test fun onlyExactSystemConfirmedReplacedVersionCanBeShown() {
        assertTrue(navReceiptMatches(188, "0.4.166-rc20", 188, "0.4.166-rc20"))
        assertTrue(navReceiptMatches(200, "1.0.0", 200, "1.0.0"))
        assertFalse(navReceiptMatches(-1, null, 200, "1.0.0"))
        assertFalse(navReceiptMatches(0, "1.0.0", 200, "1.0.0"))
        assertFalse(navReceiptMatches(188, "", 188, "0.4.166-rc20"))
        assertFalse(navReceiptMatches(188, "0.4.166-rc20", 200, "1.0.0"))
        assertFalse(navReceiptMatches(188, "0.4.164-rc18", 188, "0.4.166-rc20"))
    }

    @Test fun onlySystemPackageReplacedRecordsReceiptAndUserAcknowledgesIt() {
        val dir = "src/main/java/com/navtasks/app/"
        val receiver = java.io.File(dir + "NavNotifications.kt").readText()
        val activity = java.io.File(dir + "MainActivity.kt").readText()
        val receipt = java.io.File(dir + "NavUpdateReceipt.kt").readText()
        val manifest = java.io.File("src/main/AndroidManifest.xml").readText()
        assertTrue(receiver.contains("intent.action == Intent.ACTION_MY_PACKAGE_REPLACED"))
        assertTrue(receiver.contains("NavUpdateReceipt.recordPackageReplaced(context)"))
        assertTrue(manifest.contains("android:name=\".NavSystemTimeReceiver\""))
        assertTrue(manifest.contains("android.intent.action.MY_PACKAGE_REPLACED"))
        assertTrue(activity.contains("installedUpdateNotice = NavUpdateReceipt.pending(this)"))
        assertTrue(activity.contains("NavUpdateInstalledNotice("))
        assertTrue(activity.contains("NavUpdateReceipt.acknowledge(this, receipt)"))
        assertTrue(activity.contains("NavUpdateOffer(settings = appSettings, dark = dark, context = this)"))
        assertTrue(receipt.contains("fun acknowledge(context: Context, version: String)"))
        assertTrue(receipt.contains(".remove(CODE).remove(NAME).commit()"))
        assertFalse(receipt.contains("clear()"))
        assertFalse(receipt.contains("DELETE"))
    }

    @Test fun testUpdaterEntryNotExposedInProductionButStableCheckRemains() {
        val dir = "src/main/java/com/navtasks/app/"
        val ui = java.io.File(dir + "PlanAndTasksUi.kt").readText()
        val qa = java.io.File(dir + "NavUpdateQa.kt").readText()
        val testButton = ui.indexOf("Проверить тестовый канал")
        val gated = ui.lastIndexOf("if (NavUpdateQa.isQaBuild(context))", testButton)
        val normal = ui.indexOf("Проверить обновления")
        assertTrue(gated > 0 && gated < testButton)
        assertTrue(normal > 0 && normal < testButton)
        assertTrue(ui.contains("settings.updateAutomaticUpdateOffers(it)"))
        assertTrue(qa.contains("isQaVersionName(name: String): Boolean = name.endsWith(\"-rc15\")"))
        assertFalse(NavUpdateQa.isQaVersionName("1.0.0"))
        assertFalse(NavUpdateQa.isQaVersionName("0.4.166-rc20"))
    }

    @Test fun noFirstInstallFalseSuccessAndOnlyOneDialogAtOnce() {
        val dir = "src/main/java/com/navtasks/app/"
        val activity = java.io.File(dir + "MainActivity.kt").readText()
        val receipt = java.io.File(dir + "NavUpdateReceipt.kt").readText()
        assertTrue(receipt.contains("pendingCode > 0L && pendingCode == installedCode"))
        assertTrue(receipt.contains("onDismissRequest = {}"))
        assertTrue(activity.contains("if (receipt != null)"))
        assertTrue(activity.contains("} else {\n                NavUpdateOffer("))
        assertTrue(receipt.contains("onClick = onAcknowledge"))
    }
}
