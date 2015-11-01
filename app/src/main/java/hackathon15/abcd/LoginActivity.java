package hackathon15.abcd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class LoginActivity extends AppCompatActivity {

    EditText username, password;
    public static final String url = "http://172.16.16.57:3000/";
    public static final String prefs = "UserPreferences"; /* the shared preferences */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sharedPreferences = getSharedPreferences(prefs, 0);
        String id = "";
        try {
            id = sharedPreferences.getString("_id", "");
            if (!id.equals("")) {
                Log.d("[ID]", id);
                new LoginHandlerTask().execute("", "", id);
            } else {
                setContentView(R.layout.activity_login);
                username = (EditText) findViewById(R.id.username);
                password = (EditText) findViewById(R.id.password);
            }

        } catch (Exception e) {
            Log.d("[Exception]", e.toString());
        }
    }

    public void gotoRegister(View v) {
        Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(i);
    }

    /* try to login */
    public void attemptLogin(View v) {
        /* is network allowed ?? */
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            /* show a toast, we cannot use the internet right now */
            Toast t = Toast.makeText(this, "network_unavailable_message", Toast.LENGTH_SHORT);
            t.setText("Network Unavailable.");
            t.show();
            return;
            /* aka, do nothing */
        }
        String ustr = username.getText().toString();
        String pstr = password.getText().toString();
        if (ustr.length() > 0 && pstr.length() > 0) {
            new LoginHandlerTask().execute(ustr, pstr, "");
        } else {
            Toast t = Toast.makeText(LoginActivity.this, "fill_all_fields", Toast.LENGTH_LONG);
            t.setText("Please fill all fields.");
            t.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public class LoginHandlerTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;
        private int status = -1;
        private boolean success = false;
        private boolean mode = false;

        /* show a spinner, that you're trying */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(LoginActivity.this, "Signing in ...", "");
            progressDialog.setCancelable(false);
        }


        /* the real devil */
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String id = params[2];
            String response = "";
            String urlParams = "username="+username+"&password="+password;
            mode = id.equals("");

            /* post it to site, and get back data, plus validate */
            try {
                HTTPHelper httpHelper = new HTTPHelper();
                if (!mode) response = httpHelper.post(LoginActivity.this.url+"users/id", "id="+id);
                else response = httpHelper.post(LoginActivity.this.url+"userLogin", urlParams);
                status = httpHelper.response_code;
                JSONObject jsonObject = new JSONObject(response);
                success = jsonObject.get("success").toString().equals("true");
                if (!success) return null;
            } catch (Exception e) {
                Log.d("[Exception]", e.toString());
            }
            Log.d("[Response]", response);
            return response;
        }

        /* stop the spinner, it's done !! */
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            if (progressDialog.isShowing()) progressDialog.dismiss();

            /* I intend to show the profile !! */
            if ((mode && status == 200) || success) {
                Intent i = new Intent(LoginActivity.this, ProfileActivity.class);
                /* parse the response (res) */
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    JSONObject credentials = jsonObject.getJSONObject("user");
                    i.putExtra("email", credentials.get("email").toString());
                    i.putExtra("first_name", credentials.get("first_name").toString());
                    i.putExtra("last_name", credentials.get("last_name").toString());
                    i.putExtra("phone", credentials.get("phone").toString());
                    SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.this.prefs, 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("_id", credentials.get("_id").toString());
                    editor.commit();
                    Log.d("[Id]", sharedPreferences.getString("_id", ""));
                    startActivity(i);
                } catch (Exception e) {
                    Log.d("[Exception]", e.toString());
                }
                finish();
            } else {
                Toast t = Toast.makeText(LoginActivity.this, "response", Toast.LENGTH_LONG);
                t.setText("Login Failed !!");
                t.show();
                //setContentView(R.layout.activity_login);
            }
        }

    }

}