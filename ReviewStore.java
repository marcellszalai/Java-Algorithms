package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.IReviewStore;
import uk.ac.warwick.cs126.models.Review;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.SortedArrayList;

import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.KeywordChecker;
import uk.ac.warwick.cs126.util.StringFormatter;


public class ReviewStore implements IReviewStore {

    private MyArrayList<Review> reviewArray;
    private DataChecker dataChecker;
    private MyArrayList<Review> blackList;
    private KeywordChecker keywordChecker;
    private StringFormatter stringFormatter;

    /**
     * Constructor method
     * @param reviewArray array that contains the reviews
     * @param dataChecked object for the metods in the DataChecker class
     * @param blackList array that contains the blacklisted items
     * @param keywordChecker object for the methods in the KeywordChecker class
     * @param stringFormatter object for the methods in the StringFormatter class
     */
    public ReviewStore() {
        reviewArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        blackList = new MyArrayList<>();
        keywordChecker = new KeywordChecker();
        stringFormatter = new StringFormatter();
    }

    public Review[] loadReviewDataToArray(InputStream resource) {
        Review[] reviewArray = new Review[0];

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

            Review[] loadedReviews = new Review[lineCount - 1];

            BufferedReader tsvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int reviewCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            tsvReader.readLine();
            while ((row = tsvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split("\t");
                    Review review = new Review(
                            Long.parseLong(data[0]),
                            Long.parseLong(data[1]),
                            Long.parseLong(data[2]),
                            formatter.parse(data[3]),
                            data[4],
                            Integer.parseInt(data[5]));
                    loadedReviews[reviewCount++] = review;
                }
            }
            tsvReader.close();

            reviewArray = loadedReviews;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return reviewArray;
    }

    /**
     * Adds one review to the array
     * @param review review to be added
     * @return true when added, false when not
     */
    public boolean addReview(Review review) {

        //firstly it checks if the review is valid
        if(!dataChecker.isValid(review)){return false;}

        Review tmp;

        //then it checks if it is blacklisted
        for(int i = 0;i<blackList.size();i++){
            if(review.getID().equals(blackList.get(i).getID())){
                return false;
            }
        }

        //then it check if there is a review with the same id
        //if yes, removes the other review and blacklists the id
        for(int i = 0; i<reviewArray.size() ; i++){
            tmp = reviewArray.get(i);
            if (tmp.getID().equals(review.getID())){
                reviewArray.remove(tmp);
                blackList.add(review);
                return false;
            }
            if(tmp.getCustomerID().equals(review.getCustomerID()) && tmp.getRestaurantID().equals(review.getRestaurantID())){
                if(review.getDateReviewed().compareTo(tmp.getDateReviewed())>0){
                    blackList.add(tmp);
                    reviewArray.set(i, review);
                    return true;
                }
            }
        }
        
        //adds review if the array is empty
        if (reviewArray.isEmpty()==true){         
            return reviewArray.add(review);        
        }

        Review temp;

        //adds the reviews to the array sorted by their date from newest to oldest
        for (int i = 0; i<reviewArray.size(); i++){
            temp = reviewArray.get(i);
            if (temp.getDateReviewed().compareTo(review.getDateReviewed())<0){            
                reviewArray.set(i,review);
                review = temp;
            }

            //if they are equaly, then by their id
            else if(temp.getDateReviewed().equals(review.getDateReviewed())){
                if (temp.getID().compareTo(review.getID())>0){
                    reviewArray.set(i,review);
                    review = temp;
                }
            }
        }
        return reviewArray.add(review);
    }

    /**
     * Adds an array of reviews
     * @param reviews the array to be added
     * @return true when all of the are added, false otherwise
     */
    public boolean addReview(Review[] reviews) {
        boolean notAdded=false;

        //if at least one is not added returns false
        for(int i =0 ;i<reviews.length;i++){
            if (!addReview(reviews[i])){
                notAdded=true;
            }
        }
        return !notAdded;
    }

    /**
     * Gets a review which has the same id as the input
     * @param id input ID
     * @return the review which has the same id, or null
     */
    public Review getReview(Long id) {
        Review tmp=null;

        //scans through the array 
        //when IDs are the same, retruns the review
        for(int i = 0; i<reviewArray.size();i++){
            tmp=reviewArray.get(i);
            if(tmp.getID().equals(id)){ 
                return tmp;
            }
        }

        //returns null when it cant find review
        return null;
    }

