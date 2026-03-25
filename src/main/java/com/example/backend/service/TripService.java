package com.example.backend.service;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TripService {

    private final GooglePlacesService googlePlacesService;

    public TripService(GooglePlacesService googlePlacesService) {
        this.googlePlacesService = googlePlacesService;
    }

    public Map<String, Object> generatePlan(String destination, String fromLocation, 
                                           String fromDate, String toDate, String budget) {
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> plan = new ArrayList<>();

        if (destination == null || destination.trim().isEmpty()) {
            destination = "Hyderabad";
        }
        destination = destination.trim();

        // Get real tourist places using Google Places API
        List<Map<String, Object>> places = googlePlacesService.getTouristPlaces(destination);

        // Fallback if API fails or returns nothing
        if (places == null || places.isEmpty()) {
            places = getFallbackPlaces(destination);
        }

        int days = 7; // You can make this dynamic later based on fromDate-toDate

        for (int i = 0; i < days; i++) {
            Map<String, Object> day = new HashMap<>();

            // Get place for this day
            Map<String, Object> placeData = places.get(i % places.size());
            String placeName = (String) placeData.getOrDefault("name", destination + " Attraction");

            // Get photo URL (from Google or Unsplash fallback)
            String photoUrl = extractPhotoUrl(placeData, placeName);

            // Get nearby restaurant
            List<Map<String, Object>> restaurants = googlePlacesService.getRestaurantsNear(destination);
            String restaurantName = restaurants.isEmpty() 
                ? "Popular Local Restaurant" 
                : (String) restaurants.get(i % restaurants.size()).getOrDefault("name", "Local Restaurant");

            // Hotel name
            String hotelName = destination + " Premium Hotel";

            // Cost calculation based on budget
            int multiplier = "high budget".equalsIgnoreCase(budget) ? 3 : 
                            "medium budget".equalsIgnoreCase(budget) ? 2 : 1;

            int placeCost = 400 * multiplier;
            int foodCost = 250 * multiplier;
            int hotelCost = 5000 * multiplier;

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
        result.put("flightCost", 9500);
        result.put("flightDetails", "IndiGo / Air India • Economy • Non-stop");
        result.put("aiExplanation", "Beautiful " + days + "-day itinerary for " + destination + " with top attractions, great food & comfortable stays.");

        return result;
    }

    // Fallback places if Google API fails
    private List<Map<String, Object>> getFallbackPlaces(String destination) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] names = {"Main Attraction", "Historic Site", "Scenic Spot", "Popular Landmark", "Local Favorite"};

        for (String name : names) {
            Map<String, Object> place = new HashMap<>();
            place.put("name", destination + " " + name);
            list.add(place);
        }
        return list;
    }

    // Extract photo from Google Places or use Unsplash
    private String extractPhotoUrl(Map<String, Object> placeData, String placeName) {
        if (placeData != null) {
            Object photos = placeData.get("photos");
            if (photos instanceof List && !((List<?>) photos).isEmpty()) {
                Object photo = ((List<?>) photos).get(0);
                if (photo instanceof Map) {
                    String photoRef = (String) ((Map<?, ?>) photo).get("photo_reference");
                    if (photoRef != null && googlePlacesService != null) {
                        String url = googlePlacesService.buildPhotoUrl(photoRef, 800);
                        if (url != null) return url;
                    }
                }
            }
        }

        // Fallback to Unsplash with better keywords
        String query = (placeName + " " + placeName.split(" ")[0]).replace(" ", "+");
        return "https://source.unsplash.com/800x600/?" + query;
    }
}
