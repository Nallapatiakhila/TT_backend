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

        // Get real tourist places using better query
        List<Map<String, Object>> touristPlaces = googlePlacesService.getTouristPlaces(destination);

        if (touristPlaces == null || touristPlaces.isEmpty()) {
            touristPlaces = getFallbackPlaces(destination);
        }

        int days = 7;

        for (int i = 0; i < days; i++) {
            Map<String, Object> day = new HashMap<>();

            Map<String, Object> place = touristPlaces.get(i % touristPlaces.size());

            String placeName = (String) place.getOrDefault("name", destination + " Attraction " + (i+1));
            String photoUrl = extractPhotoUrl(place, placeName);

            // Get nearby restaurants
            List<Map<String, Object>> restaurants = googlePlacesService.getRestaurantsNear(destination, budget);
            String restaurantName = restaurants.isEmpty() 
                ? "Popular Local Restaurant" 
                : (String) restaurants.get(i % restaurants.size()).getOrDefault("name", "Local Restaurant");

            String hotelName = destination + " Premium Stay";

            // Budget-based costing
            int multiplier = budget != null && budget.toLowerCase().contains("high") ? 3 :
                            budget != null && budget.toLowerCase().contains("medium") ? 2 : 1;

            int placeCost = 400 * multiplier;
            int foodCost = 250 * multiplier;
            int hotelCost = 5500 * multiplier;

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
        result.put("flightDetails", "IndiGo • Economy");
        result.put("aiExplanation", "AI-powered " + days + "-day itinerary for " + destination + " with real attractions and recommendations.");

        return result;
    }

    private List<Map<String, Object>> getFallbackPlaces(String destination) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] fallback = {"Main Landmark", "Cultural Site", "Scenic Spot", "Popular Attraction", "Local Favorite"};

        for (String name : fallback) {
            Map<String, Object> p = new HashMap<>();
            p.put("name", destination + " " + name);
            list.add(p);
        }
        return list;
    }

    private String extractPhotoUrl(Map<String, Object> place, String placeName) {
        if (place != null) {
            Object photos = place.get("photos");
            if (photos instanceof List && !((List<?>) photos).isEmpty()) {
                Object first = ((List<?>) photos).get(0);
                if (first instanceof Map) {
                    String photoRef = (String) ((Map<?, ?>) first).get("photo_reference");
                    if (photoRef != null) {
                        String url = googlePlacesService.buildPhotoUrl(photoRef, 800);
                        if (url != null) return url;
                    }
                }
            }
        }

        // Beautiful Unsplash fallback
        return "https://source.unsplash.com/800x600/?" + 
               URLEncoder.encode(placeName + " " + placeName.split(" ")[0], java.nio.charset.StandardCharsets.UTF_8);
    }
}