    /**
     * Sorts all the reviews in order of their IDs
     * @return array of sorted reviews
     */
    public Review[] getReviews() {
        Review[] reviews = new Review[reviewArray.size()]; 

        for(int i = 0;i<reviewArray.size();i++){
            reviews[i]=reviewArray.get(i);
        }

        Review[] allReviews = new Review[reviews.length];// new array
        int size = 0;
        Review tempReview;

        //each element goes through the array
        //sorts them by id
        for(int j=0 ;j<reviews.length;j++){
            for (int i = 0; i<size; i++){
                if (allReviews[i].getID().compareTo(reviews[j].getID())>0){
                    tempReview= allReviews[i];
                    allReviews[i]=reviews[j];
                    reviews[j] = tempReview;
                }
            }
            allReviews[size]=reviews[j];
            size++;
        }

        //sorted
        return allReviews;
    }

    /**
     * Gets reviews by their date newest to oldest
     * Since they are organised that way already this is easy
     * @return the array of all the reviews sorted
     */
    public Review[] getReviewsByDate() {
        Review[] allReviews = new Review[reviewArray.size()];
        for(int i = 0;i<reviewArray.size();i++){
            allReviews[i]=reviewArray.get(i);
        }

        return allReviews;
    }

    /**
     * Gets all the reviews by their rating
     * @return array of all reviews sorted
     */
    public Review[] getReviewsByRating() {

        //loads the reviews
        Review[] reviews = new Review[reviewArray.size()];
        for(int i = 0;i<reviewArray.size();i++){
            reviews[i]=reviewArray.get(i);
        }

        Review[] allReviews = new Review[reviews.length]; // new array
        int size = 0;
        Review tempReview;

        //firstly by their rating
        //highest first
        for(int j=0 ;j<reviews.length;j++){
            for (int i = 0; i<size; i++){
                if (allReviews[i].getRating()<reviews[j].getRating()){
                    tempReview= allReviews[i];
                    allReviews[i]=reviews[j];
                    reviews[j] = tempReview;
                }

                //then by their date
                //newest first
                else if (allReviews[i].getRating()==reviews[i].getRating()){
                    if (allReviews[i].getDateReviewed().compareTo(reviews[i].getDateReviewed())<0){
                        tempReview= allReviews[i];
                        allReviews[i]=reviews[j];
                        reviews[j] = tempReview;
                    }
                }
            }
            allReviews[size]=reviews[j];
            size++;
        }
        //sorted
        return allReviews;
    }

    //same as the method above but by customerID
    public Review[] getReviewsByCustomerID(Long id) {

        Review[] reviews = new Review[reviewArray.size()];
        reviews = getReviewsByDate(); // loads reviews
        Review[] allReviews = new Review[0]; // new array

        int size=0;

        //sorts by customerID
        for (int i = 0;i<reviews.length;i++){
            if(reviews[i].getCustomerID().equals(id)){
                Review[] tempReviews = new Review[size+1];
                for(int j = 0;j<size;j++){
                    tempReviews[j]=allReviews[j];
                }
                tempReviews[size]=reviews[i];
                allReviews=tempReviews;
                size++;
            }
        }
        //sorted
        return allReviews;
    }

    //same but with restaurantID
    public Review[] getReviewsByRestaurantID(Long id) {
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getReviewsByDate(); // loads reviews
        Review[] allReviews = new Review[0]; // new array

        int size=0;
        for (int i = 0;i<reviews.length;i++){

            //sorts by restaurantID
            if(reviews[i].getRestaurantID().equals(id)){
                Review[] tempReviews = new Review[size+1];
                for(int j = 0;j<size;j++){
                    tempReviews[j]=allReviews[j];
                }
                tempReviews[size]=reviews[i];
                allReviews=tempReviews;
                size++;
            }
        }
        //sorted
        return allReviews;
    }

    /**
     * Gets the average rating of a customer
     * @param id id of the customer
     * @return floating point number  wich is the average rating of a certain customer
     */
    public float getAverageCustomerReviewRating(Long id) {
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getReviewsByDate(); // load reviews
        int[] allReviews = new int[0]; // new array

        int size=0;

        //search for the reviews with  the same customerID as the  ID
        for (int i = 0;i<reviews.length;i++){

            //adds the value of the review when its a match
            if(reviews[i].getCustomerID().equals(id)){
                int[] tempReviews = new int[size+1];
                for(int j = 0;j<size;j++){
                    tempReviews[j]=allReviews[j];
                }
                tempReviews[size]=reviews[i].getRating();
                allReviews=tempReviews;
                size++;
            }
        }

        float result = 0;

        //adds together the matches
        for(int i=0;i<allReviews.length;i++){
            result=result+allReviews[i];
        }

        //divides the result by the number of matches
        result=result/allReviews.length;

        return result;
    }

