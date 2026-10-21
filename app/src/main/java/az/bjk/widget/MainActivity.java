package az.bjk.widget;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Xml;
import android.view.Gravity;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int BG = Color.rgb(8, 9, 12);
    private static final int PANEL = Color.rgb(21, 23, 29);
    private static final int MUTED = Color.rgb(175, 179, 190);
    private static final String[] CATEGORIES = {"Futbol", "Basketbol", "Transfer", "Klub"};
    private static final String[] SEARCHES = {
        "Beşiktaş futbol", "Beşiktaş basketbol", "Beşiktaş transfer", "site:bjk.com.tr Beşiktaş"
    };
    private final ExecutorService network = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<TextView> tabs = new ArrayList<>();
    private LinearLayout feed;
    private TextView status;
    private int selected = 0;
    private int request = 0;

    private int dp(int value) { return (int)(getResources().getDisplayMetrics().density * value + 0.5f); }

    private GradientDrawable background(int color, int radius, int border) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radius));
        if (border != 0) d.setStroke(dp(1), border);
        return d;
    }

    private TextView text(String s, int size, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        if (bold) v.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return v;
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        buildScreen();
        showCategory(0);
    }

    private void buildScreen() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        setContentView(scroll);

        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(18), dp(16), dp(18), dp(28));
        scroll.addView(page);

        ImageView eagle = new ImageView(this);
        eagle.setImageResource(R.drawable.eagle);
        eagle.setScaleType(ImageView.ScaleType.FIT_CENTER);
        page.addView(eagle, new LinearLayout.LayoutParams(-1, dp(165)));

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(header);

        LinearLayout names = new LinearLayout(this);
        names.setOrientation(LinearLayout.VERTICAL);
        header.addView(names, new LinearLayout.LayoutParams(0, -2, 1f));
        TextView title = text("BEŞİKTAŞ", 30, Color.WHITE, true);
        names.addView(title);
        names.addView(text("QARA QARTAL  •  SON XƏBƏRLƏR", 11, MUTED, true));

        TextView refresh = text("↻", 29, Color.WHITE, true);
        refresh.setGravity(Gravity.CENTER);
        refresh.setContentDescription("Xəbərləri yenilə");
        refresh.setBackground(background(PANEL, 14, Color.rgb(65, 67, 73)));
        header.addView(refresh, new LinearLayout.LayoutParams(dp(48), dp(48)));
        refresh.setOnClickListener(v -> load());

        HorizontalScrollView horizontal = new HorizontalScrollView(this);
        horizontal.setHorizontalScrollBarEnabled(false);
        LinearLayout.LayoutParams horizontalLayout = new LinearLayout.LayoutParams(-1, dp(65));
        horizontalLayout.topMargin = dp(13);
        page.addView(horizontal, horizontalLayout);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        horizontal.addView(bar);
        for (int i = 0; i < CATEGORIES.length; i++) {
            final int index = i;
            TextView tab = text(CATEGORIES[i], 14, MUTED, true);
            tab.setPadding(dp(20), dp(12), dp(20), dp(12));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
            lp.rightMargin = dp(8);
            bar.addView(tab, lp);
            tabs.add(tab);
            tab.setOnClickListener(v -> showCategory(index));
        }

        LinearLayout line = new LinearLayout(this);
        line.setGravity(Gravity.CENTER_VERTICAL);
        page.addView(line);
        TextView heading = text("XƏBƏR LENTİ", 14, Color.WHITE, true);
        line.addView(heading, new LinearLayout.LayoutParams(0, -2, 1f));
        status = text("", 11, MUTED, false);
        line.addView(status);

        feed = new LinearLayout(this);
        feed.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams feedLP = new LinearLayout.LayoutParams(-1, -2);
        feedLP.topMargin = dp(13);
        page.addView(feed, feedLP);

        TextView official = text("BEŞİKTAŞ RƏSMİ SAYTI   ↗", 13, Color.WHITE, true);
        official.setGravity(Gravity.CENTER);
        official.setBackground(background(Color.rgb(39, 42, 50), 15, Color.rgb(70, 73, 80)));
        LinearLayout.LayoutParams officialLP = new LinearLayout.LayoutParams(-1, dp(51));
        officialLP.topMargin = dp(14);
        page.addView(official, officialLP);
        official.setOnClickListener(v -> open("https://bjk.com.tr/tr/haberler"));

        TextView foot = text("Xəbərlər Google News RSS-dən alınır. Bu, klubun rəsmi tətbiqi deyil.", 11, MUTED, false);
        foot.setGravity(Gravity.CENTER);
        foot.setPadding(dp(6), dp(14), dp(6), 0);
        page.addView(foot);
    }

    private void showCategory(int index) {
        selected = index;
        for (int i = 0; i < tabs.size(); i++) {
            TextView t = tabs.get(i);
            boolean current = i == index;
            t.setBackground(background(current ? Color.WHITE : PANEL, 35, current ? 0 : Color.rgb(47, 49, 56)));
            t.setTextColor(current ? Color.BLACK : MUTED);
        }
        load();
    }

    private void message(String text) {
        feed.removeAllViews();
        TextView info = text(text, 14, MUTED, false);
        info.setGravity(Gravity.CENTER);
        info.setPadding(dp(15), dp(33), dp(15), dp(33));
        info.setBackground(background(PANEL, 14, Color.rgb(50, 53, 61)));
        feed.addView(info);
    }

    private static final class Item {
        String title = "";
        String url = "";
        String source = "";
        String date = "";
    }

    private void load() {
        final int currentRequest = ++request;
        final String query = SEARCHES[selected];
        status.setText("Yenilənir…");
        message("Son xəbərlər yüklənir…");
        network.execute(() -> {
            try {
                ArrayList<Item> results = retrieve(query);
                handler.post(() -> {
                    if (!isFinishing() && request == currentRequest) {
                        status.setText("CANLI LENT");
                        display(results);
                    }
                });
            } catch (Exception ex) {
                final String diagnostic = ex.getClass().getSimpleName()
                    + (ex.getMessage() == null ? "" : ": " + ex.getMessage());
                android.util.Log.e("BesiktasNews", "RSS yenilənmə xətası", ex);
                handler.post(() -> {
                    if (!isFinishing() && request == currentRequest) {
                        status.setText("XƏTA");
                        message("Xəbərlər yüklənmədi. Səbəb: " + diagnostic
                            + "\n\nİnternet bağlantısını yoxlayın və yenidən cəhd edin.");
                    }
                });
            }
        });
    }

    private ArrayList<Item> retrieve(String q) throws Exception {
        String endpoint = Uri.parse("https://news.google.com/rss/search").buildUpon()
            .appendQueryParameter("q", q)
            .appendQueryParameter("hl", "tr")
            .appendQueryParameter("gl", "TR")
            .appendQueryParameter("ceid", "TR:tr").build().toString();
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(16000);
        connection.setRequestProperty("Cache-Control", "no-cache");
        connection.setRequestProperty("Accept", "application/rss+xml, application/xml, text/xml;q=0.9");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; BesiktasKartalWidget/1.1)");
        try {
            int code = connection.getResponseCode();
            if (code != 200) throw new Exception("HTTP " + code + " - Google News RSS servisinden cavab alinmadi");
            try (InputStream input = new BufferedInputStream(connection.getInputStream())) {
                XmlPullParser xml = Xml.newPullParser();
                xml.setInput(input, "UTF-8");
                ArrayList<Item> items = new ArrayList<>();
                Item item = null;
                int event;
                while ((event = xml.next()) != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        String tag = xml.getName();
                        if ("item".equals(tag)) item = new Item();
                        else if (item != null) {
                            if ("title".equals(tag)) item.title = xml.nextText();
                            else if ("link".equals(tag)) item.url = xml.nextText();
                            else if ("source".equals(tag)) item.source = xml.nextText();
                            else if ("pubDate".equals(tag)) item.date = xml.nextText();
                        }
                    } else if (event == XmlPullParser.END_TAG && "item".equals(xml.getName()) && item != null) {
                        if (!item.title.isEmpty() && item.url.startsWith("https://")) items.add(item);
                        item = null;
                        if (items.size() >= 20) break;
                    }
                }
                return items;
            }
        } finally {
            connection.disconnect();
        }
    }

    private String clean(String raw) {
        return Html.fromHtml(raw == null ? "" : raw, Html.FROM_HTML_MODE_LEGACY).toString().trim();
    }

    private void display(ArrayList<Item> results) {
        feed.removeAllViews();
        if (results.isEmpty()) {
            message("Hazırda bu kateqoriyada xəbər tapılmadı.");
            return;
        }
        for (Item item : results) {
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(16), dp(16), dp(16), dp(16));
            card.setBackground(background(PANEL, 16, Color.rgb(55, 58, 66)));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.bottomMargin = dp(11);
            feed.addView(card, lp);

            String source = clean(item.source);
            String headline = clean(item.title);
            if (!source.isEmpty() && headline.endsWith(" - " + source))
                headline = headline.substring(0, headline.length() - source.length() - 3);
            card.addView(text(source.isEmpty() ? "BEŞİKTAŞ XƏBƏRLƏRİ" : source.toUpperCase(Locale.ROOT), 11, MUTED, true));
            TextView title = text(headline, 17, Color.WHITE, true);
            title.setPadding(0, dp(9), 0, dp(10));
            card.addView(title);

            String date = item.date;
            try {
                Date parsed = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH).parse(date);
                if (parsed != null)
                    date = new SimpleDateFormat("dd MMM • HH:mm", new Locale("tr", "TR")).format(parsed);
            } catch (Exception ignored) { }
            card.addView(text(date + "   ↗", 12, MUTED, false));
            card.setOnClickListener(v -> open(item.url));
        }
    }

    private void open(String url) {
        if (!url.startsWith("https://")) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, "Brauzer tapılmadı", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onDestroy() {
        request++;
        network.shutdownNow();
        super.onDestroy();
    }
}
