package com.example.carride;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView userNameTextView;
    private TextView tripsTextView;
    private Button startNewTripButton;
    private RecyclerView tripsRecyclerView;
    private TripAdapter tripAdapter;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        userNameTextView = findViewById(R.id.iconuservalue);
        tripsTextView = findViewById(R.id.txtnrviagens);
        startNewTripButton = findViewById(R.id.startNewTripButton);
        tripsRecyclerView = findViewById(R.id.tripsRecyclerView);

        String userId = auth.getCurrentUser().getUid();

        Log.d(TAG, "UserID: " + userId);

        updateFuelPrices();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            userNameTextView.setText(userName);

                            Long numberOfTrips = documentSnapshot.getLong("tripsCount");
                            if (numberOfTrips != null) {
                                tripsTextView.setText(String.valueOf(numberOfTrips));
                            } else {
                                tripsTextView.setText("0");
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Documento do utilizador não encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Erro ao buscar dados do utilizador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Log para registrar o erro ao buscar dados do utilizador
                        Log.e(TAG, "Erro ao buscar dados do usuário: " + e.getMessage(), e);
                    }
                });

        startNewTripButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DefaultActivity.class);
                startActivity(intent);
            }
        });

        // Recuperar e exibir as viagens do utilizador
        retrieveAndDisplayTrips(userId);
    }



    @Override
    protected void onResume() {
        super.onResume();
        updateUserData();
    }

    private void updateUserData() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            String userName = documentSnapshot.getString("name");
                            userNameTextView.setText(userName);

                            Long numberOfTrips = documentSnapshot.getLong("tripsCount");
                            if (numberOfTrips != null) {
                                tripsTextView.setText(String.valueOf(numberOfTrips));
                            } else {
                                tripsTextView.setText("0");
                            }

                            retrieveAndDisplayTrips(userId);
                        } else {
                            Toast.makeText(MainActivity.this, "Documento do utilizador não encontrado", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Erro ao buscar dados do utilizador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Erro ao buscar dados do utilizador: " + e.getMessage(), e);
                    }
                });
    }



    private void retrieveAndDisplayTrips(String userId) {
        db.collection("trips")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> tripStrings = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String timestamp = document.getString("timestamp");
                        double distanceInMeters = document.getDouble("distance");
                        double distanceInKilometers = distanceInMeters / 1000; // Convertendo para quilômetros
                        long elapsedTime = document.getLong("elapsedTime");
                        double pricePerPerson = document.getDouble("costPerTraveler");
                        double rating = document.getDouble("rating");

                        String elapsedTimeFormatted = formatElapsedTime(elapsedTime);
                        String dateFormatted = formatDate(timestamp);
                        String tripDetails = String.format(Locale.getDefault(), "%s | %.2f km | %s | €/P: %.2f€ | Rating: %.2f",
                                dateFormatted, distanceInKilometers, elapsedTimeFormatted, pricePerPerson, rating);

                        tripStrings.add(tripDetails);
                        Log.d(TAG, "Detalhes da viagem adicionados: " + tripDetails);
                    }
                    displayTrips(tripStrings);
                    updateNumberOfTrips(tripStrings.size());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Erro ao buscar viagens do usuário: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private String formatElapsedTime(long elapsedTimeSeconds) {
        long hours = elapsedTimeSeconds / 3600;
        long minutes = (elapsedTimeSeconds % 3600) / 60;

        return String.format(Locale.getDefault(), "%dh%02d", hours, minutes);
    }

    private String formatDate(String timestamp) {
        String[] parts = timestamp.split(" ");
        if (parts.length > 0) {
            return parts[0];
        } else {
            return "";
        }
    }

    private void displayTrips(List<String> trips) {
        tripAdapter = new TripAdapter(trips);
        tripsRecyclerView.setAdapter(tripAdapter);
        tripsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void updateNumberOfTrips(int numberOfTrips) {
        tripsTextView.setText(String.valueOf(numberOfTrips));
    }




    private void updateFuelPrices() {
        new AsyncTask<Void, Void, Map<String, String>>() {
            @Override
            protected Map<String, String> doInBackground(Void... voids) {
                // URL do site
                String url = "https://www.maisgasolina.com/";

                // Mapa para armazenar os preços dos combustíveis
                Map<String, String> fuelPrices = new HashMap<>();

                try {
                    // Faz a requisição HTTP para obter o conteúdo da página
                    Document document = Jsoup.connect(url).get();

                    // Seletor CSS para encontrar os elementos com os preços dos combustíveis
                    Elements priceElements = document.select(".box#homeAverage .homePricesValue");

                    // Itera sobre os elementos encontrados
                    for (Element element : priceElements) {
                        // Extrai o tipo de combustível e seu preço
                        String fuelType = element.previousElementSibling().text();
                        String fuelPrice = element.ownText();

                        // Adiciona o tipo de combustível e seu preço ao mapa
                        fuelPrices.put(fuelType, fuelPrice);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }

                return fuelPrices;
            }

            @Override
            protected void onPostExecute(Map<String, String> fuelPrices) {
                // Atualiza os preços na coleção de combustíveis na Firestore
                if (fuelPrices != null) {
                    // Para cada tipo de combustível, cria um documento na coleção "fuel_prices"
                    for (Map.Entry<String, String> entry : fuelPrices.entrySet()) {
                        String fuelType = entry.getKey();
                        String fuelPrice = entry.getValue();
                        Map<String, Object> data = new HashMap<>();
                        data.put("price", fuelPrice);
                        db.collection("fuel_prices").document(fuelType).set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Se houver um erro ao atualizar os preços na Firestore
                                        Toast.makeText(MainActivity.this, "Erro ao atualizar o preço do combustível " + fuelType + " na Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Erro ao atualizar os preços dos combustíveis", Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }




}
