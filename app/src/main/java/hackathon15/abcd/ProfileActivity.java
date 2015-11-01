package hackathon15.abcd;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.w3c.dom.Text;

public class ProfileActivity extends AppCompatActivity {

    TextView email_field, first_name_field, last_name_field, phone_field;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        email_field = (TextView) findViewById(R.id.email);
        first_name_field = (TextView) findViewById(R.id.first_name);
        last_name_field = (TextView) findViewById(R.id.last_name);
        phone_field = (TextView) findViewById(R.id.phone);
        Intent i = getIntent();
        try {
            Bundle extras = i.getExtras();
            email_field.setText(extras.get("email").toString());
            first_name_field.setText(extras.get("first_name").toString());
            last_name_field.setText(extras.get("last_name").toString());
            phone_field.setText(extras.get("phone").toString());
        } catch(Exception e) {
            SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.prefs, 0);
            String id = sharedPreferences.getString("_id", "");
            new LoginHandlerTask().execute("", "", id);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    /* log me out !! */
    public void attemptLogout(View view) {
        ProgressDialog progressDialog = ProgressDialog.show(ProfileActivity.this, "Signing out ...", "");
        progressDialog.setCancelable(false);
        SharedPreferences sharedPreferences = getSharedPreferences(LoginActivity.prefs, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("_id");
        editor.commit();
        Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
        startActivity(i);
        if (progressDialog.isShowing()) progressDialog.dismiss();
    }

    public void reportGarbage(View view) {
        Intent i = new Intent(ProfileActivity.this, ReportActivity.class);
        startActivity(i);
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

        private int status = -1;
        private boolean success = false;

        /* the real devil */
        @Override
        protected String doInBackground(String... params) {
            String username = params[0];
            String password = params[1];
            String id = params[2];
            String response = "";
            String urlParams = "username="+username+"&password="+password;

            /* post it to site, and get back data, plus validate */
            try {
                HTTPHelper httpHelper = new HTTPHelper();
                response = httpHelper.post(LoginActivity.url+"users/id", "id="+id);
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
            /* I intend to show the profile !! */
            if (!success) {
                Intent i = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            } else {
                try {
                    JSONObject jsonObject = new JSONObject(res);
                    JSONObject credentials = jsonObject.getJSONObject("user");
                    email_field.setText(credentials.get("email").toString());
                    first_name_field.setText(credentials.get("first_name").toString());
                    last_name_field.setText(credentials.get("last_name").toString());
                    phone_field.setText(credentials.get("phone").toString());
                } catch (Exception e) {
                    Log.d("[Exception]", e.toString());
                }
            }
        }

    }

}
