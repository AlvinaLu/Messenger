package com.android.example.messenger.data


import com.android.example.messenger.data.remote.request.*
import com.android.example.messenger.data.remote.request.MessageReadRequest
import com.android.example.messenger.data.response.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import retrofit2.http.*
import java.util.concurrent.CompletableFuture

private const val BASE_URL = "https://messenger-lushnalv.herokuapp.com/"

private val retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(JacksonConverterFactory.create(jacksonObjectMapper()))
    .build()

interface AppWebApi {

    @POST("users/registrations")
    fun createUser(@Body user: UserRequestObject): Observable<UserVO>

    @POST("login")
    @Headers("Content-Type: application/json")
    fun login(@Body user: LoginRequestObject): Observable<Response<ResponseBody>>

    @GET("users/details")
    fun echoDetails(@Header("Authorization") authorization: String): Observable<UserVO>

    @PUT("users/token")
    fun updateUserToken(
        @Body request: TokenUpdateRequestObject,
        @Header("Authorization") authorization: String
    ): Observable<UserVO>

    @POST("users/updateUrl")
    fun updateUserUrl(
        @Body request: UrlUpdateRequestObject,
        @Header("Authorization") authorization: String
    ): Observable<UserVO>

    @GET("users")
    fun listUsers(@Header("Authorization") authorization: String): CompletableFuture<UserListVO>
    @POST("users/updateOnline")
    fun updateUserOnline(@Header("Authorization") authorization: String) : CompletableFuture<UserVO>

    @GET("conversations")
    fun listConversations(@Header("Authorization") authorization: String): CompletableFuture<ConversationListVO>

    @GET("conversations/{conversationId}/{lastMessageId}")
    fun showConversation(
        @Path("conversationId") conversationId: Long,
        @Path("lastMessageId")lastMessageId: Long,
        @Header("Authorization") authorization: String): CompletableFuture<ConversationVO>


    @POST("messages")
    fun createMessage(
        @Body messageRequestObject: MessageRequestObject,
        @Header("Authorization") authorization: String): CompletableFuture<MessageVO>

    @POST("messages/read")
    fun readMessage(
        @Body messageRequestObject: MessageReadRequest,
        @Header("Authorization") authorization: String)


    companion object {
        @Volatile
        private var INSTANCE: AppWebApi? = null

        fun getApiService(): AppWebApi {
            return INSTANCE ?: synchronized(this) {
                val instance = retrofit.create(AppWebApi::class.java)
                INSTANCE = instance
                instance
            }
        }
    }
}
