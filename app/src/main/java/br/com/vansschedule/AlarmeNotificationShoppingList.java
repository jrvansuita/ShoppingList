package br.com.vansschedule;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import br.com.activity.R;
import br.com.bean.ShoppingList;
import br.com.dao.ShoppingListDAO;
import br.com.vansact.AddItemShoppingList;
import br.com.vansexception.VansException;

public class AlarmeNotificationShoppingList extends BroadcastReceiver {

	private Context context;
	private Intent receiveIntent;

	// Tag names
	private static final String ACTION = "ACTION";
	private static final String ID_TAG = "ID";

	// tag values
	private static final String START_TAG = "START";
	private static final String STOP_TAG = "STOP";
	private static final String GOTO_TAG = "GOTO";

	public static void initAlarme(Context context, int idShoppingList, Calendar calendar) {
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, calendar.getTimeInMillis(), getPendingIntent(context, idShoppingList, START_TAG));
	}

	public static void cancelAlarme(Context context, int idShoppingList) {
		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).set(AlarmManager.RTC, System.currentTimeMillis(), getPendingIntent(context, idShoppingList, STOP_TAG));
	}

	private static PendingIntent getPendingIntent(Context context, int id, String actionValue) {
		Intent i = new Intent(context, AlarmeNotificationShoppingList.class);
		i.putExtra(ACTION, actionValue);
		i.putExtra(ID_TAG, id);
		return PendingIntent.getBroadcast(context, id, i, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		this.receiveIntent = intent;

		if (intent.getExtras().getString(ACTION).equals(START_TAG)) {
			notificate();
		} else if (intent.getExtras().getString(ACTION).equals(GOTO_TAG)) {
			gotoShoppingList();
		} else if (intent.getExtras().getString(ACTION).equals(STOP_TAG)) {
			stopNotification();
		}
	}

	private ShoppingList getShoppingList(Context context, Intent intent) {
		if (intent.getExtras().getInt(ID_TAG) > 0) {
			try {
				return ShoppingListDAO.select(context, intent.getExtras().getInt(ID_TAG));
			} catch (VansException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void notificate() {
		ShoppingList shoppingList = getShoppingList(context, receiveIntent);
		if (shoppingList != null) {
			Bitmap bigIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);

			NotificationCompat.Builder builder = new NotificationCompat.Builder(context).setSmallIcon(R.drawable.ic_launcher_action_bar).setContentTitle(shoppingList.getName()).setLargeIcon(bigIcon).setContentText(context.getString(R.string.notification_arrived)).setLights(0xFFFF8000, 1000, 1000)
					.setContentIntent(getPendingIntent(context, shoppingList.getId(), GOTO_TAG)).setVibrate(new long[] { Notification.DEFAULT_VIBRATE, 500 });

			((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(shoppingList.getId(), builder.build());
		}
	}

	private void stopNotification() {
		int id = receiveIntent.getExtras().getInt(ID_TAG);

		((AlarmManager) context.getSystemService(Context.ALARM_SERVICE)).cancel(getPendingIntent(context, id, STOP_TAG));
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(id);
	}

	private void gotoShoppingList() {
		stopNotification();

		ShoppingList shoppingList = getShoppingList(context, receiveIntent);
		if (shoppingList != null) {
			Intent resultIntent = new Intent(context, AddItemShoppingList.class);
			resultIntent.putExtra(context.getString(R.string.id_shopping_list), shoppingList.getId());
			resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(resultIntent);
		}
	}

}
