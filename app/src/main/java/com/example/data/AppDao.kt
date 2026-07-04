package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Schools ---
    @Query("SELECT * FROM schools ORDER BY name ASC")
    fun getAllSchools(): Flow<List<School>>

    @Query("SELECT * FROM schools WHERE district = :district ORDER BY name ASC")
    fun getSchoolsByDistrict(district: String): Flow<List<School>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<School>)

    // --- Teachers ---
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<Teacher>>

    @Query("SELECT * FROM teachers WHERE schoolId = :schoolId ORDER BY name ASC")
    fun getTeachersBySchool(schoolId: String): Flow<List<Teacher>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<Teacher>)

    // --- Students ---
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId ORDER BY name ASC")
    fun getStudentsBySchool(schoolId: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    // --- Attendance Records ---
    @Query("SELECT * FROM attendance_records ORDER BY timestamp DESC")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE syncStatus = 'PENDING'")
    suspend fun getPendingAttendanceRecords(): List<AttendanceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecord(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecord>)

    @Query("UPDATE attendance_records SET syncStatus = :status WHERE id = :id")
    suspend fun updateAttendanceSyncStatus(id: String, status: String)

    // --- Performance Reports ---
    @Query("SELECT * FROM performance_reports ORDER BY timestamp DESC")
    fun getAllPerformanceReports(): Flow<List<PerformanceReport>>

    @Query("SELECT * FROM performance_reports WHERE syncStatus = 'PENDING'")
    suspend fun getPendingPerformanceReports(): List<PerformanceReport>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformanceReport(report: PerformanceReport)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformanceReports(reports: List<PerformanceReport>)

    @Query("UPDATE performance_reports SET syncStatus = :status WHERE id = :id")
    suspend fun updatePerformanceSyncStatus(id: String, status: String)

    // --- Student Tracks ---
    @Query("SELECT * FROM student_tracks ORDER BY timestamp DESC")
    fun getAllStudentTracks(): Flow<List<StudentTrack>>

    @Query("SELECT * FROM student_tracks WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getStudentTracksForStudent(studentId: String): Flow<List<StudentTrack>>

    @Query("SELECT * FROM student_tracks WHERE syncStatus = 'PENDING'")
    suspend fun getPendingStudentTracks(): List<StudentTrack>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentTrack(track: StudentTrack)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentTracks(tracks: List<StudentTrack>)

    @Query("UPDATE student_tracks SET syncStatus = :status WHERE id = :id")
    suspend fun updateStudentTrackSyncStatus(id: String, status: String)

    // --- Sync History ---
    @Query("SELECT * FROM sync_history ORDER BY timestamp DESC")
    fun getSyncHistory(): Flow<List<SyncHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncHistory(history: SyncHistory)

    @Query("DELETE FROM sync_history")
    suspend fun clearSyncHistory()
}
