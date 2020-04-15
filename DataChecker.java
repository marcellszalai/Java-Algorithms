package uk.ac.warwick.cs126.util;

import uk.ac.warwick.cs126.interfaces.IDataChecker;

import uk.ac.warwick.cs126.models.Customer;
import uk.ac.warwick.cs126.models.Restaurant;
import uk.ac.warwick.cs126.models.Favourite;
import uk.ac.warwick.cs126.models.Review;

import java.util.Date;

public class DataChecker implements IDataChecker {

    public DataChecker() {
        // Initialise things here
    }

    /**
     * Extracts the true ID from the input IDs
     * @param repeatedID Array of IDs
     * @return null or true ID
     */
    public Long extractTrueID(String[] repeatedID) {
        Long result;

        //returns null when there are not exactly 3 elements in the array
        if(repeatedID.length!=3){
            return null;
        }

        //if some id appears at least 4 times that means that it is the true ID
        else{
            String rID1 = repeatedID[0];
            String rID2 = repeatedID[1];
            String rID3 = repeatedID[2];
            if(rID1.equals(rID2)){
                result = Long.parseLong(rID1);
            }
            else if(rID2.equals(rID3)){
                result = Long.parseLong(rID2);
            }
            else if(rID1.equals(rID3)){
                result = Long.parseLong(rID1);
            }
            else{
                return null;
            }
        }

        //returns the true ID
        return result;
    }

    /**
     * Checks if the ID is valis
     * @param inputID the ID to be checked
     * @return false or true
     */
    public boolean isValid(Long inputID) {
        Long num = inputID;
        int count = 0;
        Long tmp = num;
        Long remainder = 0L;

        //If the ID contains a 0 that means that is invalid, thus returns false
        if (String.valueOf(num).contains("0")){
            return false;
        }
        
        //A valid ID must consist exactly 16 characters
        //Returns false if not
        while (tmp>=1){
            tmp /= 10;
            count++;
        }
        if(count!=16){
            return false;
        }

        //A valid cannot consist more than 3 of each digit
        //If it consists more the ID is invalid, so it returns false 
        tmp=num;
        Long[] digitArray = new Long[10];
        for(int i=0;i<digitArray.length;i++){
            digitArray[i]=0L;
        }
        while (tmp > 0) {
            remainder = tmp % 10;
            digitArray[remainder.intValue()]++;
            tmp= tmp / 10;
        }
        for (int counter = 0; counter < digitArray.length; counter++) {
            // get the count
            Long digitCount = digitArray[counter];
            if (digitCount > 3) {
                return false;
            }
        }           
        //Otherwise the ID is valid so the method returns true
        return true; 
    }

    /**
     * Checks if the customer id valid
     * @param customer input to be checked
     * @return true when valid, false when not 
     */
    public boolean isValid(Customer customer) {
        if(customer==null || customer.getID()==null || customer.getFirstName()==null || customer.getLastName()==null || customer.getDateJoined()==null || customer.getLatitude()==0.0f || customer.getLongitude()==0.0f){
            return false;
        }
        Long id = customer.getID();
        if (isValid(id)==false){
            return false;
        }       
        return true;
    }

    /**
     * Checks if the restaurant id valid
     * @param restaurant input to be checked
     * @return true when valid, false when not 
     */
    public boolean isValid(Restaurant restaurant) {

        //A restaurant is valid if it is not null and if all of its parameters are valid
        if (restaurant==null || restaurant.getID()==null || restaurant.getRepeatedID()==null ||restaurant.getOwnerFirstName()==null || restaurant.getOwnerLastName()==null || restaurant.getCuisine()==null || restaurant.getEstablishmentType()==null || restaurant.getPriceRange()==null || restaurant.getDateEstablished()==null || restaurant.getLatitude()==0.0f || restaurant.getLastInspectedDate()==null){
            return false;
        }

        //Gets the ID from the repeatedID
        //Extracts the true ID from the method above
        //If the true ID is not valid returns false
        String[] id = restaurant.getRepeatedID();
        Long trueID = extractTrueID(id);
        if (isValid(trueID)==false){
            return false;
        }     

        //Returns false when:
        // - the food inspection ratid is not between 0 and 5
        // - the corresponding warwick stars are not between 0 and 3
        // - the customer rating is not between 1 and 5 or not 0
        if (restaurant.getFoodInspectionRating()<0 || restaurant.getFoodInspectionRating()>5 || restaurant.getWarwickStars()<0 || restaurant.getWarwickStars()>3){
            return false;
        }
        if((restaurant.getCustomerRating()<1.0f ||restaurant.getCustomerRating()>5.0f) && restaurant.getCustomerRating()!=0.0f){
            return false;
        }
        return true;
    }

    /**
     * Checks if the favourite id valid
     * @param favourite input to be checked
     * @return true when valid, false when not 
     */
    public boolean isValid(Favourite favourite) {

        //A favourite is valid when it is not null and all the parameters not null
        if(favourite==null || favourite.getID()==null || favourite.getCustomerID()==null ||favourite.getRestaurantID()==null || favourite.getDateFavourited()==null){
            return false;
        }

        //The ID, the ID of the corresponding reastaurant and customer has to be valid
        Long id = favourite.getID();
        Long rid = favourite.getRestaurantID();
        Long cid = favourite.getCustomerID();
        if (isValid(id)==false || isValid(rid)==false || isValid(cid)==false){
            return false;
        }       
        return true;
    }

    /**
     * Checks if the favourite id valid
     * @param favourite input to be checked
     * @return true when valid, false when not 
     */
    public boolean isValid(Review review) {

        //A review is valid when it is not null and all the parameters not null
        if(review==null || review.getID()==null || review.getCustomerID()==null ||review.getRestaurantID()==null || review.getDateReviewed()==null || review.getRating()==0 || review.getReview()==null){
            return false;
        }

        //The ID, the ID of the corresponding reastaurant and customer has to be valid
        Long id = review.getID();
        Long rid = review.getRestaurantID();
        Long cid = review.getCustomerID();
        if (isValid(id)==false || isValid(rid)==false || isValid(cid)==false){
            return false;
        }       
        return true;
    }
}