package com.example.dell.crop02;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    ImageView imageView;

    private final static int CAMERA_REQUEST_CODE= 200;
    private final static int STORAGE_REQUEST_CODE= 400;
    private final static int IMAGE_PICK_GALLERY_CODE= 1000;
    private final static int IMAGE_PICK_CAMERA_CODE= 1001;

    String cameraPermission[];
    String storagePermission[];

    Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar= getSupportActionBar();
        actionBar.setSubtitle("click + button to insert image");

        editText= findViewById(R.id.editTextId);
        imageView= findViewById(R.id.imageViewId);

        //camera permission
        cameraPermission= new String[]{Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};

        //storage permission
        storagePermission= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

    }

    //action bar menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //handle action bar

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //int id= item.getGroupId();
        if(item.getItemId()==R.id.addImage){
            showImageImport();
        }
        if(item.getItemId()==R.id.setting){
            Toast.makeText(this,"Setting",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showImageImport() {
        String[] item= {"Camera"," Gallery"};

        AlertDialog.Builder dialoge = new AlertDialog.Builder(this);
        dialoge.setTitle("Select Image");
        dialoge.setItems(item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if(which==0){
                    //camera option clicked
                    if(!checkCameraPermission()){
                        //camera permission nor allowd, request it
                        requestCameraPermission();
                    }
                    else {
                        //permission allowd, take picture
                        pickCamera();
                    }
                }

                if(which==1){
                    //gallery option clicked
                    if(!checkStoragePermission()){
                        //storage permission nor allowd, request it
                        requestStoragePermission();
                    }
                    else {
                        //permission allowd, take picture
                        pickGallery();

                    }
                }
            }
        });
        dialoge.create().show();
    }

    private void pickGallery() {
        //intent to pick image from gallery
        Intent intent= new Intent(Intent.ACTION_PICK);
        //set Intent type to image
        intent.setType("image/*");
        startActivityForResult(intent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera() {
        //Intent to take image from camera and it also saved storage
        ContentValues values= new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"NewPic");//title of pic
        values.put(MediaStore.Images.Media.DESCRIPTION,"Image to text");//Description
        image_uri= getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);
        Intent cameraIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);

    }

    private boolean checkStoragePermission() {
        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }

    private boolean checkCameraPermission() {

        boolean result= ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1= ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
                if(grantResults.length>0){
                   boolean cameraAccecpted= grantResults[0]==
                   PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccecpted= grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;

                    if(cameraAccecpted && writeStorageAccecpted){
                        pickCamera();
                    }
                }
                else {
                    Toast.makeText(this,"Permission Denide",Toast.LENGTH_SHORT).show();
                }
                break;
            case STORAGE_REQUEST_CODE:
                if(grantResults.length>0){

                    boolean writeStorageAccecpted= grantResults[0]==
                            PackageManager.PERMISSION_GRANTED;

                    if(writeStorageAccecpted){
                        pickGallery();
                    }
                }
                else {
                    Toast.makeText(this,"Permission Denide",Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    //handle image result

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode==RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                //got image from galary now crop it
                CropImage.activity(data.getData())   //enable image guidline
                        .setGuidelines(CropImageView.Guidelines.ON).start(this);

            }
            if(requestCode==IMAGE_PICK_CAMERA_CODE){
                //got image from camera now crop it
                CropImage.activity(image_uri)   //enable image guidline
                        .setGuidelines(CropImageView.Guidelines.ON).start(this);
            }
        }

        //get cropped image
        if(requestCode ==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE){
            CropImage.ActivityResult result= CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){
                Uri resultUri= result.getUri(); //get image URI
                //set image to ImageView
                imageView.setImageURI(resultUri);

                //get drawebal bitmap for text recognise

                BitmapDrawable bitmapDrawable= (BitmapDrawable) imageView.getDrawable();
                Bitmap bitmap= bitmapDrawable.getBitmap();

                TextRecognizer recognizer= new TextRecognizer.Builder(getApplicationContext()).build();

                if(!recognizer.isOperational()){
                    Toast.makeText(this,"Error",Toast.LENGTH_SHORT).show();
                }
                else {
                    Frame frame= new Frame.Builder().setBitmap(bitmap).build();
                    SparseArray<TextBlock> items= recognizer.detect(frame);
                    StringBuilder builder= new StringBuilder();

                    //get text from stringbulder until there is no text

                    for(int i=0;i<items.size();i++){

                        TextBlock myItem= items.valueAt(i);
                        builder.append(myItem.getValue());
                        builder.append("\n");
                    }
                    //set text to edit text
                    editText.setText(builder.toString());
                }

            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE){
                //if there is any error show
                Exception error= result.getError();
                Toast.makeText(this,""+error,Toast.LENGTH_SHORT).show();
            }
        }

    }
}
