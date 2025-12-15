package com.example.eventfeed.ui.login

import com.example.eventfeed.data.auth.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LoginVMTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `login success updates ui to success`() = runTest {
        val fakeRepo = object : AuthRepository {
            override suspend fun login(username: String, password: String): Result<Unit> =
                Result.success(Unit)
        }

        val vm = LoginVM(fakeRepo)
        vm.login("user", "pass")
        advanceUntilIdle()

        val ui = vm.ui.value
        assertTrue(ui.success)
        assertFalse(ui.isLoading)
        assertNull(ui.error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `login failure updates ui with error message`() = runTest {
        val fakeRepo = object : AuthRepository {
            override suspend fun login(username: String, password: String): Result<Unit> =
                Result.failure(Exception("invalid credentials"))
        }

        val vm = LoginVM(fakeRepo)
        vm.login("user", "bad")
        advanceUntilIdle()

        val ui = vm.ui.value
        assertFalse(ui.success)
        assertFalse(ui.isLoading)
        assertEquals("invalid credentials", ui.error)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `resetUiState clears ui state`() = runTest {
        val fakeRepo = object : AuthRepository {
            override suspend fun login(username: String, password: String): Result<Unit> =
                Result.success(Unit)
        }

        val vm = LoginVM(fakeRepo)
        vm.login("user", "pass")
        advanceUntilIdle()
        assertTrue(vm.ui.value.success)

        vm.resetUiState()
        assertEquals(LoginUiState(), vm.ui.value)
    }
}