package com.placeholder2312.researchwidgets.data

import android.content.ComponentName
import android.content.Intent

/** Intents that hand off to the Aftergleam fork. Shared by the widgets and the in-app test button. */
object AftergleamLinks {
    /**
     * Explicit intent for the fork's signature-protected PaperOpenActivity, without a paper id.
     * Widgets use it as a PendingIntent template and add the id per card (fill-in intent).
     */
    fun openPaperTemplate(): Intent =
        Intent(AftergleamContract.ACTION_OPEN_PAPER)
            .setComponent(ComponentName(AftergleamContract.PACKAGE, AftergleamContract.PAPER_OPEN_COMPONENT))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun openPaper(paperId: String): Intent =
        openPaperTemplate().putExtra(AftergleamContract.EXTRA_PAPER_ID, paperId)

    /** Opens Aftergleam's Library tab with [shelf] selected, e.g. [AftergleamContract.SHELF_DOWNLOADED] for the Offline chip. */
    fun openLibrary(shelf: String): Intent =
        Intent(AftergleamContract.ACTION_OPEN_LIBRARY)
            .setComponent(ComponentName(AftergleamContract.PACKAGE, AftergleamContract.PAPER_OPEN_COMPONENT))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .putExtra(AftergleamContract.EXTRA_LIBRARY_SHELF, shelf)

    /** Same intent the launcher uses to start Aftergleam from its icon. */
    fun launchApp(): Intent =
        Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(AftergleamContract.PACKAGE, AftergleamContract.PACKAGE + ".MainActivity"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
}
