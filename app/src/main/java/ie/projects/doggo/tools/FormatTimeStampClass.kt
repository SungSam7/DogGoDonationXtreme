package ie.projects.doggo.tools

import android.app.Application
import android.text.format.DateFormat
import java.util.*

class FormatTimeStampClass : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {
        fun formatTimeStamp(timestamp: Long): String {
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            //format dd//MM/yyyy

            return DateFormat.format("dd/MM/yyyy", cal).toString()
        }
    }
}
