package com.example.akundu.arnabsmessenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendListActivity extends AppCompatActivity {

    private final String TAG = FriendListActivity.class.getSimpleName();

    private ListView friendListView;
    private ArrayList<String> frndId = new ArrayList<>();
    private ProgressBar progressBar;
    private FirebaseListAdapter firebaseListAdapter;
    private String filepath = "";
    private StorageReference mStorageReference, mDownloadStorageReference;
    private boolean doubleBackToExitPressedOnce = false;
    private HashMap<String, Bitmap> U_id_Image_HashMap = new HashMap<>();
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_list);

        if (AmessengerApplication.appfirebaseUser == null) {
            Intent intent = new Intent(FriendListActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            mStorageReference = FirebaseStorage.getInstance().getReference();
            mDownloadStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://arnabsmassanger.appspot.com");
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo").child(AmessengerApplication.appfirebaseAuth.getCurrentUser().getUid());
            Map<String, Object> hasMap = new HashMap<>();
            hasMap.put("online", "online");
            databaseReference.updateChildren(hasMap);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    progressBar.setVisibility(View.GONE);
                    friendListView.setVisibility(View.VISIBLE);
                    Map<String, String> stringStringMap = (Map<String, String>) dataSnapshot.getValue();
                    Log.d("msg " + TAG, stringStringMap.get("name"));
                    AmessengerApplication.username = stringStringMap.get("name");

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            DatabaseReference friendDatabaseReference = FirebaseDatabase.getInstance().getReference().child("AllUserInfo");
            friendListView = (ListView) findViewById(R.id.friend_list);

            firebaseListAdapter = new FirebaseListAdapter<Friend>(this, Friend.class, R.layout.row_friend_list, friendDatabaseReference) {
                @Override
                protected void populateView(View v, final Friend model, int position) {


                    final CircleImageView profilePic = (CircleImageView) v.findViewById(R.id.profile_pic);
                    Bitmap bitmap = U_id_Image_HashMap.get(model.getU_id());
                    if (bitmap == null) {
                        StorageReference islandRef = mDownloadStorageReference.child("image/" + model.getU_id());
                        final long ONE_MEGABYTE = 1024 * 1024;
                        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                // Data for "images/island.jpg" is returns, use this as needed
                                try {
                                    System.gc();
                                    Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    profilePic.setImageBitmap(bmp);
                                    U_id_Image_HashMap.put(model.getU_id(), bmp);
                                    bmp = null;
                                } catch (OutOfMemoryError e) {
                                    e.printStackTrace();
                                    Log.e("msg", "" + e);
                                    // handle gracefully.
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    } else {
                        profilePic.setImageBitmap(bitmap);
                    }

                    TextView name = (TextView) v.findViewById(R.id.cardviewname);
                    TextView address = (TextView) v.findViewById(R.id.cardviewaddress);
                    ImageView online = (ImageView) v.findViewById(R.id.online);
                    name.setText(model.getName());
                    address.setText(model.getAddress());
                    switch (model.getOnline()) {
                        case "online":
                            online.setImageResource(android.R.drawable.presence_online);
                            break;
                        case "offline":
                            online.setImageResource(android.R.drawable.presence_offline);
                            break;
                        default:
                            online.setImageResource(android.R.drawable.presence_away);
                            break;
                    }
                    frndId.add(model.getU_id());
                    if (AmessengerApplication.appfirebaseUser != null) {
                        if (model.getU_id().equals(AmessengerApplication.appfirebaseUser.getUid())) {
                            name.setTextColor(Color.GRAY);
                            address.setTextColor(Color.GRAY);
                        }
                    }
                }
            };
            friendListView.setAdapter(firebaseListAdapter);
            friendListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    TextView frndNameTextView = (TextView) view.findViewById(R.id.cardviewname);
                    //frndNameTextView.getText().toString();
                    Log.d("msgUID", AmessengerApplication.appfirebaseUser.getUid());
                    Log.d("msg frndId", frndId.get(i));
                    Log.d("msg counter", i + "");
                    if (frndId.get(i).equals(AmessengerApplication.appfirebaseUser.getUid())) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(FriendListActivity.this, R.style.MyDatePickerStyle);
                        builder.setMessage("Would you like to change Profile picture?");
                        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendListActivity.this, R.style.MyDatePickerStyle);
                                builder.setMessage("Select your option to upload");
                                builder.setPositiveButton("Take Picture", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(FriendListActivity.this, CropActivity.class);
                                        intent.putExtra("value", "CAMERA");
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                builder.setNegativeButton("Open Images", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent1 = new Intent(FriendListActivity.this, CropActivity.class);
                                        intent1.putExtra("value", "CROP");
                                        startActivity(intent1);
                                        finish();
                                    }
                                });
                                builder.show();
                            }
                        });
                        builder.setNegativeButton("No", null);
                        builder.show();
                    } else {
                        intent = new Intent(FriendListActivity.this, ListchatActivity.class);
                        intent.putExtra("frnd_id", frndId.get(i));
                        intent.putExtra("u_id", AmessengerApplication.appfirebaseUser.getUid());
                        intent.putExtra("frnd_name", frndNameTextView.getText().toString());
                        startActivity(intent);
                    }
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_friend_list, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this, R.style.MyDatePickerStyle);
        alertDialog.setMessage("Do you want to logout?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //HashMap<String, Object> hashMap = new HashMap<String, Object>();
                //hashMap.put("online", "offline");
                //databaseReference.updateChildren(hashMap);
                //databaseReference = null;
                //friendDatabaseReference = null;
                //firebaseListAdapter = null;
                AmessengerApplication.appfirebaseUser = null;
                startActivity(new Intent(FriendListActivity.this, LoginActivity.class));
                finish();
            }
        });
        alertDialog.setNegativeButton("No", null);
        alertDialog.show();
        /*Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_list);
        dialog.show();*/
        return super.onOptionsItemSelected(item);
    }

    //region For Image Upload from sd card to firebase storage
    public void uploadImage() {
        // To open up a gallery browser
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        //intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
        // To handle when an image is selected from the browser, add the following to your Activity
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                // currImageURI is the global variable I'm using to hold the content:// URI of the image
                Uri currImageURI = data.getData();
                Log.d("msg", data.getDataString());
                Log.d("msg", "" + getRealPathFromURI(currImageURI));
                File file = new File(getRealPathFromURI(currImageURI));

//File  file = new File(data.getDataString());
                if (file.exists()) {
                    if (file.length() / 1024 > 1024) {
                        Toast.makeText(this, "Please select less than 1MB size photo", Toast.LENGTH_SHORT).show();
                    } else {
                        filepath = file.getAbsolutePath();
                        Toast.makeText(this, "your file " + filepath, Toast.LENGTH_SHORT).show();
                        upload();
                    }
                } else {
                    System.out.println("File Not Found");
                }
            }
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        // can post image
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri,
                proj, // Which columns to return
                null, // WHERE clause; which rows to return (all rows)
                null, // WHERE clause selection arguments (none)
                null); // Order-by clause (ascending by name)
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void upload() {
        if (filepath.equals("")) {
            Toast.makeText(this, "select file", Toast.LENGTH_SHORT).show();
        } else {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            path += "/enrique.png";
            StorageReference child = mStorageReference.child("image").child("" + AmessengerApplication.appfirebaseUser.getUid());// System.currentTimeMillis());

            final Uri uri = Uri.fromFile(new File(filepath));
            child.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(FriendListActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("msg", e.toString());
                    Toast.makeText(FriendListActivity.this, "failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    //endregion


    @Override
    protected void onResume() {
        super.onResume();
        Log.d("msg FriendList", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("msg FriendList", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("msg FriendList", "onStop");

        firebaseListAdapter = null;


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("msg FriendList", "onDestroy");
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to EXIT", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }
}
