package com.uni.crimes.config;

/**
 * Configuration class for Mapbox Maps integration
 * Contains API keys and map settings for Yorkshire Crimes app
 */
public class MapboxConfig {
    
    // Mapbox API Keys
    public static final String MAPBOX_PUBLIC_TOKEN = "pk.eyJ1Ijoic2hlcmEtMTEiLCJhIjoiY21qdm0yYWgxNGI3dDNnc2NpMDRyZXNxYyJ9.tdGO5PvanEwllGJ1o62AjA";
    public static final String MAPBOX_SECRET_TOKEN = "sk.eyJ1Ijoic2hlcmEtMTEiLCJhIjoiY21qdm05N3VoMHB6ZDNmczRrNGtyYzVsYyJ9.zdbkokHnh_Nwji08wlkbZQ";
    
    // Yorkshire region coordinates
    public static final double YORKSHIRE_CENTER_LAT = 53.8008;
    public static final double YORKSHIRE_CENTER_LNG = -1.5491;
    public static final double DEFAULT_ZOOM_LEVEL = 8.0;
    
    // Map bounds for Yorkshire region
    public static final double YORKSHIRE_NORTH = 54.7;
    public static final double YORKSHIRE_SOUTH = 53.0;
    public static final double YORKSHIRE_EAST = -0.5;
    public static final double YORKSHIRE_WEST = -2.5;
    
    // Map style options
    public static final String DEFAULT_MAP_STYLE = "mapbox://styles/mapbox/streets-v12";
    public static final String SATELLITE_MAP_STYLE = "mapbox://styles/mapbox/satellite-v9";
    public static final String DARK_MAP_STYLE = "mapbox://styles/mapbox/dark-v11";
    
    // Marker settings
    public static final double MARKER_SIZE = 1.0;
    public static final String MARKER_COLOR = "#FF0000"; // Red for crime markers
    
    /**
     * Check if coordinates are within Yorkshire bounds
     */
    public static boolean isWithinYorkshire(double latitude, double longitude) {
        return latitude >= YORKSHIRE_SOUTH && latitude <= YORKSHIRE_NORTH &&
               longitude >= YORKSHIRE_WEST && longitude <= YORKSHIRE_EAST;
    }
    
    /**
     * Get appropriate zoom level based on number of markers
     */
    public static double getOptimalZoom(int markerCount) {
        if (markerCount <= 10) return 10.0;
        if (markerCount <= 50) return 9.0;
        if (markerCount <= 100) return 8.5;
        return DEFAULT_ZOOM_LEVEL;
    }
}