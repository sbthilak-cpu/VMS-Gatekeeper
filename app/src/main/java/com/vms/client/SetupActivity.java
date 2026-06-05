package com.vms.client; import android.os.Bundle; import android.widget.*; import androidx.appcompat.app.AppCompatActivity; import com.google.gson.JsonObject; import retrofit2.*; import retrofit2.converter.gson.GsonConverterFactory;
public class SetupActivity extends AppCompatActivity {
    private EditText etCompanyField, etSiteField, etAddressField; private Button btnSaveConfig; private ApiService apiService; private final String SERVER_END_POINT = "http://192.168.8.100/vms/";
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_setup);
        etCompanyField = findViewById(R.id.etCompanyField); etSiteField = findViewById(R.id.etSiteField); etAddressField = findViewById(R.id.etAddressField); btnSaveConfig = findViewById(R.id.btnSaveConfig);
        apiService = new Retrofit.Builder().baseUrl(SERVER_END_POINT).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class); pullCurrentServerConfig();
        btnSaveConfig.setOnClickListener(v -> postUpdatedConfiguration());
    }
    private void pullCurrentServerConfig() {
        apiService.getSettings().enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) { if (response.isSuccessful() && response.body() != null) { JsonObject config = response.body(); etCompanyField.setText(config.get("company_name").getAsString()); etSiteField.setText(config.get("site_name").getAsString()); etAddressField.setText(config.get("site_address").getAsString()); } }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }
    private void postUpdatedConfiguration() {
        JsonObject payload = new JsonObject(); payload.addProperty("company_name", etCompanyField.getText().toString().trim()); payload.addProperty("site_name", etSiteField.getText().toString().trim()); payload.addProperty("site_address", etAddressField.getText().toString().trim());
        apiService.updateSettings(payload).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) { Toast.makeText(SetupActivity.this, "Server system variables updated.", Toast.LENGTH_SHORT).show(); finish(); }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }
}
