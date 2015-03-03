package com.andersoncarvalho.lddm.mobile;

import android.content.ContentResolver;
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
    private AutoCompleteTextView autoComplete;
    private String Ident;
    private Button ligar, mandarEmail, mapa;
    String numero;
    String [] emailContato;
    String endereco;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listaContatos = new ArrayList<Map<String, String>>();
        montarLista();
        autoComplete = (AutoCompleteTextView) findViewById(R.id.mmWhoNo);
        ligar=(Button) findViewById(R.id.ligar);
        mandarEmail=(Button) findViewById(R.id.email);
        mandarEmail.setEnabled(false);
        mapa=(Button) findViewById(R.id.mapa);
        mapa.setEnabled(false);
        mAdapter = new SimpleAdapter(this, listaContatos, R.layout.custcontview,
                new String[] { "Nome", "Telefone" }, new int[] {
                R.id.ccontName, R.id.ccontNo });
        autoComplete.setAdapter(mAdapter);
         Ident = "";
        endereco = "";
        autoComplete.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View arg1, int index,
                                    long arg3) {
                Map<String, String> map = (Map<String, String>) av.getItemAtPosition(index);

                String name  = map.get("Nome");
                numero = map.get("Telefone");
                Ident = map.get("Id");
                autoComplete.setText(name+" " +numero);
                emailContato = null;
                endereco = "";
                lerDadosContato(Ident);
                
                

            }
        });

        ligar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, numero.toString(), Toast.LENGTH_SHORT).show();
                try {
                    if (autoComplete != null) {
                        startActivity(new Intent(Intent.ACTION_CALL, Uri
                                .parse("tel:" +numero)));
                    }else if(autoComplete != null && numero.toString().length()==0){
                        Toast.makeText(getApplicationContext(), "You missed to type the numero!", Toast.LENGTH_SHORT).show();
                    }else if(autoComplete != null && autoComplete.getText().length()<10){
                        Toast.makeText(getApplicationContext(), "Check whether you entered correct numero!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("DialerAppActivity", "error: " + e.getMessage(),
                            e);
                }
            }
        });
        mandarEmail.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (autoComplete != null) {
                        if(emailContato != null) {
                            Intent intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                            intent.putExtra(Intent.EXTRA_EMAIL, emailContato);
                            Log.d("Tentando enviar email para", emailContato[0]);
                            if (intent.resolveActivity(getPackageManager()) != null) {
                                startActivity(intent);
                            }
                        }
                    }
                } catch (Exception e) {
                    Log.e("DialerAppActivity", "error: " + e.getMessage(),
                            e);
                }
            }
        });
        mapa.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(endereco));
                    Log.d("Tentando ir para o endereco", endereco);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
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
                Cursor phones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,
                        null, null);
                while (phones.moveToNext()){
                    String phonenumero = phones.getString(
                            phones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Map<String, String> NamePhoneType = new HashMap<String, String>();
                    NamePhoneType.put("Nome", contactName);
                    NamePhoneType.put("Telefone", phonenumero);
                    NamePhoneType.put("Id", contactId);
                    listaContatos.add(NamePhoneType);
                }
                phones.close();
            }
        }
        people.close();
//        startManagingCursor(people);
    }


    public void lerDadosContato(String ident) {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                if (ident.equals(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)))) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        System.out.println("name : " + name + ", ID : " + id);
                        Log.d("NOME", name);

                        // Pegar numero do telefone
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phone = pCur.getString(
                                    pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            System.out.println("phone" + phone);
                        }
                        pCur.close();


                        // pegar email e tipo

                        Cursor emailCur = cr.query(
                                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        mandarEmail.setEnabled(false);
                        while (emailCur.moveToNext()) {
                            // This would allow you get several email addresses
                            // if the email addresses were stored in an array
                            String email = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                            String emailType = emailCur.getString(
                                    emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                            mandarEmail.setEnabled(true);
                            if(emailContato == null) {
                                emailContato = new String[] { email };
                                System.out.println("Email " + email + " Email Type : " + emailType);
                            }
                        }
                        emailCur.close();

                        // Pegar observacoes
                        String noteWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                        String[] noteWhereParams = new String[]{id,
                                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                        Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, noteWhere, noteWhereParams, null);
                        if (noteCur.moveToFirst()) {
                            String note = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                            System.out.println("Note " + note);
                        }
                        noteCur.close();

                        // Pegar endereço

                        String addrWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                        String[] addrWhereParams = new String[]{id,
                                ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE};
                        Cursor addrCur = cr.query(ContactsContract.Data.CONTENT_URI,
                                null, null, null, null);
                        mapa.setEnabled(false);
                        while (addrCur.moveToNext()) {
                            String poBox = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
                            String street = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                            String city = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                            String state = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                            String postalCode = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
                            String country = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                            String type = addrCur.getString(
                                    addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
                            if(street != null && city != null && state != null) {
                                mapa.setEnabled(true);
                                endereco = "geo:0,0?q=" + street + "+" + city + "+" + state;
                            }
                            // Do something with these....

                        }
                        addrCur.close();

                        // Get Instant Messenger.........
                        String imWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                        String[] imWhereParams = new String[]{id,
                                ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE};
                        Cursor imCur = cr.query(ContactsContract.Data.CONTENT_URI,
                                null, imWhere, imWhereParams, null);
                        if (imCur.moveToFirst()) {
                            String imName = imCur.getString(
                                    imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
                            String imType;
                            imType = imCur.getString(
                                    imCur.getColumnIndex(ContactsContract.CommonDataKinds.Im.TYPE));
                        }
                        imCur.close();

                        // Get Organizations.........

                        String orgWhere = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                        String[] orgWhereParams = new String[]{id,
                                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
                        Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
                                null, orgWhere, orgWhereParams, null);
                        if (orgCur.moveToFirst()) {
                            String orgName = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                            String title = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                        }
                        orgCur.close();
                    }
                }
            }
        }
    }

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