package com.kmsoft.contacts_demo;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class AddNewContacts extends AppCompatActivity {

    ImageView person_photo;
    EditText person_name, person_number;
    Button btn, btn1, btn2;
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_CONTACTS};
    private static final int CAMERA_REQUEST = 100;
    Bitmap bitmap;
    String path;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_contacts);

        person_photo = findViewById(R.id.person_photo);
        person_name = findViewById(R.id.person_name);
        person_number = findViewById(R.id.person_number);
        btn = findViewById(R.id.btn);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);

        email = getIntent().getStringExtra("Email");

        btn.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            } else {
                String name = person_name.getText().toString();
                String number = person_number.getText().toString();

                addDeviceContacts(this, name, number, bitmap);
            }
        });

        btn1.setOnClickListener(v -> {
            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(AddNewContacts.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            } else {
                String name = person_name.getText().toString();
                String number = person_number.getText().toString();
                ContentResolver contentResolver = getContentResolver();

                addSimContacts(contentResolver, name, number);
            }
        });

        btn2.setOnClickListener(v -> {

            if (!allPermissionsGranted()) {
                ActivityCompat.requestPermissions(AddNewContacts.this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
            } else {
                String name = person_name.getText().toString();
                String number = person_number.getText().toString();
                ContentResolver contentResolver = getContentResolver();

                addEmailContacts(contentResolver, name, number, email, bitmap);
            }
        });

        person_photo.setOnClickListener(v -> cameraPermission());
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

    private boolean allPermissionsGranted() {
        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addDeviceContacts(Context context, String displayName, String phoneNumber, Bitmap photo) {
        ContentResolver contentResolver = context.getContentResolver();
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        int rawContactInsertIndex = 0;

        // Add contact display name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        // Add contact phone number
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                .build());

        // Add contact display name
        ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
                .build());

        if (photo != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, rawContactInsertIndex)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray)
                    .build());
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
            Toast.makeText(context, "Contact added successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSimContacts(ContentResolver contentResolver, String contactName, String contactNumber) {
        Uri simUri = Uri.parse("content://icc/adn");

        ContentValues values = new ContentValues();
        values.put("tag", contactName);
        values.put("number", contactNumber);

        try {
            contentResolver.insert(simUri, values);
            Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to add contact", Toast.LENGTH_SHORT).show();
        }
    }

    private void addEmailContacts(ContentResolver contentResolver, String contactName, String contactNumber, String email, Bitmap contactPhoto) {
        ContentValues values = new ContentValues();
        values.put(ContactsContract.RawContacts.ACCOUNT_TYPE, "com.google");
        values.put(ContactsContract.RawContacts.ACCOUNT_NAME, email);

        // Insert a new raw contact
        Uri rawContactUri = contentResolver.insert(ContactsContract.RawContacts.CONTENT_URI, values);
        assert rawContactUri != null;
        long rawContactId = ContentUris.parseId(rawContactUri);

        // Insert contact name
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, contactName);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);

        // Insert contact phone number
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
        values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER, contactNumber);
        contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);

        // Insert contact photo
        if (contactPhoto != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            contactPhoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID, rawContactId);
            values.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, byteArray);
            contentResolver.insert(ContactsContract.Data.CONTENT_URI, values);
        }

        Toast.makeText(this, "Contact added successfully", Toast.LENGTH_SHORT).show();
    }
}