    //same as the one above but with a certain restaurant
    public float getAverageRestaurantReviewRating(Long id) {
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getReviewsByDate();
        int[] allReviews = new int[0];

        int size=0;
        for (int i = 0;i<reviews.length;i++){
            if(reviews[i].getRestaurantID().equals(id)){
                int[] tempReviews = new int[size+1];
                for(int j = 0;j<size;j++){
                    tempReviews[j]=allReviews[j];
                }
                tempReviews[size]=reviews[i].getRating();
                allReviews=tempReviews;
                size++;
            }
        }
        float result = 0;
        for(int i=0;i<allReviews.length;i++){
            result=result+allReviews[i];
        }
        result=result/allReviews.length;

        return result;
    }

    /**
     * Counts each rating of a given user
     * @param id id of the desired customer
     * @return array of size five, and the amount of times the customer gave that rating to a restaurant
     */
    public int[] getCustomerReviewHistogramCount(Long id) {
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getReviewsByDate(); // loads the array 
        int[] result = new int[5];

        //searches through the array
        for(int i=0;i<reviews.length;i++){

            //when its a match it increments the corresponding element of the array
            if(reviews[i].getCustomerID().equals(id)){
                result[reviews[i].getRating()-1]++;
            }         
        }
        
        return result;
    }

    //same as the method above but for ratings on a given restaurant
    public int[] getRestaurantReviewHistogramCount(Long id) {
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getReviewsByDate();
        int[] result = new int[5];
        for(int i=0;i<reviews.length;i++){
            if(reviews[i].getRestaurantID().equals(id)){
                result[reviews[i].getRating()-1]++;
            }         
        }
        
        return result;
    }

    /**
     * Gets all of the reviews sorted by the customerID
     */
    public Review[] getAllReviewsByCustomerID(){
        Review[] reviews = new Review[reviewArray.size()]; // loads the array
        for(int i = 0;i<reviewArray.size();i++){
            reviews[i]=reviewArray.get(i);
        }

        Review[] allReviews = new Review[reviews.length]; // new  array
        int size = 0;
        Review tempReview;

        //firstly by  customerID
        for(int j=0 ;j<reviews.length;j++){
            for (int i = 0; i<size; i++){
                if (allReviews[i].getCustomerID().compareTo(reviews[j].getCustomerID())>0){
                    tempReview= allReviews[i];
                    allReviews[i]=reviews[j];
                    reviews[j] = tempReview;
                }

                //secondly by ID
                else if (allReviews[i].getCustomerID().compareTo(reviews[j].getCustomerID())==0){
                    if (allReviews[i].getID().compareTo(reviews[j].getID())>0){
                        tempReview = allReviews[i];
                        allReviews[i]=reviews[j];
                        reviews[j] = tempReview;
                    }
                }
            }
            allReviews[size]=reviews[j];
            size++;
        }
        return allReviews;
    }

    //same as the method above but sorted by restaurantID
    public Review[] getAllReviewsByRestaurantID(){
        Review[] reviews = new Review[reviewArray.size()];
        for(int i = 0;i<reviewArray.size();i++){
            reviews[i]=reviewArray.get(i);
        }

        Review[] allReviews = new Review[reviews.length];
        int size = 0;
        Review tempReview;
        for(int j=0 ;j<reviews.length;j++){
            for (int i = 0; i<size; i++){
                if (allReviews[i].getRestaurantID().compareTo(reviews[j].getRestaurantID())>0){
                    tempReview= allReviews[i];
                    allReviews[i]=reviews[j];
                    reviews[j] = tempReview;
                }
                else if (allReviews[i].getRestaurantID().compareTo(reviews[j].getRestaurantID())==0){
                    if (allReviews[i].getID().compareTo(reviews[j].getID())>0){
                        tempReview = allReviews[i];
                        allReviews[i]=reviews[j];
                        reviews[j] = tempReview;
                    }
                }
            }
            allReviews[size]=reviews[j];
            size++;
        }
        return allReviews;
    }

