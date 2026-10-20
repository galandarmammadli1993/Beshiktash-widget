package az.bjk.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class KartalWidgetProvider extends AppWidgetProvider {
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.kartal_widget);
            Intent news = new Intent(context, MainActivity.class);
            news.setAction("az.bjk.widget.OPEN_NEWS");
            PendingIntent click = PendingIntent.getActivity(
                context, id, news, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.tap_area, click);
            manager.updateAppWidget(id, views);
        }
    }
}
