package com.example.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class GooglePlacesService {

    @Value("${google.places.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Get tourist attractions for a city
     */
    public List<Map<String, Object>> getTouristPlaces(String city) {
        try {
            String query = "best tourist attractions in " + city;
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" 
                         + URLEncoder.encode(query, StandardCharsets.UTF_8) 
                         + "&key=" + apiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"OK".equals(response.get("status"))) {
                System.out.println("Google Places API Error: " + response.get("status"));
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) response.get("results");

        } catch (Exception e) {
            System.out.println("Error fetching tourist places: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get restaurants in a city
     */
    public List<Map<String, Object>> getRestaurants(String city) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=restaurants+in+" 
                         + URLEncoder.encode(city, StandardCharsets.UTF_8) 
                         + "&key=" + apiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"OK".equals(response.get("status"))) {
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) response.get("results");

        } catch (Exception e) {
            System.out.println("Error fetching restaurants: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get restaurants near a location with budget filter
     */
    public List<Map<String, Object>> getRestaurantsNear(String location, String budget) {
        try {
            String priceQuery = "";
            if ("Low Budget".equalsIgnoreCase(budget)) {
                priceQuery = " cheap";
            } else if ("High Budget".equalsIgnoreCase(budget)) {
                priceQuery = " fine dining";
            }

            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=restaurants+near+" 
                         + URLEncoder.encode(location + priceQuery, StandardCharsets.UTF_8) 
                         + "&key=" + apiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"OK".equals(response.get("status"))) {
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) response.get("results");

        } catch (Exception e) {
            System.out.println("Error fetching nearby restaurants: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get hotels near coordinates
     */
    public List<Map<String, Object>> getHotelsNearLocation(double lat, double lng, int radiusMeters) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=" 
                         + lat + "," + lng 
                         + "&radius=" + radiusMeters 
                         + "&type=lodging&key=" + apiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"OK".equals(response.get("status"))) {
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) response.get("results");

        } catch (Exception e) {
            System.out.println("Error fetching hotels: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get hotels in a city
     */
    public List<Map<String, Object>> getHotelsInDestination(String city) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=hotels+in+" 
                         + URLEncoder.encode(city, StandardCharsets.UTF_8) 
                         + "&key=" + apiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null || !"OK".equals(response.get("status"))) {
                return Collections.emptyList();
            }

            return (List<Map<String, Object>>) response.get("results");

        } catch (Exception e) {
            System.out.println("Error fetching hotels in destination: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get place location (lat, lng)
     */
    public Map<String, Object> getPlaceLocation(String placeName) {
        try {
            String url = "https://maps.googleapis.com/maps/api/place/textsearch/json?query=" 
                         + URLEncoder.encode(placeName, StandardCharsets.UTF_8) 
                         + "&key=" + apiKey;

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            if (results == null || results.isEmpty()) {
                return null;
            }

            Map<String, Object> first = results.get(0);
            Object geometry = first.get("geometry");
            if (!(geometry instanceof Map)) return null;

            Object location = ((Map<?, ?>) geometry).get("location");
            if (!(location instanceof Map)) return null;

            Map<String, Object> info = new HashMap<>();
            Object latObj = ((Map<?, ?>) location).get("lat");
            Object lngObj = ((Map<?, ?>) location).get("lng");

            if (latObj instanceof Number && lngObj instanceof Number) {
                info.put("lat", ((Number) latObj).doubleValue());
                info.put("lng", ((Number) lngObj).doubleValue());
            }

            if (first.get("name") instanceof String) {
                info.put("name", first.get("name"));
            }

            return info;

        } catch (Exception e) {
            System.out.println("Error getting place location: " + e.getMessage());
            return null;
        }
    }

    /**
     * Build Google Photo URL from photo reference
     */
    public String buildPhotoUrl(String photoReference, int maxWidth) {
        if (photoReference == null || photoReference.isEmpty() || apiKey == null) {
            return null;
        }
        return "https://maps.googleapis.com/maps/api/place/photo?maxwidth=" 
                + maxWidth 
                + "&photoreference=" + photoReference 
                + "&key=" + apiKey;
    }
}
