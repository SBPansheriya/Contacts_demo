package com.kmsoft.contacts_demo;

import static com.kmsoft.contacts_demo.Constant.phoneTypeArrayList;
import static com.kmsoft.contacts_demo.Constant.typeArrayList;
import static com.kmsoft.contacts_demo.Constant.usersArrayList;
import static com.kmsoft.contacts_demo.Constant.usersEmailArrayList;
import static com.kmsoft.contacts_demo.Constant.usersPhoneArrayList;
import static com.kmsoft.contacts_demo.Constant.usersSimArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DrawerAdapter.DrawerItemClickedListener {

    private DrawerLayout mDrawerLayout;
    RecyclerView recyclerView;
    Button add;
    private RecyclerView mDrawerRecyclerView;
    ImageView homeThree;
    private ActionBarDrawerToggle mDrawerToggle;
    DrawerAdapter mDrawerAdapter;
    NewRecyclerAdapter newRecyclerAdapter;
    List<DrawerItem> mDrawerItems = new ArrayList<>();
    private static final int REQUEST_CODE_PERMISSIONS = 101;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.GET_ACCOUNTS, Manifest.permission.READ_CONTACTS};
    String email;
    String tag;
    List<String> emailAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();

        if (!allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        } else {
            getSimContacts();
            getEmailAddresses(this);
            getContactList();
        }

        mDrawerItems.add(new DrawerItem(getString(R.string.all_contacts), usersArrayList.size()));
        mDrawerItems.add(new DrawerItem(getString(R.string.phone), usersPhoneArrayList.size()));
        for (String email : emailAddress) {
            int contactCount = countContactsForEmail(usersArrayList, email);
            mDrawerItems.add(new DrawerItem(email, contactCount));
        }
        mDrawerItems.add(new DrawerItem(getString(R.string.sim), usersSimArrayList.size()));

        mDrawerRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDrawerAdapter = new DrawerAdapter(this, mDrawerItems, this);
        mDrawerRecyclerView.setAdapter(mDrawerAdapter);

        View headerView = LayoutInflater.from(this).inflate(R.layout.drawer_header, mDrawerRecyclerView, false);
        mDrawerAdapter.addHeaderView(headerView);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        homeThree.setOnClickListener(view -> mDrawerLayout.openDrawer(GravityCompat.START));

        add.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddNewContacts.class);
            intent.putExtra("Email", email);
            startActivity(intent);
        });
    }

    private int countContactsForEmail(ArrayList<Users> usersList, String email) {
        int count = 0;
        for (Users user : usersList) {
            if (user.getAccountName().contains(email)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClicked(DrawerItem drawerItem) {
        String itemName = drawerItem.getName();
        if (itemName.equals(getString(R.string.all_contacts))) {
            tag = getString(R.string.all_contacts);
        } else if (itemName.equals(getString(R.string.phone))) {
            tag = getString(R.string.phone);
        } else if (emailAddress.contains(itemName)) {
            tag = getString(R.string.email);
            usersEmailArrayList = new ArrayList<>();
            for (Users user : usersArrayList) {
                if (user.getAccountName().contains(itemName)) {
                    usersEmailArrayList.add(user);
                }
            }
        } else if (itemName.equals(getString(R.string.sim))) {
            tag = getString(R.string.sim);
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        Display();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    private void init() {
        homeThree = findViewById(R.id.home_three);
        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerRecyclerView = findViewById(R.id.drawer_list);
        recyclerView = findViewById(R.id.contacts_recyclerview);
        add = findViewById(R.id.add);
    }

    private void getEmailAddresses(Context context) {
        emailAddress = new ArrayList<>();
        Account[] accounts = AccountManager.get(context).getAccounts();

        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                emailAddress.add(account.name);
            }
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
                getContactList();
                getSimContacts();
                getEmailAddresses(this);
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @SuppressLint("Range")
    private void getContactList() {
        usersArrayList = new ArrayList<>();
        usersEmailArrayList = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String phoneName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                String givenName = "";
                String familyName = "";
                Cursor nameCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME},
                        ContactsContract.Data.CONTACT_ID + "=? AND " +
                                ContactsContract.Data.MIMETYPE + "=?",
                        new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE},
                        null);
                if (nameCursor != null && nameCursor.moveToFirst()) {
                    givenName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
                    familyName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
                    nameCursor.close();
                }

                List<String> phoneNumbers = getPhoneNumbers(contentResolver, contactId);
                String phoneNumber = "";
                String officeNumber = "";
                if (phoneNumbers.size() > 0) {
                    if (phoneNumbers.size() >= 2) {
                        phoneNumber = phoneNumbers.get(0).replaceAll(" ", "").trim();
                        officeNumber = phoneNumbers.get(1).replaceAll(" ", "").trim();
                    } else {
                        phoneNumber = phoneNumbers.get(0).replaceAll(" ", "").trim();
                        officeNumber = "";
                    }
                }

                List<String> accountName = retrieveAccountName(contactId);
                List<String> accountType = retrieveAccountType(contactId);

                phoneTypeArrayList = getAllPhoneNumbers(this, contactId);

                Users user = new Users(contactId, photoUri, phoneName, givenName, familyName, phoneTypeArrayList, phoneNumber, officeNumber, accountType, accountName);
                usersArrayList.add(user);

                usersPhoneArrayList = new ArrayList<>();
                for (Users users : usersArrayList) {
                    for (String originalString : users.accountType) {
                        String[] parts = originalString.split("\\.");
                        String lastPart = parts[parts.length - 1].toLowerCase();
                        String otherStringLower = "Phone".toLowerCase();
                        if (lastPart.equals(otherStringLower)) {
                            usersPhoneArrayList.add(users);
                        }
                    }
                }
            }
            cursor.close();
        }
        Display();
    }

    public ArrayList<Phone> getAllPhoneNumbers(Context context, String contactId) {

        ContentResolver contentResolver = context.getContentResolver();

        String[] projection = {
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL};

        String selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionArgs = {contactId};

        Cursor cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection, selection, selectionArgs, null);

        phoneTypeArrayList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                @SuppressLint("Range") String phoneNumber = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                @SuppressLint("Range") int phoneType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                String type = mapTypeToCustomLabel(phoneType);

                Phone phone = new Phone(phoneNumber, phoneType, type);
                phoneTypeArrayList.add(phone);

            } while (cursor.moveToNext());

            cursor.close();
        }
        return phoneTypeArrayList;
    }

    private List<String> getPhoneNumbers(ContentResolver contentResolver, String contactId) {
        List<String> phoneNumbers = new ArrayList<>();

        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] phoneProjection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
        String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        Cursor phoneCursor = contentResolver.query(phoneUri, phoneProjection, phoneSelection, new String[]{contactId}, null);

        if (phoneCursor != null) {
            while (phoneCursor.moveToNext()) {
                @SuppressLint("Range") String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)).trim();
                phoneNumbers.add(phoneNumber);
            }
            phoneCursor.close();
        }
        return phoneNumbers;
    }

    public static String mapTypeToCustomLabel(int phoneType) {

        typeArrayList = new ArrayList<>();

        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE, "Mobile"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_HOME, "Home"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK, "Work"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_MAIN, "Main"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK, "Work Fax"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME, "Home Fax"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE, "Work Mobile"));
        typeArrayList.add(new PhoneType(ContactsContract.CommonDataKinds.Phone.TYPE_PAGER, "Pager"));
        typeArrayList.add(new PhoneType(-1, "Other"));

        switch (phoneType) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                return "Mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                return "Home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                return "Work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN:
                return "Main";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK:
                return "Work Fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME:
                return "Home Fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER:
                return "Pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                return "Work Mobile";
            default:
                return "Other";
        }
    }

    private void getSimContacts() {

        Uri simUri = Uri.parse("content://icc/adn");
        Cursor simCursor = getContentResolver().query(simUri, null, null, null, null);

        if (simCursor != null && simCursor.getCount() > 0) {
            while (simCursor.moveToNext()) {
                @SuppressLint("Range") String contactId = simCursor.getString(simCursor.getColumnIndex("_id"));
                @SuppressLint("Range") String name = simCursor.getString(simCursor.getColumnIndex("name"));
                @SuppressLint("Range") String phoneNumber = simCursor.getString(simCursor.getColumnIndex("number"));

                String firstName = "";
                String lastName = "";
                if (!TextUtils.isEmpty(name)) {
                    if (name.contains(" ")) {
                        String[] separated = name.split(" ");
                        firstName = separated[0];
                        lastName = separated[1];
                    } else {
                        firstName = name;
                        lastName = "";
                    }
                }

                Users user = new Users(contactId, null, name, firstName, lastName, null, phoneNumber, null, null, null);
                usersSimArrayList.add(user);
            }
            simCursor.close();
        }
    }

