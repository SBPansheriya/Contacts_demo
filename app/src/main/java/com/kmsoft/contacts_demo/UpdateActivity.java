package com.kmsoft.contacts_demo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class UpdateActivity extends AppCompatActivity {
    ImageView person_photo;
    EditText person_name, person_number;
    Button btn;
    Users users;
    private static final int CAMERA_REQUEST = 100;
    Bitmap bitmap;
    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        person_photo = findViewById(R.id.edit_person_photo);
        person_name = findViewById(R.id.edit_person_name);
        person_number = findViewById(R.id.edit_person_number);
        btn = findViewById(R.id.btn);

        users = (Users) getIntent().getSerializableExtra("User");

        if (!TextUtils.isEmpty(users != null ? users.image : null)) {
            Picasso.get().load(users.getImage()).into(person_photo);
        } else {
            person_photo.setImageResource(R.drawable.ic_launcher_foreground);
        }
        person_name.setText(users != null ? users.getFullName() : null);
        person_number.setText(users.getPersonPhone());

        btn.setOnClickListener(v -> {
            String name = person_name.getText().toString();
            String number = person_number.getText().toString();
            updateContact(this, Long.parseLong(users.getContactId()), name, number, path);
//            updateSimContact(getContentResolver(),name,number);
        });

        person_photo.setOnClickListener(v -> cameraPermission());
    }

    public void updateContact(Context context, long contactId, String displayName, String phoneNumber, String newImage) {
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

        // Update contact phone name
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        // Update contact phone number
        ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .build());

        ArrayList<ContentProviderOperation> ops1 = new ArrayList<>();
        ContentProviderOperation.Builder builder;

        if (newImage != null) {
            if (users.image != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(newImage));
                    ByteArrayOutputStream image = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, image);

                    builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
                    builder.withSelection(ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "=?", new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE});
                    builder.withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, image.toByteArray());
                    ops1.add(builder.build());
                    getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    ContentValues values = new ContentValues();
                    ContentResolver contentResolver3 = getContentResolver();

                    bitmap = null;

                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(newImage));
                    ByteArrayOutputStream image = new ByteArrayOutputStream();

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, image);
                    values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
                    values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                    values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, image.toByteArray());
                    contentResolver3.insert(ContactsContract.Data.CONTENT_URI, values);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
//         Update contact photo
//        if (newImage != null) {
//            if (users.getImage() != null) {
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] byteArray = stream.toByteArray();
//
//                ops.add(ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
//                        .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + "=?",
//                                new String[]{String.valueOf(contactId), ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE})
//                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray)
//                        .build());
//            } else {
//                ContentValues values = new ContentValues();
//                ContentResolver contentResolver3 = getContentResolver();
//
//                bitmap = null;
//
//                try {
//                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(newImage));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                ByteArrayOutputStream image = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, image);
//                values.put(ContactsContract.Data.RAW_CONTACT_ID, contactId);
//                values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
//                values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, image.toByteArray());
//                contentResolver3.insert(ContactsContract.Data.CONTENT_URI, values);
//            }
//        }
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(context, "Contact updated successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to update contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSimContact(ContentResolver contentResolver, String contactName, String contactNumber) {
        Uri simUri = Uri.parse("content://icc/adn");

        ContentValues values = new ContentValues();
        values.put("tag", contactName);
        values.put("number", contactNumber);

        String selection = "tag=?";
        String[] selectionArgs = new String[]{contactName};

        try {
            int rowsUpdated = contentResolver.update(simUri, values, selection, selectionArgs);
            if (rowsUpdated > 0) {
                Toast.makeText(this, "Contact updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Contact not found or update failed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to update contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void cameraPermission() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            bitmap = (Bitmap) data.getExtras().get("data");
            person_photo.setImageBitmap(bitmap);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "title", null);
        }
    }
}