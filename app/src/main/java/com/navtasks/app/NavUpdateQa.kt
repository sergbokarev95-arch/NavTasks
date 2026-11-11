package com.navtasks.app

import android.content.Context

/** Deliberate exact RC-only gate: FALSE for all stable 1.0.x builds. */
internal object NavUpdateQa {
    fun isQaVersionName(name: String): Boolean = name.endsWith("-rc15")
    fun isQaBuild(context: Context): Boolean =
        isQaVersionName(context.packageManager
            .getPackageInfo(context.packageName, 0).versionName.orEmpty())
}
