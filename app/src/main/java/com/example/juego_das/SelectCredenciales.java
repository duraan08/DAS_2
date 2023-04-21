package com.example.juego_das;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SelectCredenciales extends Worker {
    public SelectCredenciales(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //Datos enviados desde InicioSesionActivity
        String username = getInputData().getString("nombre");

        //Se conecta con el servidor
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/uduran005/WEB/select_BD_DAS.php";
        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            //Rellenamos los parametros
            String parametros = "nombre="+username;

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            //Enviar los parametros al php
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametros);
            //Agregar esta línea para asegurarse de que los datos se envíen correctamente
            out.flush();

            int status = urlConnection.getResponseCode();
            Log.d("Prueba_Select", "Status --> " + status);

            //Si la respuesta es "200 OK" Entonces se realiza la recogida de datos
            if(status == 200){
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(inputStream, "UTF-8"));
                String line, result="";
                while ((line = bufferedReader.readLine()) != null){
                    result += line;
                }
                Log.d("Prueba_Select", "resultado --> " + result);
                inputStream.close();

                if (!result.equals("null")){
                    Log.d("Prueba_Select", "He entrado aqui");
                    //Se parsean los datos como JSON
                    JSONArray jsonArray = new JSONArray(result);
                    ArrayList<String[]> lista = new ArrayList<>();
                    for(int i = 0; i < jsonArray.length(); i++){
                        String[] datos = {jsonArray.getJSONObject(i).getString("nombre"), jsonArray.getJSONObject(i).getString("password")};
                        lista.add(datos);
                    }
                    Log.d("Select_Prueba", "Usuario --> " + lista.get(0)[0]);
                    String[] array = {lista.get(0)[0], lista.get(0)[1]}; //Nombre || Password
                    Data data = new Data.Builder()
                            .putStringArray("array",array)
                            .build();
                    return Result.success(data);
                }
                else {
                    Log.d("Prueba_Select", "He entrado --> " + result);
                    String[] array = {"null", "null"};
                    Data data = new Data.Builder()
                            .putStringArray("array",array)
                            .build();
                    return Result.success(data);
                }
            }
        }
        catch (Exception e){
            Log.d("DAS","Error: " + e);
        }
        return Result.failure();
    }
}
