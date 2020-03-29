package com.example.hopinnow.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.hopinnow.R;
import com.example.hopinnow.database.DriverRequestDatabaseAccessor;
import com.example.hopinnow.entities.Driver;
import com.example.hopinnow.entities.Request;
import com.example.hopinnow.entities.Trip;
import com.example.hopinnow.statuslisteners.DriverRequestListener;
import com.google.zxing.Result;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.ArrayList;
import java.util.Date;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class DriverScanPaymentActivity extends AppCompatActivity
        implements ZXingScannerView.ResultHandler, DriverRequestListener {
    private ZXingScannerView cameraView;
    private Driver driver;
    private Request curRequest;
    private String encoded;
    private RxPermissions rxPermissions;
    private int permissionCount = 0;
    private TextView permissionMsg;

    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_scanning);
        driver = (Driver) getIntent().getSerializableExtra("Driver");
        //curRequest
        curRequest = driver.getCurRequest();

        rxPermissions = new RxPermissions(DriverScanPaymentActivity.this);
        cameraView = findViewById(R.id.camera_scan_view);
        permissionMsg = findViewById(R.id.permission_camera_textView);

        if (ActivityCompat.checkSelfPermission(DriverScanPaymentActivity.this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            permissionMsg.setVisibility(TextView.INVISIBLE);
            cameraView.setResultHandler(this);
            cameraView.startCamera();
        } else {
            cameraPermission();
        }

        cameraView.setOnClickListener(v -> {
            cameraPermission();
        });

    }


    @Override
    public void handleResult(Result rawResult){
        encoded = rawResult.getText();
        String result[] = encoded.split(":");
        if (driver.getEmail().equals(result[0])){ //

            //todo trigger rider rating by removing request from firebase
            //double prevDeposit = driver.getDeposit();
            //driver.setDeposit(prevDeposit + Double.valueOf(result[1]));
            Toast.makeText(this, "You have successfully received " + result[1] +
                    " QR bucks for you completed ride!", Toast.LENGTH_SHORT).show();


            Date current_time = new Date();


            DriverRequestDatabaseAccessor driverRequestDatabaseAccessor = new DriverRequestDatabaseAccessor();
            ArrayList<Trip> driverTripList = driver.getDriverTripList();
            if(driverTripList == null){
                driverTripList = new ArrayList<>();

            }
            driverTripList.add(new Trip(curRequest.getDriverEmail(),curRequest.getRiderEmail(),
                    curRequest.getPickUpLoc(),curRequest.getDropOffLoc(),
                    curRequest.getPickUpLocName(),curRequest.getDropOffLocName(),
                    (Date)curRequest.getPickUpDateTime(),  (Date)current_time,
                    (int)Math.abs(current_time.getTime() -
                            curRequest.getPickUpDateTime().getTime()),
                    curRequest.getCar(),curRequest.getEstimatedFare(),5.0));
            driver.setDriverTripList(driverTripList);
            //driver complete the request and trigger the rider to rate.
            driverRequestDatabaseAccessor.driverCompleteRequest(curRequest,this);

            Intent intent = new Intent(DriverScanPaymentActivity.this, DriverMapActivity.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "This QR code does not belong to the trip " +
                    "that you have completed.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("CheckResult")
    private void cameraPermission(){
        if (ActivityCompat.checkSelfPermission(DriverScanPaymentActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            rxPermissions
                    .request(Manifest.permission.CAMERA)
                    .subscribe(granted -> {
                        if (granted) {
                            cameraView.setResultHandler(this);
                            cameraView.startCamera();
                            permissionMsg.setVisibility(TextView.INVISIBLE);
                        } else {
                            Toast.makeText(DriverScanPaymentActivity.this,
                                    "You must enable camera to receive your payment.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
            permissionCount += 1;

            // if driver has denied camera permission three times
            if ((ActivityCompat.checkSelfPermission(DriverScanPaymentActivity.this,
                    Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) &&
                    (permissionCount > 3)) {
                Toast.makeText(DriverScanPaymentActivity.this, "Guess you " +
                                "decided to donate your earning to the HopInNow Team, thanks! :D"
                        , Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DriverScanPaymentActivity.this,
                        DriverMapActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDriverRequestAccept() {

    }

    @Override
    public void onDriverRequestTimeoutOrFail() {

    }

    @Override
    public void onRequestAlreadyTaken() {

    }

    @Override
    public void onRequestCanceledByRider() {

    }

    @Override
    public void onDriverPickupSuccess() {

    }

    @Override
    public void onDriverPickupFail() {

    }

    @Override
    public void onDriverRequestCompleteSuccess() {

    }

    @Override
    public void onDriverRequestCompleteFailure() {

    }
}