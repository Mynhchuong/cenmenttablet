package com.example.cementtablet;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CementTableAdapter adapter;
    private List<CementLog> dataList;

    private Button btnAddRow;
    private Button btnSave;
    
    private List<String> arrayGroupSum = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataList = new ArrayList<>();
        dataList.add(new CementLog());

        recyclerView = findViewById(R.id.recyclerView);
        btnAddRow = findViewById(R.id.btnAddRow);
        btnSave = findViewById(R.id.btnSave);
        android.widget.TextView tvHeaderDate = findViewById(R.id.tvHeaderDate);
        
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());
        tvHeaderDate.setText("Ngày: " + today);

        fetchGroupSums();

        adapter = new CementTableAdapter(dataList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAddRow.setOnClickListener(v -> {
            CementLog newLog = new CementLog();
            if (!dataList.isEmpty()) {
                CementLog firstLog = dataList.get(0);
                if (!firstLog.isSaved()) {
                    newLog.setCLine(firstLog.getCLine());
                    newLog.setCSample(firstLog.getCSample());
                }
            }
            dataList.add(0, newLog);
            adapter.notifyItemInserted(0);
            recyclerView.scrollToPosition(0);
        });

        btnSave.setOnClickListener(v -> {
            JSONArray jsonArray = new JSONArray();
            List<CementLog> pendingLogs = new ArrayList<>();
            try {
                for (CementLog log : dataList) {
                    if (log.isSaved()) continue;
                    
                    if (log.getCLine().isEmpty() && log.getMeasurement().isEmpty()) {
                        continue;
                    }

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("Id", 0);
                    jsonObject.put("LineCode", log.getCLine());
                    
                    String currentTime = new java.text.SimpleDateFormat("HH", java.util.Locale.getDefault()).format(new java.util.Date());
                    jsonObject.put("HH", currentTime);
                    
                    jsonObject.put("SampleCode", log.getCSample());
                    jsonObject.put("DGather", log.getDGather());
                    
                    try {
                        jsonObject.put("Measurement", Double.parseDouble(log.getMeasurement().isEmpty() ? "0" : log.getMeasurement()));
                    } catch (Exception ex) {
                        jsonObject.put("Measurement", 0);
                    }

                    jsonArray.put(jsonObject);
                    pendingLogs.add(log);
                }

                if (jsonArray.length() == 0) {
                    Toast.makeText(MainActivity.this, "Không có dữ liệu mới để lưu!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(MainActivity.this, "Đang gửi " + jsonArray.length() + " dòng...", Toast.LENGTH_SHORT).show();

                java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
                android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
                
                executor.execute(() -> {
                    int successCount = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        try {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            
                            java.net.URL url = new java.net.URL("http://192.168.1.24/SamhoAPI/api/cement/insert");
                            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json; utf-8");
                            conn.setRequestProperty("Accept", "application/json");
                            conn.setDoOutput(true);

                            try(java.io.OutputStream os = conn.getOutputStream()) {
                                byte[] input = obj.toString().getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }
                            
                            int responseCode = conn.getResponseCode();
                            if (responseCode == 200) {
                                java.io.InputStream in = new java.io.BufferedInputStream(conn.getInputStream());
                                java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\A");
                                String responseStr = scanner.hasNext() ? scanner.next() : "";
                                
                                try {
                                    JSONObject resJson = new JSONObject(responseStr);
                                    boolean isSuccess = resJson.optBoolean("Success", false);
                                    if (isSuccess) {
                                        pendingLogs.get(i).setSaved(true);
                                        successCount++;
                                    } else {
                                        String errMsg = resJson.optString("Message", "Lỗi C# Backend");
                                        Log.e("CementLog", "API Error: " + errMsg);
                                    }
                                } catch (Exception parseEx) {
                                    Log.e("CementLog", "Parse Response Error: " + responseStr);
                                }
                            } else {
                                Log.e("CementLog", "HTTP Error Code: " + responseCode);
                            }
                            conn.disconnect();
                            
                        } catch (Exception e) {
                            Log.e("CementLog", "Network exception: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    final int finalSuccess = successCount;
                    handler.post(() -> {
                        if (finalSuccess > 0) {
                            Toast.makeText(MainActivity.this, "Lưu thành công " + finalSuccess + "/" + jsonArray.length() + " dòng!", Toast.LENGTH_LONG).show();
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(MainActivity.this, "Lưu KHÔNG thành công! Mở Logcat Android Studio để xem lỗi C#", Toast.LENGTH_LONG).show();
                        }
                    });
                });

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Lỗi đóng gói dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchGroupSums() {
        Toast.makeText(this, "Đang tải dữ liệu Group Sum...", Toast.LENGTH_SHORT).show();
        java.util.concurrent.ExecutorService executor = java.util.concurrent.Executors.newSingleThreadExecutor();
        android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());

        executor.execute(() -> {
            try {
                java.net.URL url = new java.net.URL("http://192.168.1.13/test/arduino/getallmesgroupsum");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);

                if (conn.getResponseCode() == 200) {
                    java.io.InputStream in = new java.io.BufferedInputStream(conn.getInputStream());
                    java.util.Scanner scanner = new java.util.Scanner(in).useDelimiter("\\A");
                    String jsonStr = scanner.hasNext() ? scanner.next() : "";

                    JSONArray jsonArray = new JSONArray(jsonStr);
                    List<String> tempGroupSum = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject c = jsonArray.getJSONObject(i);
                        tempGroupSum.add(c.getString("MES_GROUP_SUM"));
                    }

                    handler.post(() -> {
                        arrayGroupSum.clear();
                        arrayGroupSum.addAll(tempGroupSum);
                        if (adapter != null) {
                            adapter.setGroupSums(arrayGroupSum);
                        }
                        Toast.makeText(MainActivity.this, "Tải xong " + arrayGroupSum.size() + " Group Sum", Toast.LENGTH_SHORT).show();
                    });
                }
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                handler.post(() -> Toast.makeText(MainActivity.this, "Lỗi tải Group Sum", Toast.LENGTH_SHORT).show());
            }
        });
    }
}
