package taweesak.com.myappcontactsnotify.Fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;


import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import taweesak.com.myappcontactsnotify.Activity.EditActivity;
import taweesak.com.myappcontactsnotify.Activity.MainActivity;
import taweesak.com.myappcontactsnotify.R;

public class FragmentEdit extends Fragment {

    private EditText editFirstName, editLastName, editPhone, editEmail, editLineid;
    private ImageView imageView;
    //    private CheckBox favorite;
    private Button btnSave;
    int flag = 0;
    public static final int PICK_IMAGE = 1;

    Uri selectedImage = null;
    CheckBox editFavorite;


    public FragmentEdit() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);

        imageView = (ImageView) view.findViewById(R.id.edit_image);
        byte[] bytes = EditActivity.currentContact.getImage();
        Log.d("FRAGMENT-EDIT: Bytes: ", "" + bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imageView.setImageBitmap(bitmap);

        editFirstName = (EditText) view.findViewById(R.id.edit_first_name);
        editLastName = (EditText) view.findViewById(R.id.edit_last_name);
        editPhone = (EditText) view.findViewById(R.id.edit_phone);
        editEmail = (EditText) view.findViewById(R.id.edit_email);
        editLineid = (EditText) view.findViewById(R.id.edit_line);
        btnSave = (Button) view.findViewById(R.id.edit_save);
        editFavorite = (CheckBox) view.findViewById(R.id.edit_favorite);

        editFirstName.setText(EditActivity.currentContact.getFirstName());
        editLastName.setText(EditActivity.currentContact.getLastName());
        editPhone.setText(EditActivity.currentContact.getPhone());
        editEmail.setText(EditActivity.currentContact.getEmail());
        editLineid.setText(EditActivity.currentContact.getLine());

        final int fav = EditActivity.currentContact.getFavorite();

        if(fav == 1) {
            editFavorite.setChecked(true);
        }
        else {
            editFavorite.setChecked(false);
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int num = editFavorite.isChecked()?1:0;

                MainActivity.myDb.updateData(EditActivity.currentContact.getId(), editFirstName.getText().toString(), editLastName.getText().toString(), editEmail.getText().toString(),
                        editPhone.getText().toString(), num, imageView.getDrawable(), editLineid.getText().toString());

                EditActivity.currentContact.setFirstName(editFirstName.getText().toString());
                EditActivity.currentContact.setLastName(editLastName.getText().toString());
                EditActivity.currentContact.setPhone(editPhone.getText().toString());
                EditActivity.currentContact.setEmail(editEmail.getText().toString());
                //EditActivity.currentContact.setFavorite(EditActivity.currentContact.getFavorite());
                EditActivity.currentContact.setFavorite(num);
                EditActivity.currentContact.setLine(editLineid.getText().toString());

                //Log.d("EDIT location: ", "" + EditActivity.currentContact.getLocation());

                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] imageInByte = baos.toByteArray();
                EditActivity.currentContact.setImageInByte(imageInByte);


                MainActivity.getThemAll();
                Intent i = new Intent(getActivity(), MainActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_IMAGE) {
            imageView.setImageResource(R.drawable.ic_person);
            Log.d("PICK_IMAGE", " IS NOT TRIGGERED");
        }

        if (requestCode == PICK_IMAGE && null != data) {
            selectedImage = data.getData();

            CropImage.activity(selectedImage)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4, 4)
                    //.start(getActivity());
                    .start(getActivity(), FragmentEdit.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            flag = 1;
            selectedImage = data.getData();
            ParcelFileDescriptor parcelFD = null;

            Uri resultUri = result.getUri();
            //imageView.setImageURI(resultUri);
            selectedImage = resultUri;


            try {
                parcelFD = getActivity().getContentResolver().openFileDescriptor(selectedImage, "r");
                FileDescriptor imageSource = parcelFD.getFileDescriptor();

                // Decode image size
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(imageSource, null, o);

                // the new size we want to scale to
                //final int REQUIRED_SIZE = 1024;
                final int REQUIRED_SIZE = 512; // Test Size Image ******************

                // Find the correct scale value. It should be the power of 2.
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                while (true) {
                    if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE) {
                        break;
                    }
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }

                // decode with inSampleSize
                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(imageSource, null, o2);

                imageView.setImageBitmap(bitmap);
                imageView.setTag(bitmap);
                Log.d("ChkImage image:", imageView.toString());


            } catch (FileNotFoundException e) {
                // handle errors
                e.printStackTrace();
            } /*catch (IOException e) {
                e.printStackTrace();
                // handle errors
            }*/ finally {
                if (parcelFD != null)
                    try {
                        parcelFD.close();
                    } catch (IOException e) {
                        // ignored
                    }
            }
        }
    }

}
