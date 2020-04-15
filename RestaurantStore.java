package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IRestaurantStore;
import uk.ac.warwick.cs126.models.Cuisine;
import uk.ac.warwick.cs126.models.EstablishmentType;
import uk.ac.warwick.cs126.models.Place;
import uk.ac.warwick.cs126.models.PriceRange;
import uk.ac.warwick.cs126.models.Restaurant;
import uk.ac.warwick.cs126.models.RestaurantDistance;
import java.util.regex.Pattern;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;

import uk.ac.warwick.cs126.util.ConvertToPlace;
import uk.ac.warwick.cs126.util.HaversineDistanceCalculator;
import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class RestaurantStore implements IRestaurantStore {

    private MyArrayList<Restaurant> restaurantArray;
    private DataChecker dataChecker;
    private MyArrayList<Long> blackList;
    private HaversineDistanceCalculator distanceCalc;
    private StringFormatter stringFormatter;

    /**
     * Constructor method
     * @param restaurantArray array that contains the restaurants
     * @param dataChecked object for the metods in the DataChecker class
     * @param blackList array that contains the blacklisted items
     * @param distanceCalc object for the methods in the HaversineDistanceCalculator class
     * @param stringFormatter object for the methods in the StringFormatter class
     */
    public RestaurantStore() {

        restaurantArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        blackList = new MyArrayList<>();
        distanceCalc = new HaversineDistanceCalculator();
        stringFormatter = new StringFormatter();
    }

    public Restaurant[] loadRestaurantDataToArray(InputStream resource) {
        Restaurant[] restaurantArray = new Restaurant[0];

        try {
            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line = lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Restaurant[] loadedRestaurants = new Restaurant[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            String row;
            int restaurantCount = 0;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");

                    Restaurant restaurant = new Restaurant(
                            data[0],
                            data[1],
                            data[2],
                            data[3],
                            Cuisine.valueOf(data[4]),
                            EstablishmentType.valueOf(data[5]),
                            PriceRange.valueOf(data[6]),
                            formatter.parse(data[7]),
                            Float.parseFloat(data[8]),
                            Float.parseFloat(data[9]),
                            Boolean.parseBoolean(data[10]),
                            Boolean.parseBoolean(data[11]),
                            Boolean.parseBoolean(data[12]),
                            Boolean.parseBoolean(data[13]),
                            Boolean.parseBoolean(data[14]),
                            Boolean.parseBoolean(data[15]),
                            formatter.parse(data[16]),
                            Integer.parseInt(data[17]),
                            Integer.parseInt(data[18]));

                    loadedRestaurants[restaurantCount++] = restaurant;
                }
            }
            csvReader.close();

            restaurantArray = loadedRestaurants;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return restaurantArray;
    }

    /**
     * Adds one restaurant to the array
     * @param restaurant restaurant to be added
     * @return true when added, false when not
     */
    public boolean addRestaurant(Restaurant restaurant) {  
        
        //extracts the true id using the DataChecker class
        //true id cant be null
        Long trueID = dataChecker.extractTrueID(restaurant.getRepeatedID());
        if(trueID==null){return false;}

        //if it is not null sets the id of the restaurant
        restaurant.setID(trueID);

        //checks if the resaturants is valid with the DataChecker class
        if(!dataChecker.isValid(restaurant)){return false;}

        Restaurant tmp;

        //checks if there is a restaurant with the same id in the array
        // if yes, removes that restaurant+blacklists the id
        for(int i = 0; i<restaurantArray.size();i++){
            tmp = restaurantArray.get(i);
            if (tmp.getID().equals(restaurant.getID())){
                restaurantArray.remove(tmp);
                blackList.add(restaurant.getID());
                return false;
            }
        }

        //checks if the id is blacklisted
        for(int i = 0;i<blackList.size();i++){
            if(restaurant.getID().equals(blackList.get(i))){
                return false;
            }
        }

        //if the array is empty, adds the restaurant
        if (restaurantArray.isEmpty()==true){    
            return restaurantArray.add(restaurant);      
        }

        //adds the restaurant in ascending order of their id
        Restaurant temp;
        for (int i = 0; i<restaurantArray.size(); i++){
            temp = restaurantArray.get(i);
            if (temp.getID().compareTo(restaurant.getID())>=0){            
                restaurantArray.set(i,restaurant);
                restaurant = temp;
            }
        }

        //returns true if it succesfully adds a restaurant
        return restaurantArray.add(restaurant);      
    }

    /**
     * Adds an array of restaurants
     * @param restaurnats array of restaurants to be added
     * @return true, when all of the restaurants are added, false otherwise
     */
    public boolean addRestaurant(Restaurant[] restaurants) {
        boolean notAdded=false;

        //when at least on of the restaurants is not added returns false
        for(int i =0 ;i<restaurants.length;i++){
            if (!addRestaurant(restaurants[i])){
                notAdded=true;
            }
        }

        return !notAdded;   
    }

    /**
     * Gets the restaurants which has the same id as the input
     * @param id input id of the restaurant
     */
    public Restaurant getRestaurant(Long id) {
        Restaurant tmp=null;

        //linear search through the reaturants
        for(int i = 0; i<restaurantArray.size();i++){
            tmp=restaurantArray.get(i);
            if(tmp.getID().equals(id)){ 
                return tmp;
            }
        }
        return null;
    }

    /**
     * Gest all the restaurants in ascending order of their id
     * @return an array of sorted restaurants
     */
    public Restaurant[] getRestaurants() {
        Restaurant[] allRestaurants = new Restaurant[restaurantArray.size()];

        //since the restaurants are already sorted this way there is nothing to do with them but insert them in an array
        for(int i = 0;i<restaurantArray.size();i++){
            allRestaurants[i]=restaurantArray.get(i);
        }

        return allRestaurants;
    }

    /**
     * Sorts the input restaurint in ascending order of their id
     * @param restaurants the inputted, unsorted restaurants
     * @return a sorted array of the same restaurants
     */
    public Restaurant[] getRestaurants(Restaurant[] restaurants) {
        Restaurant[] allRestaurants = new Restaurant[restaurants.length];
        int size = 0;
        Restaurant tempRestaurant;

        //each elements goes through the array
        for(int j=0 ;j<restaurants.length;j++){
            for (int i = 0; i<size; i++){

                //they switch places when the element is smaller
                if (allRestaurants[i].getID().compareTo(restaurants[j].getID())>=0){
                    tempRestaurant = allRestaurants[i];
                    allRestaurants[i]=restaurants[j];
                    restaurants[j] = tempRestaurant;
                }
            }
            //the largest element gets added in the end
            allRestaurants[size]=restaurants[j];
            size++;
        }

        return allRestaurants;
    }

    /**
     * Organises all of the restaurants by their names 
     * If the name is the same then by their IDs
     */
    public Restaurant[] getRestaurantsByName() {
        Restaurant[] restaurants = new Restaurant[restaurantArray.size()];
        for(int i = 0;i<restaurantArray.size();i++){
            restaurants[i]=restaurantArray.get(i);
        }

        Restaurant[] allRestaurants = new Restaurant[restaurants.length]; // new array
        int size = 0;
        Restaurant tempRestaurant;

        //each element goes through the array
        //same procedure as above but with two steps
        for(int j=0 ;j<restaurants.length;j++){
            for (int i = 0; i<size; i++){

                //firstly compares the names
                if (allRestaurants[i].getName().compareTo(restaurants[j].getName())>0){
                    tempRestaurant = allRestaurants[i];
                    allRestaurants[i]=restaurants[j];
                    restaurants[j] = tempRestaurant;
                }

                //if names are the same , checks the  IDs
                //IDs  cant be the same
                else if (allRestaurants[i].getName().compareTo(restaurants[j].getName())==0){
                    if (allRestaurants[i].getID().compareTo(restaurants[j].getID())>0){
                        tempRestaurant = allRestaurants[i];
                        allRestaurants[i]=restaurants[j];
                        restaurants[j] = tempRestaurant;
                    }
                }
            }
            allRestaurants[size]=restaurants[j];
            size++;
        }

        //returns the organsied array
        return allRestaurants;
    }

    //uses the method below for all the restaurants in the array
    public Restaurant[] getRestaurantsByDateEstablished() {
        Restaurant[] restaurants = new Restaurant[restaurantArray.size()];
        for(int i = 0;i<restaurantArray.size();i++){
            restaurants[i]=restaurantArray.get(i);
        }
        return getRestaurantsByDateEstablished(restaurants);

        
    }

    /**
     * Sorts the restaurants according to their opening date
     * Oldest goes first
     * If dates are equal, then by names
     * If names are equla then by the IDs
     * Ids cant be equal
     */
    public Restaurant[] getRestaurantsByDateEstablished(Restaurant[] restaurants) {
        Restaurant[] allRestaurants = new Restaurant[restaurants.length]; // new array
        int size = 0;
        Restaurant tempRestaurant;

        //each element goes through the array like in the ones above
        //only differnece is that there are 3 steps
        for(int j=0 ;j<restaurants.length;j++){

            //firstly checks the Estabilishment Date
            for (int i = 0; i<size; i++){
                if (allRestaurants[i].getDateEstablished().compareTo(restaurants[j].getDateEstablished())>0){
                    tempRestaurant = allRestaurants[i];
                    allRestaurants[i]=restaurants[j];
                    restaurants[j] = tempRestaurant;
                }

                //secondly the names
                else if (allRestaurants[i].getDateEstablished().compareTo(restaurants[j].getDateEstablished())==0){
                    if (allRestaurants[i].getName().compareTo(restaurants[j].getName())>0){
                        tempRestaurant = allRestaurants[i];
                        allRestaurants[i]=restaurants[j];
                        restaurants[j] = tempRestaurant;
                    }

                    //thirdly the ids
                    else if (allRestaurants[i].getName().compareTo(restaurants[j].getName())==0){
                        if (allRestaurants[i].getID().compareTo(restaurants[j].getID())>0){
                            tempRestaurant = allRestaurants[i];
                            allRestaurants[i]=restaurants[j];
                            restaurants[j] = tempRestaurant;
                        }
                    }
                }
            }
            allRestaurants[size]=restaurants[j];
            size++;
        }

        //returns the new, sorted array
        return allRestaurants;
    }

    /**
     * Sorts all of the restaurants by their Warwick stars
     * If they are the same, then by their names
     * If names are the same then by their IDs
     * IDs cant be the same
     * Same as the method above
     */
    public Restaurant[] getRestaurantsByWarwickStars() {
        Restaurant[] restaurants = new Restaurant[restaurantArray.size()];
        for(int i = 0;i<restaurantArray.size();i++){
            restaurants[i]=restaurantArray.get(i);
        }
        if (restaurants.length==0){
            return new Restaurant[0];
        }
        Restaurant[] allRestaurants = new Restaurant[restaurants.length]; // new array
        int size = 0;
        Restaurant tempRestaurant;

        //each element goes through the array
        for(int j=0 ;j<restaurants.length;j++){

            //there are three steps
            //firstly compares the warwickstars
            if(restaurants[j].getWarwickStars()>=1){
                for (int i = 0; i<size; i++){
                    if (allRestaurants[i].getWarwickStars()>restaurants[j].getWarwickStars()){
                        tempRestaurant = allRestaurants[i];
                        allRestaurants[i]=restaurants[j];
                        restaurants[j] = tempRestaurant;
                    }

                    //then the names
                    else if (allRestaurants[i].getWarwickStars()==restaurants[j].getWarwickStars()){
                        if (allRestaurants[i].getName().compareTo(restaurants[j].getName())>0){
                            tempRestaurant = allRestaurants[i];
                            allRestaurants[i]=restaurants[j];
                            restaurants[j] = tempRestaurant;
                        }

                        //lastly the ids
                        else if (allRestaurants[i].getName().compareTo(restaurants[j].getName())==0){
                            if (allRestaurants[i].getID().compareTo(restaurants[j].getID())>0){
                                tempRestaurant = allRestaurants[i];
                                allRestaurants[i]=restaurants[j];
                                restaurants[j] = tempRestaurant;
                            }
                        }
                    }
                }
                allRestaurants[size]=restaurants[j];
                size++;
            }
            
        }
        Restaurant[] finalR = new Restaurant[size];
        for(int i = 0;i<size;i++){
            finalR[i]=allRestaurants[i];
        }
        //sorted
        return finalR;
    }

    /**
     * Same as the ones above
     * Sorting order: Rating->Name->ID
     * @param restaurants the array of restaurants to sort
     */
    public Restaurant[] getRestaurantsByRating(Restaurant[] restaurants) {
        Restaurant[] allRestaurants = new Restaurant[restaurants.length];
        int size = 0;
        Restaurant tempRestaurant;
        for(int j=0 ;j<restaurants.length;j++){
            for (int i = 0; i<size; i++){
                if (allRestaurants[i].getCustomerRating()<restaurants[j].getCustomerRating()){
                    tempRestaurant = allRestaurants[i];
                    allRestaurants[i]=restaurants[j];
                    restaurants[j] = tempRestaurant;
                }
                else if (allRestaurants[i].getCustomerRating()==restaurants[j].getCustomerRating()){
                    if (allRestaurants[i].getName().compareTo(restaurants[j].getName())>0){
                        tempRestaurant = allRestaurants[i];
                        allRestaurants[i]=restaurants[j];
                        restaurants[j] = tempRestaurant;
                    }
                    else if (allRestaurants[i].getName().compareTo(restaurants[j].getName())==0){
                        if (allRestaurants[i].getID().compareTo(restaurants[j].getID())>0){
                            tempRestaurant = allRestaurants[i];
                            allRestaurants[i]=restaurants[j];
                            restaurants[j] = tempRestaurant;
                        }
                    }
                }
            }
            allRestaurants[size]=restaurants[j];
            size++;
        }
        return allRestaurants;
    }

    /**
     * Gets all the restaurants and their distance from the certain coordinates
     * @param latitude the x coordinate of Earth
     * @param longitude the y coordinate of Earth
     * @return the desired RestaurantDistence array
     */
    public RestaurantDistance[] getRestaurantsByDistanceFrom(float latitude, float longitude) {
        Restaurant[] restaurants = new Restaurant[restaurantArray.size()];
        for(int i = 0;i<restaurantArray.size();i++){
            restaurants[i]=restaurantArray.get(i);
        }

        //returns nothing if the array is empty
        if (restaurants.length==0){
            return new RestaurantDistance[0];
        }

        //uses the next method to calculate the distance
        return getRestaurantsByDistanceFrom(restaurants, latitude, longitude);
    }

    /**
     * Gets the restaurants from the input array and their distances from the given coordinates
     * @param restaurants the array of restaurants we are looking for
     * @param latitude the x coordinate of Earth
     * @param longitude the y coordinate of Earth
     * @return the desired RestaurantDistance array
     */
    public RestaurantDistance[] getRestaurantsByDistanceFrom(Restaurant[] restaurants, float latitude, float longitude) {
        RestaurantDistance[] allRestaurants = new RestaurantDistance[restaurants.length];
        int size = 0;
        RestaurantDistance tempRestaurant;

        //each element goes through the array
        for(int j=0 ;j<restaurants.length;j++){

            //calculates the distance using the distance calculator method
            float distance = distanceCalc.inKilometres(latitude, longitude, restaurants[j].getLatitude(), restaurants[j].getLongitude());
            RestaurantDistance tempDistance = new RestaurantDistance(restaurants[j], distance);
            
            //sorts them according to the distance from the coordinates
            for (int i = 0; i<size; i++){
                if (allRestaurants[i].getDistance()>distance){
                    tempRestaurant = allRestaurants[i];
                    allRestaurants[i]=tempDistance;
                    tempDistance = tempRestaurant;
                }

                //secondly by their names
                else if (allRestaurants[i].getDistance()>distance){
                    if (allRestaurants[i].getRestaurant().getName().compareTo(restaurants[j].getName())>0){
                        tempRestaurant = allRestaurants[i];
                        allRestaurants[i]=tempDistance;
                        tempDistance = tempRestaurant;
                    }

                    //thirdly by their IDs
                    else if (allRestaurants[i].getRestaurant().getName().compareTo(restaurants[j].getName())==0){
                        if (allRestaurants[i].getRestaurant().getID().compareTo(restaurants[j].getID())>0){
                            tempRestaurant = allRestaurants[i];
                            allRestaurants[i]=tempDistance;
                            tempDistance = tempRestaurant;
                        }
                    }
                }
            }
            allRestaurants[size]= tempDistance;
            size++;          
        }
        RestaurantDistance[] finalR = new RestaurantDistance[size];
        for(int i = 0;i<size;i++){
            finalR[i]=allRestaurants[i];
        }
        //sorted
        return finalR;
    }

    /**
     * Searches for the restaurants that contain a certain String
     * @param searchTerm the String that we search for between the restaurants
     * @return an array of restaurants that contain the string
     */
    public Restaurant[] getRestaurantsContaining(String searchTerm) {
        Restaurant[] contains = new Restaurant[0];

        //if the input is empty returns nothing
        if (searchTerm==null){
            return contains;
        }
        int size = 0;
        
        //converts the input to be readable
        String searchTermConvertedFaster = stringFormatter.convertAccentsFaster(searchTerm);

        //splits the input to separate Strings
        //Recognises multiple spaces too
        String[] terms = searchTermConvertedFaster.split("\\s+");

        //checks every restaurant
        for(int i = 0; i<restaurantArray.size();i++){

            //checks every word
            for(int j = 0;j<terms.length;j++){

                //if name does not contain
                if(!Pattern.compile(Pattern.quote(terms[j]), Pattern.CASE_INSENSITIVE).matcher(restaurantArray.get(i).getName()).find()){
                    break;
                }

                //if it gets to the last word then the restaurant contains the string
                //adds the restaurant to the array
                if(j==terms.length-1){
                    size++;
                    Restaurant[] temp = new Restaurant[size];
                    for(int k=0;k<size-1;k++){
                        temp[k]=contains[k];
                    }
                    temp[size-1]=restaurantArray.get(i);
                    contains = temp;
                }
            }
        } 
        //returns the array organsied by the names of the restaurants
        return contains;
    }
}
