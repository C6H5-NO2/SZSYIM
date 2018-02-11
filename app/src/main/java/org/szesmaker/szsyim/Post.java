package org.szesmaker.szsyim;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.view.View;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
public abstract class Post {
    private String url, notification;
    private HashMap<String, String> cookies;
    private View container;
    public Post(String url, HashMap<String, String> cookies, View container, String notification) {
        this.url = url;
        this.cookies = cookies;
        this.container = container;
        this.notification = notification;
    }
    protected void execute() {
        new PostMethod().execute();
    }
    protected abstract Map<String, String> parseData(Document doc);
    protected abstract boolean respondAnalysis(Connection.Response response);
    protected abstract void postExecute(boolean flag);
    private class PostMethod extends AsyncTask<Void, Void, Boolean> {
        @Override protected void onPreExecute() {
            super.onPreExecute();
            Snackbar.make(container, notification, Snackbar.LENGTH_LONG).show();
        }
        @Override protected Boolean doInBackground(Void... voids) {
            String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36 Edge/16.16299";
            Connection connection = Jsoup.connect(url).userAgent(ua).cookies(cookies).followRedirects(true).timeout(3000);
            Connection.Response response;
            try {
                response = connection.method(Connection.Method.GET).execute();
            }
            catch (IOException e) {
                return false;
            }
            connection.data(parseData(Jsoup.parse(response.body())));
            try {
                response = connection.method(Connection.Method.POST).execute();
            }
            catch (IOException e) {
                return false;
            }
            return respondAnalysis(response);
        }
        @Override protected void onPostExecute(Boolean flag) {
            super.onPostExecute(flag);
            postExecute(flag);
        }
    }
}
