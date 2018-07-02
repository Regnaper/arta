package com.example.mapdemo;

import static com.example.mapdemo.MyLocationDemoActivity.angle;
import static com.example.mapdemo.MyLocationDemoActivity.distance;
import static com.example.mapdemo.ShootTables.getFromShootTables;
import static com.example.mapdemo.ShootTables.of45_4p;
import static java.lang.Math.cos;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

public class MeteoBulletin {
    static double wind_x;
    static double wind_y;
    private static int[][] wind_speed = {
            {3, 4, 5, 6, 7, 7, 8, 9, 10, 11, 12, 12, 0},
            {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 1},
            {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 1},
            {4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 1},
            {4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 14, 15, 1},
            {4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 2},
            {4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 2},
            {4, 5, 6, 7, 8, 9, 10, 11, 13, 14, 15, 16, 2},
            {4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16, 2},
            {4, 5, 7, 8, 8, 9, 11, 12, 13, 15, 15, 16, 2},
            {4, 5, 7, 8, 8, 9, 11, 12, 13, 15, 15, 16, 2},
            {4, 5, 7, 8, 8, 9, 11, 12, 13, 15, 15, 16, 2},
            {4, 5, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 15, 17, 17, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 15, 17, 17, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 15, 17, 17, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 15, 17, 18, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 16, 17, 18, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 16, 17, 18, 3},
            {4, 6, 7, 8, 9, 10, 11, 13, 14, 16, 17, 18, 3},
            {4, 6, 7, 8, 9, 10, 12, 13, 15, 16, 17, 18, 3},
            {4, 6, 8, 9, 9, 10, 12, 14, 15, 16, 18, 19, 3},
            {4, 6, 8, 9, 9, 10, 12, 14, 15, 16, 18, 19, 3},
            {4, 6, 8, 9, 9, 10, 12, 14, 15, 16, 18, 19, 3},
            {4, 6, 8, 9, 9, 10, 12, 14, 15, 16, 18, 19, 3},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 16, 18, 19, 3},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 18, 19, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 15, 17, 19, 20, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 16, 18, 19, 20, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 16, 18, 19, 20, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 16, 18, 19, 20, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 16, 18, 19, 20, 4},
            {5, 6, 8, 9, 10, 11, 12, 14, 16, 18, 19, 20, 4}};

    static double Yb;

    static void Wind() {
        Yb = getFromShootTables(of45_4p, 17, (int) distance);
        int alt = (int) round(Yb / 100);
        int angle_on = (int) angle;
        if (alt > 40) alt = 40;
        int wind_a = 2500;
        int wind_w = 0;
        int wind_d = 50;
        if (alt >= 2) {
            wind_w = wind_speed[alt - 2][wind_d / 10 - 4];
            int wind_delta_a = wind_speed[alt - 2][12] * 100;
            wind_a += wind_delta_a;
            wind_a = angle_on - wind_a;
            if (wind_a < 0) wind_a += 6000;
        }
        wind_x = -wind_w * cos(toRadians(((double) wind_a / 100) * 6));
        wind_y = wind_w * sin(toRadians(((double) wind_a / 100) * 6));
    }
}
