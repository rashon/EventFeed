package com.example.eventfeed.ui.profile

import com.example.eventfeed.data.profile.UserProfileRepository
import com.example.eventfeed.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UserProfileVMTest {

    // 1. Create the dispatcher as a class property so it can be shared
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `successfully loads profile`() = runTest(testDispatcher) { // 3. Pass the SAME instance to runTest
        val fakeRepo = object : UserProfileRepository {
            override suspend fun fetchProfile(): UserProfile? =
                UserProfile("id1", "Alice", "alice@example.com", "bio", "https://avatar")
        }

        val vm = UserProfileVM(fakeRepo)

        // Now this advances 'testDispatcher', which is also Main, so the VM coroutine runs
        advanceUntilIdle()

        val profile = vm.profile.value
        assertNotNull(profile)
        assertEquals("Alice", profile?.name)
        assertNull(vm.error.value)
    }
}