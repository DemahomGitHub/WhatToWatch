package com.toure.demahom.whattowatch.view;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.toure.demahom.whattowatch.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.transform.ErrorListener;

import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.entity.StringEntity;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import model.User;

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        btnRegister = (Button)findViewById(R.id.okRegisterBtnID);
        lnkCancel = (TextView)findViewById(R.id.cancelRegisterID);

        etEnteredEmail = (EditText)findViewById(R.id.etUserEmailID);
        etEnteredPassword = (EditText)findViewById(R.id.etUserPasswordID);
        etConfirmedPassword = (EditText) findViewById(R.id.etUserConfirmedPassword);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageRegisteringValidation();
            }
        });

        lnkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, MainActivity.class));
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void manageRegisteringValidation() {
        sEnteredEmail = etEnteredEmail.getText().toString();
        sEnteredPassword = etEnteredPassword.getText().toString();
        sConfirmedPassword = etConfirmedPassword.getText().toString();

        if (isValid())
            sendUserDataToDataBase();
        else
            Toast.makeText(getApplicationContext(),"Invalid information",Toast.LENGTH_LONG).show();

    }

    public void sendUserDataToDataBase() {
        progressDialog.show();
        (new HttpPostAsyncTask()).execute(MY_URL);
    }
    public boolean isValid() {
        if (sEnteredEmail.length() > 1 && sEnteredPassword.length() > 1 && sConfirmedPassword.length() > 1) {
            if (sEnteredPassword.equals(sEnteredPassword))
                return true;
            return false;
        }
        return false;
    }

    public String POST(String url, User nUser){
        InputStream inputStream = null;
        String result = "";
        try {

            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();
            jsonObject.accumulate("email",nUser.getUsername() );
            jsonObject.accumulate("password", nUser.getPassword());
            jsonObject.accumulate("dateDeNaissance", null);

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
            else
                result = "Registration failed";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        // 11. return result
        return result;
    }
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;

    }

    public class HttpPostAsyncTask extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... params) {
            User nUser = new User(sEnteredEmail,sEnteredPassword,null);

            return POST(params[0],nUser);
        }
        protected void onPostExecute(String result) {
            boolean isRegistered=false;
            progressDialog.dismiss();
            try {
                JSONObject jsonObject = new JSONObject(result);
                String email = jsonObject.getString("email");
                String password = jsonObject.getString("password");
                if (email.equals(sEnteredEmail) && password.equals(sEnteredPassword))
                    isRegistered = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (isRegistered) {
                Toast.makeText(getBaseContext(), "You have been successful registered", Toast.LENGTH_LONG).show();
                etEnteredEmail.getText().clear();
                etEnteredPassword.getText().clear();
                etConfirmedPassword.getText().clear();
            }
            else
                Toast.makeText(getBaseContext(), "registration failed", Toast.LENGTH_LONG).show();

        }
    }
    /*
    private static Key generateKey() throws Exception {
        Key key = new SecretKeySpec(keyValue, ALGORITHM);
        return key;
    }

    public String encrypt(String valueToEnc) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encValue = c.doFinal(valueToEnc.getBytes());
        String encryptedValue = new BASE64Encoder().encode(encValue);
        return encryptedValue;
    }

    public String decrypt(String encryptedValue) throws Exception {
        Key key = generateKey();
        Cipher c = Cipher.getInstance(ALGORITHM);
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = new BASE64Decoder().decodeBuffer(encryptedValue);
        byte[] decValue = c.doFinal(decordedValue);
        String decryptedValue = new String(decValue);
        return decryptedValue;
    } */

    private Button btnRegister;
    private TextView lnkCancel;
    private EditText etEnteredEmail;
    private EditText etEnteredPassword;
    private EditText etConfirmedPassword;
    private String sEnteredEmail;
    private String sEnteredPassword;
    private String sConfirmedPassword;
    private final String MY_URL =  "http://whattowatchapi.azurewebsites.net/api/users";
    private ProgressDialog progressDialog;
    //private static final byte[] keyValue = new byte[] { 'T', 'h', 'i', 's', 'I', 's', 'A', 'S', 'e', 'c', 'r', 'e', 't', 'K', 'e', 'y' };
    //private static final String ALGORITHM = "AES";
}
