package com.example.contentprovider;

import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.example.contentprovider.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends AppCompatActivity {

    private ListView contactsListView;
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contactactivity);

        contactsListView = findViewById(R.id.ListView);
        showContacts();
    }

    private void showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[] { Manifest.permission.READ_CONTACTS }, PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts();
            } else {
                Toast.makeText(this, "Permission denied to read your Contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getContacts() {
        List<Map<String, String>> contactsList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                Map<String, String> contactMap = new HashMap<>();
                contactMap.put("name", "Name: " + name);

                if (Integer.parseInt(
                        cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor phoneCursor = getContentResolver().query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[] { id },
                            null);

                    while (phoneCursor.moveToNext()) {
                        String phone = phoneCursor
                                .getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contactMap.put("phone", "Phone: " + phone);
                    }
                    phoneCursor.close();
                }
                contactsList.add(contactMap);
            }
            cursor.close();

            // Create a SimpleAdapter to display the data in the ListView
            SimpleAdapter adapter = new SimpleAdapter(this, contactsList,
                    android.R.layout.simple_list_item_2,
                    new String[] { "name", "phone" },
                    new int[] { android.R.id.text1, android.R.id.text2 });
            contactsListView.setAdapter(adapter);

        } else {
            // If no contacts are found, you could display a message
            Toast.makeText(this, "No contacts found.", Toast.LENGTH_SHORT).show();
        }
    }
}