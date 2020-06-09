package com.delaroystudios.uploadmedia.networking;

/**
 * Created by delaroystudios on 10/5/2016.
 */
import java.util.Map;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PartMap;


public interface ApiConfig {

    @Multipart
    @FormUrlEncoded
    @POST("someurl")
    Call<ServerResponse> upload(
            @Header("Authorization") String authorization,
            @PartMap Map<String, RequestBody> map
    );

    @POST("api/v1/questions")
    Call<ServerResponse> upload(
            @Header("Authorization") String authorization,
            @Body RequestBody file
    );
}