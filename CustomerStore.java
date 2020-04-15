package uk.ac.warwick.cs126.stores;

import uk.ac.warwick.cs126.interfaces.ICustomerStore;
import uk.ac.warwick.cs126.models.Customer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;


import org.apache.commons.io.IOUtils;

import uk.ac.warwick.cs126.structures.MyArrayList;
import uk.ac.warwick.cs126.structures.SortedArrayList;

import uk.ac.warwick.cs126.util.DataChecker;
import uk.ac.warwick.cs126.util.StringFormatter;

public class CustomerStore implements ICustomerStore {

    private MyArrayList<Customer> customerArray;
    private DataChecker dataChecker;
    private MyArrayList<Long> blackList; 
    private StringFormatter stringFormatter;

    /**
     * Constructor method
     * @param customerArray array that contains the customers
     * @param dataChecked object for the metods in the DataChecker class
     * @param blackList array that contains the blacklisted items
     */
    public CustomerStore() {
        customerArray = new MyArrayList<>();
        dataChecker = new DataChecker();
        blackList = new MyArrayList<>();
        stringFormatter = new StringFormatter();
    }

    public Customer[] loadCustomerDataToArray(InputStream resource) {
        Customer[] customerArray = new Customer[0];

        try {
            byte[] inputStreamBytes = IOUtils.toByteArray(resource);
            BufferedReader lineReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int lineCount = 0;
            String line;
            while ((line=lineReader.readLine()) != null) {
                if (!("".equals(line))) {
                    lineCount++;
                }
            }
            lineReader.close();

            Customer[] loadedCustomers = new Customer[lineCount - 1];

            BufferedReader csvReader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(inputStreamBytes), StandardCharsets.UTF_8));

            int customerCount = 0;
            String row;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            csvReader.readLine();
            while ((row = csvReader.readLine()) != null) {
                if (!("".equals(row))) {
                    String[] data = row.split(",");

                    Customer customer = (new Customer(
                            Long.parseLong(data[0]),
                            data[1],
                            data[2],
                            formatter.parse(data[3]),
                            Float.parseFloat(data[4]),
                            Float.parseFloat(data[5])));

                    loadedCustomers[customerCount++] = customer;
                }
            }
            csvReader.close();

