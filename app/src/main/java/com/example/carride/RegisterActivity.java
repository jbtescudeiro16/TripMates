package com.example.carride;

import android.content.Intent;
import android.os.Bundle;
import androidx.activity.ComponentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class RegisterActivity extends ComponentActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword;
    private Button btnRegister;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nome = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String confirmpassword = etConfirmPassword.getText().toString().trim();

                boolean isValid = true;

                if (nome.isEmpty()) {
                    etUsername.setError("Campo nome é obrigatório");
                    isValid = false;
                }
                if (email.isEmpty() || !isValidEmail(email)) {
                    etEmail.setError("Email inválido");
                    isValid = false;
                }
                if (password.isEmpty()) {
                    etPassword.setError("Campo password é obrigatório");
                    isValid = false;
                }
                if (confirmpassword.isEmpty()) {
                    etConfirmPassword.setError("Confirmação de password é obrigatória");
                    isValid = false;
                }
                if (!confirmpassword.equals(password)) {
                    etConfirmPassword.setError("Passwords não coincidem");
                    isValid = false;
                }

                if (isValid) {
                    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Registro bem-sucedido, agora salvamos o nome do usuário no Firestore
                            String userId = auth.getCurrentUser().getUid();
                            DocumentReference userRef = db.collection("users").document(userId);
                            Map<String, Object> userData = new HashMap<>();
                            userData.put("name", nome);
                            userRef.set(userData).addOnCompleteListener(databaseTask -> {
                                if (databaseTask.isSuccessful()) {
                                    Toast.makeText(RegisterActivity.this, "Registo completo", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Erro ao salvar nome do usuário: " + databaseTask.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, "Não foi possível efetuar o registo: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Por favor, preencha todos os campos corretamente", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }
}
