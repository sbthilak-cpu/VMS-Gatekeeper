package com.vms.client; import android.content.Intent; import android.os.Bundle; import android.view.View; import android.widget.*; import androidx.appcompat.app.AppCompatActivity; import com.google.gson.*; import com.journeyapps.barcodescanner.*; import retrofit2.*; import retrofit2.converter.gson.GsonConverterFactory;
public class MainActivity extends AppCompatActivity {
    private Button btnScanQR, btnConfigureSite, btnCommitEntry; private TextView tvDisplayDetails; private LinearLayout layoutMaterialSubForm; private EditText etMaterialName, etMaterialSerial;
    private ApiService apiService; private String validatedRecordId = ""; private final String SERVER_END_POINT = "http://192.168.8.100/vms/";
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); setContentView(R.layout.activity_main);
        btnScanQR = findViewById(R.id.btnScanQR); btnConfigureSite = findViewById(R.id.btnConfigureSite); btnCommitEntry = findViewById(R.id.btnCommitEntry); tvDisplayDetails = findViewById(R.id.tvDisplayDetails); layoutMaterialSubForm = findViewById(R.id.layoutMaterialSubForm); etMaterialName = findViewById(R.id.etMaterialName); etMaterialSerial = findViewById(R.id.etMaterialSerial);
        apiService = new Retrofit.Builder().baseUrl(SERVER_END_POINT).addConverterFactory(GsonConverterFactory.create()).build().create(ApiService.class);
        btnScanQR.setOnClickListener(v -> barcodeEngineLauncher.launch(new ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE).setPrompt("Align Visitor QR Code inside frame").setOrientationLocked(false)));
        btnConfigureSite.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SetupActivity.class)));
        btnCommitEntry.setOnClickListener(v -> commitTrackingCheckpoint("checked_in"));
    }
    private final androidx.activity.result.ActivityResultLauncher<ScanOptions> barcodeEngineLauncher = registerForActivityResult(new ScanContract(), result -> { if(result.getContents() != null) { queryTargetToken(result.getContents()); } else { Toast.makeText(this, "Scanning cancelled", Toast.LENGTH_SHORT).show(); } });
    private void queryTargetToken(String qrTokenString) {
        apiService.verifyQR(qrTokenString).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null && "success".equals(response.body().get("status").getAsString())) {
                    JsonObject visitorObj = response.body().getAsJsonObject("data"); validatedRecordId = visitorObj.get("id").getAsString();
                    String textBlock = "Visitor: " + visitorObj.get("visitor_name").getAsString() + "\nCompany: " + (visitorObj.get("visitor_company").isJsonNull() ? "N/A" : visitorObj.get("visitor_company").getAsString()) + "\nPurpose: " + (visitorObj.get("purpose").isJsonNull() ? "General" : visitorObj.get("purpose").getAsString()) + "\nHost: " + visitorObj.get("host_name").getAsString();
                    tvDisplayDetails.setText(textBlock); layoutMaterialSubForm.setVisibility(View.VISIBLE);
                } else { tvDisplayDetails.setText("Access Denied: Unrecognized Pass Token."); layoutMaterialSubForm.setVisibility(View.GONE); }
            }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) { Toast.makeText(MainActivity.this, "Network Connection Timeout", Toast.LENGTH_SHORT).show(); }
        });
    }
    private void commitTrackingCheckpoint(String statusFlag) {
        JsonObject dataPayload = new JsonObject(); dataPayload.addProperty("id", validatedRecordId); dataPayload.addProperty("status", statusFlag);
        String descInput = etMaterialName.getText().toString().trim();
        if (!descInput.isEmpty()) {
            JsonArray mList = new JsonArray(); JsonObject cargo = new JsonObject(); cargo.addProperty("description", descInput); cargo.addProperty("serial_no", etMaterialSerial.getText().toString().trim()); mList.add(cargo); dataPayload.add("materials", mList);
        }
        apiService.processCheckpoint(dataPayload).enqueue(new Callback<JsonObject>() {
            @Override public void onResponse(Call<JsonObject> call, Response<JsonObject> response) { Toast.makeText(MainActivity.this, "Transaction logged on server.", Toast.LENGTH_LONG).show(); layoutMaterialSubForm.setVisibility(View.GONE); etMaterialName.setText(""); etMaterialSerial.setText(""); tvDisplayDetails.setText("Standby Mode: Awaiting target scan capture..."); }
            @Override public void onFailure(Call<JsonObject> call, Throwable t) {}
        });
    }
}