            customerArray = loadedCustomers;

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return customerArray;
    }

    /**
     * Adds one customer to the array
     * @param customer customer to be added
     * @return true when added, false when not
     */
    public boolean addCustomer(Customer customer) {

        //checks if the customer is valid
        if(!dataChecker.isValid(customer)){return false;}

        Customer tmp;

        //checks if the id of the customer already exists in the array
        //when yes it removes the customer with the same id and add the id to the blacklist array
        for(int i = 0; i<customerArray.size();i++){
            tmp = customerArray.get(i);
            if (tmp.getID().equals(customer.getID())){
                customerArray.remove(tmp);
                blackList.add(customer.getID());
                return false;
            }
        }

        //checks if the id of the user is already blacklisted
        for(int i = 0;i<blackList.size();i++){
            if(customer.getID().equals(blackList.get(i))){
                return false;
            }
        }

        //adds the first customer
        if (customerArray.isEmpty()==true){
            return customerArray.add(customer);
        }

        Customer temp;

        //makes a sorted array according to their ID
        for (int i = 0; i<customerArray.size(); i++){
            if (customerArray.get(i).getID().compareTo(customer.getID())>=0){
                temp = customerArray.get(i);
                customerArray.set(i,customer);
                customer = temp;
            }
        }

        //adds the customer with the smallest id to the end of the array
        return customerArray.add(customer);
    }
    
    /**
     * Adds an array of customers to the customerArray
     * @param customers array of customers to be added
     * @return true when all of the are added, false if at least one is not
     */
    public boolean addCustomer(Customer[] customers) {
        boolean notAdded=false;

        //if one customer is not added -> notAdded becomes true and it will return false
        for(int i =0 ;i<customers.length;i++){
            if (!addCustomer(customers[i])){
                notAdded=true;
            }
        }
        return !notAdded;   
    }

    /**
     * Gets the customer with the same id
     * @param id the ID of the customer
     * @return customer with the same id or null if there is no customer with such id
     */
    public Customer getCustomer(Long id) {
        Customer tmp=null;

        //linear search through the array of customers
        for(int i = 0; i<customerArray.size();i++){
            tmp=customerArray.get(i);
            if(tmp.getID().equals(id)){         
                return tmp;
            }
        }

        //if there is no match
        return null;
    }

    /**
     * Gets all the customers in ascending order of their IDs
     * Since the IDs are already sorted that way this is very fast
     * @return array of all customers
     */
    public Customer[] getCustomers() {
        Customer[] allCustomers = new Customer[customerArray.size()];
        for(int i = 0;i<customerArray.size();i++){
            allCustomers[i]=customerArray.get(i);
        }

        
        return allCustomers;
    }

    /**
     * Sorts the input customers in ascending order of their IDs
     * @param customers array of customers to sort
     * @return the same customers as the input, but sorted
     */
    public Customer[] getCustomers(Customer[] customers) {

        //new array
        Customer[] allCustomers = new Customer[customers.length];
        int size = 0;
        Customer tempCustomer;

        //for all the customers
        // - goes through the new array
        // - when it has a smaller id they switch 
        // - switched custumer goes forward
        // - add the cusomer with the largest ID  to the end 
        for(int j=0 ;j<customers.length;j++){
            for (int i = 0; i<size; i++){
                if (allCustomers[i].getID().compareTo(customers[j].getID())>=0){
                    tempCustomer = allCustomers[i];
                    allCustomers[i]=customers[j];
                    customers[j] = tempCustomer;
                }
            }
            allCustomers[size]=customers[j];
            size++;
        }

        return allCustomers;
    }

    /**
     * Returns an array of sorted customers acoording to their Last name
     * If Last name is the same, according to their First name
     * If First name is the same, according to their ID
     */
    public Customer[] getCustomersByName() {
        Customer[] customers = new Customer[customerArray.size()];

        //loads all the  customers in an array 
        for(int i = 0;i<customerArray.size();i++){
            customers[i]=customerArray.get(i);
        }
        
        //uses the next method to sort them
        return getCustomersByName(customers);
    }

    /**
     * Returns an array of customers
     * Sorting is like the method above
     * @param customers array of customers to sort
     * @return array of sorted customers
     */
    public Customer[] getCustomersByName(Customer[] customers) {

        //new array
        Customer[] allCustomers = new Customer[customers.length];
        int size = 0;
        Customer tempCustomer;

        //each customer goes through the array
        for(int j=0 ;j<customers.length;j++){

            //until the the value of the new array is null
            for (int i = 0; i<size; i++){

                //compares their last name
                if (allCustomers[i].getLastName().compareTo(customers[j].getLastName())>0){
                    tempCustomer = allCustomers[i];
                    allCustomers[i]=customers[j];
                    customers[j] = tempCustomer;
                }

                //if last name is the same, compares their first name
                else if (allCustomers[i].getLastName().compareTo(customers[j].getLastName())==0){
                    if (allCustomers[i].getFirstName().compareTo(customers[j].getFirstName())>0){
                        tempCustomer = allCustomers[i];
                        allCustomers[i]=customers[j];
                        customers[j] = tempCustomer;
                    }

                    //if first name is the same, compares their id
                    //id cant be the same
                    else if (allCustomers[i].getFirstName().compareTo(customers[j].getFirstName())==0){
                        if (allCustomers[i].getID().compareTo(customers[j].getID())>0){
                            tempCustomer = allCustomers[i];
                            allCustomers[i]=customers[j];
                            customers[j] = tempCustomer;
                        } 
                    }
                }
            }

            //eventually the customer with the "largest" surname gets added
            allCustomers[size]=customers[j];
            size++;
        }

        return allCustomers;
    }

    /**
     * Searches for the customers that contain a certain String
     * @param searchTerm the String that we search for between the customers
     * @return an array of customers that contain the string
     */
    public Customer[] getCustomersContaining(String searchTerm) {
        
        //if the input is empty returns nothing
        Customer[] contains = new Customer[0];
        if (searchTerm==null){
            return contains;
        }
        int size = 0;
        
        //converts the input to be readable
        String searchTermConvertedFaster = stringFormatter.convertAccentsFaster(searchTerm);

        //splits the input to separate Strings
        //Recognises multiple spaces too
        String[] terms = searchTermConvertedFaster.split("\\s+");

        //checks every customer
        for(int i = 0; i<customerArray.size();i++){

            //checks every word
            for(int j = 0;j<terms.length;j++){

                //if neither the first nor the last name contains a word then it breaks out of the loop
                if(!(Pattern.compile(Pattern.quote(terms[j]), Pattern.CASE_INSENSITIVE).matcher(customerArray.get(i).getFirstName()).find() || Pattern.compile(Pattern.quote(terms[j]), Pattern.CASE_INSENSITIVE).matcher(customerArray.get(i).getLastName()).find())){
                    break;
                }

                //if it gets to the last word then the customer contains the string
                //adds the customer to the array
                if(j==terms.length-1){
                    size++;
                    Customer[] temp = new Customer[size];
                    for(int k=0;k<size-1;k++){
                        temp[k]=contains[k];
                    }
                    temp[size-1]=customerArray.get(i);
                    contains = temp;
                }
                
            }
        }
        
        //returns the array organsied by the names of the customers
        return getCustomersByName(contains);
    }

}
