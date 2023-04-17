package com.example.juego_das;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.ListenableWorker;
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

public class FirebaseNotificacion extends Worker{
    public FirebaseNotificacion(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    @Override
    public ListenableWorker.Result doWork() {
        //Datos enviados desde InicioSesionActivity
        String token = getInputData().getString("token");
        Log.d("Prueba_Java", "Token --> " + token);

        //Se conecta con el servidor
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/uduran005/WEB/firebase.php";
        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            //Rellenamos los parametros
            String parametros = "token="+token;

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            //Enviar los parametros al php
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametros);
            //Agregar esta línea para asegurarse de que los datos se envíen correctamente
            out.flush();

            int status = urlConnection.getResponseCode();
            Log.d("Prueba_FCM", "Status --> " + status);

            //Si la respuesta es "200 OK" Entonces se realiza la recogida de datos
            if(status == 200){
                BufferedInputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader bufferedReader = new BufferedReader (new InputStreamReader(inputStream, "UTF-8"));
                String line, result="";
                while ((line = bufferedReader.readLine()) != null){
                    result += line;
                }
                Log.d("Prueba_FCM", "resultado --> " + result);
                inputStream.close();

                Data data = new Data.Builder()
                        .putString("result",result)
                        .build();
                return ListenableWorker.Result.success(data);
            }
        }
        catch (Exception e){
            Log.d("DAS","Error: " + e);
        }
        return Result.failure();
    }
}
