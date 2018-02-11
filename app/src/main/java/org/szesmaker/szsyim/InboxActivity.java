package org.szesmaker.szsyim;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class InboxActivity extends AppCompatActivity {
    private CoordinatorLayout container;
    private HashMap<String, String> cookies;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        container = (CoordinatorLayout) this.findViewById(R.id.inboxContainer);
        final SwipeRefreshLayout srl = (SwipeRefreshLayout) this.findViewById(R.id.swipeRefreshLayout);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override public void onRefresh() {
                new Refresh().execute();
                srl.setRefreshing(false);
            }
        });
        FloatingActionButton fab = (FloatingActionButton) this.findViewById(R.id.addChat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                Intent intent = new Intent(InboxActivity.this, EditActivity.class);
                intent.putExtra("cookies", cookies);
                startActivity(intent);
            }
        });
        cookies = (HashMap<String, String>) getIntent().getSerializableExtra("cookies");
        initView(getData(Jsoup.parse(getIntent().getStringExtra("html"))));
    }
    private void initView(ArrayList<Map<String, String>> data) {
        RecyclerView rv = (RecyclerView) this.findViewById(R.id.chatList);
        RecyclerView.LayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv.setLayoutManager(manager);
        InboxAdapter adapter = new InboxAdapter(data);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListener(new InboxAdapter.OnRecyclerViewItemClickListener() {
            @Override public void onRecyclerViewItemClick(View view, HashMap<String, String> map) {
                new LoadMessage().execute(map.get("link"));
            }
        });
    }
    private ArrayList<Map<String, String>> getData(Document doc) {
        ArrayList<Map<String, String>> data = new ArrayList<>();
        Elements msgs = doc.select("tbody > tr");
        if (msgs.first().text().equals("没有站内信")) {
            Snackbar.make(container, "No message found", Snackbar.LENGTH_INDEFINITE).show();
        }
        else
            for (Element msg : msgs) {
                Map<String, String> map = new HashMap<>();
                Element info = msg.children().select("td.privatemsg-list-subject > a").first();
                map.put("link", info.attr("href"));
                map.put("title", info.text());
                info = msg.children().select("td.privatemsg-list-participants").first();
                map.put("participants", info.text());
                data.add(map);
            }
        return data;
    }
    private String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299";
    private class Refresh extends AsyncTask<Void, Void, Boolean> {
        Document doc;
        @Override protected Boolean doInBackground(Void... voids) {
            String url = "https://chengjiyun.com/gdsyxx/?q=messages&sort=desc&order=最后更新";
            try {
                doc = Jsoup.connect(url).userAgent(ua).cookies(cookies).followRedirects(true).timeout(3000).get();
            }
            catch (IOException e) {
                return false;
            }
            return true;
        }
        @Override protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            if (flag)
                initView(getData(doc)); //todo: notifyItemInserted
            else {
                Snackbar snackbar = Snackbar.make(container, "Refresh failed", Snackbar.LENGTH_SHORT);
                ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(Color.RED);
                snackbar.show();
            }
        }
    }
    private class LoadMessage extends AsyncTask<String, Void, Boolean> {
        Document doc;
        @Override protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(container, "Loading...", Snackbar.LENGTH_SHORT).show();
        }
        @Override protected Boolean doInBackground(String... url) {
            try {
                doc = Jsoup.connect(url[0]).userAgent(ua).cookies(cookies).followRedirects(true).timeout(3000).get();
            }
            catch (IOException e) {
                return false;
            }
            return true;
        }
        @Override protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            if (flag) {
                Intent intent = new Intent(InboxActivity.this, ChatActivity.class);
                intent.putExtra("html", doc.toString());
                intent.putExtra("cookies", cookies);
                startActivity(intent);
            }
            else {
                Snackbar snackbar = Snackbar.make(container, "Loading failed", Snackbar.LENGTH_SHORT);
                ((TextView) snackbar.getView().findViewById(R.id.snackbar_text)).setTextColor(Color.RED);
                snackbar.show();
            }
        }
    }
}
