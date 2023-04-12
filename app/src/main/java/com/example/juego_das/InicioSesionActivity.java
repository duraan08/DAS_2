package com.example.juego_das;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class InicioSesionActivity extends AppCompatActivity {
    String usu, passwd;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion);
    }

    public void comprobar(View view){
        EditText user = findViewById(R.id.user);
        EditText psw = findViewById(R.id.psw);

        String usuario = user.getText().toString().toLowerCase();
        String pass = psw.getText().toString();

        if (usuario.isEmpty() || pass.isEmpty()){
            Toast.makeText(InicioSesionActivity.this, "Debe rellenar todos los campos", Toast.LENGTH_LONG).show();
        }
        else{
            Data data = new Data.Builder()
                    .putString("nombre", usuario).build();
            OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(SelectCredenciales.class)
                    .setInputData(data).build();
            WorkManager.getInstance(InicioSesionActivity.this).getWorkInfoByIdLiveData(otwr.getId()).observe(InicioSesionActivity.this,new Observer<WorkInfo>() {
                @Override
                public void onChanged (WorkInfo workInfo){
                    if (workInfo != null && workInfo.getState().isFinished()) {
                        String[] lista = workInfo.getOutputData().getStringArray("array");
                        //Log.d("Inicio_Prueba", "usuario --> " + lista[0].equals(usuario));
                        //Log.d("Inicio_Prueba", "password --> " + lista[1].equals(pass));
                        if (lista[0].equals(usuario) && lista[1].equals(pass)){
                            Intent inicio = new Intent(InicioSesionActivity.this, ParticipantesActivity.class);
                            startActivity(inicio);
                            finish();
                        }
                        else{
                            Toast.makeText(InicioSesionActivity.this, "Tu usuario o contraseña son incorrectos", Toast.LENGTH_LONG).show();

                        }
                    }
                }
            });
            WorkManager.getInstance(InicioSesionActivity.this).enqueue(otwr);
        }
    }

    public void registrar(View view){
        Intent registro = new Intent(InicioSesionActivity.this, RegistroActivity.class);
        startActivity(registro);
    }

}