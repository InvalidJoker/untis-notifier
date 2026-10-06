package untis

import org.bytedream.untis4j.responseObjects.Timetable
import java.time.LocalTime

data class LessonPart(val startTime: LocalTime, val endTime: LocalTime, val lesson: Timetable.Lesson)

