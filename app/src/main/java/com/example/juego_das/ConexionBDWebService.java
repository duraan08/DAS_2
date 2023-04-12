package com.example.juego_das;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

//Desarrollado en conjunto a Marcos Martínez (Compañero) y con ayuda de ChatGPT para algunos errores encontrados
public class ConexionBDWebService extends Worker {
    public ConexionBDWebService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        //Se recogen los datos enviados desde registroActivity
        String userName = getInputData().getString("nombre");
        String pass = getInputData().getString("password");

        //Se conecta con el servidor
        String direccion = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/uduran005/WEB/conexion_BD_DAS.php";
        HttpURLConnection urlConnection = null;
        try {
            URL destino = new URL(direccion);
            urlConnection = (HttpURLConnection) destino.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(5000);

            //Rellenamos los parametros que se van a introducir en la BBDD
            String parametros = "nombre="+userName+"&password="+pass;

            urlConnection.setRequestMethod("POST");
            urlConnection.setDoOutput(true);
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            //Enviar los parametros al php
            PrintWriter out = new PrintWriter(urlConnection.getOutputStream());
            out.print(parametros);
            //Agregar esta línea para asegurarse de que los datos se envíen correctamente
            out.flush();


            InputStream inputStream = urlConnection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String response = stringBuilder.toString();
            // Procesar la respuesta según tus necesidades
            JSONObject jsonResponse = new JSONObject(response);

            reader.close();
            inputStream.close();
            out.close();
            urlConnection.disconnect();
            return Result.success();
        }
        catch (Exception e) {
            return Result.failure();
        }
    }
}

