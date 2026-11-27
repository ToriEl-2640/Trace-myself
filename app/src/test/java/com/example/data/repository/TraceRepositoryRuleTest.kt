package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class TraceRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createProblem_authenticatedUser_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser("alice@trace.test")
        val repository = TraceRepository(firestore)

        val problemId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProblem(
                title = "Kano Tomato Spoilage",
                sector = "Agriculture & Food",
                location = "Kano, Nigeria",
                problemStatement = "Post-harvest losses exceed 40% during transport."
            )
        }

        assertNotNull(problemId)
        assertTrue(problemId.isNotBlank())
    }

    @Test
    fun getProblemById_authenticatedOwner_returnsProblem() = runBlocking {
        signInTestUser("alice@trace.test")
        val repository = TraceRepository(firestore)

        val problemId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createProblem(
                title = "SME Packaging Film Costs",
                sector = "Packaging & Materials",
                location = "Lagos, Nigeria",
                problemStatement = "Imported packaging laminates consume 35% of revenue."
            )
        }

        val retrieved = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getProblemById(problemId)
        }

        assertNotNull(retrieved)
        assertEquals(problemId, retrieved?.id)
        assertEquals("SME Packaging Film Costs", retrieved?.title)
    }

    @Test
    fun addLibraryItem_authenticatedUser_succeeds() = runBlocking {
        signInTestUser("alice@trace.test")
        val repository = TraceRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addLibraryItem(
                title = "Solar-Powered Cool Storage for Smallholders",
                source = "CGIAR Research Series",
                authors = "Adeyemi et al.",
                publicationYear = "2024",
                domain = "Agriculture",
                summary = "Empirical validation of low-cost evaporative chambers."
            )
        }
    }

    @Test
    fun observeProblems_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = TraceRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeProblems().first()
            }
            fail("Expected exception for unauthenticated call")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            // Should either fail with IllegalStateException("User must be signed in") or PERMISSION_DENIED
            assertTrue(
                e is IllegalStateException ||
                        (firestoreException != null && firestoreException.code == FirebaseFirestoreException.Code.PERMISSION_DENIED)
            )
        }
    }

    companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
