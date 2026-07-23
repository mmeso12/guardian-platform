package com.guardian.cloud.service;

import org.springframework.stereotype.Component;

@Component
public class GeofenceDistanceCalculator {

    private static final double EARTH_RADIUS_METERS =
            6_371_000.0;

    public double calculateMeters(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double latitude1Radians =
                Math.toRadians(latitude1);

        double latitude2Radians =
                Math.toRadians(latitude2);

        double latitudeDifference =
                Math.toRadians(latitude2 - latitude1);

        double longitudeDifference =
                Math.toRadians(longitude2 - longitude1);

        double a =
                Math.sin(latitudeDifference / 2.0)
                        * Math.sin(latitudeDifference / 2.0)
                        + Math.cos(latitude1Radians)
                        * Math.cos(latitude2Radians)
                        * Math.sin(longitudeDifference / 2.0)
                        * Math.sin(longitudeDifference / 2.0);

        double angularDistance =
                2.0 * Math.atan2(
                        Math.sqrt(a),
                        Math.sqrt(1.0 - a)
                );

        return EARTH_RADIUS_METERS * angularDistance;
    }

    public boolean isInside(
            double deviceLatitude,
            double deviceLongitude,
            double geofenceLatitude,
            double geofenceLongitude,
            double radiusMeters
    ) {
        double distanceMeters =
                calculateMeters(
                        deviceLatitude,
                        deviceLongitude,
                        geofenceLatitude,
                        geofenceLongitude
                );

        return distanceMeters <= radiusMeters;
    }
}