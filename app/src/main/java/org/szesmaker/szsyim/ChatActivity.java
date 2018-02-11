package org.szesmaker.szsyim;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class ChatActivity extends AppCompatActivity {
    private HashMap<String, String> cookies, params;
    private CoordinatorLayout container;
    private EditText msgSend;
    private Button btnSend;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = getIntent();
        cookies = (HashMap<String, String>) intent.getSerializableExtra("cookies");
        Document doc = Jsoup.parse(intent.getStringExtra("html"));
        this.setTitle(doc.select("h1#page-title").first().text());
        params = new HashMap<>();
        params.put("form_build_id", doc.select("input[name=form_build_id]").first().attr("value"));
        params.put("form_token", doc.select("input[name=form_token]").first().attr("value"));
        final String url = "https://chengjiyun.com" + doc.select("form#privatemsg-new").first().attr("action");
        // String participants = parseParticipants(doc.select("div.privatemsg-message-participants").first().text());
        container = (CoordinatorLayout) this.findViewById(R.id.sendContainer);
        msgSend = (EditText) this.findViewById(R.id.msgSend);
        btnSend = (Button) this.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                new sendMessage().execute(url);
            }
        });
        initView(getData(doc));
    }
    private String parseParticipants(String participants) {
        //todo: regex
        return participants;
    }
    private void initView(ArrayList<Map<String, String>> data) {
        RecyclerView rv = (RecyclerView) this.findViewById(R.id.msgList);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(manager);
        ChatAdapter adapter = new ChatAdapter(data);
        rv.setAdapter(adapter);
        rv.scrollToPosition(adapter.getItemCount() - 1);
    }
    private ArrayList<Map<String, String>> getData(Document doc) {
        ArrayList<Map<String, String>> data = new ArrayList<>();;
        Elements divs = doc.select("div#block-system-main > div").first().select("div.privatemsg-message");
        for (Element div : divs) {
            Map<String, String> map = new HashMap<>();
            map.put("from", div.select("span.privatemsg-author-name").first().text());
            map.put("time", div.select("span.privatemsg-message-date").first().text());
            map.put("message", parseMessage(div.select("div.privatemsg-message-body").first().html()));
            data.add(map);
        }
        return data;
    }
    private static String parseMessage(String html) {
        if (html == null)
            return html;
        String s = Jsoup.clean(html, "", Whitelist.none().addTags("br", "p"), new Document.OutputSettings().prettyPrint(true));
        //todo: I need a new HTML rendering engine
        s = s.replaceAll("<br> ", "\n");
        return Jsoup.clean(s, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false));
    }
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private ArrayList<Map<String, String>> data;
        public ChatAdapter(ArrayList<Map<String, String>> data) {
            this.data = data;
        }
        @Override public ChatAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
            return new ViewHolder(view);
        }
        @Override public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
            holder.sentTime.setText(data.get(position).get("time"));
            holder.msgDisplay.setText(data.get(position).get("message"));
            String sender = data.get(position).get("from");
            if (sender.equals("您")) {
                holder.msgContainer.setGravity(Gravity.RIGHT);
                holder.msgDisplay.setTextColor(Color.parseColor("#ff4081"));
                holder.sender.setText("");
            }
            else {
                holder.msgContainer.setGravity(Gravity.LEFT);
                holder.msgDisplay.setTextColor(Color.parseColor("#000000"));
                holder.sender.setText(sender);
            }
        }
        @Override public int getItemCount() {
            return data == null ? 0 : data.size();
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView sender, sentTime, msgDisplay;
            LinearLayout msgContainer;
            public ViewHolder(View item) {
                super(item);
                sender = (TextView) item.findViewById(R.id.sender);
                sentTime = (TextView) item.findViewById(R.id.sentTime);
                msgDisplay = (TextView) item.findViewById(R.id.msgDisplay);
                msgContainer = (LinearLayout) item.findViewById(R.id.msgContainer);
            }
        }
    }
    private class sendMessage extends AsyncTask<String, Void, Boolean> {
        private Connection.Response response;
        private Document doc;
        @Override protected void onPreExecute() {
            super.onPreExecute();
            msgSend.setEnabled(false);
            btnSend.setClickable(false);
        }
        @Override protected Boolean doInBackground(String... url) {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299";
            Connection connection = Jsoup.connect(url[0]).userAgent(ua).cookies(cookies).followRedirects(true).timeout(3000);
            HashMap<String, String> data = new HashMap<>();
            data.putAll(params);
            data.put("body[value]", msgSend.getText().toString());
            data.put("form_id", "privatemsg_new");
            data.put("op", "发送消息");
            connection.data(data);
            try {
                response = connection.method(Connection.Method.POST).execute();
            }
            catch (IOException e) {
                return false;
            }
            doc = Jsoup.parse(response.body());
            return !doc.select("div.status").isEmpty();
        }
        @Override protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            msgSend.setEnabled(true);
            btnSend.setClickable(true);
            if (flag) {
                msgSend.setText("");
                params.put("form_build_id", doc.select("input[name=form_build_id]").first().attr("value"));
                params.put("form_token", doc.select("input[name=form_token]").first().attr("value"));
                initView(getData(doc));
            }
            else {
                Snackbar snackbar = Snackbar.make(container, "Sent failed", Snackbar.LENGTH_SHORT);
                ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(Color.RED);
                snackbar.show();
            }
        }
    }
}
