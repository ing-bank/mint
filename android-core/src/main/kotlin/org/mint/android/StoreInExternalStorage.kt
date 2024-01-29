package com.ing.mint.android

import android.os.Environment
import androidx.test.platform.app.InstrumentationRegistry
import com.ing.mint.StateRepository
import com.ing.mint.android.query.AnyQuery
import java.io.File
import java.util.*

object StoreInExternalStorage {
    fun mintFolder() = File(
        InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(
            Environment.DIRECTORY_DOCUMENTS,
        ),
        "mint",
    )

    fun apply(
        repo: StateRepository<AndroidState>,
        reportID: String = UUID.randomUUID().toString(),
        target: File = mintFolder(),
    ): File {
        val states = repo.query(AnyQuery)

        val doc = AndroidStateUtils.toDoc(states)

        if (!target.exists()) {
            target.mkdirs()
        }

        val file = File(target, "report-$reportID.xml")

        AndroidStateUtils.writeDoc(doc, file)

        return file
    }
}
