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
    private String Ident, numero, endereco;
    private Button ligar, mandarEmail, mapa;
    String [] emailContato;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listaContatos = new ArrayList<Map<String, String>>();
        montarLista();
        autoComplete = (AutoCompleteTextView) findViewById(R.id.autoComplete);
        ligar=(Button) findViewById(R.id.ligar);
        mandarEmail=(Button) findViewById(R.id.email);
        mandarEmail.setEnabled(false);
        mapa=(Button) findViewById(R.id.mapa);
        mapa.setEnabled(false);
        mAdapter = new SimpleAdapter(this, listaContatos, R.layout.contato,
                new String[] { "Nome", "Telefone" }, new int[] {
                R.id.nomeCont, R.id.numeroCont });
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
                autoComplete.setText(name + " " +numero);
                emailContato = null;
                endereco = null;
                lerDadosContato(Ident);
            }
        });

        ligar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    if (autoComplete != null) {
                        startActivity(new Intent(Intent.ACTION_CALL, Uri
                                .parse("tel:" +numero)));
                    }else if(autoComplete != null && numero.toString().length()==0){
                        Toast.makeText(getApplicationContext(), "O Numero é invalido", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("Erro", "Erro ao tentar efetuar a ligacao " + e.getMessage(),
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
                    Log.e("Erro", "Erro ao enviar email " + e.getMessage(),
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
                    Log.e("Erro", "Erro ao enviar endereco para mapa " + e.getMessage(),
                            e);
                }
            }
        });
    }

    public void montarLista() {
        listaContatos.clear();
        Cursor contato = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        while (contato.moveToNext()) {
            String nomeContato = contato.getString(contato
                    .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String contatoId = contato.getString(contato
                    .getColumnIndex(ContactsContract.Contacts._ID));
            String temTelefone = contato
                    .getString(contato
                            .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

            if ((Integer.parseInt(temTelefone) > 0)){
                Cursor telefones = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contatoId,
                        null, null);
                while (telefones.moveToNext()){
                    String numeroTel = telefones.getString(
                            telefones.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                    Map<String, String> NomeTel = new HashMap<String, String>();
                    NomeTel.put("Nome", nomeContato);
                    NomeTel.put("Telefone", numeroTel);
                    NomeTel.put("Id", contatoId);
                    listaContatos.add(NomeTel);
                }
                telefones.close();
            }
        }
        contato.close();
    }


    public void lerDadosContato(String ident) {
        ContentResolver cr = getContentResolver();
                    // pegar email
                    Cursor emailCur = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                            new String[]{ident}, null);
                    mandarEmail.setEnabled(false);
                    while (emailCur.moveToNext()) {
                        String email = emailCur.getString(
                                emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        if(emailContato == null) {
                            mandarEmail.setEnabled(true);
                            emailContato = new String[] { email };
                            System.out.println("Email " + email);
                        }
                    }
                    emailCur.close();

                    // Pegar observacoes
//                        String obs = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
//                        String[] noteWhereParams = new String[]{id,
//                                ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
//                        Cursor obsCur = cr.query(ContactsContract.Data.CONTENT_URI, null, obs, noteWhereParams, null);
//                        if (obsCur.moveToFirst()) {
//                            String note = obsCur.getString(obsCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
//                            System.out.println("Observações: " + note);
//                        }
//                        obsCur.close();

                    // Pegar endereço
                    Cursor endCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            null, ContactsContract.Data.CONTACT_ID + " = ?",
                            new String[]{ident}, null);
                    mapa.setEnabled(false);
                    while (endCur.moveToNext()) {
                        String rua = endCur.getString(
                                endCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
                        String cidade = endCur.getString(
                                endCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
                        String estado = endCur.getString(
                                endCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
                        String pais = endCur.getString(
                                endCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
                        if(rua != null && cidade != null && estado != null && endereco == null) {
                            System.out.println(rua + cidade + estado);
                            mapa.setEnabled(true);
                            endereco = "geo:0,0?q=" + rua + "+" + cidade + "+" + estado;
                        }
                    }
                    endCur.close();



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