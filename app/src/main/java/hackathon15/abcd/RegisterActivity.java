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

public class RegisterActivity extends AppCompatActivity {

    EditText email, password, cnfpassword, fname, lname, phone;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        email = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        cnfpassword = (EditText) findViewById(R.id.cnfpassword);
        fname = (EditText) findViewById(R.id.first_name);
        lname = (EditText) findViewById(R.id.last_name);
        phone = (EditText) findViewById(R.id.phone);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    public void gotoLogin(View view) {
        Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(i);
    }

    /* lets try and register */
    public void attemptRegister(View view) {
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
        String u = email.getText().toString();
        String p = password.getText().toString();
        String cp = cnfpassword.getText().toString();
        String f = fname.getText().toString();
        String l = lname.getText().toString();
        String ph = phone.getText().toString();
        if (u.length() > 0 && p.length() > 0 && cp.length() > 0 && f.length() > 0 && l.length() > 0 && ph.length() > 0) {
            new RegistrationHandlerTask().execute(u, p, cp, f, l, ph);
        } else {
            Toast t = Toast.makeText(RegisterActivity.this, "fill_required_fields", Toast.LENGTH_LONG);
            t.setText("Please fill all fields.");
            t.show();
        }
    }


    private class RegistrationHandlerTask extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialog;
        private int status = -1;
        private boolean success = false;
        private boolean passwordsMatch = true;

        @Override
        protected String doInBackground(String... params) {
            String u = params[0];
            String p = params[1];
            String cp = params[2];
            String f = params[3];
            String l = params[4];
            String ph = params[5];
            String response = "";
            if (!cp.equals(p)) {
                passwordsMatch = false;
                return "";
            }
            String urlParams = "email="+u+"&password="+p+"&first_name="+f+"&last_name="+l+"&phone="+ph;

            //progressDialog = ProgressDialog.show(RegisterActivity.this, "Registering ... ", "");
            //progressDialog.setCancelable(false);
            /* post it to site, and get back data, plus validate */
            try {
                HTTPHelper httpHelper = new HTTPHelper();
                response = httpHelper.post(LoginActivity.url+"users/register", urlParams);
                status = httpHelper.response_code;
                JSONObject jsonObject = new JSONObject(response);
                success = jsonObject.get("success").toString().equals("true");
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
            //if (progressDialog.isShowing()) progressDialog.dismiss();
            if (res == null) return;
                /* show a toast that you're not doing it good enough */
            if (!passwordsMatch) {
                Toast t = Toast.makeText(RegisterActivity.this, "passwords_not_matching", Toast.LENGTH_LONG);
                t.setText("Passwords don't match.");
                t.show();
            } else if (success) {
                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            } else {
                Toast t = Toast.makeText(RegisterActivity.this, "response", Toast.LENGTH_LONG);
                t.setText("Registration Failed !!");
                t.show();
            }
        }

    }

}