    /**
     * Gets the top 20 customers by their review count
     * @return an array of 20 elements that contains the customers with the most reviews in a descending order
     */
    public Long[] getTopCustomersByReviewCount() {
        Long[][] customersWithScores=new Long[reviewArray.size()][2]; // two dimension array
        Review[] reviews = new Review[reviewArray.size()]; 
        reviews = getAllReviewsByCustomerID(); // loads the array such that the reviews with the same customerID are next to each other
        int tempI=0;
        int index=0;

        //counts the number of reviews for each customer
        for(int i =0;i<reviewArray.size();i++){

            //the first column in the first row is the customer with the smallest ID
            //the secon column is 1
            if(i==0){
                customersWithScores[0][0]=reviews[0].getCustomerID();
                customersWithScores[0][1]=1L;
            }

            //if the customerID changes so does the row
            else if(!(reviews[i].getCustomerID().equals(reviews[tempI].getCustomerID()))){
                index++;
                customersWithScores[index][0]=reviews[i].getCustomerID();
                customersWithScores[index][1]=1L;
                tempI=i;
            }
            
            //if the customerID is the same, then the second column gets incremented and the row does not change
            else{
                customersWithScores[index][1]++;
            }
        }
        
        //next step is to sort them in a descending order
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

        //last step is to get the top 20
        Long[] result = new Long[20];
        for(int i = 0;i<result.length;i++){
            result[i]=sortedCustomersWithScore[i][0];
        }
        return result;
    }

