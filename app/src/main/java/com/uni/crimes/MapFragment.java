package com.uni.crimes;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationType;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;
import com.uni.crimes.model.Crime;
import com.uni.crimes.viewmodel.CrimeViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapFragment extends Fragment {

    private static final String TAG = "MapFragment";
    private static final String ARG_CRIMES_LIST = "crimes_list";
    private static final int BATCH_SIZE = 100; // Process markers in batches

    private MapView mapView;
    private FloatingActionButton fabResetView;
    private FloatingActionButton fabZoomIn;
    private FloatingActionButton fabZoomOut;
    private ProgressBar progressBar;
    private TextView tvLoadingStatus;
    private View loadingOverlay;
    
    private PointAnnotationManager pointAnnotationManager;
    private MapboxMap mapboxMap;
    private CrimeViewModel crimeViewModel;
    private List<Crime> crimesToShow = new ArrayList<>();
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    
    private boolean isMapReady = false;
    private boolean isStyleLoaded = false;
    private Bitmap cachedMarkerBitmap;

    private static final double YORKSHIRE_LAT = 53.8008;
    private static final double YORKSHIRE_LNG = -1.5491;
    private static final double DEFAULT_ZOOM = 8.0;

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public static MapFragment newInstance(ArrayList<Crime> crimes) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIMES_LIST, crimes);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate() called");

        if (getArguments() != null) {
            ArrayList<Crime> crimes = (ArrayList<Crime>) getArguments().getSerializable(ARG_CRIMES_LIST);
            if (crimes != null) {
                crimesToShow = new ArrayList<>(crimes);
                Log.d(TAG, "Received " + crimes.size() + " crimes to display");
            }
        }

        crimeViewModel = new ViewModelProvider(this).get(CrimeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView() called");
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initViews(view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupMap();
        setupFabs();
        observeViewModel();
    }

    private void initViews(View view) {
        mapView = view.findViewById(R.id.map_view);
        fabResetView = view.findViewById(R.id.fab_reset_view);
        fabZoomIn = view.findViewById(R.id.fab_zoom_in);
        fabZoomOut = view.findViewById(R.id.fab_zoom_out);
        progressBar = view.findViewById(R.id.progress_bar);
        tvLoadingStatus = view.findViewById(R.id.tv_loading_status);
        loadingOverlay = view.findViewById(R.id.loading_overlay);
    }

    private void setupMap() {
        showLoading("Initializing map...");
        
        mapboxMap = mapView.getMapboxMap();
        
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Log.d(TAG, "Mapbox style loaded successfully");
                isStyleLoaded = true;
                
                try {
                    initAnnotationManager();
                    resetMapView();
                    isMapReady = true;
                    
                    // Load markers if we have crimes
                    if (!crimesToShow.isEmpty()) {
                        loadCrimeMarkersAsync();
                    } else {
                        hideLoading();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error initializing map components", e);
                    showError("Failed to initialize map");
                }
            }
        });
    }

    private void initAnnotationManager() {
        try {
            AnnotationPlugin annotationPlugin = mapView.getPlugin(Plugin.MAPBOX_ANNOTATION_PLUGIN_ID);
            if (annotationPlugin != null) {
                pointAnnotationManager = (PointAnnotationManager) annotationPlugin.createAnnotationManager(
                        AnnotationType.PointAnnotation, new AnnotationConfig());
                Log.d(TAG, "Annotation manager initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating annotation manager", e);
        }
    }

    private void setupFabs() {
        fabResetView.setOnClickListener(v -> {
            Log.d(TAG, "Reset view clicked");
            resetMapView();
        });
        
        fabZoomIn.setOnClickListener(v -> {
            if (mapboxMap != null) {
                double currentZoom = mapboxMap.getCameraState().getZoom();
                CameraOptions options = new CameraOptions.Builder()
                        .zoom(Math.min(currentZoom + 1, 18))
                        .build();
                mapboxMap.setCamera(options);
            }
        });
        
        fabZoomOut.setOnClickListener(v -> {
            if (mapboxMap != null) {
                double currentZoom = mapboxMap.getCameraState().getZoom();
                CameraOptions options = new CameraOptions.Builder()
                        .zoom(Math.max(currentZoom - 1, 3))
                        .build();
                mapboxMap.setCamera(options);
            }
        });
    }

    private void observeViewModel() {
        if (crimesToShow.isEmpty()) {
            crimeViewModel.getAllCrimes().observe(getViewLifecycleOwner(), crimes -> {
                if (crimes != null && !crimes.isEmpty()) {
                    Log.d(TAG, "Received " + crimes.size() + " crimes from ViewModel");
                    crimesToShow = new ArrayList<>(crimes);
                    if (isMapReady) {
                        loadCrimeMarkersAsync();
                    }
                } else {
                    Log.d(TAG, "No crimes received from ViewModel");
                    hideLoading();
                }
            });
        }
    }

    private void loadCrimeMarkersAsync() {
        if (pointAnnotationManager == null || crimesToShow.isEmpty()) {
            Log.d(TAG, "Cannot load markers - manager not ready or no crimes");
            hideLoading();
            return;
        }

        showLoading("Loading " + crimesToShow.size() + " crime locations...");
        
        executor.execute(() -> {
            try {
                // Pre-cache the marker bitmap
                mainHandler.post(() -> {
                    cachedMarkerBitmap = getMarkerBitmap();
                });
                
                // Wait a bit for bitmap to be ready
                Thread.sleep(100);
                
                // Clear existing markers on main thread
                mainHandler.post(() -> {
                    if (pointAnnotationManager != null) {
                        pointAnnotationManager.deleteAll();
                    }
                });
                
                // Process crimes and create annotation options
                List<PointAnnotationOptions> allOptions = new ArrayList<>();
                int validCount = 0;
                
                for (Crime crime : crimesToShow) {
                    try {
                        double lat = crime.getLatitude();
                        double lng = crime.getLongitude();
                        
                        if (isValidCoordinate(lat, lng)) {
                            PointAnnotationOptions options = new PointAnnotationOptions()
                                    .withPoint(Point.fromLngLat(lng, lat));
                            allOptions.add(options);
                            validCount++;
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Error processing crime: " + crime.getCrimeId());
                    }
                }
                
                final int totalValid = validCount;
                
                // Add markers in batches on main thread
                for (int i = 0; i < allOptions.size(); i += BATCH_SIZE) {
                    final int start = i;
                    final int end = Math.min(i + BATCH_SIZE, allOptions.size());
                    final int progress = (int) ((end * 100.0) / allOptions.size());
                    
                    mainHandler.post(() -> {
                        if (pointAnnotationManager != null && cachedMarkerBitmap != null && isAdded()) {
                            List<PointAnnotationOptions> batch = allOptions.subList(start, end);
                            for (PointAnnotationOptions opt : batch) {
                                opt.withIconImage(cachedMarkerBitmap);
                                pointAnnotationManager.create(opt);
                            }
                            updateLoadingProgress(progress, totalValid);
                        }
                    });
                    
                    // Small delay between batches to prevent UI freeze
                    Thread.sleep(50);
                }
                
                mainHandler.post(() -> {
                    hideLoading();
                    if (getContext() != null && isAdded()) {
                        Toast.makeText(getContext(),
                                "Loaded " + totalValid + " crime locations",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Error loading markers", e);
                mainHandler.post(() -> {
                    hideLoading();
                    showError("Failed to load crime markers");
                });
            }
        });
    }

    private Bitmap getMarkerBitmap() {
        if (cachedMarkerBitmap != null) {
            return cachedMarkerBitmap;
        }
        
        try {
            Drawable drawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_crime_marker);
            if (drawable != null) {
                int width = drawable.getIntrinsicWidth() > 0 ? drawable.getIntrinsicWidth() : 48;
                int height = drawable.getIntrinsicHeight() > 0 ? drawable.getIntrinsicHeight() : 48;
                
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, width, height);
                drawable.draw(canvas);
                return bitmap;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating marker bitmap", e);
        }
        
        // Fallback: create a simple red marker
        Bitmap bitmap = Bitmap.createBitmap(32, 32, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(0xFFE53935);
        return bitmap;
    }

    private boolean isValidCoordinate(double lat, double lng) {
        return lat >= -90.0 && lat <= 90.0 && 
               lng >= -180.0 && lng <= 180.0 &&
               (lat != 0.0 || lng != 0.0);
    }

    private void resetMapView() {
        if (mapboxMap != null) {
            CameraOptions cameraOptions = new CameraOptions.Builder()
                    .center(Point.fromLngLat(YORKSHIRE_LNG, YORKSHIRE_LAT))
                    .zoom(DEFAULT_ZOOM)
                    .build();
            mapboxMap.setCamera(cameraOptions);
        }
    }

    private void showLoading(String message) {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.VISIBLE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (tvLoadingStatus != null) {
            tvLoadingStatus.setVisibility(View.VISIBLE);
            tvLoadingStatus.setText(message);
        }
    }

    private void updateLoadingProgress(int progress, int total) {
        if (tvLoadingStatus != null && isAdded()) {
            tvLoadingStatus.setText("Loading markers: " + progress + "% (" + total + " locations)");
        }
    }

    private void hideLoading() {
        if (loadingOverlay != null) {
            loadingOverlay.setVisibility(View.GONE);
        }
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (tvLoadingStatus != null) {
            tvLoadingStatus.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        hideLoading();
        if (getContext() != null && isAdded()) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView() called");
        isMapReady = false;
        isStyleLoaded = false;
        cachedMarkerBitmap = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
