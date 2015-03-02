package com.andersoncarvalho.lddm.mobile;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements OnItemClickListener {

    private ArrayList<Map<String, String>> listaContatos;
    private SimpleAdapter mAdapter;
    private AutoCompleteTextView numTel;
    private Button ligar;
    String numero;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listaContatos = new ArrayList<Map<String, String>>();
        montarLista();
        numTel = (AutoCompleteTextView) findViewById(R.id.mmWhoNo);
        ligar=(Button) findViewById(R.id.button1);
        mAdapter = new SimpleAdapter(this, listaContatos, R.layout.custcontview,
                new String[] { "Name", "Phone", "Type" }, new int[] {
                R.id.ccontName, R.id.ccontNo, R.id.ccontType });
        numTel.setAdapter(mAdapter);

        numTel.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);

                String name  = map.get("Name");
                numero = map.get("Phone");
                numTel.setText(name+" " +numero);
            }
        });

        ligar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, numero.toString(), Toast.LENGTH_SHORT).show();
                try {
                    if (numTel != null) {
                        startActivity(new Intent(Intent.ACTION_CALL, Uri
                                .parse("tel:" +numero)));
                    }else if(numTel != null && numero.toString().length()==0){
                        Toast.makeText(getApplicationContext(), "You missed to type the numero!", Toast.LENGTH_SHORT).show();
                    }else if(numTel != null && numTel.getText().length()<10){
                        Toast.makeText(getApplicationContext(), "Check whether you entered correct numero!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("DialerAppActivity", "error: " + e.getMessage(),
                            e);
                }
            }
        });
    }

    public void montarLista() {
        listaContatos.clear();
        Cursor people = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (people.moveToNext()) {
            String contactName = people.getString(people
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contactId = people.getString(people
                    .getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = people
                    .getString(people
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if ((Integer.parseInt(hasPhone) > 0)){
                // You know have the numero so now query it like this
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
                        null, null);
                while (phones.moveToNext()){
                    //store numeros and display a dialog letting the user select which.
                    String phonenumero = phones.getString(
                            phones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String numeroType = phones.getString(phones.getColumnIndex(
                            ContactsContract.CommonDataKinds.Phone.TYPE));
                    Map<String, String> NamePhoneType = new HashMap<String, String>();
                    NamePhoneType.put("Name", contactName);
                    NamePhoneType.put("Phone", phonenumero);
                    if(numeroType.equals("0"))
                        NamePhoneType.put("Type", "Work");
                    else
                    if(numeroType.equals("1"))
                        NamePhoneType.put("Type", "Home");
                    else if(numeroType.equals("2"))
                        NamePhoneType.put("Type",  "Mobile");
                    else
                        NamePhoneType.put("Type", "Other");
                    //Then add this map to the list.
                    listaContatos.add(NamePhoneType);
                }
                phones.close();
            }
        }
        people.close();
        startManagingCursor(people);
    }


/*public void onItemClick(AdapterView<?> av, View v, int index, long arg){
    Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);
    Iterator<String> myVeryOwnIterator = map.keySet().iterator();
    while(myVeryOwnIterator.hasNext()) {
        String key=(String)myVeryOwnIterator.next();
        String value=(String)map.get(key);
        numTel.setText(value);
    }
}*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

    }

}