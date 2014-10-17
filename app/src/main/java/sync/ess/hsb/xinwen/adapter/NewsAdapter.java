package sync.ess.hsb.xinwen.adapter;

import sync.ess.hsb.xinwen.R;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NewsAdapter extends BaseAdapter {

	private List<String> LIST_TITLE = new ArrayList<String>();
	private List<String> LIST_LINK = new ArrayList<String>();
	private List<String> LIST_DESRIPTION = new ArrayList<String>();
	private List<String> LIST_PUB_DATE = new ArrayList<String>();
	Activity mcontext;

	LayoutInflater inflater;

	public NewsAdapter(Activity context, List<String> LIST_TITLE,
			List<String> LIST_LINK, List<String> LIST_DESRIPTION,
			List<String> LIST_PUB_DATE) {

		mcontext = context;
		this.LIST_TITLE = LIST_TITLE;
		this.LIST_LINK = LIST_LINK;
		this.LIST_DESRIPTION = LIST_DESRIPTION;
		this.LIST_PUB_DATE = LIST_PUB_DATE;
		inflater = (LayoutInflater) mcontext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return LIST_TITLE.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		ViewHolder holder;

		if (convertView == null) {

			vi = inflater.inflate(R.layout.rss_item_list_row, null);
			holder = new ViewHolder();
			holder.mTitle = (TextView) vi.findViewById(R.id.title);
			holder.mLink = (TextView) vi.findViewById(R.id.page_url);
			holder.mDescription = (TextView) vi.findViewById(R.id.link);
			holder.mPubDate = (TextView) vi.findViewById(R.id.pub_date);

			vi.setTag(holder);
		} else
			holder = (ViewHolder) vi.getTag();
		if (LIST_TITLE.size() <= 0) {
			holder.mTitle.setText("");
		} else {

			holder.mTitle.setText(LIST_TITLE.get(position));
			holder.mLink.setText(LIST_LINK.get(position));
			
			Document doc = Jsoup.parse(LIST_DESRIPTION.get(position));
			Elements links = doc.select("a");
			Element head = doc.select("#head").first();
			
		//	holder.mDescription.getSettings().setJavaScriptEnabled(true);
			//String summary = "<html><body>You scored" +LIST_DESRIPTION.get(position)+ "<b>192</b> points.</body></html>";
			//summary.replaceAll(" ", "");
			//String webData = "<html><body>"+LIST_DESRIPTION.get(position)+"</html></body>";
		//	holder.mDescription.loadData(summary, "text/html", "UTF-8");
			
		//	holder.mDescription.loadDataWithBaseURL(null, summary, "text/html", "UTF-8", null);
			holder.mDescription.setText(Html.fromHtml(LIST_DESRIPTION.get(position)));
			holder.mPubDate.setText(LIST_PUB_DATE.get(position));

		}

		return vi;

	}

	public static class ViewHolder {

		TextView mTitle;
		TextView mLink;
		TextView mDescription;
		TextView mPubDate;

	}
}
