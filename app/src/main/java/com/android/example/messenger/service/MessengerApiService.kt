package com.android.example.messenger.service

import com.android.example.messenger.data.remote.request.*
import com.android.example.messenger.data.remote.request.TokenUpdateRequestObject
import com.android.example.messenger.data.vo.*
import io.reactivex.Observable
import retrofit2.http.*
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.ResponseBody
import retrofit2.Retrofit

interface MessengerApiService {

    @POST("login")
    @Headers("Content-Type: application/json")
    fun login(@Body user: LoginRequestObject): Observable<retrofit2.Response<ResponseBody>>

    @POST("users/registrations")
    fun createUser(@Body user: UserRequestObject): Observable<UserVO>

    @GET("users")
    fun listUsers(@Header("Authorization") authorization: String): Observable<UserListVO>

    @PUT("users")
    fun updateUserStatus(
        @Body request: StatusUpdateRequestObject,
        @Header("Authorization") authorization: String): Observable<UserVO>

    @PUT("users/token")
    fun updateUserToken(
        @Body request: TokenUpdateRequestObject,
        @Header("Authorization") authorization: String): Observable<UserVO>

    @POST("users/updateUrl")
    fun updateUserUrl(
        @Body request: UrlUpdateRequestObject,
        @Header("Authorization") authorization: String): Observable<UserVO>

    @POST("users/updateOnline")
    fun updateUserOnline(
        @Header("Authorization") authorization: String)

    @GET("users/{userId}")
    fun showUser(
        @Path("userId") userId: Long,
        @Header("Authorization") authorization: String): Observable<UserVO>

    @GET("users/details")
    fun echoDetails(@Header("Authorization") authorization: String): Observable<UserVO>


    @POST("messages")
    fun createMessage(
        @Body messageRequestObject: MessageRequestObject,
        @Header("Authorization") authorization: String): Observable<MessageVO>

    @GET("conversations")
    fun listConversations(@Header("Authorization") authorization: String): Observable<ConversationListVO>

    @GET("conversations/{conversationId}")
    fun showConversation(
        @Path("conversationId") conversationId: Long,
        @Header("Authorization") authorization: String): Observable<ConversationVO>



    companion object Factory {

        private var service: MessengerApiService? = null

        fun getInstance(): MessengerApiService {
            if (service == null) {

                val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://messenger-lushnalv.herokuapp.com/")
                    .build()

                service = retrofit.create(MessengerApiService::class.java)
            }

            return service as MessengerApiService
        }
    }
}