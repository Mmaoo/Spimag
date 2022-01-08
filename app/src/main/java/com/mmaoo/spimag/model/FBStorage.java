package com.mmaoo.spimag.model;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class FBStorage implements Storage{

    public static final String PATH_USER = "/users";
    public static final String PATH_AREA_BACKGROUND = "/background";
    public static final String PATH_AREA = "/areas";
    public static final String NAME_AREA_BACKGROUND = "background";

    FirebaseStorage firebaseStorage = null;

    public FBStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
    }

    @Override
    public Task<Bitmap> getAreaBackground(String areaId) {
        String uid = AppUser.getInstance().getUid();
        StorageReference storageReference = firebaseStorage.getReference().child(PATH_USER).child(uid)
                .child(PATH_AREA).child(areaId).child(PATH_AREA_BACKGROUND).child(NAME_AREA_BACKGROUND);
        Command<byte[],Bitmap> command = new Command<byte[], Bitmap>() {
            @Override
            public Bitmap run(byte[] bytes) {
                Log.d(this.getClass().toString(),"run command");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                return bitmap;
            }
        };
        GetDataTask<Bitmap> task = new GetDataTask<>(storageReference,command);
        return task;
    }

    @Override
    public Task<Bitmap> putAreaBackground(String areaId, Uri uri) {
        String uid = AppUser.getInstance().getUid();
        StorageReference storageReference = firebaseStorage.getReference().child(PATH_USER).child(uid)
                .child(PATH_AREA).child(areaId).child(PATH_AREA_BACKGROUND).child(NAME_AREA_BACKGROUND);
        storageReference.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d(this.getClass().toString(),"upload file "+task.isSuccessful());
            }
        });
        return null;
    }



    /**
     * Universal task to get data from firebase database
     * @param <T> - result type
     */
    private class GetDataTask<T> extends Task<T>{

        boolean isComplete = false;
        boolean isSuccessfull = false;
        boolean isCanceled = false;
        Exception exception = null;

        Command command = null;
        T result;

        ArrayList<OnSuccessListener> onSuccessListeners = new ArrayList<>();
        ArrayList<OnFailureListener> onFailtureListeners = new ArrayList<>();
        ArrayList<OnCompleteListener> onCompleteListeners = new ArrayList<>();
        ArrayList<OnCanceledListener> onCanceledListeners = new ArrayList<>();

        /**
         * @param storageReference - reference to data in storage
         */
        public GetDataTask(StorageReference storageReference,Command<byte[],T> command){
//          //query.addListenerForSingleValueEvent(new FBDatabase.GetDataTask.ValueListener(this,command));
            this.command = command;
            final GetDataTask<T> t = this;
            storageReference.getBytes(1024*1024)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            if(command != null) result = command.run(bytes);
                            isSuccessfull = true;
                            for(OnSuccessListener listener : onSuccessListeners) listener.onSuccess(result);
                        }
                    }).addOnCanceledListener(new OnCanceledListener() {
                        @Override
                        public void onCanceled() {
                            isCanceled = true;
                            for(OnCanceledListener listener : onCanceledListeners) listener.onCanceled();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            exception = e;
                            for(OnFailureListener listener : onFailtureListeners) listener.onFailure(e);
                        }
                    }).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                        @Override
                        public void onComplete(@NonNull Task<byte[]> task) {
                            isComplete = task.isComplete();
                            for(OnCompleteListener listener : onCompleteListeners) listener.onComplete(t);
                        }
                    });
        }

        @Override
        public boolean isComplete() {
            return isComplete;
        }

        @Override
        public boolean isSuccessful() {
            return isSuccessfull;
        }

        @Override
        public boolean isCanceled() {
            return isCanceled;
        }

        @Nullable
        @Override
        public T getResult() {
            return result;
        }

        @Nullable
        @Override
        public <X extends Throwable> T getResult(@NonNull Class<X> aClass) throws X {
            return result;
        }

        @Nullable
        @Override
        public Exception getException() {
            return exception;
        }

        @NonNull
        @Override
        public Task<T> addOnSuccessListener(@NonNull OnSuccessListener<? super T> onSuccessListener) {
            onSuccessListeners.add(onSuccessListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnSuccessListener(@NonNull Executor executor, @NonNull OnSuccessListener<? super T> onSuccessListener) {
            onSuccessListeners.add(onSuccessListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnSuccessListener(@NonNull Activity activity, @NonNull OnSuccessListener<? super T> onSuccessListener) {
            onSuccessListeners.add(onSuccessListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnFailureListener(@NonNull OnFailureListener onFailureListener) {
            onFailtureListeners.add(onFailureListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnFailureListener(@NonNull Executor executor, @NonNull OnFailureListener onFailureListener) {
            onFailtureListeners.add(onFailureListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnFailureListener(@NonNull Activity activity, @NonNull OnFailureListener onFailureListener) {
            onFailtureListeners.add(onFailureListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCompleteListener(@NonNull OnCompleteListener<T> onCompleteListener) {
            onCompleteListeners.add(onCompleteListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCompleteListener(@NonNull Executor executor, @NonNull OnCompleteListener<T> onCompleteListener) {
            onCompleteListeners.add(onCompleteListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCompleteListener(@NonNull Activity activity, @NonNull OnCompleteListener<T> onCompleteListener) {
            onCompleteListeners.add(onCompleteListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCanceledListener(@NonNull OnCanceledListener onCanceledListener) {
            onCanceledListeners.add(onCanceledListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCanceledListener(@NonNull Executor executor, @NonNull OnCanceledListener onCanceledListener) {
            onCanceledListeners.add(onCanceledListener);
            return this;
        }

        @NonNull
        @Override
        public Task<T> addOnCanceledListener(@NonNull Activity activity, @NonNull OnCanceledListener onCanceledListener) {
            onCanceledListeners.add(onCanceledListener);
            return this;
        }
    }
}
