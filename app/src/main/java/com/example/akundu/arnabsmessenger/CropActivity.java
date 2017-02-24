package com.example.akundu.arnabsmessenger;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.arnab.crop.Crop;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CropActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 1;
    private ImageView resultView;
    private String filepath = "";
    private boolean flag;
    private final int CAMERA_PIC_REQUEST = 100;
    Bitmap bitmap;
    String cameraOrCrop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        resultView = (ImageView) findViewById(R.id.result_image);
        cameraOrCrop = getIntent().getExtras().getString("value");
        if (cameraOrCrop.equals("CROP")) {
            Crop.pickImage(this);
        } else if (cameraOrCrop.equals("CAMERA")) {
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_crop, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select) {
            resultView.setImageDrawable(null);
            Crop.pickImage(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());

        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);

            upload();

        } else if (requestCode == CAMERA_PIC_REQUEST && resultCode==RESULT_OK) {
            bitmap = (Bitmap) result.getExtras().get("data");
            filepath = saveImageFile(bitmap);
            upload();
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);

    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            resultView.setImageURI(Crop.getOutput(result));

            Uri imageUri = Crop.getOutput(result);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                filepath = saveImageFile(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImageFile(Bitmap bitmap) {
        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
    }

    private String getFilename() {
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "ArnabsMessenger");
        if (!file.exists()) {
            file.mkdirs();
        }
        String uriSting = (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".jpg");
        return uriSting;
    }

    public void upload() {
        StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
        if (filepath.equals("")) {
            //startActivity(new Intent(this, FriendListActivity.class));
            //finish();
            Toast.makeText(this, "Plz select a file to upload", Toast.LENGTH_SHORT).show();
        } else {
            final ProgressDialog progressDialog = new ProgressDialog(this, AlertDialog.THEME_HOLO_DARK);
            progressDialog.setMessage("Uploading.....");
            progressDialog.setCancelable(false);
            progressDialog.show();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/enrique.png";
            StorageReference child = mStorageReference.child("image").child("" + AmessengerApplication.appfirebaseUser.getUid());// System.currentTimeMillis());

            final Uri uri = Uri.fromFile(new File(filepath));
            child.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Toast.makeText(FriendListActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    //File file = new File(Environment.getExternalStorageDirectory().getPath(), "TestFolder");
                    //if(file.exists())
                    //file.delete();
                    new File(filepath).delete();
                    progressDialog.dismiss();
                    startActivity(new Intent(CropActivity.this, FriendListActivity.class));
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("msg", e.toString());
                    Log.e("msg", e.toString());
                    new File(filepath).delete();
                    progressDialog.dismiss();
                    Toast.makeText(CropActivity.this, "failed" + e, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, FriendListActivity.class));
        finish();
    }

   /* private void checkPermission() {
        int var = Build.VERSION.SDK_INT;
        if (var >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    if (flag) {
                        Snackbar.make(findViewById(R.id.result_image), "Storage permission is required to open Images.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("OK", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        flag = false;
                                        checkPermission();
                                    }
                                }).show();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
                    }
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST);
                }
            }
        } else
            Crop.pickImage(this);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //  Toast.makeText(this, "yay", Toast.LENGTH_SHORT).show();
                    Crop.pickImage(this);
                } else {
                    //Toast.makeText(this, "boo", Toast.LENGTH_SHORT).show();
                    flag = true;
                    checkPermission();
                }
            }
        }
    }*/
}
