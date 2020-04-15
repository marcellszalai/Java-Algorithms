package uk.ac.warwick.cs126.util;

public class HaversineDistanceCalculator {

    private final static float R = 6372.8f;

    /**
     * Calculates the distance in kms
     * @param lat1,lon1 parameters of the first place
     * @param lat2,lon2 parameters of the second place
     * @return the distance between them in kms
     */
    public static float inKilometres(float lat1, float lon1, float lat2, float lon2) {
        double latitude1 = Math.toRadians(lat1);
        double latitude2 = Math.toRadians(lat2);
        double longitude1 = Math.toRadians(lon1);
        double longitude2 = Math.toRadians(lon2);

        //This algorithm calculates the distance in kms
        double a = Math.pow(Math.sin((latitude1-latitude2)/2),2)+Math.cos(latitude1)*Math.cos(latitude2)*Math.pow(Math.sin((longitude2-longitude1)/2),2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double d = R * c;
        double e = Math.round(d * 10) / 10.0;
        float result = (float)e;

        //returns the distance
        return result;
    }

    /**
     * Calculates the distance in kms
     * @param lat1,lon1 parameters of the first place
     * @param lat2,lon2 parameters of the second place
     * @return the distance between them in miles
     */
    public static float inMiles(float lat1, float lon1, float lat2, float lon2) {

        //Firstly it calls the other method to calculate the distance in  kms
        //Then converts it to miles
        float resultInKM=inKilometres(lat1, lon1, lat2, lon2);
        double resultInMiles=resultInKM * 0.62137;
        double e = Math.round(resultInMiles * 10) / 10.0;
        float result = (float)e;

        //Returns the distance in miles
        return result;
    }

}