    //same as the method above but for the top 20 most reviewed restaurants
    public Long[] getTopRestaurantsByReviewCount() {
        Long[][] restaurantsWithScores=new Long[reviewArray.size()][2];
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getAllReviewsByRestaurantID();
        int tempI=0;
        int index=0;
        for(int i =0;i<reviewArray.size();i++){
            if(i==0){
                restaurantsWithScores[0][0]=reviews[0].getRestaurantID();
                restaurantsWithScores[0][1]=1L;
            }
            else if(!(reviews[i].getRestaurantID().equals(reviews[tempI].getRestaurantID()))){
                index++;
                restaurantsWithScores[index][0]=reviews[i].getRestaurantID();
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

    /**
     * Gets the top 20 highest rated restaurants in descending order of the rating
     * @return the ids of the highest rated restaurants
     */
    public Long[] getTopRatedRestaurants() {
        
        //same two-dimension array but with 3 columns
        Long[][] restaurantsWithScores=new Long[reviewArray.size()][3];
        Review[] reviews = new Review[reviewArray.size()];
        reviews = getAllReviewsByRestaurantID();//loads the reviews
        int tempI=0;
        int index=0;

        //first column: restaurantID
        //second column: number of ratings
        //third column: sum of the values of the ratings
        for(int i =0;i<reviewArray.size();i++){

            //first element gets added no matter what
            if(i==0){
                restaurantsWithScores[0][0]=reviews[0].getRestaurantID();
                restaurantsWithScores[0][1]=1L;
                restaurantsWithScores[0][2]=Long.valueOf(reviews[0].getRating());
            }

            //if the restaurantID changes we jump to the next row
            else if(!(reviews[i].getRestaurantID().equals(reviews[tempI].getRestaurantID()))){
                index++;
                restaurantsWithScores[index][0]=reviews[i].getRestaurantID();
                restaurantsWithScores[index][1]=1L;
                restaurantsWithScores[index][2]=Long.valueOf(reviews[i].getRating());
                tempI=i;
            }

            //if the restaurantID does not change we increment the second column by one and the third by the value of the rating
            else{
                restaurantsWithScores[index][1]++;
                restaurantsWithScores[index][2]+=Long.valueOf(reviews[i].getRating());
            }
        }
        
        //next step is to calculate the avarage rating to each restaurant
        index++;
        Long[][] restaurants=new Long[index][2];
        for(int i=0; i<index;i++){
            restaurants[i][0]=restaurantsWithScores[i][0];
            restaurants[i][1]=(long)(((float)restaurantsWithScores[i][2]/(float)restaurantsWithScores[i][1])*100);
        }

        //next step is to sort the restaurants in a descending order of their rating
        Long[][] sortedRestaurantsWithScore = new Long[index][2];
        int size=0;
        for(int i = 0;i<index;i++){
            Long tempRestaurant=restaurants[i][0];
            Long tempScore=restaurants[i][1];
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

        //last step is to put the top 20 restaurants with the largest average rating to a new array
        Long[] result = new Long[20];
        if(size<5){
            for(int i = 0;i<size;i++){
                result[i]=sortedRestaurantsWithScore[i][0];
            }
        }
        else{
            for(int i = 0;i<result.length;i++){
                result[i]=sortedRestaurantsWithScore[i][0];
            }
        }
        

        return result;
    }

    /**
     * Gets the top 5 keywords associated with a certain restaurant
     * @param id the id of the desired restaurand
     * @return an array of 5 elements containing the 5 mostly-occuring keywords in a review for the restaurant
     */
    public String[] getTopKeywordsForRestaurant(Long id) {
        SortedArrayList<String> keywords = new SortedArrayList<>();

        //adds all the keywords in a sorted arraylist
        //that means that the same keywords are going to be next to each other
        for(int i=0;i<reviewArray.size();i++){
            if(reviewArray.get(i).getRestaurantID().equals(id)){

                //splits the review into words
                String[] terms = reviewArray.get(i).getReview().split("\\s+");

                //checks every word in the review with the keywordchecked
                //if it is a keyword, it gets added
                for(int j = 0;j<terms.length;j++){
                    if(keywordChecker.isAKeyword(terms[j])){
                        keywords.add(terms[j]);
                    }
                }
            }
        }

        //next step is to count each of the keywords
        String[][] keywordsArray = new String[1000][2];
        int index = 0;
        int tempI = 0;
        for(int i =0;i<keywords.size();i++){
            if(i==0){
                keywordsArray[0][0]=keywords.get(0);
                keywordsArray[0][1]="1";
            }
            else if(!(keywords.get(i).equals(keywords.get(tempI)))){
                index++;
                keywordsArray[index][0]=keywords.get(i);
                keywordsArray[index][1]="1";
                tempI = i;            
            }
            else{
                int tempInt=Integer.parseInt(keywordsArray[index][1]);
                tempInt++;
                keywordsArray[index][1]=Integer.toString(tempInt);
            }
        }
        index++;

        //next step is to sort the keywords in descending order of their occurance
        String[][] sortedKeyWords = new String[index][2];
        int size=0;
        for(int i = 0;i<index;i++){
            String tempKeyword=keywordsArray[i][0];
            String tempScore=keywordsArray[i][1];
            for(int j=0;j<size;j++){
                if(tempScore.compareTo(sortedKeyWords[j][1])>=0){
                    String temp1 = tempKeyword;
                    String temp2 = tempScore;
                    tempKeyword= sortedKeyWords[j][0];
                    tempScore= sortedKeyWords[j][1];
                    sortedKeyWords[j][0]=temp1;
                    sortedKeyWords[j][1]=temp2;

                }
            }
            sortedKeyWords[size][0]=tempKeyword;
            sortedKeyWords[size][1]=tempScore;
            size++;
        }

        //last step is to get the top 5 mostly occuring keyword 
        
        String[] result = new String[5];
        if(size<5){
            for (int i = 0;i<size;i++){
                result[i]=sortedKeyWords[i][0];
            }
        }
        else{
            for (int i = 0;i<result.length;i++){
                result[i]=sortedKeyWords[i][0];
            }
        }
        

        return result;
    }
    /**
     * Searches for the review that contain a certain String
     * @param searchTerm the String that we search for between the reviews
     * @return an array of reviews that contain the string
     */
    public Review[] getReviewsContaining(String searchTerm) {
        Review[] contains = new Review[0];

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

        //checks every review
        for(int i = 0; i<reviewArray.size();i++){

            //checks every word
            for(int j = 0;j<terms.length;j++){

                //if rievew does not contain
                if(!Pattern.compile(Pattern.quote(terms[j]), Pattern.CASE_INSENSITIVE).matcher(reviewArray.get(i).getReview()).find()){
                    break;
                }

                //if it gets to the last word then the review contains the string
                //adds the reviewto the array
                if(j==terms.length-1){
                    size++;
                    Review[] temp = new Review[size];
                    for(int k=0;k<size-1;k++){
                        temp[k]=contains[k];
                    }
                    temp[size-1]=reviewArray.get(i);
                    contains = temp;
                }
                
            }
        } 

        //returns the array organsied by the names of the restaurants
        return contains;
    }
}
