package com.example.juego_das;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import android.Manifest;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class CamaraActivity extends AppCompatActivity  {

    ImageView imagenSeleccionada;
    String currentPhotoPath;
    static  final int REQUEST_TAKE_PHOTO = 1;
    public static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    StorageReference storageReference;
    Uri uriFinal;
    boolean vacia = true;
    boolean pulsado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camara);
        imagenSeleccionada = findViewById(R.id.imagenCamara);
        storageReference = FirebaseStorage.getInstance().getReference();

        if (savedInstanceState != null){
            vacia = savedInstanceState.getBoolean("vacia");
            pulsado = savedInstanceState.getBoolean("pulsado");
            Log.d("Prueba_foto", "vacia es --> " + vacia);
            if (vacia == false ){
                uriFinal = Uri.parse(savedInstanceState.getString("imagen"));
                if ( pulsado == true){
                    verImagen(uriFinal);
                }
                //Picasso.get().load(uriFinal).into(imagenSeleccionada);
            }
        }
    }

    public void activarCamara(View view){
        askCameraPermissions();
    }

    private void askCameraPermissions(){
        if (ContextCompat.checkSelfPermission(CamaraActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(CamaraActivity.this, new String[] {Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }else{
            dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (uriFinal != null){
            Log.d("Prueba_foto", "Uri en el save --> " + uriFinal);
            outState.putString("imagen", uriFinal.toString());
        }
        outState.putBoolean("vacia", vacia);
        outState.putBoolean("pulsado", pulsado);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERM_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
            }
            else {
                Toast.makeText(CamaraActivity.this, "Camara", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE){

            if (resultCode == Activity.RESULT_OK){
                Log.d("Prueba_Foto", "Entro3");
                File f = new File(currentPhotoPath);
                Log.d("Prueba_Foto", currentPhotoPath);
                //imagenSeleccionada.setImageURI(Uri.fromFile(f));

                //Obtenemos la uri
                Log.d("Prueba_Foto", "Url absoluto --> " + Uri.fromFile(f));
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                //contentUri contiene la direccion en el movil de la foto
                Uri contentUri = Uri.fromFile(f);
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

                uploadImageToFirebase(f.getName(), contentUri);
            }
        }
    }

    private void uploadImageToFirebase(String name, Uri contentUri) {
        StorageReference image = storageReference.child("images/" + name);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Log.d("Prueba_Camara", "La url de la imagen subida es : " + uri.toString());
                        //Picasso.get().load(uri).into(imagenSeleccionada);
                        uriFinal = uri;
                        vacia = false;
                        Log.d("Prueba_foto", "La uri es --> " + uriFinal);
                    }
                });
                Toast.makeText(CamaraActivity.this, "La imagen se ha subido correctamente", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CamaraActivity.this, "Fallo en la subida", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void VerImagenSubida(View view){
        verImagen(uriFinal);
    }

    private void verImagen(Uri uri){
        Log.d("Prueba_foto", "La uri es --> " + uri);
        if (uri != null){
            pulsado = true;
            imagenSeleccionada = findViewById(R.id.imagenCamara);
            Picasso.get().load(uri).into(imagenSeleccionada);
        }
        else {
            Toast.makeText(CamaraActivity.this, "Debería sacar una foto", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // Se crea la imagen con un TimeStamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        //File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Se asegura de que hay una actividad para poder coger el intent
        //if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            Log.d("Prueba_foto", "Entro");
            File photoFile = null;
            try {
                photoFile = createImageFile();
            }catch (IOException e){Log.d("Prueba_Foto", e.toString());}

            //Solo continua si el archivo se ha creado correctamente
            if (photoFile != null){
                Uri photoUri = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
       // }
       // else {Log.d("Prueba_foto", "Ha entrado en el else");}
    }
}