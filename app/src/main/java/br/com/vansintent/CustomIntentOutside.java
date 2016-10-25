package br.com.vansintent;

import java.io.File;

import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.widget.Toast;
import br.com.activity.R;
import br.com.bean.ShoppingList;
import br.com.dao.ItemShoppingListDAO;
import br.com.dao.ShoppingListDAO;
import br.com.vansexception.VansException;

public class CustomIntentOutside {

	private static boolean sucessInternetConnectivity(Context context, boolean doAlert) {
		ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

		boolean isConnected = netInfo != null && netInfo.isConnected();

		if (doAlert && !isConnected) {
			Toast.makeText(context, R.string.no_connection, Toast.LENGTH_LONG).show();
		}

		return isConnected;
	}

	public static void shareShoppingListFile(Context context, String title, File file) {
		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType(HTTP.OCTET_STREAM_TYPE);

		i.putExtra(android.content.Intent.EXTRA_TEXT, title);
		i.putExtra(android.content.Intent.EXTRA_STREAM, Uri.fromFile(file));

		context.startActivity(Intent.createChooser(i, context.getString(R.string.share_via)));
	}

	public static void shareShoppingListText(Context context, int idShoppingList) throws VansException {
		ShoppingList shoppingList = ShoppingListDAO.select(context, idShoppingList);
		String title = context.getString(R.string.share_title) + " " + shoppingList.getName() + " \n";

		Intent i = new Intent(android.content.Intent.ACTION_SEND);
		i.setType(HTTP.PLAIN_TEXT_TYPE);
		// i.putExtra(android.content.Intent.EXTRA_SUBJECT, title);
		i.putExtra(android.content.Intent.EXTRA_TEXT, title + ItemShoppingListDAO.toString(context, idShoppingList));
		context.startActivity(Intent.createChooser(i, context.getString(R.string.share_via)));
	}

	public static void UpdateApp(Context context) {
		if (sucessInternetConnectivity(context, true)) {
			try {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
			} catch (android.content.ActivityNotFoundException anfe) {
				context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
			}
		}
	}

	public static void barcodeScanner(Context context, int requestCode) {
		try {
			Intent intent = new Intent("com.google.zxing.client.android.SCAN");
			// intent.setPackage("com.google.zxing.client.android");
			intent.putExtra("SCAN_MODE", "PRODUCT_MODE"); // PRODUCT_MODE
			/*
			 * Intent intent = new
			 * Intent("com.google.zxing.client.android.SCAN");
			 * intent.setPackage("com.google.zxing.client.android");
			 * //intent.putExtra("SCAN_MODE", "PRODUCT_MODE");
			 * intent.putExtra("SCAN_FORMATS",
			 * "CODE_39,CODE_93,CODE_128,DATA_MATRIX,ITF,CODABAR,EAN_13,EAN_8,UPC_A,QR_CODE"
			 * ); startActivityForResult(intent, 0);
			 */

			((Activity) context).startActivityForResult(intent, requestCode);
		} catch (Exception e) {
			if (sucessInternetConnectivity(context, true)) {
				Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
				Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
				context.startActivity(marketIntent);
			}
		}
	}

/*	public static boolean speechRecognizer(Context context, String hint, int requestCode) {
		try {
			Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);			
			intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.getClass().getPackage().getName());
			intent.putExtra(RecognizerIntent.EXTRA_PROMPT, hint);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault());
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			((Activity) context).startActivityForResult(intent, requestCode);

			return true;

		} catch (Exception e) {
			Toast.makeText(context, R.string.erro_speech_recognizer, Toast.LENGTH_LONG).show();
			return false;
		}

	}
*/
	
	
}
