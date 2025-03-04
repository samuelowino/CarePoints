package com.injiri.healthcarepoints.services;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;

import com.google.android.gms.location.LocationListener;

import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.injiri.healthcarepoints.R;


public class UserlocationService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public UserlocationService() {
    }

    public static String LOCATION_BROADCAST_ACTION = UserlocationService.class.getName() + "locationBroadcast";
    public static String USER_LATITUDE = "extra_lat";
    public static String USER_LONGITUDE = "extra_lng";

    private static String TAG = UserlocationService.class.getSimpleName();
    GoogleApiClient myLocationClient;
    LocationRequest myLocationRequest = new LocationRequest();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        myLocationClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        myLocationRequest.setInterval(150000000);
        myLocationRequest.setFastestInterval(150000000);
        int priority = LocationRequest.PRIORITY_HIGH_ACCURACY;
        myLocationRequest.setPriority(priority);
        myLocationClient.connect();
        //make the service less prone to system stops
        return START_STICKY;

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    public BroadcastReceiver stopReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Recieved stop broadcast");
            //stop the service when the notification is taped
            unregisterReceiver(stopReciever);
            stopSelf();
        }
    };

    public void buildNotification() {
        String stop = "stop";
        registerReceiver(stopReciever, new IntentFilter(stop));
        PendingIntent broadcastIntent = PendingIntent.getBroadcast(this, 0, new Intent(stop), PendingIntent.FLAG_UPDATE_CURRENT);
        //make a persistent notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentTitle(getString(R.string.app_name)).setContentText(getString(R.string.notification_text)).setOngoing(true
        ).setContentIntent(broadcastIntent).setSmallIcon(R.drawable.common_google_signin_btn_icon_dark);
        startForeground(1, builder.build());
    }

    public void publishcurrentLocation(String lat, String lng) {
        Log.d(TAG, "sending location_info...");
        Intent intent = new Intent(LOCATION_BROADCAST_ACTION);
        intent.putExtra(USER_LATITUDE, lat);
        intent.putExtra(USER_LONGITUDE, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location has changed");
        if (location != null) {
            Log.d(TAG, "Location != null");
            //broadcast the location result to the other classes listening to the service
            publishcurrentLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "==onConnected permission not  granded");
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(myLocationClient, myLocationRequest, this);
        Log.d(TAG, "connected to ggle api");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection has been susppendded");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "sorry connection to google api failed");
    }
}