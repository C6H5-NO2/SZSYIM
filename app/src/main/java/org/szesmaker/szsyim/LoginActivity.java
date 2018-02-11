package org.szesmaker.szsyim;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.HashMap;
import java.util.Map;
public class LoginActivity extends AppCompatActivity {
    private HashMap<String, String> cookies;
    private CoordinatorLayout container;
    private EditText username, password;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        container = (CoordinatorLayout) this.findViewById(R.id.loginContainer);
        username = (EditText) this.findViewById(R.id.username);
        password = (EditText) this.findViewById(R.id.password);
        Button signin = (Button) this.findViewById(R.id.signin);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                cookies = new HashMap<>();
                cookies.put("has_js", "1");
                new Login("https://chengjiyun.com/gdsyxx/?q=login&destination=messages", cookies, container, "Logging in...").execute();
            }
        });
    }
    private class Login extends Post {
        private Connection.Response response;
        public Login(String url, HashMap<String, String> cookies, View container, String notification) {
            super(url, cookies, container, notification);
        }
        @Override protected Map<String, String> parseData(Document doc) {
            Map<String, String> map = new HashMap<>();
            map.put("form_build_id", doc.select("input[name=form_build_id]").first().attr("value"));
            map.put("form_id", "school_users_login");
            map.put("op", "登录");
            map.put("password", password.getText().toString());
            map.put("username", username.getText().toString());
            return map;
        }
        @Override protected boolean respondAnalysis(Connection.Response response) {
            this.response = response;
            return Jsoup.parse(response.body()).title().startsWith("站内信");
        }
        @Override protected void postExecute(boolean flag) {
            if (flag) {
                Intent intent = new Intent(LoginActivity.this, InboxActivity.class);
                intent.putExtra("html", response.body());
                intent.putExtra("cookies", new HashMap<String, String>(response.cookies()));
                startActivity(intent);
            }
            else {
                Snackbar snackbar = Snackbar.make(container, "Login failed", Snackbar.LENGTH_SHORT);
                ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(Color.RED);
                snackbar.show();
            }
        }
    }
}
