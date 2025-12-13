//package com.example.eventfeed.data
//
//
//import retrofit2.Response
//import retrofit2.http.*
//
//data class LoginRequest(val username: String, val password: String)
//data class LoginResponse(val token: String, val userId: String, val name: String)
//
//data class EventDto(
//    val id: String,
//    val title: String,
//    val description: String,
//    val timestamp: Long,
//    val downloadUrl: String? = null
//)
//
//data class EventsPage(val events: List<EventDto>, val page: Int, val pageSize: Int, val total: Int)
//
//interface ApiService {
//    @POST("login")
//    suspend fun login(@Body req: LoginRequest): Response<LoginResponse>
//
//    @GET("events")
//    suspend fun events(@Query("page") page: Int, @Query("pageSize") pageSize: Int): Response<EventsPage>
//
//    @GET("events/{id}")
//    suspend fun event(@Path("id") id: String): Response<EventDto>
//}