//    @SuppressLint("Range")
//    private void getDeviceContacts() {
//        ContentResolver contentResolver = getContentResolver();
//
//        String[] projection = new String[]{
//                ContactsContract.Contacts._ID,
//                ContactsContract.Contacts.DISPLAY_NAME,
//                ContactsContract.Contacts.PHOTO_URI
//        };
//
//        String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + " = ?";
//        String[] selectionArgs = new String[]{"1"};
//
//        Cursor cursor = contentResolver.query(
//                ContactsContract.Contacts.CONTENT_URI,
//                projection,
//                selection,
//                selectionArgs,
//                null);
//
//        if (cursor != null && cursor.getCount() > 0) {
//            while (cursor.moveToNext()) {
//                @SuppressLint("Range") String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
//                @SuppressLint("Range") String contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//                @SuppressLint("Range") String photoUri = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));
//                ArrayList<Phone> phoneArrayList = getAllPhoneNumbers(this, contactId);
//
//                String givenName = "";
//                String familyName = "";
//                Cursor nameCursor = contentResolver.query(ContactsContract.Data.CONTENT_URI,
//                        new String[]{ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
//                                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME},
//                        ContactsContract.Data.CONTACT_ID + "=? AND " +
//                                ContactsContract.Data.MIMETYPE + "=?",
//                        new String[]{contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE},
//                        null);
//                if (nameCursor != null && nameCursor.moveToFirst()) {
//                    givenName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
//                    familyName = nameCursor.getString(nameCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
//                    nameCursor.close();
//                }
//
//                List<String> phoneNumbers = getPhoneNumbers(contentResolver, contactId);
//                String phoneNumber = "";
//                String officeNumber = "";
//                if (phoneNumbers.size() > 0) {
//                    if (phoneNumbers.size() >= 2) {
//                        phoneNumber = phoneNumbers.get(0).replaceAll(" ", "").trim();
//                        officeNumber = phoneNumbers.get(1).replaceAll(" ", "").trim();
//                    } else {
//                        phoneNumber = phoneNumbers.get(0).replaceAll(" ", "").trim();
//                        officeNumber = "";
//                    }
//                }
//
//                Users user = new Users(contactId, photoUri, contactName, givenName, familyName, phoneArrayList, phoneNumber, officeNumber, null,null);
//                usersPhoneArrayList.add(user);
//            }
//            cursor.close();
//        }
//    }

    @SuppressLint("Range")
    private List<String> retrieveAccountName(String contactId) {
        List<String> accountName = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_NAME
                },
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{contactId},
                null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String accountName1 = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));

                if (accountName1 != null && !accountName.contains(accountName1)) {
                    accountName.add(accountName1);
                }
            }
            cursor.close();
        }
        return accountName;
    }

    @SuppressLint("Range")
    private List<String> retrieveAccountType(String contactId) {
        List<String> accountType = new ArrayList<>();
        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(
                ContactsContract.RawContacts.CONTENT_URI,
                new String[]{
                        ContactsContract.RawContacts._ID,
                        ContactsContract.RawContacts.ACCOUNT_TYPE
                },
                ContactsContract.RawContacts.CONTACT_ID + "=?",
                new String[]{contactId},
                null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String accountType1 = cursor.getString(cursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));

                if (accountType1 != null && !accountType.contains(accountType1)) {
                    accountType.add(accountType1);
                }
            }
            cursor.close();
        }
        return accountType;
    }

    private void Display() {
        if (TextUtils.equals(tag, "All Contacts")) {
            if (usersArrayList.size() > 0) {

                Comparator<Users> nameComparator = Comparator.comparing(Users::getFullName);
                usersArrayList.sort(nameComparator);

                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                newRecyclerAdapter = new NewRecyclerAdapter(this, usersArrayList);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(newRecyclerAdapter);
            } else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        } else if (TextUtils.equals(tag, "Phone")) {
            if (usersPhoneArrayList.size() > 0) {

                Comparator<Users> nameComparator = Comparator.comparing(Users::getFullName);
                usersPhoneArrayList.sort(nameComparator);

                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                newRecyclerAdapter = new NewRecyclerAdapter(this, usersPhoneArrayList);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(newRecyclerAdapter);
            } else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        } else if (TextUtils.equals(tag, "Email")) {
            if (usersEmailArrayList.size() > 0) {

                Comparator<Users> nameComparator = Comparator.comparing(Users::getFullName);
                usersEmailArrayList.sort(nameComparator);

                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                newRecyclerAdapter = new NewRecyclerAdapter(this, usersEmailArrayList);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(newRecyclerAdapter);
            } else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        } else if (TextUtils.equals(tag, "Sim")) {
            if (usersSimArrayList.size() > 0) {

                Comparator<Users> nameComparator = Comparator.comparing(Users::getFullName);
                usersSimArrayList.sort(nameComparator);

                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                newRecyclerAdapter = new NewRecyclerAdapter(this, usersSimArrayList);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(newRecyclerAdapter);
            } else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (usersArrayList.size() > 0) {

                Comparator<Users> nameComparator = Comparator.comparing(Users::getFullName);
                usersArrayList.sort(nameComparator);

                LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
                newRecyclerAdapter = new NewRecyclerAdapter(this, usersArrayList);
                recyclerView.setLayoutManager(manager);
                recyclerView.setAdapter(newRecyclerAdapter);
            } else {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}