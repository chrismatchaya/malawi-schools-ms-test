package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class School(
    @PrimaryKey val id: String,
    val name: String,
    val district: String,
    val region: String
)

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey val id: String,
    val name: String,
    val schoolId: String,
    val classesTaught: String,
    val email: String,
    val performanceScore: Double, // 1.0 to 5.0
    val attendanceRate: Double // e.g. 95.5 for 95.5%
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey val id: String,
    val name: String,
    val schoolId: String,
    val className: String,
    val gender: String
)

@Entity(tableName = "attendance_records")
data class AttendanceRecord(
    @PrimaryKey val id: String, // UUID
    val date: String, // YYYY-MM-DD
    val studentId: String,
    val studentName: String,
    val schoolId: String,
    val isPresent: Boolean,
    val submittedBy: String,
    val timestamp: Long,
    val syncStatus: String, // "PENDING", "SYNCED"
    val encryptedPayload: String // Visualizing the AES-256 encrypted payload
)

@Entity(tableName = "performance_reports")
data class PerformanceReport(
    @PrimaryKey val id: String, // UUID
    val date: String,
    val teacherId: String,
    val teacherName: String,
    val schoolId: String,
    val lessonPlanScore: Int, // 1-5
    val studentEngagementScore: Int, // 1-5
    val timeManagementScore: Int, // 1-5
    val submittedBy: String,
    val timestamp: Long,
    val syncStatus: String, // "PENDING", "SYNCED"
    val encryptedPayload: String
)

@Entity(tableName = "sync_history")
data class SyncHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long,
    val type: String, // "ATTENDANCE", "PERFORMANCE", "FULL_SYNC"
    val recordsCount: Int,
    val status: String, // "SUCCESS", "FAILED"
    val details: String
)

@Entity(tableName = "student_tracks")
data class StudentTrack(
    @PrimaryKey val id: String, // UUID
    val studentId: String,
    val studentName: String,
    val className: String,
    val schoolId: String,
    val trackType: String, // "Academic", "Attendance", "Behavior", "Literacy", "Numeracy"
    val trackStatus: String, // "Exceeding", "On Track", "Remedial", "At Risk"
    val notes: String,
    val dateUpdated: String, // YYYY-MM-DD
    val updatedBy: String,
    val timestamp: Long,
    val syncStatus: String, // "PENDING", "SYNCED"
    val encryptedPayload: String // Offline-first secured payload
)

data class UserAccount(
    val username: String,
    val password: String,
    val fullName: String,
    val role: String // "Teacher" (Local School Administrator), "Admin" (Field Worker), "DEO" (District Education Officer)
)

