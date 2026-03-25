package com.example.backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TripService {

    public Map<String, Object> generatePlan(String destination, String fromLocation, 
                                           String fromDate, String toDate, String budget) {

        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> plan = new ArrayList<>();

        if (destination == null || destination.trim().isEmpty()) {
            destination = "Hyderabad";
        }
        destination = destination.trim();

        // Beautiful real places for popular Indian destinations
        String[] places = switch (destination.toLowerCase()) {
            case "goa" -> new String[]{"Baga Beach", "Fort Aguada", "Anjuna Beach", "Dudhsagar Falls", "Old Goa Churches"};
            case "manali" -> new String[]{"Rohtang Pass", "Solang Valley", "Hidimba Temple", "Mall Road", "Vashisht Hot Springs"};
            case "kerala" -> new String[]{"Alleppey Backwaters", "Munnar Tea Gardens", "Kovalam Beach", "Thekkady Wildlife", "Fort Kochi"};
            case "ooty" -> new String[]{"Ooty Lake", "Doddabetta Peak", "Botanical Gardens", "Rose Garden", "Nilgiri Railway"};
            default -> new String[]{"Charminar", "Golconda Fort", "Hussain Sagar", "Ramoji Film City", "Birla Mandir", "Chowmahalla Palace", "Salar Jung Museum"};
        };

        String[] restaurants = {"Paradise Biryani", "Bawarchi", "Ohri's", "Chutney's", "Mehfil", "Shah Ghouse"};

        String[] hotels = {"Taj Krishna", "Hyderabad Marriott", "Novotel Hyderabad", "Park Hyatt", "Lemon Tree Premier"};

        for (int i = 0; i < 7; i++) {
            Map<String, Object> day = new HashMap<>();

            String placeName = places[i % places.length];
            String restaurantName = restaurants[i % restaurants.length];
            String hotelName = hotels[i % hotels.length];

            // High quality Unsplash images
            String photoUrl = "https://source.unsplash.com/800x600/?" + 
                             placeName.replace(" ", "+") + "," + destination.toLowerCase();

            int multiplier = budget != null && budget.toLowerCase().contains("high") ? 3 : 
                            budget != null && budget.toLowerCase().contains("medium") ? 2 : 1;

            int placeCost = 450 * multiplier;
            int foodCost = 280 * multiplier;
            int hotelCost = 5200 * multiplier;

            day.put("day", "Day " + (i + 1));
            day.put("place", placeName);
            day.put("restaurant", restaurantName);
            day.put("hotel", hotelName);
            day.put("photoUrl", photoUrl);
            day.put("placeCost", placeCost);
            day.put("foodCost", foodCost);
            day.put("hotelCost", hotelCost);
            day.put("dailyCost", placeCost + foodCost + hotelCost);

            plan.add(day);
        }

        int totalCost = plan.stream().mapToInt(d -> (int) d.get("dailyCost")).sum();

        result.put("plan", plan);
        result.put("totalCost", totalCost);
        result.put("flightCost", 9200);
        result.put("flightDetails", "IndiGo • Economy • Non-stop");
        result.put("aiExplanation", "Beautiful 7-day curated trip to " + destination + " with top attractions, delicious food & comfortable stays.");

        return result;
    }
}
