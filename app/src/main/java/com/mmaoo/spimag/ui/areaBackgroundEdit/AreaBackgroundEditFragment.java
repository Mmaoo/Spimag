package com.mmaoo.spimag.ui.areaBackgroundEdit;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.mmaoo.spimag.BuildConfig;
import com.mmaoo.spimag.Navigable;
import com.mmaoo.spimag.R;
import com.mmaoo.spimag.model.AppStorage;
import com.mmaoo.spimag.model.Area;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class AreaBackgroundEditFragment extends Fragment {

    private static int REQUEST_IMAGE_CAPTURE = 101;

    View root;
    Navigable navigable;

    ImageView backgroundImageView;
    Button galleryButton;
    Button cameraButton;
    Button saveButton;

//    Area area;
    String areaId;

    Bitmap bitmap;
    Uri imageUri;

//    ActivityResultLauncher<Uri> takeAPhoto;

    ActivityResultLauncher<String> galleryResultLauncher;
    ActivityResultLauncher<Uri> cameraResultLauncher;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_area_background_edit, container, false);

        cameraResultLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if(result){
                    Log.d(this.getClass().toString(),result.toString());
                }
            }
        });

        galleryResultLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri result) {
                        if(result != null) {
                            imageUri = result;
                            Log.d(this.getClass().toString(), result.toString());
//                            backgroundImageView.setImageURI(result);
                            backgroundImageView.setVisibility(View.VISIBLE);
                            saveButton.setVisibility(View.VISIBLE);
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(),result);
//                                Log.d(this.getClass().toString(),bitmap.toString());
                                backgroundImageView.setImageBitmap(bitmap);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        backgroundImageView = root.findViewById(R.id.areaBackgroundImageView);
        galleryButton = root.findViewById(R.id.galleryButton);
        cameraButton = root.findViewById(R.id.cameraButton);
        saveButton = root.findViewById(R.id.saveButton);

        Bundle arguments = getArguments();
        if(arguments == null) navigable.navigateUp();
        try{
//            area = (Area) arguments.getSerializable("area");
            areaId = arguments.getString("areaId");
        }catch (ClassCastException e) {
            e.printStackTrace();
            navigable.navigateUp();
        }

        AppStorage.getInstance().getAreaBackground(areaId).addOnSuccessListener(new OnSuccessListener<Bitmap>() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                backgroundImageView.setImageBitmap(bitmap);
                backgroundImageView.setVisibility(View.VISIBLE);
            }
        });

        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryResultLauncher.launch("image/\\*");
            }
        });

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                takePhoto();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(this.getClass().toString(),"save clicked");
                AppStorage.getInstance().putAreaBackground(areaId,imageUri);
                navigable.navigateUp();
            }
        });
    }

//    private void takePhoto(){
//        String uuid = UUID.randomUUID().toString();
//        File outputDir = requireContext().getExternalCacheDir();
//        File file;
//        try{
//            file = File.createTempFile( uuid, ".jpg", outputDir );
////            file = File.createTempFile( uuid,"jpg");
//        } catch (IOException e) {
//            e.printStackTrace();
//            return;
//        }
//        cameraResultLauncher.launch(ClipData./*Uri.fromFile(file)*/);
//    }

    private void takePhoto()
    {
        String uuid = UUID.randomUUID().toString();
        File outputDir = requireContext().getExternalCacheDir();
        File file;
        Uri photoTakenUri;
        try
        {
            file = File.createTempFile( uuid, ".jpg", outputDir );
//            file = File.createTempFile( uuid,"jpg");
        }
        catch( IOException e )
        {
            e.printStackTrace();
            return;
        }

        try
        {
//            photoTakenUri = FileProvider.getUriForFile( Objects.requireNonNull(
//                    requireContext()),
//                    BuildConfig.APPLICATION_ID + ".fileProvider", file );
              photoTakenUri = FileProvider.getUriForFile(requireContext(),"com.mmaoo.spimag.fileProvider",file);
        }
        catch( IllegalArgumentException e )
        {
            e.printStackTrace();
            return;
        }

//        takeAPhoto = registerForActivityResult(
//                new ActivityResultContracts.TakePicture(), result ->
//                {
//                    Log.w(this.getClass().toString(),result.toString());
//                    if( !result  )
//                        return;
//
////                    Helpers.loadPicIntoGlide( ivItemImage, photoTakenUri );
//                    System.out.println(photoTakenUri.getPath());
////                    etImageName.setText( photoTakenUri.getPath() );
//                } );
        Log.d(this.getClass().toString(),"before takeAPhoto uri="+photoTakenUri);
//        takeAPhoto.launch( photoTakenUri );
        cameraResultLauncher.launch(photoTakenUri);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            navigable = (Navigable) context;
        }catch (ClassCastException e){
            Log.e(this.getClass().toString(),e.getMessage());
            Log.e(this.getClass().toString(), Arrays.toString(e.getStackTrace()));
        }
    }
}