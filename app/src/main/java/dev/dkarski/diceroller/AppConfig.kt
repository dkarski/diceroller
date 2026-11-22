package dev.dkarski.diceroller

/** Per-app identity. Empty contact/support values hide their UI. App name lives in strings.xml. */
object AppConfig {
    const val DEVELOPER_NAME = "Daniel Karski"
    const val DEVELOPER_EMAIL = "daniel.karski5q@gmail.com"
    const val CONTACT_SUBJECT = "Feedback"
    const val SUPPORT_URL = "https://ko-fi.com/danielkarski"
    /** Name of the tip service shown in the Tip jar text ("…a tip on Ko-fi"); empty = the URL's host. */
    const val SUPPORT_NAME = "Ko-fi"
}
