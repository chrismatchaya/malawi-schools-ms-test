package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Malawi Schools MS", appName)
  }

  @Test
  fun testPdfReportGeneration() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val report = com.example.viewmodel.GeneratedReport(
      title = "Academic Progress Report - Dedza",
      date = "2026-06-30 05:13",
      body = "Some test body text",
      attendanceRate = 95.5,
      performanceScore = 4.5,
      schoolsCount = 5
    )
    val teachers = listOf(
      com.example.data.Teacher(
        id = "t-1",
        name = "John Phiri",
        schoolId = "s-1",
        classesTaught = "Standard 5 Maths",
        email = "john.phiri@example.com",
        performanceScore = 4.2,
        attendanceRate = 96.0
      )
    )
    
    // Clear old file if exists
    val cacheFile = java.io.File(context.cacheDir, "Malawi_Academic_Progress_Report.pdf")
    if (cacheFile.exists()) {
      cacheFile.delete()
    }
    
    com.example.util.PdfReportExporter.exportToPdf(
      context = context,
      report = report,
      teachers = teachers,
      lessonScore = 4.2f,
      engagementScore = 4.5f,
      timeScore = 4.3f
    )
    
    // Check if the PDF file was successfully created in cache directory
    org.junit.Assert.assertTrue(cacheFile.exists())
    org.junit.Assert.assertTrue(cacheFile.length() > 0)
  }

  @Test
  fun testCsvReportGeneration() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val schools = listOf(
      com.example.data.School(
        id = "s-1",
        name = "Chitedze Primary",
        district = "Lilongwe Rural",
        region = "Central"
      )
    )
    val attendanceRecords = listOf(
      com.example.data.AttendanceRecord(
        id = "record-1",
        date = "2026-07-01",
        studentId = "st-1",
        studentName = "Gift Banda",
        schoolId = "s-1",
        isPresent = true,
        submittedBy = "teacher-1",
        timestamp = System.currentTimeMillis(),
        syncStatus = "SYNCED",
        encryptedPayload = "encrypted_data"
      )
    )
    
    val fileName = "Attendance_Report_Lilongwe_Rural.csv"
    val cacheFile = java.io.File(context.cacheDir, fileName)
    if (cacheFile.exists()) {
      cacheFile.delete()
    }
    
    com.example.util.CsvReportExporter.exportAttendanceToCsv(
      context = context,
      district = "Lilongwe Rural",
      schools = schools,
      attendanceRecords = attendanceRecords
    )
    
    // Check if the CSV file was successfully created in the cache directory
    org.junit.Assert.assertTrue(cacheFile.exists())
    org.junit.Assert.assertTrue(cacheFile.length() > 0)
    
    // Verify file content contains key strings
    val content = cacheFile.readText()
    org.junit.Assert.assertTrue(content.contains("Record ID"))
    org.junit.Assert.assertTrue(content.contains("Gift Banda"))
    org.junit.Assert.assertTrue(content.contains("Chitedze Primary"))
    org.junit.Assert.assertTrue(content.contains("Present"))
  }

  @Test
  fun testSecureLoginAndRBAC() {
    val application = ApplicationProvider.getApplicationContext<android.app.Application>()
    val viewModel = com.example.viewmodel.MainViewModel(application)
    
    // 1. Initial State: logged out
    org.junit.Assert.assertFalse(viewModel.isLoggedIn.value)
    org.junit.Assert.assertNull(viewModel.loginError.value)
    
    // 2. Failed Login: invalid credentials
    val failed = viewModel.login("wrong_user", "wrong_password")
    org.junit.Assert.assertFalse(failed)
    org.junit.Assert.assertFalse(viewModel.isLoggedIn.value)
    org.junit.Assert.assertNotNull(viewModel.loginError.value)
    
    // 3. Successful Login: Local School Administrator
    val loginSchoolAdmin = viewModel.login("school_admin", "admin@2026")
    org.junit.Assert.assertTrue(loginSchoolAdmin)
    org.junit.Assert.assertTrue(viewModel.isLoggedIn.value)
    org.junit.Assert.assertEquals("Teacher", viewModel.currentRole.value)
    org.junit.Assert.assertEquals("Chisomo Banda", viewModel.currentUser.value)
    org.junit.Assert.assertNull(viewModel.loginError.value)
    
    // 4. Logout
    viewModel.logout()
    org.junit.Assert.assertFalse(viewModel.isLoggedIn.value)
    
    // 5. Successful Login: Field Worker
    val loginFieldWorker = viewModel.login("field_worker", "field@2026")
    org.junit.Assert.assertTrue(loginFieldWorker)
    org.junit.Assert.assertEquals("Admin", viewModel.currentRole.value)
    org.junit.Assert.assertEquals("District Admin Matchaya", viewModel.currentUser.value)
    
    // 6. Logout and Successful Login: District Education Officer
    viewModel.logout()
    val loginDeo = viewModel.login("district_officer", "officer@2026")
    org.junit.Assert.assertTrue(loginDeo)
    org.junit.Assert.assertEquals("DEO", viewModel.currentRole.value)
    org.junit.Assert.assertEquals("Dr. Chimwemwe Nkhoma", viewModel.currentUser.value)
  }

  @Test
  fun testOfflineCachingAndSync() = kotlinx.coroutines.test.runTest {
    val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    try {
      val application = ApplicationProvider.getApplicationContext<android.app.Application>()
      val viewModel = com.example.viewmodel.MainViewModel(application)
      
      // Seed with a mock school to avoid empty database issues
      val dao = com.example.data.AppDatabase.getInstance(application).dao
      val school = com.example.data.School("s-test", "Test Rural Primary", "Lilongwe", "Central")
      dao.insertSchools(listOf(school))
      
      // Set user and initial offline state
      viewModel.login("school_admin", "admin@2026")
      if (viewModel.isOnline.value) {
        viewModel.toggleConnectivity()
      }
      org.junit.Assert.assertFalse(viewModel.isOnline.value)
      
      // Clear any previous records for isolation
      val initialRecords = viewModel.attendanceRecords.value
      val countBefore = initialRecords.count { it.schoolId == "s-test" }
      
      // Submit attendance offline
      val attendanceMap = mapOf("student-1" to true)
      val nameMap = mapOf("student-1" to "Limbani Phiri")
      
      viewModel.submitAttendance("s-test", "2026-06-30", attendanceMap, nameMap)
      
      // Wait for insertion coroutine to settle
      Thread.sleep(250)
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      
      // Assert that the record was cached locally as PENDING
      val updatedRecords = viewModel.attendanceRecords.value
      val testRecord = updatedRecords.find { it.schoolId == "s-test" && it.studentId == "student-1" }
      
      org.junit.Assert.assertNotNull(testRecord)
      org.junit.Assert.assertEquals("PENDING", testRecord?.syncStatus)
      
      // Switch connection online and sync
      viewModel.toggleConnectivity()
      org.junit.Assert.assertTrue(viewModel.isOnline.value)
      
      viewModel.triggerSync()
      
      // Poll and wait for Room invalidation/updates to propagate
      var syncedRecord: com.example.data.AttendanceRecord? = null
      for (i in 1..30) {
        testScheduler.advanceUntilIdle()
        Thread.sleep(100)
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
        val syncedRecords = viewModel.attendanceRecords.value
        syncedRecord = syncedRecords.find { it.schoolId == "s-test" && it.studentId == "student-1" }
        if (syncedRecord?.syncStatus == "SYNCED") {
          break
        }
      }
      
      org.junit.Assert.assertEquals("SYNCED", syncedRecord?.syncStatus)
    } finally {
      kotlinx.coroutines.Dispatchers.resetMain()
    }
  }

  @Test
  fun testAddSchoolAndRegisterUserDynamicRoles() = kotlinx.coroutines.test.runTest {
    val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    try {
      val application = ApplicationProvider.getApplicationContext<android.app.Application>()
      val viewModel = com.example.viewmodel.MainViewModel(application)

      // 1. Add School via dynamic admin tool
      viewModel.addSchool("s-99", "Mwanza Secondary School", "Mwanza", "Southern")
      
      // Wait for insertion
      Thread.sleep(250)
      org.robolectric.shadows.ShadowLooper.idleMainLooper()

      // Verify it exists in database flow
      val allSchools = viewModel.schools.value
      val addedSchool = allSchools.find { it.id == "s-99" }
      org.junit.Assert.assertNotNull(addedSchool)
      org.junit.Assert.assertEquals("Mwanza Secondary School", addedSchool?.name)

      // 2. Register a new user dynamically with DEO role
      val dynamicUser = com.example.data.UserAccount(
          username = "mwanza_deo",
          password = "mwanza@2026",
          fullName = "Chief Officer Phiri",
          role = "DEO"
      )
      
      val registrationSuccess = viewModel.registerUserAccount(dynamicUser)
      org.junit.Assert.assertTrue(registrationSuccess)

      // Verify dynamic login works with the newly provisioned credentials
      viewModel.logout()
      val dynamicLoginSuccess = viewModel.login("mwanza_deo", "mwanza@2026")
      org.junit.Assert.assertTrue(dynamicLoginSuccess)
      org.junit.Assert.assertTrue(viewModel.isLoggedIn.value)
      org.junit.Assert.assertEquals("DEO", viewModel.currentRole.value)
      org.junit.Assert.assertEquals("Chief Officer Phiri", viewModel.currentUser.value)
    } finally {
      kotlinx.coroutines.Dispatchers.resetMain()
    }
  }

  @Test
  fun testBulkAttendanceEntry() = kotlinx.coroutines.test.runTest {
    val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
    kotlinx.coroutines.Dispatchers.setMain(testDispatcher)
    try {
      val application = ApplicationProvider.getApplicationContext<android.app.Application>()
      val viewModel = com.example.viewmodel.MainViewModel(application)
      val dao = com.example.data.AppDatabase.getInstance(application).dao
      
      // Seed with a school and two students of different classes
      val schoolId = "SCH-TEST-BULK"
      val school = com.example.data.School(schoolId, "Bulk Test School", "Zomba", "Southern")
      val student1 = com.example.data.Student("std-b1", "Limbani Tembo", schoolId, "Standard 5", "Male")
      val student2 = com.example.data.Student("std-b2", "Memory Phiri", schoolId, "Standard 6", "Female")
      
      dao.insertSchools(listOf(school))
      dao.insertStudents(listOf(student1, student2))
      
      // Simulate logging in
      viewModel.login("school_admin", "admin@2026")
      
      // Now simulate taking bulk attendance
      // Classroom 1 (Standard 5) marked present
      // Classroom 2 (Standard 6) marked absent
      val attendanceMap = mutableMapOf<String, Boolean>()
      attendanceMap[student1.id] = true  // Standard 5 -> present
      attendanceMap[student2.id] = false // Standard 6 -> absent
      
      val nameMap = mapOf(
        student1.id to student1.name,
        student2.id to student2.name
      )
      
      viewModel.submitAttendance(schoolId, "2026-06-30", attendanceMap, nameMap)
      
      // Wait for room inserts to complete
      Thread.sleep(250)
      org.robolectric.shadows.ShadowLooper.idleMainLooper()
      
      // Assert results in the view model or database
      val records = viewModel.attendanceRecords.value
      val record1 = records.find { it.studentId == student1.id }
      val record2 = records.find { it.studentId == student2.id }
      
      org.junit.Assert.assertNotNull(record1)
      org.junit.Assert.assertNotNull(record2)
      org.junit.Assert.assertTrue(record1!!.isPresent)
      org.junit.Assert.assertFalse(record2!!.isPresent)
      org.junit.Assert.assertEquals("PENDING", record1.syncStatus)
    } finally {
      kotlinx.coroutines.Dispatchers.resetMain()
    }
  }
}
