package com.example.juego_das;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class RegistroActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);
    }

    public void registrar(View view) throws InterruptedException, ExecutionException {
        EditText user = findViewById(R.id.user_register);
        EditText psw1 = findViewById(R.id.psw_register);
        EditText psw2 = findViewById(R.id.psw_register_2);

        String usuario = user.getText().toString().toLowerCase();
        String pass = psw1.getText().toString();
        String pass2 = psw2.getText().toString();

        //Se comprueba que ambas password coincidan y que haya introducido algo en el campo de usuario
        if (!usuario.isEmpty() && !pass.isEmpty() && !pass2.isEmpty()){
            if (pass.equals(pass2)){
                //Se comprueba que el usuario no existe
                comprobarUser(usuario, pass);
            }
            else {
                Toast.makeText(RegistroActivity.this, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show();
            }
        }
        else{
            Toast.makeText(RegistroActivity.this, "Debe rellenar todos los campos", Toast.LENGTH_SHORT).show();
        }
    }

    private void comprobarUser(String user, String pass){
        final boolean[] existe = {false};
        Data data = new Data.Builder()
                .putString("nombre", user).build();
        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SelectCredenciales.class)
                .setInputData(data).build();
        WorkManager.getInstance(RegistroActivity.this).enqueue(otwr);

        WorkManager.getInstance(RegistroActivity.this).getWorkInfoByIdLiveData(otwr.getId()).observe(RegistroActivity.this, new Observer<WorkInfo>() {
            @Override
            public void onChanged(@Nullable WorkInfo workInfo) {
                if (workInfo != null && workInfo.getState().isFinished()) {
                    String[] lista = workInfo.getOutputData().getStringArray("array"); //Se recoge la respuesta del servidor (Array[])
                    if (lista != null){
                        existe[0] = lista[0].equals(user);
                    }
                    else{
                        existe[0] = false;
                    }
                    if (existe[0] == true){
                        Toast.makeText(RegistroActivity.this, "El usuario ya existe", Toast.LENGTH_LONG).show();
                    }
                    else{
                        Data data = new Data.Builder()
                                .putString("nombre",user).putString("password", pass).build();

                        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(ConexionBDWebService.class)
                                .setInputData(data)
                                .build();
                        WorkManager.getInstance(RegistroActivity.this).enqueue(otwr);
                        finish();
                    }
                }
            }
        });
    }
}