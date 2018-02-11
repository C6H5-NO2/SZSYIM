package org.szesmaker.szsyim;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.util.HashMap;
import java.util.Map;
public class EditActivity extends AppCompatActivity {
    private HashMap<String, String> cookies;
    private EditText newParticipants, newTitle, newMessage;
    private CoordinatorLayout container;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        cookies = (HashMap<String, String>) getIntent().getSerializableExtra("cookies");
        container = (CoordinatorLayout) this.findViewById(R.id.editContainer);
        newParticipants = (EditText) this.findViewById(R.id.newParticipants);
        newParticipants.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override public void onFocusChange(View view, boolean focus) {
                if (focus)
                    newParticipants.setHint("separate with English comma");
                else
                    newParticipants.setHint("");
            }
        });
        newTitle = (EditText) this.findViewById(R.id.newTitle);
        newMessage = (EditText) this.findViewById(R.id.newMessage);
        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.sendMsg);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                new newMessage("https://chengjiyun.com/gdsyxx/?q=messages/new", cookies, container,  "Generating...").execute();
            }
        });
    }
    private class newMessage extends Post {
        private Connection.Response response;
        public newMessage(String url, HashMap<String, String> cookies, View container, String notification) {
            super(url, cookies, container, notification);
        }
        @Override protected Map<String, String> parseData(Document doc) {
            Map<String, String> map = new HashMap<>();
            map.put("recipient", newParticipants.getText().toString());
            map.put("subject", newTitle.getText().toString());
            map.put("body[value]", newMessage.getText().toString());
            map.put("form_build_id", doc.select("input[name=form_build_id]").first().attr("value"));
            map.put("form_token", doc.select("input[name=form_token]").first().attr("value"));
            map.put("form_id", "privatemsg_new");
            map.put("op", "发送消息");
            return map;
        }
        @Override protected boolean respondAnalysis(Connection.Response response) {
            this.response = response;
            return !Jsoup.parse(response.body()).select("div.status").isEmpty();
        }
        @Override protected void postExecute(boolean flag) {
            if (flag) {
                Intent intent = new Intent(EditActivity.this, ChatActivity.class);
                intent.putExtra("html", response.body());
                intent.putExtra("cookies", cookies);
                startActivity(intent);
                EditActivity.this.finish();
            }
            else {
                Snackbar snackbar = Snackbar.make(container, "Generated failed", Snackbar.LENGTH_SHORT);
                ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(Color.RED);
                snackbar.show();
            }
        }
    }
}
