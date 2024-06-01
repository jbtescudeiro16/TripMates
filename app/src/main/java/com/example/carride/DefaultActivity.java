package com.example.carride;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DefaultActivity extends ComponentActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private String fuelConsumption;
    private String fuelType;
    private int numberOfTravelers;
    private String name;
    private int tripsCount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_default);

        // Inicializa o Firebase Firestore e a autenticação
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Obtém os dados do usuário da Firestore
        fetchUserData();
    }

    // Obtém o ID do usuário atual
    private String getUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    // Obtém os dados do usuário da Firestore
    private void fetchUserData() {
        String userId = getUserId();

        if (userId != null) {
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    fuelConsumption = documentSnapshot.getString("fuelConsumption");
                    fuelType = documentSnapshot.getString("fuelType");
                    Long travelers = documentSnapshot.getLong("numberOfTravelers");
                    numberOfTravelers = travelers != null ? travelers.intValue() : 0;
                    name = documentSnapshot.getString("name");
                    Long trips = documentSnapshot.getLong("tripsCount");
                    tripsCount = trips != null ? trips.intValue() : 0;
                }
            });
        }
    }

    // Exibe o diálogo para inserir o consumo de combustível
    public void showFuelConsumptionDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Consumo (L/100 km)");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        if (fuelConsumption != null && !fuelConsumption.isEmpty()) {
            input.setText(fuelConsumption);
        }

        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String consumption = input.getText().toString().trim();
                if (!consumption.isEmpty()) {
                    fuelConsumption = consumption;
                }
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Exibe o diálogo para selecionar o tipo de combustível
    public void showFuelTypeDialog(View view) {
        final String[] fuelTypes = {"GPL Auto", "Gasolina 95 +", "Gasolina 95 Simples", "Gasolina 98 +", "Gasolina 98 Simples", "Gasóleo +", "Gasóleo Simples"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Tipo de Combustível");

        int selected = -1; // Para manter a seleção vazia se não houver valor existente
        if (fuelType != null && !fuelType.isEmpty()) {
            for (int i = 0; i < fuelTypes.length; i++) {
                if (fuelTypes[i].equals(fuelType)) {
                    selected = i;
                    break;
                }
            }
        }

        builder.setSingleChoiceItems(fuelTypes, selected, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                fuelType = fuelTypes[which];
            }
        });

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Exibe o diálogo para selecionar o número de viajantes
    public void showTravelersDialog(View view) {
        NumberPicker numberPicker = new NumberPicker(this);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(10);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Número de Viajantes");
        builder.setView(numberPicker);

        if (numberOfTravelers != 0) {
            numberPicker.setValue(numberOfTravelers);
        }

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberOfTravelers = numberPicker.getValue();
            }
        });

        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    // Inicia a viagem
    public void start(View view) {
        // Obtém o ID do usuário
        String userId = getUserId();

        if (userId != null) {
            // Referência ao documento do usuário na Firestore
            DocumentReference userRef = db.collection("users").document(userId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Verifica se todos os campos obrigatórios foram preenchidos
                    if (!validateFields()) {
                        return;
                    }

                    // Atualiza os dados do usuário na Firestore
                    Map<String, Object> updatedData = new HashMap<>();
                    updatedData.put("fuelConsumption", fuelConsumption);
                    updatedData.put("fuelType", fuelType);
                    updatedData.put("numberOfTravelers", numberOfTravelers);
                    updatedData.put("name", name);
                    updatedData.put("tripsCount", tripsCount);

                    userRef.set(updatedData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(DefaultActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();

                                // Inicia a atividade da viagem
                                Intent intent = new Intent(DefaultActivity.this, TripActivity.class);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DefaultActivity.this, "Erro ao atualizar os dados do utilizador: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                } else {
                    // Verifica se todos os campos obrigatórios foram preenchidos
                    if (!validateFields()) {
                        return;
                    }

                    // Salva os novos dados do usuário na Firestore
                    Map<String, Object> newData = new HashMap<>();
                    newData.put("fuelConsumption", fuelConsumption);
                    newData.put("fuelType", fuelType);
                    newData.put("numberOfTravelers", numberOfTravelers);
                    newData.put("name", name);
                    newData.put("tripsCount", tripsCount);

                    userRef.set(newData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(DefaultActivity.this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();

                                // Inicia a atividade da viagem
                                Intent intent = new Intent(DefaultActivity.this, TripActivity.class);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(DefaultActivity.this, "Erro ao salvar dados: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(DefaultActivity.this, "Erro ao acessar o Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Valida se todos os campos obrigatórios foram preenchidos
    private boolean validateFields() {
        if (numberOfTravelers == 0) {
            Toast.makeText(this, "Campo número de viajantes é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fuelType == null || fuelType.isEmpty()) {
            Toast.makeText(this, "Campo tipo de combustível é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fuelConsumption == null || fuelConsumption.isEmpty()) {
            Toast.makeText(this, "Campo consumo é obrigatório", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
