package com.walter.myinternshiplogbook;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogSyncService extends Service {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private FirebaseFirestore db;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Syncing logs...", Toast.LENGTH_SHORT).show();
        executorService.execute(() -> {
            // In a real app, you would fetch logs from your local DB and sync them to Firestore
            // For this example, we'll just show a toast
            // You can add your log syncing logic here
            stopSelf(); // Stop the service when the task is complete
        });
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
