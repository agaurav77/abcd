package hackathon15.abcd;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

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
        Bundle extras = i.getExtras();
        email_field.setText(extras.get("email").toString());
        first_name_field.setText(extras.get("first_name").toString());
        last_name_field.setText(extras.get("last_name").toString());
        phone_field.setText(extras.get("phone").toString());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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
}
