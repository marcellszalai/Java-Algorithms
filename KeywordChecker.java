package uk.ac.warwick.cs126.util;

import uk.ac.warwick.cs126.interfaces.IKeywordChecker;
import java.util.regex.Pattern;

public class KeywordChecker implements IKeywordChecker {

    //The array of all the keywords
    private static final String[] keywords = {
            "0",
            "agreeable",
            "air-headed",
            "apocalypse",
            "appetizing",
            "average",
            "awesome",
            "biohazard",
            "bland",
            "bleh",
            "burnt",
            "charming",
            "clueless",
            "cockroach",
            "cold",
            "crap",
            "dancing",
            "dead",
            "decadent",
            "decent",
            "dirty",
            "disgusting",
            "dreadful",
            "droppings",
            "dry",
            "dumpy",
            "excellent",
            "favourite",
            "feel-good",
            "flavourful",
            "frozen",
            "gem",
            "gross",
            "heart",
            "heavenly",
            "horrendous",
            "horrible",
            "incredible",
            "interesting",
            "lame",
            "lousy",
            "mediocre",
            "meh",
            "mess",
            "microwaved",
            "mouth-watering",
            "nightmares",
            "ok",
            "okay",
            "overcooked",
            "overhyped",
            "perfection",
            "polite",
            "prompt",
            "quality",
            "rude",
            "satisfaction",
            "savoury",
            "sewer",
            "singing",
            "slow",
            "so-so",
            "spongy",
            "sticky",
            "sublime",
            "succulent",
            "sucked",
            "surprised",
            "terrible",
            "tingling",
            "tired",
            "toxic",
            "traumatizing",
            "uncomfortable",
            "under-seasoned",
            "undercooked",
            "unique",
            "unprofessional",
            "waste",
            "worst",
            "yuck",
            "yummy"
    };

    public KeywordChecker() {
        // Initialise things here
        
    }

    /**
     * Checks if the input is a keyword
     * @param word input to be checked
     * @return true when it is a keyword, false when not 
     */
    public boolean isAKeyword(String word) {
        int left = 0;
        int right = keywords.length-1;
        int pivot = (left+right)/2;
        int size = 0;

        //Performs a binary search on the keywords
        while(size!=keywords.length) {

            //If the word and keyword matches returns true
            if (keywords[pivot].compareTo(word)==0){
                return true;
            }
            else if (keywords[pivot].compareTo(word)>0){
                right=pivot;
                pivot = (left+right)/2;
            }
            else{
                left=pivot;
                pivot = (left+right)/2;
            }
            size++;
        }

        //if it can not find a keyword returns false
        return false;
    }
        
}