package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AttendanceRecord
import com.example.data.School
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvReportExporter {

    fun exportAttendanceToCsv(
        context: Context,
        district: String,
        schools: List<School>,
        attendanceRecords: List<AttendanceRecord>
    ) {
        try {
            // Map of schoolId to schoolName for fast lookup
            val schoolMap = schools.associateBy { it.id }

            // Define CSV Headers
            val headers = listOf(
                "Record ID",
                "Date",
                "School Name",
                "District",
                "Student ID",
                "Student Name",
                "Status",
                "Submitted By",
                "Timestamp",
                "Sync Status"
            )

            val csvContent = java.lang.StringBuilder()
            
            // Append header row
            csvContent.append(headers.joinToString(",") { escapeCsvValue(it) }).append("\n")

            // Format timestamp helper
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

            // Sort records chronologically
            val sortedRecords = attendanceRecords.sortedWith(
                compareBy<AttendanceRecord> { it.date }.thenBy { it.studentName }
            )

            // Append each record row
            for (record in sortedRecords) {
                val school = schoolMap[record.schoolId]
                val schoolName = school?.name ?: "Unknown School"
                val schoolDistrict = school?.district ?: district
                val statusStr = if (record.isPresent) "Present" else "Absent"
                val timeStr = try {
                    sdf.format(Date(record.timestamp))
                } catch (e: Exception) {
                    ""
                }

                val rowValues = listOf(
                    record.id,
                    record.date,
                    schoolName,
                    schoolDistrict,
                    record.studentId,
                    record.studentName,
                    statusStr,
                    record.submittedBy,
                    timeStr,
                    record.syncStatus
                )

                csvContent.append(rowValues.joinToString(",") { escapeCsvValue(it) }).append("\n")
            }

            // Save CSV string to a file in cache
            val fileName = "Attendance_Report_${district.replace(" ", "_")}.csv"
            val cacheFile = File(context.cacheDir, fileName)
            FileOutputStream(cacheFile).use { writer ->
                writer.write(csvContent.toString().toByteArray(Charsets.UTF_8))
            }

            // Generate content URI using FileProvider
            val fileUri = FileProvider.getUriForFile(
                context,
                "com.example.provider",
                cacheFile
            )

            // Intent to share/export CSV with spreadsheet compatible app or files
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_SUBJECT, "Attendance Audit Report - $district")
                putExtra(Intent.EXTRA_TEXT, "Malawi School Attendance Ledger spreadsheet CSV export for $district District.")
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Wrap in chooser and launch
            val chooserIntent = Intent.createChooser(intent, "Export CSV to Spreadsheet/Files").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error exporting CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Helper to properly escape CSV values according to RFC 4180
     */
    private fun escapeCsvValue(value: String): String {
        var escaped = value
        if (escaped.contains("\"")) {
            escaped = escaped.replace("\"", "\"\"")
        }
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            escaped = "\"$escaped\""
        }
        return escaped
    }
}
