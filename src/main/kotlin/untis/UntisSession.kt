package untis

import config.UntisConfig
import kotlinx.io.IOException
import org.bytedream.untis4j.LoginException
import org.bytedream.untis4j.Session
import java.time.LocalDate
import mainLogger

inline fun closingUntisSession(config: UntisConfig, block: (session: Session) -> Unit) =
    try {
        Session.login(config.username, config.password, config.server, config.school ?: "").apply(block).logout()
    } catch(e: LoginException) {
        mainLogger.error("failed to login to untis")
        e.printStackTrace()
    } catch(e: IOException) {
        mainLogger.error("unknown exception occurred")
        e.printStackTrace()
    }


fun Session.timetable(from: LocalDate, to: LocalDate) = getTimetableFromPersonId(from, to, infos.personId)