package hackathon15.abcd;

import android.content.Context;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class HTTPHelper {

    int response_code;

    HTTPHelper() {
        this.response_code = -1;
    }

    String post(String url, String data) throws Exception {
        byte[] postData = data.getBytes(Charset.forName("UTF-8"));
        URL urlobj = new URL(url);
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
        int status = conn.getResponseCode();
        response_code = status;
        return response;
    }

}
