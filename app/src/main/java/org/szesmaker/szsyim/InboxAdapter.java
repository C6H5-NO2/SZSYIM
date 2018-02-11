package org.szesmaker.szsyim;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class InboxAdapter extends RecyclerView.Adapter<InboxAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<Map<String, String>> data;
    public InboxAdapter(ArrayList<Map<String, String>> data) {
        this.data = data;
    }
    @Override public InboxAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inbox, parent, false);
        view.setOnClickListener(this);
        return new ViewHolder(view);
    }
    @Override public void onBindViewHolder(InboxAdapter.ViewHolder holder, int position) {
        holder.participants.setText(data.get(position).get("participants"));
        holder.title.setText(data.get(position).get("title"));
        HashMap<String, String> map = new HashMap<>();
        map.put("position", "" + position);
        map.put("link", "https://chengjiyun.com" + data.get(position).get("link"));
        holder.itemView.setTag(map);
    }
    @Override public int getItemCount() {
        return data == null ? 0 : data.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView participants, title;
        public ViewHolder(View item) {
            super(item);
            participants = (TextView) item.findViewById(R.id.participants);
            title = (TextView) item.findViewById(R.id.title);
        }
    }
    public static interface OnRecyclerViewItemClickListener {
        void onRecyclerViewItemClick(View view, HashMap<String, String> map);
    }
    private OnRecyclerViewItemClickListener inboxListener = null;
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.inboxListener = listener;
    }
    @Override public void onClick(View view) {
        if (inboxListener != null) {
            inboxListener.onRecyclerViewItemClick(view, (HashMap<String, String>) view.getTag());
        }
    }
}
