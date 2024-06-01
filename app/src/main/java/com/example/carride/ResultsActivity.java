package com.example.carride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Locale;

public class ResultsActivity extends ComponentActivity {

    private TextView txtTotalTime;
    private TextView txtTotalDistance;
    private TextView txtTotalCost;
    private TextView txtCostPerTraveler;
    private RatingBar ratingBar;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String selectedFuelType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // Initialize views
        txtTotalTime = findViewById(R.id.txtTimeValue);
        txtTotalDistance = findViewById(R.id.txtDistanceValue);
        txtTotalCost = findViewById(R.id.txtCostValue);
        txtCostPerTraveler = findViewById(R.id.txtCostPerTravelerValue);
        ratingBar = findViewById(R.id.ratingBar);
        Button backButton = findViewById(R.id.btnback);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        String userId = getUserId();

        if (userId == null) {
            return;
        }

        // Find last trip data
        db.collection("trips")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot lastTrip = queryDocumentSnapshots.getDocuments().get(0);
                        double distance = lastTrip.getDouble("distance");
                        double distanceInKilometers = distance / 1000.0;
                        long elapsedTime = lastTrip.getLong("elapsedTime");
                        int lightJolts = lastTrip.getLong("light_jolts").intValue();
                        int normalJolts = lastTrip.getLong("normal_jolts").intValue();
                        int strongJolts = lastTrip.getLong("strong_jolts").intValue();
                        int totalPhoneAccesses = lastTrip.getLong("phoneAccesses").intValue();


                        db.collection("users").document(userId).get().addOnSuccessListener(userDocument -> {
                            if (userDocument.exists()) {
                                selectedFuelType = userDocument.getString("fuelType");
                                double autonomy = Double.parseDouble(userDocument.getString("fuelConsumption"));
                                Long numberOfTravelersLong = userDocument.getLong("numberOfTravelers");
                                int numberOfTravelers = numberOfTravelersLong != null ? numberOfTravelersLong.intValue() : 0;

                                // Retrieve fuel price from Firestore
                                db.collection("fuel_prices").document(selectedFuelType).get().addOnSuccessListener(document -> {
                                    String priceString = document.getString("price");
                                    double fuelPrice = extractPriceFromText(priceString);
                                    double totalCost = distanceInKilometers *autonomy/100 * fuelPrice;
                                    double costPerTraveler;
                                    if (numberOfTravelers == 0) {
                                        costPerTraveler = 0.0;
                                    } else {
                                        costPerTraveler = totalCost / numberOfTravelers;
                                    }

                                    // Calculate rating
                                    double rating = calculateRating(lightJolts, normalJolts, strongJolts, totalPhoneAccesses);

                                    // Update calculated fields in trip document
                                    lastTrip.getReference().update(
                                            "totalCost", totalCost,
                                            "costPerTraveler", costPerTraveler,
                                            "rating", rating
                                    ).addOnSuccessListener(aVoid -> {
                                        txtTotalDistance.setText(String.format(Locale.getDefault(), "%.2f km", distanceInKilometers));
                                        txtTotalTime.setText(formatTime(elapsedTime));
                                        txtTotalCost.setText(String.format(Locale.getDefault(), "%.2f €", totalCost));
                                        txtCostPerTraveler.setText(String.format(Locale.getDefault(), "%.2f €", costPerTraveler));
                                        ratingBar.setRating((float) rating);
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(ResultsActivity.this, "Error updating trip data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                                }).addOnFailureListener(e -> {
                                    Toast.makeText(ResultsActivity.this, "Error fetching fuel price: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                Toast.makeText(ResultsActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(ResultsActivity.this, "Error fetching user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Toast.makeText(ResultsActivity.this, "No trip found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResultsActivity.this, "Error fetching last trip data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultsActivity.this, MainActivity.class);
            startActivity(intent);
        });
    }



    private double calculateRating(int lightJolts, int normalJolts, int strongJolts, int totalPhoneAccesses) {
        if (lightJolts == 0 && normalJolts == 0 && strongJolts == 0 && totalPhoneAccesses == 0) {
            return 5.0;
        }

        double joltWeight = 0.0;
        double rating = 5.0;

        joltWeight += lightJolts * 0.25;
        joltWeight += normalJolts * 3.0;
        joltWeight += strongJolts * 6.0;

        // Calculando o peso dos acessos ao telefone
        double phoneAccessWeight = totalPhoneAccesses * 7.0;

        rating -= (joltWeight + phoneAccessWeight) / 10.0;

        return Math.max(rating, 0.0);
    }

    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    private String formatTime(long totalTime) {
        long hours = totalTime / 3600;
        long minutes = (totalTime % 3600) / 60;

        return String.format(Locale.getDefault(), "%dh %dm", hours, minutes);
    }

    private double extractPriceFromText(String priceText) {
        // Remove euro symbol and convert to double
        String priceValue = priceText.replace("€", "").trim();
        return Double.parseDouble(priceValue);
    }
}
