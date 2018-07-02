/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.mapdemo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.ElevationApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.ElevationResult;

import java.util.Objects;

import static com.google.maps.android.SphericalUtil.computeDistanceBetween;
import static com.google.maps.android.SphericalUtil.computeHeading;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.tan;

/**
 * This demo shows how GMS Location can be used to check for changes to the users location.  The
 * "My Location" button uses GMS Location to set the blue dot representing the users location.
 * Permission for {@link android.Manifest.permission#ACCESS_FINE_LOCATION} is requested at run
 * time. If the permission has not been granted, the Activity is finished with an error message.
 */
public class MyLocationDemoActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMyLocationClickListener,
        OnMapLongClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in
     * {@link #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean mPermissionDenied = false;

    private TextView mTapTextView;
    private GoogleMap mMap;
    public LatLng myPoint;
    public LatLng myLocation = new LatLng(0,0);
    public ElevationResult result;
    private GeoApiContext context = new GeoApiContext.Builder()
            .apiKey("AIzaSyBeU86oXU_rfyjtePw5fHmLYkSmgJ9w_k8")
            .build();
    static int targetAltitude = 0;
    static int myAltitude = 0;
    static double angle;
    static double distance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_location_demo);
        Objects.requireNonNull(getSupportActionBar()).hide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        MyLocationListener.SetUpLocationListener(this);
        mTapTextView = findViewById(R.id.tap_text);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        myAltitude = 0;
        LatLng loc_point = new LatLng(location.getLatitude(),location.getLongitude());
        if (location.getAltitude() != 0) myAltitude = (int) location.getAltitude();
                else {
            try {
                getElevationOfMyLocation(loc_point);
            } catch (Exception e) {
                e.printStackTrace();
                myAltitude = (int) result.elevation;
            }
            if (result != null) myAltitude = (int) result.elevation;
        }
        Toast.makeText(this, "Ваши координаты x, y, h =\n" + toSixteen(location.getLongitude()) + ", " + toSixteen(location.getLatitude())
                + ", " + myAltitude,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    @Override
    public void onMapLongClick(LatLng point) {
        addMarker(point);
        targetAltitude = 0;
        myPoint = point;
        myLocation = new LatLng(MyLocationListener.imHere.getLatitude(), MyLocationListener.imHere.getLongitude());
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(myPoint)
                .add(myLocation)
                .color(Color.RED).width(3);
        mMap.addPolyline(polylineOptions);
        try {
            getElevationOfMyLocation(myPoint);
         } catch (Exception e) {
            e.printStackTrace();
        }
        if (result != null) targetAltitude = (int) result.elevation;
        GaussCoord point_GK = toG_K(myPoint.longitude, myPoint.latitude);
        distance = computeDistanceBetween(myLocation,myPoint);
        angle = computeHeading(myLocation,myPoint);
        if (angle < 0) angle += 360;
        angle /= (double) 6;
        angle *= 100;
        angle = round(angle);
        MeteoBulletin.Wind();
        String output_string = "N: " + (int) point_GK.getN() + ", E: " + (int) point_GK.getE() + "\n" +//", h: " + targetAltitude
                //+ ",\nDistance: " + (int) distance + ", Angle: " + angle  + " , Yb: " + Yb + ", Wind: " + round(wind_x) + "/" + round(wind_y) + ", dLsum: "
                //+ String.format("%.4g%n", getLateralCorrection());
                myPoint.latitude + ",  " + myPoint.longitude + "\n" + toWorld(point_GK.getN(),point_GK.getE());
        mTapTextView.setText(output_string);
    }

    public void getElevationOfMyLocation(LatLng point) throws Exception {
        result = null;
        result = ElevationApi.getByPoint(context, new com.google.maps.model.LatLng(
                point.latitude, point.longitude)).await();
    }

    String toSixteen (double hundred)
    {
        short hours = (short) hundred;
        double d_minutes = (hundred - hours)*60;
        short minutes;
        minutes = (short)(d_minutes);
        short seconds = (short)((d_minutes - minutes)*60*100);
        double d_seconds = (double)seconds/100;
        return hours + "°" + minutes + "'" + d_seconds + '"';
    }


    LatLng toWorld (double x, double y) // перевод из Г-К в мировые координаты
    {
        double x1 = myLocation.longitude; // широта и долгота произвольной точки (местоположение)
        double y1 = myLocation.latitude;
        GaussCoord coord = toG_K (x1, y1); // преобразуем в метры
        double N = coord.getN();
        double E = coord.getE();
        double dx = N-x;
        double dy = E-y;
        x1 -= dx / 40000000*360;
        y1 -= dy / 40000000*360;
        /*double xCoof = 0.0000089; // коэффициент смещения точки (~1м)
        double yCoof = 0.0000089;
        if (N > x) xCoof *= -1;
        if (E > y) yCoof *= -1;
        while (true) // пока координаты не сравнялись смещаем произвольную точку
        {
            if (abs(N-x)<1000 || abs(E-y)<1000) break;
            if (abs(N-x)>1000) x1 += xCoof;
            if (abs(E-y)>1000) y1 += yCoof;
            coord = toG_K (x1, y1); // преобразуем в метры
            N = coord.getN();
            E = coord.getE();
        }*/
        return new LatLng(y1, x1);
    }

    GaussCoord toG_K (double dLon, double dLat)
    {
        // Перевод географических координат (широты и долготы) точки в прямоугольные
// координаты проекции Гаусса-Крюгера (на примере координат Москвы).

// Номер зоны Гаусса-Крюгера (если точка рассматривается в системе
// координат соседней зоны, то номер зоны следует присвоить вручную)
        int zone =  (int) (dLon/6.0+1);

// Параметры эллипсоида Красовского
        double a = 6378245.0;          // Большая (экваториальная) полуось
        double b = 6356863.019;        // Малая (полярная) полуось
        double e2 = (pow(a,2)- pow(b,2))/ pow(a,2);  // Эксцентриситет
        double n = (a-b)/(a+b);        // Приплюснутость


// Параметры зоны Гаусса-Крюгера
        double F = 1.0;                   // Масштабный коэффициент
        double Lat0 = 0.0;                // Начальная параллель (в радианах)
        double Lon0 = (zone*6-3)* PI/180;  // Центральный меридиан (в радианах)
        double N0 = 0.0;                  // Условное северное смещение для начальной параллели
        double E0 = zone*1e6+500000.0;    // Условное восточное смещение для центрального меридиана


        double ro  = 206264.8062; //Число угловых секунд в радиане

        //Линейные элементы трансформирования, в метрах
        double dx = 23.92;
        double dy = -141.27;
        double dz = -80.9;
        double wx = 0;
        double wy = -0.35;
        double wz = -0.86;
        double ms = -0.00000012;

        //Эллипсоид Красовского
        double aP  = 6378245; //Большая полуось
        double alP = 1 / 298.3; //Сжатие
        double e2P = 2 * alP - pow(alP, 2); //Квадрат эксцентриситета

        //Эллипсоид WGS84 (GRS80, эти два эллипсоида сходны по большинству параметров)
        double aW = 6378137; //Большая полуось
        double alW = 1 / 298.257223563; //Сжатие
        double e2W = 2 * alW - pow(alW, 2); //Квадрат эксцентриситета

        //Вспомогательные значения для преобразования эллипсоидов
        double A = (aP + aW) / 2;
        double E2 = (e2P + e2W) / 2;
        double da = aW - aP;
        double de2 = e2W - e2P;

        double B;
        double L;
        double MM;
        double N1;
        double dB;
        double dL;
        double Bd = dLat;
        double Ld = dLon;
        double H = 0;

        B = Bd * PI / 180;
        L = Ld * PI / 180;
        MM = A * (1 - E2) / pow((1 - E2 * pow(sin(B),2)),1.5);
        N1 = A * pow(1 - E2 * pow(sin(B),2),-0.5);
        dB = ro / (MM + H) * (N1 / A * E2 * sin(B) * cos(B) * da + ( pow(N1,2) /  pow(A,2) + 1) * N1 * sin(B) * cos(B) * de2 / 2 - (dx * cos(L) + dy * sin(L)) * sin(B) + dz * cos(B))
                - wx * sin(L) * (1 + E2 * cos(2 * B)) + wy * cos(L) * (1 + E2 * cos(2 * B)) - ro * ms * E2 * sin(B) * cos(B);

        dL = ro / ((N1 + H) * cos(B)) * (-dx * sin(L) + dy * cos(L)) + tan(B) * (1 - E2) * (wx * cos(L) + wy * sin(L)) - wz;

        dLat = Bd - dB / 3600;
        dLon = Ld - dL / 3600;



// Перевод широты и долготы в радианы
        double Lat = dLat* PI/180.0;
        double Lon = dLon* PI/180.0;

// Вычисление переменных для преобразования
        double sinLat = sin(Lat);
        double cosLat = cos(Lat);
        double tanLat = tan(Lat);

        double v = a * F * pow(1-e2* pow(sinLat,2),-0.5);
        double p = a*F*(1-e2) * pow(1-e2* pow(sinLat,2),-1.5);
        double n2 = v/p-1;
        double M1 = (1+n+5.0/4.0* pow(n,2) +5.0/4.0* pow(n,3)) * (Lat-Lat0);
        double M2 = (3*n+3* pow(n,2) +21.0/8.0* pow(n,3)) * sin(Lat - Lat0) * cos(Lat + Lat0);
        double M3 = (15.0/8.0* pow(n,2) +15.0/8.0* pow(n,3))* sin(2 * (Lat - Lat0))* cos(2 * (Lat + Lat0));
        double M4 = 35.0/24.0* pow(n,3) * sin(3 * (Lat - Lat0)) * cos(3 * (Lat + Lat0));
        double M = b*F*(M1-M2+M3-M4);
        double I = M+N0;
        double II = v/2 * sinLat * cosLat;
        double III = v/24 * sinLat * pow(cosLat,3) * (5- pow(tanLat,2)+9*n2);
        double IIIA = v/720 * sinLat * pow(cosLat,5) * (61-58* pow(tanLat,2)+ pow(tanLat,4));
        double IV = v * cosLat;
        double V = v/6 * pow(cosLat,3) * (v/p- pow(tanLat,2));
        double VI = v/120 * pow(cosLat,5) * (5-18* pow(tanLat,2)+ pow(tanLat,4)+14*n2-58* pow(tanLat,2)*n2);

// Вычисление северного и восточного смещения (в метрах)
        double N = I+II* pow(Lon-Lon0,2)+III* pow(Lon-Lon0,4)+IIIA* pow(Lon-Lon0,6);
        double E = E0+IV*(Lon-Lon0)+V* pow(Lon-Lon0,3)+VI* pow(Lon-Lon0,5);
        return new GaussCoord(N,E);
    }

    void addMarker(LatLng latLng){

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.flat(false);
        markerOptions.draggable(false);

        mMap.clear();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.addMarker(markerOptions);
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    final class GaussCoord {
        private final double first;
        private final double second;

        GaussCoord(double first, double second) {
            this.first = first;
            this.second = second;
        }

        double getN() {
            return first;
        }

        public double getE() {
            return second;
        }
    }
}
