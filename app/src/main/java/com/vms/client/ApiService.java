package com.vms.client; import com.google.gson.JsonObject; import retrofit2.Call; import retrofit2.http.*;
public interface ApiService {
    @GET("api.php?action=get_settings") Call<JsonObject> getSettings();
    @POST("api.php?action=update_settings") Call<JsonObject> updateSettings(@Body JsonObject settings);
    @GET("api.php?action=verify_qr") Call<JsonObject> verifyQR(@Query("pass_code") String passCode);
    @POST("api.php?action=process_checkpoint") Call<JsonObject> processCheckpoint(@Body JsonObject payload);
}
