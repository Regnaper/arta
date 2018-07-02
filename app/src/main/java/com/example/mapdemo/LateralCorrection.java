package com.example.mapdemo;

import static com.example.mapdemo.MeteoBulletin.wind_y;
import static com.example.mapdemo.ShootTables.getFromShootTables;
import static com.example.mapdemo.ShootTables.of45_4p;
import static com.example.mapdemo.MyLocationDemoActivity.distance;

class LateralCorrection {
    static double getLateralCorrection() {
        double Z = getFromShootTables(of45_4p, 5, (int) distance);
        double dZw = getFromShootTables(of45_4p, 6, (int) distance);
        return Z + 0.1 * dZw * wind_y;
    }
}