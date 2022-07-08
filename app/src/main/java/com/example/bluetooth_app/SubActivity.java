package com.example.bluetooth_app;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class SubActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);
        LatLng pos = new LatLng(MainActivity.lat, MainActivity.lon);

        MarkerOptions markerOptions = new MarkerOptions();         // 마커 생성
        markerOptions.position(pos);    // 위치 설정
        markerOptions.title("현재 위치");                         // 마커 제목
        markerOptions.snippet(pos.toString());         // 마커 설명
        mMap.addMarker(markerOptions);

        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));                 // 카메라 이동
        mMap.animateCamera(CameraUpdateFactory.zoomTo(30));                         // 줌의 정도
        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);                           // 지도 유형 설정

    }
}