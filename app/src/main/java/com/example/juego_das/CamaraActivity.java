package com.example.juego_das;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import android.util.Base64;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import android.Manifest;

public class CamaraActivity extends AppCompatActivity  {
    //Codigo extraido de --> https://youtu.be/kebuipmQliE; Autor --> Códigos de Programación
    Button btnCamara;
    ImageView img;
    Bundle extras;
    Bitmap imagenBitmap;
    String url = "http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/uduran005/WEB/imagenes.php";
    int indiceIamgen = 0;
    ActivityResultLauncher<Intent> mLauncher;
    private static final int PERMISSIONS_REQUEST_CAMERA = 1;
    private static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        btnCamara = findViewById(R.id.btnCamara);
        img = findViewById(R.id.imagenCamara);

        if (savedInstanceState != null){
            indiceIamgen = savedInstanceState.getInt("indiceImagen");
            extras = savedInstanceState.getBundle("Imagen");
            imagenBitmap = (Bitmap) extras.get("data");
            img.setImageBitmap(imagenBitmap);
        }

        mLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        extras = data.getExtras();
                        //Mapeamos la imagen para poder colocarla en el ImageView
                        imagenBitmap = (Bitmap) extras.get("data");
                        img.setImageBitmap(imagenBitmap);
                    }
                });
    }

    public void utilizarCamara(View view){
        abrirCamara();
    }

    //Iniciamos la app de la camara para sacar fotos
    private void abrirCamara() {
        if (!checkPermissions()) {
            ActivityCompat.requestPermissions(CamaraActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CAMERA);
            return;
        }

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null){
            mLauncher.launch(i);
        }
    }


    private boolean checkPermissions() {
        int cameraPermission = ActivityCompat.checkSelfPermission(CamaraActivity.this, Manifest.permission.CAMERA);
        int writeExternalStoragePermission = ActivityCompat.checkSelfPermission(CamaraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return cameraPermission == PackageManager.PERMISSION_GRANTED && writeExternalStoragePermission == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CAMERA && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        }
    }
    /*public void enviarImagen(View view) {
        if (imagenBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageBytes = baos.toByteArray();
            String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            //Creamos la petición HTTP POST y adjuntamos la imagen en el cuerpo de la petición
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Toast.makeText(CamaraActivity.this, response, Toast.LENGTH_LONG).show();
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //Manejar el error en caso de que falle la petición
                }
            }) {
                @Override
                public Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("imagen", encodedImage);
                    return params;
                }
            };

            //Agregamos la petición a la cola de solicitudes HTTP
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);
            finish();
        }
    }*/

    public void enviarImagen(View view) {
        if (imagenBitmap == null) {
            Toast.makeText(getApplicationContext(), "No se ha capturado ninguna imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imagenBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        // Crear un formato de fecha y hora
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        // Crear el título con el formato deseado
        String titulo = "imagen" + dateFormat.format(new Date());

        // Creamos una tarea asíncrona para realizar la petición HTTP
        AsyncTask<Void, Void, Void> tarea = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://ec2-54-93-62-124.eu-central-1.compute.amazonaws.com/uduran005/WEB/imagenes.php");

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    // Creamos el cuerpo de la petición con los datos de la imagen y el título
                    String body = "imagen=" + URLEncoder.encode(encodedImage, "UTF-8") +
                            "&titulo=" + URLEncoder.encode(titulo, "UTF-8");

                    OutputStream os = conn.getOutputStream();
                    os.write(body.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    Log.d("Camara", "Respuesta --> " + responseCode);
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Si la petición es exitosa, mostramos un mensaje al usuario
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Imagen enviada correctamente", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Si hay algún error, mostramos un mensaje al usuario
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error al enviar la imagen", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        // Ejecutamos la tarea asíncrona
        tarea.execute();
    }





    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle("Imagen", extras);
        outState.putInt("indiceImagen", indiceIamgen);
    }
}