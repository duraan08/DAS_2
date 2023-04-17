package com.example.juego_das;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Observer;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import android.Manifest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ktx.Firebase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.Manifest;
import android.content.pm.PackageManager;



//Codigo extraido de la lista de videos --> https://youtu.be/s1aOlr3vbbk?list=PLlGT4GXi8_8eopz0Gjkh40GG6O5KhL1V1 Autor : SmallAcademy
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
    static String token;

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
        askNotificationPermission();
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
                //Se llama al metodo que ejecuta el php para enviar el mensaje FCM
                onTokenRefresh();
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
    }

    public void onTokenRefresh() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("Prueba_FCM", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Conseguir el token
                        token = task.getResult();

                        Log.d("Prueba_FCM", "El token aqui es --> " + token);
                        Data data = new Data.Builder().putString("token", token).build();
                        OneTimeWorkRequest otwr = new OneTimeWorkRequest.Builder(FirebaseNotificacion.class).setInputData(data).build();
                        WorkManager.getInstance(CamaraActivity.this).enqueue(otwr);
                        WorkManager.getInstance(CamaraActivity.this).getWorkInfoByIdLiveData(otwr.getId()).observe(CamaraActivity.this, new Observer<WorkInfo>() {
                            public void onChanged(@Nullable WorkInfo workInfo) {
                                if (workInfo != null && workInfo.getState().isFinished()) {
                                    String resultado = workInfo.getOutputData().getString("result");
                                    // Si el php devuelve que se ha identificado CORRECTAMENTE
                                    Log.d("Prueba_FCM", "Resultado --> " + resultado);
                                }
                            }
                        });
                    }
                });
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

}