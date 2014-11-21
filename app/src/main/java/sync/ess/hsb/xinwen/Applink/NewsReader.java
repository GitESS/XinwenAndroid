package sync.ess.hsb.xinwen.Applink;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import sync.ess.hsb.xinwen.entity.News;
import sync.ess.hsb.xinwen.parser.RSSItem;
import sync.ess.hsb.xinwen.parser.RSSParser;
import sync.ess.hsb.xinwen.util.Utils;

/**
 * Created by Hemant Bisht on 11/4/2014.
 */
public class NewsReader {


    public String google_temp_url = "http://news.google.com/news?pz=1&cf=all&ned=us&hl=en&q=sports&cf=all&output=rss";

    String rss_cat = "TopNews";
    String rss_link = Utils.PRE_CAT_URL + rss_cat + Utils.POST_CAT_URL;
    List<News> newsList;
    AppLinkService appLinkService;

    public void fetchNews() {
        new ReadNewsForCategory().execute();
    }

    public class ReadNewsForCategory extends AsyncTask<Void, Void, Void> {
        List<RSSItem> rssItems = new ArrayList<RSSItem>();
        RSSParser rssParser = new RSSParser();
        onItemDownloaded itemDownloaded;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            newsList = new ArrayList<News>();

        }

        @Override
        protected Void doInBackground(Void... params) {
            // rss link url
            String rss_url = google_temp_url;

            // list of rss items
            rssItems = rssParser.getRSSFeedItems(rss_url);
            int newscounter = 0;
            // looping through each item
            for (RSSItem item : rssItems) {
                String htmlpars = item.getDescription();
                String newsDescription = rssParser
                        .getDescriptionFromHtml(htmlpars);
                newsList.add(new News(item.getTitle(), item.getDescription(), "" + newscounter));
                newscounter++;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            appLinkService = new AppLinkService();
           // itemDownloaded.OnNewsDownloded(newsList);
            appLinkService.readNewsTitles(newsList);
        }
    }

    public interface onItemDownloaded {
        public void OnNewsDownloded(List<News> news);
    }
}