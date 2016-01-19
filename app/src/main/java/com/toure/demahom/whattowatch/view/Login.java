package com.toure.demahom.whattowatch.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.toure.demahom.whattowatch.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import model.User;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait while connecting...");

        btnLogin = (Button)findViewById(R.id.loginPageLoginBntID);
        lnkCancel = (TextView)findViewById(R.id.cancelLoginID);

        etUsername = (EditText)findViewById(R.id.etLoginUserNameID);
        etPassword = (EditText)findViewById(R.id.etLoginPasswordID);

        lnkCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, MainActivity.class));
            }
        });
        setupLoginButtonListener();
        setupVirtualKeyboardListener();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void setupLoginButtonListener() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manageConnectionValidation();
            }
        });
    }

    public void manageConnectionValidation() {
        enteredEmail = etUsername.getText().toString();
        enteredPassword = etPassword.getText().toString();

        if (isValid(enteredEmail,enteredPassword)) {
            //progressDialog.show();
            (new GetUsersFromDataBaseTask()).execute(MY_URL);
            //startActivity(new Intent(Login.this, MyTabbedActivity.class));
        }
        else {
            Toast.makeText(getApplicationContext(), "Wrong information",Toast.LENGTH_LONG).show();
        }
    }

    public void setupVirtualKeyboardListener(){
        etPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event != null && keyCode == event.KEYCODE_ENTER)  {
                    manageConnectionValidation();
                    return true;
                }
                return false;
            }
        });
    }

    public boolean isValid(String username,String password) {

        return username.length() > 1 && password.length() > 1;
    }

    public class GetUsersFromDataBaseTask extends AsyncTask<String,String,ArrayList<User>> {

        public GetUsersFromDataBaseTask() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //progressDialog.show();
        }

        @Override
        protected ArrayList<User> doInBackground(String... params) {
            ArrayList<User> result = new ArrayList<>();
            try {
                createConnection(params[0]);
                reader = retrieveDataFromServer();
                readBuffer();
                result= parseJsonObject();

            } catch (MalformedURLException e) {
                Log.i("Exception: ", e.getMessage());
            } catch (IOException e) {
                Log.i("Exception: ", e.getMessage());
            }
            finally {
                closeConnection(connection,reader);
                return result;
            }
        }

        private ArrayList<User> parseJsonObject() {
            String jsonObjectString = buffer.toString();
            ArrayList<User> result = new ArrayList<>();

            try {
                JSONArray globalArray = new JSONArray(jsonObjectString);

                for (int i=0; i < globalArray.length(); i++) {
                    JSONObject jObject = globalArray.getJSONObject(i);
                    String email = jObject.getString("email");
                    String password = jObject.getString("password");
                    result.add(new User(email,password,null));
                }
            }
            catch (Exception e) {
                Log.i("Exception : ",e.getMessage());
            }
            finally {
                return result;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<User> result) {
            //super.onPostExecute(result);
            allUsers = result;
            connectToApplication();
            //progressDialog.dismiss();
        }

        private void createConnection(String link) throws IOException{
            URL url = new URL(link);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
        }

        private BufferedReader retrieveDataFromServer() throws IOException {
            InputStream stream = connection.getInputStream();

            return new BufferedReader(new InputStreamReader(stream));
        }

        private void readBuffer() throws IOException {
            buffer = new StringBuffer();
            String line="";
            while ((line=reader.readLine()) != null) {
                buffer.append(line);
            }
        }

        private void closeConnection(HttpURLConnection connection, BufferedReader reader) {
            if (connection != null) {
                connection.disconnect();
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.i("Connection Exception:", "No input Stream");
                    }
                }
            }
        }
        private HttpURLConnection connection = null;
        private BufferedReader reader = null;
        private StringBuffer buffer = null;
    }

    private void connectToApplication() {
        boolean isRegistered=false;
        String errorMessage = "Wrong email or password. Try again!";
        for (User user : allUsers) {
            if (user.getUsername().equals(enteredEmail) && user.getPassword().equals(enteredPassword)) {
                isRegistered=true;
                break;
            }
        }
        if (isRegistered) {
            Intent mIntent = new Intent(Login.this, MyTabbedActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putString("email",enteredEmail);
            mIntent.putExtra("userEmail",mBundle);
            startActivity(mIntent);
        }
        else {
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    private ArrayList <User> allUsers;
    private Button btnLogin;
    private TextView lnkCancel;
    private final String MY_URL = "http://whattowatchapi.azurewebsites.net/api/users";
    private ProgressDialog progressDialog;
    private String enteredEmail;
    private String enteredPassword;
    private EditText etUsername;
    private EditText etPassword;
}
