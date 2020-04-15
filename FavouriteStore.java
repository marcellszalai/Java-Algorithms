package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IFavouriteStore;
import uk.ac.warwick.cs126.models.Favourite;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;

import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.structures.HashMap;


public class FavouriteStore implements IFavouriteStore {

    private MyArrayList<Favourite> favouriteArray;
    private DataChecker dataChecker;
    private MyArrayList<Long> blackList;

    //Constructor method
    public FavouriteStore() {

        favouriteArray = new MyArrayList<>(); // contains the favourites
        dataChecker = new DataChecker();      // enables to use the method from the DataChecker class    
        blackList = new MyArrayList<>();      // stores the blacklisted elements
    }

    public Favourite[] loadFavouriteDataToArray(InputStream resource) {
        Favourite[] favouriteArray = new Favourite[0];

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

            Favourite[] loadedFavourites = new Favourite[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int favouriteCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");
                    Favourite favourite = new Favourite(
                            Long.parseLong(data[0]),
                            Long.parseLong(data[1]),
                            Long.parseLong(data[2]),
                            formatter.parse(data[3]));
                    loadedFavourites[favouriteCount++] = favourite;
                }
            }
            csvReader.close();

            favouriteArray = loadedFavourites;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return favouriteArray;
    }

    /**
     * Adds one favourite to the array
     * @param favourite favourite to be added
     * @return true when added, false when not
     */
    public boolean addFavourite(Favourite favourite) {

        //checks if favourite is valid
        if(!dataChecker.isValid(favourite)){return false;}

        Favourite tmp;

        //checks if favourite is blacklisted
        for(int i = 0;i<blackList.size();i++){
            if(favourite.getID().equals(blackList.get(i))){
                return false;
            }
        }

        //checks if there is a favourite with the same ID already
        //checks if there is a favourite with the same customer and restaurant ID
        //if yes, removes that favourite
        //marks their ID blacklisted
        for(int i = 0; i<favouriteArray.size() ; i++){
            tmp = favouriteArray.get(i);
            if (tmp.getID().equals(favourite.getID())){
                favouriteArray.remove(tmp);
                blackList.add(favourite.getID());
                return false;
            }
            if(tmp.getCustomerID().equals(favourite.getCustomerID()) && tmp.getRestaurantID().equals(favourite.getRestaurantID())){
                if(favourite.getDateFavourited().compareTo(tmp.getDateFavourited())<0){
                    blackList.add(tmp.getID());
                    favouriteArray.set(i, favourite);
                    return true;
                }
            }
        }
        
        //if the array is empty, adds to the array
        if (favouriteArray.isEmpty()==true){          
            return favouriteArray.add(favourite);         
        }

        Favourite temp;

        //adds the favaourite to the array organised by their name Date
        //if the date is the same then organised by their ID
        for (int i = 0; i<favouriteArray.size(); i++){
            temp = favouriteArray.get(i);
            if (temp.getDateFavourited().compareTo(favourite.getDateFavourited())<0){            
                favouriteArray.set(i,favourite);
                favourite = temp;
            }
            else if(temp.getDateFavourited().compareTo(favourite.getDateFavourited())==0){
                if (temp.getID().compareTo(favourite.getID())<0){
                    favouriteArray.set(i,favourite);
                    favourite = temp;
                }
            }
        }
        return favouriteArray.add(favourite);
    }

    /**
     * Adds an array of favourites to the favouriteArray
     * @param favourites array of customers to be added
     * @return true when all of the are added, false if at least one is not
     */
    public boolean addFavourite(Favourite[] favourites) {
        boolean notAdded=false;

        //notAdded turns to false when one is not added
        for(int i =0 ;i<favourites.length;i++){
            if (!addFavourite(favourites[i])){
                notAdded=true;
            }
        }
        return !notAdded;  
    }

    /**
     * Gets the favourite with the same id
     * @param id the ID of the favourite
     * @return favourite with the same id or null if there is no favourite with such id
     */
    public Favourite getFavourite(Long id) {
        Favourite tmp=null;

        //linear search through the customers
        for(int i = 0; i<favouriteArray.size();i++){
            tmp=favouriteArray.get(i);
            if(tmp.getID().equals(id)){ 
                return tmp;
            }
        }

        // if there is no match
        return null;
    }

    /**
     * Gets all the favourites in ascending order of their IDs
     * Since the favourites are sorted according to their date we need to reorganise
     * @return array of all favourites
     */
    public Favourite[] getFavourites(){

        //new array
        Favourite[] favourites = new Favourite[favouriteArray.size()];

        //adds every favourite to the array
        for(int i = 0;i<favouriteArray.size();i++){
            favourites[i]=favouriteArray.get(i);
        }

        //adds every favourite to the new array reorganised
        Favourite[] allFavourites = new Favourite[favourites.length];
        int size = 0;
        Favourite tempFavourite;
        for(int j=0 ;j<favourites.length;j++){
            for (int i = 0; i<size; i++){
                if (allFavourites[i].getID().compareTo(favourites[j].getID())>0){
                    tempFavourite= allFavourites[i];
                    allFavourites[i]=favourites[j];
                    favourites[j] = tempFavourite;
                }
            }
            allFavourites[size]=favourites[j];
            size++;
        }

        //returns reorganised array
        return allFavourites;
    }

    /**
     * Gets all the favourites in ascending order of their Customer IDs
     * Since the favourites are sorted according to their date we need to reorganise
     * @return array of all favourites
     */
    public Favourite[] getAllFavouritesByCustomerID(){

        //new array
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        for(int i = 0;i<favouriteArray.size();i++){
            favourites[i]=favouriteArray.get(i);
        }

        //adds every element to the array reorganised
        Favourite[] allFavourites = new Favourite[favourites.length];
        int size = 0;
        Favourite tempFavourite;
        for(int j=0 ;j<favourites.length;j++){
            for (int i = 0; i<size; i++){
                if (allFavourites[i].getCustomerID().compareTo(favourites[j].getCustomerID())>0){
                    tempFavourite= allFavourites[i];
                    allFavourites[i]=favourites[j];
                    favourites[j] = tempFavourite;
                }

                //if the Customer IDs are the same, than according to their IDs
                else if (allFavourites[i].getCustomerID().compareTo(favourites[j].getCustomerID())==0){
                    if (allFavourites[i].getID().compareTo(favourites[j].getID())>0){
                        tempFavourite = allFavourites[i];
                        allFavourites[i]=favourites[j];
                        favourites[j] = tempFavourite;
                    }
                }
            }
            allFavourites[size]=favourites[j];
            size++;
        }

        //returns new array
        return allFavourites;
    }

    /**
     * Gets all the favourites in ascending order of their Restaurant IDs
     * Since the favourites are sorted according to their date we need to reorganise
     * @return array of all favourites
     */
    public Favourite[] getAllFavouritesByRestaurantID(){

        //new array
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        for(int i = 0;i<favouriteArray.size();i++){
            favourites[i]=favouriteArray.get(i);
        }

        //adds every favourite to the now array reorgnaised
        Favourite[] allFavourites = new Favourite[favourites.length];
        int size = 0;
        Favourite tempFavourite;
        for(int j=0 ;j<favourites.length;j++){
            for (int i = 0; i<size; i++){
                if (allFavourites[i].getRestaurantID().compareTo(favourites[j].getRestaurantID())>0){
                    tempFavourite= allFavourites[i];
                    allFavourites[i]=favourites[j];
                    favourites[j] = tempFavourite;
                }
                else if (allFavourites[i].getRestaurantID().compareTo(favourites[j].getRestaurantID())==0){
                    if (allFavourites[i].getID().compareTo(favourites[j].getID())>0){
                        tempFavourite = allFavourites[i];
                        allFavourites[i]=favourites[j];
                        favourites[j] = tempFavourite;
                    }
                }
            }
            allFavourites[size]=favourites[j];
            size++;
        }

        //returns the new array
        return allFavourites;
    }

    /**
     * Gets all the favourites in ascending order of their Dates
     * Since the favourites are already sorted that way this is very fast
     * @return array of all favourites
     */
    public Favourite[] getFavouritesByDate() {

        //new array
        Favourite[] allFavourites = new Favourite[favouriteArray.size()];

        //adds every favourite to the new array
        for(int i = 0;i<favouriteArray.size();i++){
            allFavourites[i]=favouriteArray.get(i);
        }

        //returns new array
        return allFavourites;
    }

    //same as the method above but it only does it with an input array of customers
    public Favourite[] getFavouritesByCustomerID(Long id) {
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getFavouritesByDate();
        Favourite[] allFavourites = new Favourite[0];

        int size=0;
        for (int i = 0;i<favourites.length;i++){
            if(favourites[i].getCustomerID().equals(id)){
                Favourite[] tempFavourites = new Favourite[size+1];
                for(int j = 0;j<size;j++){
                    tempFavourites[j]=allFavourites[j];
                }
                tempFavourites[size]=favourites[i];
                allFavourites=tempFavourites;
                size++;
            }
        }

        return allFavourites;

    }

    //same as the method above but it only does it with an input array of customers
    public Favourite[] getFavouritesByRestaurantID(Long id) {
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getFavouritesByDate();
        Favourite[] allFavourites = new Favourite[0];

        int size=0;
        for (int i = 0;i<favourites.length;i++){
            if(favourites[i].getRestaurantID().equals(id)){
                Favourite[] tempFavourites = new Favourite[size+1];
                for(int j = 0;j<size;j++){
                    tempFavourites[j]=allFavourites[j];
                }
                tempFavourites[size]=favourites[i];
                allFavourites=tempFavourites;
                size++;
            }
        }

        return allFavourites;
    }

    /**
     * Gets the common favourite restaurants between two customers
     * @param customer1ID,customer2ID the IDs of the customers to compare
     * @return an array of restourants that both customers favourited
     */
    public Long[] getCommonFavouriteRestaurants(Long customer1ID, Long customer2ID) {

        // new array of favourite restaurants of customer1
        MyArrayList<Long> contains1Array = new MyArrayList<>();
        // new array of favourite restaurants of customer2
        MyArrayList<Long> contains2Array = new MyArrayList<>(); 
        // new array of favourite restaurants for both customers
        MyArrayList<Long> common = new MyArrayList<>();

        //first the favourite restaurants are added to the two arrays
        //if the customerID matches one of the customers ID
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getFavouritesByDate();
        for (int i = 0;i<favourites.length;i++){
            if(favourites[i].getCustomerID().equals(customer1ID)){
                contains1Array.add(favourites[i].getRestaurantID());
            }
            if(favourites[i].getCustomerID().equals(customer2ID)){
                contains2Array.add(favourites[i].getRestaurantID());
            }
        }

        //search through the maller array
        //if the other array contains that element too it gets added to the common array
        if(contains1Array.size()<=contains2Array.size()){
            for(int i = 0;i<contains1Array.size();i++){
                if(contains2Array.contains(contains1Array.get(i))){
                    common.add(contains1Array.get(i));
                }
            }
        }
        else{
            for(int i = 0;i<contains2Array.size();i++){
                if(contains1Array.contains(contains2Array.get(i))){
                    common.add(contains2Array.get(i));
                }
            }
        }

        //lastly we insert the elements in a new array which will be the result
        Long[] result = new Long[common.size()];
        for(int i = 0;i<common.size();i++){
            result[i]=common.get(i);
        }

        //return the commonly-favourited restaurants
        return result;
        
    }

    /**
     * Gets the restaurants that were favourited by customer1 but not customer2
     * @param customer1ID the id of the first customer 
     * @param customer2ID the id of the second customer
     * @return an array of restuarants only favourited by customer1
     */
    public Long[] getMissingFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        MyArrayList<Long> missingArray = new MyArrayList<>(); // new array

        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getFavouritesByDate();
        
        //gets the restaurants favourited by customer1
        for (int i = 0; i < favourites.length; i++) {
            if(favourites[i].getCustomerID().equals(customer1ID)){             
                missingArray.add(favourites[i].getRestaurantID());
            }
        }

        //gets the restaurants favourited by both of them
        //removes the common items from the missing array
        Long[] commonArray = getCommonFavouriteRestaurants(customer1ID, customer2ID);
        for(int i = 0;i<commonArray.length;i++){
            missingArray.remove(commonArray[i]);
        }
        
        //puts the resulting elements into an array
        Long[] result = new Long[missingArray.size()];
        for(int i =0;i<missingArray.size();i++){
            result[i]=missingArray.get(i);
        }
        
        return result;
    }

    /**
     * Gets the restaurants that were favourited by either customer1 or customer2 but not both
     * @param customer1ID the id of the first customer 
     * @param customer2ID the id of the second customer
     * @return an array of not commonly favourited restaurants
     */
    public Long[] getNotCommonFavouriteRestaurants(Long customer1ID, Long customer2ID) {
        MyArrayList<Long> missingArray1 = new MyArrayList<>(); // new array
        MyArrayList<Long> missingArray2 = new MyArrayList<>(); // new array

        //the array of commonly favourited restaurants
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getFavouritesByDate();
        Long[] commonArray = getCommonFavouriteRestaurants(customer1ID, customer2ID);
        
        //the array of favourited restaurants by customer1-common ones
        for (int i = 0; i < favourites.length; i++) {
            if(favourites[i].getCustomerID().equals(customer1ID)){             
                missingArray1.add(favourites[i].getRestaurantID());
            }
        }       
        for(int i = 0;i<commonArray.length;i++){
            missingArray1.remove(commonArray[i]);
        }

        //the array of favourited restaurants by customer2-common ones
        for (int i = 0; i < favourites.length; i++) {
            if(favourites[i].getCustomerID().equals(customer1ID)){             
                missingArray2.add(favourites[i].getRestaurantID());
            }
        }     
        for(int i = 0;i<commonArray.length;i++){
            missingArray2.remove(commonArray[i]);
        }

        //the result is the sum of these arrays
        Long[] result = new Long[missingArray1.size()+missingArray2.size()];
        for(int i =0;i<missingArray1.size();i++){
            result[i]=missingArray1.get(i);
        }
        for(int i =missingArray1.size();i<missingArray2.size()+missingArray1.size();i++){
            result[i]=missingArray2.get(i);
        }
        
        return result;
    }

    /**
     * Gets the customers with the highest number of favourites
     * @return an array of length 20 with the IDs of the customers with the highest number of favourites
     */
    public Long[] getTopCustomersByFavouriteCount() {

        //two-dimension array
        //first column: customer id
        //second column: number of favourites
        Long[][] customersWithScores=new Long[favouriteArray.size()][2];
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getAllFavouritesByCustomerID(); // new array sorted by id
        int tempI=0;
        int index=0;

        //adds a new customer to the array or increases the favourites
        for(int i =0;i<favouriteArray.size();i++){

            //first element: adds a customer with a count of 1
            if(i==0){
                customersWithScores[0][0]=favourites[0].getCustomerID();
                customersWithScores[0][1]=1L;
            }

            //new customer: next row, count:1
            else if(!(favourites[i].getCustomerID().equals(favourites[tempI].getCustomerID()))){
                index++;
                customersWithScores[index][0]=favourites[i].getCustomerID();
                customersWithScores[index][1]=1L;
                tempI=i;
            }

            //same customer: same row, count increases by 1
            else{
                customersWithScores[index][1]++;
            }
        }
        
        //next step is to sort the array from highest to lowest
        index++;
        Long[][] sortedCustomersWithScore = new Long[index][2];
        int size=0;
        for(int i = 0;i<index;i++){
            Long tempCustomer=customersWithScores[i][0];
            Long tempScore=customersWithScores[i][1];
            for(int j=0;j<size;j++){
                if(tempScore.compareTo(sortedCustomersWithScore[j][1])>=0){
                    Long temp1 = tempCustomer;
                    Long temp2 = tempScore;
                    tempCustomer= sortedCustomersWithScore[j][0];
                    tempScore= sortedCustomersWithScore[j][1];
                    sortedCustomersWithScore[j][0]=temp1;
                    sortedCustomersWithScore[j][1]=temp2;

                }
            }
            sortedCustomersWithScore[size][0]=tempCustomer;
            sortedCustomersWithScore[size][1]=tempScore;
            size++;        
        }

        //last step is to add the first 20 from the array to the result array
        Long[] result = new Long[20];
        for(int i = 0;i<result.length;i++){
            result[i]=sortedCustomersWithScore[i][0];
        }
        return result;
    }

    //same as the method above but for the restaurants 
    public Long[] getTopRestaurantsByFavouriteCount() {
        Long[][] restaurantsWithScores=new Long[favouriteArray.size()][2];
        Favourite[] favourites = new Favourite[favouriteArray.size()];
        favourites = getAllFavouritesByRestaurantID();
        int tempI=0;
        int index=0;
        for(int i =0;i<favouriteArray.size();i++){
            if(i==0){
                restaurantsWithScores[0][0]=favourites[0].getRestaurantID();
                restaurantsWithScores[0][1]=1L;
            }
            else if(!(favourites[i].getRestaurantID().equals(favourites[tempI].getRestaurantID()))){
                index++;
                restaurantsWithScores[index][0]=favourites[i].getRestaurantID();
                restaurantsWithScores[index][1]=1L;
                tempI=i;
            }
            else{
                restaurantsWithScores[index][1]++;
            }
        }
            
        index++;
        Long[][] sortedRestaurantsWithScore = new Long[index][2];
        int size=0;
        for(int i = 0;i<index;i++){
            Long tempRestaurant=restaurantsWithScores[i][0];
            Long tempScore=restaurantsWithScores[i][1];
            for(int j=0;j<size;j++){
                if(tempScore.compareTo(sortedRestaurantsWithScore[j][1])>=0){
                    Long temp1 = tempRestaurant;
                    Long temp2 = tempScore;
                    tempRestaurant= sortedRestaurantsWithScore[j][0];
                    tempScore= sortedRestaurantsWithScore[j][1];
                    sortedRestaurantsWithScore[j][0]=temp1;
                    sortedRestaurantsWithScore[j][1]=temp2;

                }
            }
            sortedRestaurantsWithScore[size][0]=tempRestaurant;
            sortedRestaurantsWithScore[size][1]=tempScore;
            size++;        
        }


        Long[] result = new Long[20];
        for(int i = 0;i<result.length;i++){
            result[i]=sortedRestaurantsWithScore[i][0];
        }
        return result;
    }
}
