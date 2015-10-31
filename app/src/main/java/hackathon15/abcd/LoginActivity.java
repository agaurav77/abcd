package hackathon15.abcd;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
    final String url = "http://172.16.16.57:3000/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
    }

    /* try to login */
    public void attemptLogin(View v) {
        /* is network allowed ?? */
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        System.out.println("ABCD");
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
        new LoginHandlerTask().execute(ustr, pstr);

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

    private class LoginHandlerTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;

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
            String urlParams = "username="+username+"&password="+password;
            byte[] postData = urlParams.getBytes(Charset.forName("UTF-8"));
            int postLength = postData.length;

            /* post it to site, and get back data, plus validate */
            try {
                URL urlobj = new URL(LoginActivity.this.url+"userLogin");
                HttpURLConnection conn = (HttpURLConnection) urlobj.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setUseCaches(false);
                conn.getOutputStream().write(postData);
                Reader r = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                int ch = r.read();
                String response = "";
                while (ch != -1) {
                    response += (char)ch;
                    ch = r.read();
                }
                Log.d("[Response]", response);
                Log.d("[Code]", ""+conn.getResponseCode());
                if (conn.getResponseCode() == 200) {
                }

            } catch (Exception e) {
                Log.d("[Exception]", e.toString());
            }

            return null;
        }

        /* stop the spinner, it's done !! */
        @Override
        protected void onPostExecute(String res) {
            super.onPostExecute(res);
            if (progressDialog.isShowing()) progressDialog.dismiss();

            /* (TODO) start a new intent, if validated */
        }

    }

}