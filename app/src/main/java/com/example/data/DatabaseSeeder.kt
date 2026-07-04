package com.example.data

import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

object DatabaseSeeder {
    suspend fun seedIfNeeded(dao: AppDao) {
        val existingSchools = dao.getAllSchools().first()
        if (existingSchools.isNotEmpty()) {
            return
        }

        // Seed Districts & Schools
        val schools = listOf(
            School("SCH-001", "Mphunzi Primary School", "Dedza", "Central Region"),
            School("SCH-002", "Chongoni Demonstration School", "Dedza", "Central Region"),
            School("SCH-003", "Monkey Bay Primary School", "Mangochi", "Southern Region"),
            School("SCH-004", "Namwera Model School", "Mangochi", "Southern Region"),
            School("SCH-005", "Mitundu Secondary School", "Lilongwe Rural", "Central Region"),
            School("SCH-006", "Chitedze Demonstration School", "Lilongwe Rural", "Central Region"),
            School("SCH-007", "Chulu Model Primary", "Kasungu", "Central Region"),
            School("SCH-008", "Wimbe Community Day Secondary", "Kasungu", "Central Region"),
            School("SCH-009", "Likhubula Primary School", "Mulanje", "Southern Region"),
            School("SCH-010", "Mulanje Mission School", "Mulanje", "Southern Region")
        )
        dao.insertSchools(schools)

        // Seed Teachers
        val teachers = listOf(
            Teacher("T-001", "Chisomo Banda", "SCH-001", "Standard 5, Standard 6", "cbanda@schools.gov.mw", 4.8, 98.2),
            Teacher("T-002", "Tiwonge Phiri", "SCH-001", "Standard 7, Standard 8", "tphiri@schools.gov.mw", 4.2, 94.0),
            Teacher("T-003", "Limbani Mwase", "SCH-002", "Standard 3, Standard 4", "lmwase@schools.gov.mw", 3.9, 91.5),
            Teacher("T-004", "Eliza Mtema", "SCH-003", "Standard 6", "emtema@schools.gov.mw", 4.5, 96.0),
            Teacher("T-005", "Mtendere Kawonga", "SCH-004", "Standard 8, Form 1", "mkawonga@schools.gov.mw", 4.1, 92.8),
            Teacher("T-006", "Wongani Chilima", "SCH-005", "Form 3, Form 4", "wchilima@schools.gov.mw", 4.7, 99.0),
            Teacher("T-007", "Yamikani Chanza", "SCH-006", "Standard 7", "ychanza@schools.gov.mw", 4.3, 95.5),
            Teacher("T-008", "Kondwani Gondwe", "SCH-007", "Standard 5, Standard 6", "kgondwe@schools.gov.mw", 3.5, 88.0),
            Teacher("T-009", "Favour Nkosi", "SCH-008", "Form 1, Form 2", "fnkosi@schools.gov.mw", 4.6, 97.5),
            Teacher("T-010", "Mercy Chimwala", "SCH-009", "Standard 4, Standard 5", "mchimwala@schools.gov.mw", 4.4, 96.8)
        )
        dao.insertTeachers(teachers)

        // Seed Students
        val students = listOf(
            // Mphunzi Primary
            Student("S-001", "Memory Gondwe", "SCH-001", "Standard 5", "Female"),
            Student("S-002", "Lameck Chimoyo", "SCH-001", "Standard 5", "Male"),
            Student("S-003", "Promise Tembo", "SCH-001", "Standard 6", "Female"),
            Student("S-004", "Gift Mvalo", "SCH-001", "Standard 6", "Male"),
            Student("S-005", "Atusaye Nyirenda", "SCH-001", "Standard 5", "Male"),
            // Chongoni
            Student("S-006", "Tiyamike Phiri", "SCH-002", "Standard 3", "Female"),
            Student("S-007", "Mayamiko Chunga", "SCH-002", "Standard 3", "Male"),
            Student("S-008", "Eness Kamanga", "SCH-002", "Standard 4", "Female"),
            Student("S-009", "Kelvin Chirwa", "SCH-002", "Standard 4", "Male"),
            // Monkey Bay
            Student("S-010", "Aisha Juma", "SCH-003", "Standard 6", "Female"),
            Student("S-011", "Yassin Mussa", "SCH-003", "Standard 6", "Male"),
            Student("S-012", "Fatima Alide", "SCH-003", "Standard 6", "Female"),
            // Mitundu
            Student("S-013", "Hope Chimwaza", "SCH-005", "Form 3", "Male"),
            Student("S-014", "Agness Mwale", "SCH-005", "Form 3", "Female"),
            Student("S-015", "Chimwemwe Nkhoma", "SCH-005", "Form 4", "Female"),
            // Chulu
            Student("S-016", "Esther Saka", "SCH-007", "Standard 5", "Female"),
            Student("S-017", "Zione Jere", "SCH-007", "Standard 5", "Female"),
            Student("S-018", "Precious Mphande", "SCH-007", "Standard 6", "Male")
        )
        dao.insertStudents(students)

        // Seed Historical Attendance Records over the last 7 days
        val attendanceRecords = mutableListOf<AttendanceRecord>()
        val dates = listOf("2026-06-24", "2026-06-25", "2026-06-26", "2026-06-27", "2026-06-28", "2026-06-29", "2026-06-30")
        
        // Let's seed attendance for all students on these dates
        val random = java.util.Random(42) // Seeded random for consistent data
        dates.forEach { dateStr ->
            students.forEach { student ->
                // 92% attendance rate overall, varying slightly per student
                val isPresent = random.nextFloat() < 0.92f
                val rawPayload = "school:${student.schoolId},student:${student.id},present:$isPresent,by:Dr. Chimwemwe Nkhoma,time:${System.currentTimeMillis()}"
                val encrypted = CryptoUtils.encrypt(rawPayload)
                attendanceRecords.add(
                    AttendanceRecord(
                        id = "ATT-${student.id}-${dateStr}",
                        date = dateStr,
                        studentId = student.id,
                        studentName = student.name,
                        schoolId = student.schoolId,
                        isPresent = isPresent,
                        submittedBy = "Dr. Chimwemwe Nkhoma",
                        timestamp = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis(),
                        syncStatus = "SYNCED",
                        encryptedPayload = encrypted
                    )
                )
            }
        }
        dao.insertAttendanceRecords(attendanceRecords)

        // Seed Historical Teacher Performance Reports
        val performanceReports = mutableListOf<PerformanceReport>()
        val evalDates = listOf("2026-06-20", "2026-06-22", "2026-06-25", "2026-06-28")
        
        teachers.forEach { teacher ->
            // Let's add 2 evaluations for each teacher on different dates
            val tEvalDates = if (teacher.id.hashCode() % 2 == 0) {
                listOf(evalDates[0], evalDates[2])
            } else {
                listOf(evalDates[1], evalDates[3])
            }
            
            tEvalDates.forEach { dateStr ->
                // Dynamic scores around their average
                val base = teacher.performanceScore.toInt().coerceIn(3, 5)
                val lesson = (base + (random.nextInt(3) - 1)).coerceIn(1, 5)
                val engagement = (base + (random.nextInt(3) - 1)).coerceIn(1, 5)
                val timeMgt = (base + (random.nextInt(3) - 1)).coerceIn(1, 5)
                
                val rawPayload = "teacher:${teacher.id},lesson:$lesson,engage:$engagement,time:$timeMgt,by:Dr. Chimwemwe Nkhoma"
                val encrypted = CryptoUtils.encrypt(rawPayload)
                
                performanceReports.add(
                    PerformanceReport(
                        id = "EVAL-${teacher.id}-${dateStr}",
                        date = dateStr,
                        teacherId = teacher.id,
                        teacherName = teacher.name,
                        schoolId = teacher.schoolId,
                        lessonPlanScore = lesson,
                        studentEngagementScore = engagement,
                        timeManagementScore = timeMgt,
                        submittedBy = "Dr. Chimwemwe Nkhoma",
                        timestamp = SimpleDateFormat("yyyy-MM-DD", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis(),
                        syncStatus = "SYNCED",
                        encryptedPayload = encrypted
                    )
                )
            }
        }
        dao.insertPerformanceReports(performanceReports)

        // Seed Student Tracks
        val studentTracks = mutableListOf<StudentTrack>()
        val trackTypes = listOf("Academic", "Attendance", "Behavior", "Literacy", "Numeracy")
        val trackStatuses = listOf("Exceeding", "On Track", "Remedial", "At Risk")
        val notesTemplates = mapOf(
            "Exceeding" to "Demonstrates strong comprehension, active leadership, and consistently submits assignments ahead of schedule.",
            "On Track" to "Meets standard requirements. Shows positive participation and steady comprehension in regular lessons.",
            "Remedial" to "Requires focused supplementary reading exercises. Making steady progress with dedicated support.",
            "At Risk" to "Requires immediate intervention. High level of distractions during independent work, and has missed several sessions."
        )

        val selectedStudents = students.take(8)
        selectedStudents.forEachIndexed { idx, student ->
            val trackType = trackTypes[idx % trackTypes.size]
            val trackStatus = trackStatuses[idx % trackStatuses.size]
            val notes = notesTemplates[trackStatus] ?: "Progress tracked steadily."
            val dateStr = "2026-06-29"
            val rawPayload = "student:${student.id},trackType:$trackType,status:$trackStatus,by:Chisomo Banda,notes:$notes"
            val encrypted = CryptoUtils.encrypt(rawPayload)
            studentTracks.add(
                StudentTrack(
                    id = "TRACK-${student.id}-${idx}",
                    studentId = student.id,
                    studentName = student.name,
                    className = student.className,
                    schoolId = student.schoolId,
                    trackType = trackType,
                    trackStatus = trackStatus,
                    notes = notes,
                    dateUpdated = dateStr,
                    updatedBy = "Chisomo Banda",
                    timestamp = System.currentTimeMillis() - 100000,
                    syncStatus = "SYNCED",
                    encryptedPayload = encrypted
                )
            )
        }
        dao.insertStudentTracks(studentTracks)
    }
}
