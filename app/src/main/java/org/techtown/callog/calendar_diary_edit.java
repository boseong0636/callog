package org.techtown.callog;

import static android.app.Activity.RESULT_OK;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class calendar_diary_edit extends Fragment {
    private ImageView imageView;
    private ProgressBar progressBar;
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference("Image");
    private final StorageReference reference = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private EditText Intext;
    private String day;
    private TextView dateTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.calendar_diary_edit, container, false);
        Button strBtn = rootView.findViewById(R.id.button2);
        progressBar = rootView.findViewById(R.id.progressBar);
        imageView = rootView.findViewById(R.id.PictureOut);
        Intext = rootView.findViewById(R.id.ContentsOutput2);
        dateTextView=rootView.findViewById(R.id.dateTextView);

        if (getArguments() != null)
        {
            day = getArguments().getString("date");// ???????????????1?????? ????????? ??? ??????
            dateTextView.setText(day);
        }





        //?????????????????? ?????????
        progressBar.setVisibility(View.INVISIBLE);
        //????????? ?????? ??????
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/");
                activityResult.launch(galleryIntent);
            }
        });
        //???????????? ??????
        strBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //???????????? ?????????
                if(imageUri != null){
                    uploadToFirebase(imageUri);
                }else{
                    //???????????? ?????????
                }
            }
        });

        return rootView;
    }
    //?????? ????????????
    ActivityResultLauncher<Intent> activityResult = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null)
                    {
                        imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri);
                    }
                }
            });
    //?????????????????? ????????? ?????????
    private void uploadToFirebase(Uri uri){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //????????? ????????? ????????????
        if (user != null) {
            String email = user.getEmail();
            //StorageReference fileRef =reference.child(System.currentTimeMillis() + "." + email + getFileExtension(uri));
            StorageReference fileRef =reference.child(email + "." + day);
            fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>(){
                        public void onSuccess(Uri uri){
                            String intext = Intext.getText().toString();
                            Model model =new Model(uri.toString() , intext); //uri
                            String modelld = root.push().getKey(); //???

                            root.child(user.getUid()).child(day).setValue(model);
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    progressBar.setVisibility(View.VISIBLE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }

    }
    //???????????? ????????????
    private String getFileExtension(Uri uri){
        ContentResolver cr = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cr.getType(uri));

    }
}
