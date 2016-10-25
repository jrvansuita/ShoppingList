package br.com.vansadapt;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import br.com.activity.R;
import br.com.bean.ShoppingList;
import br.com.dao.ItemShoppingListDAO;
import br.com.dao.ShoppingListDAO;
import br.com.vansexception.VansException;
import br.com.vansformat.CustomDateFormat;
import br.com.vansprefs.UserPreferences;

public class ShoppingListCursorAdapter extends CursorAdapter {
	private Context context;

	public ShoppingListCursorAdapter(Context context) {
		super(context, null, 0);
		this.context = context;
	}

	@Override
	public ShoppingList getItem(int position) {
		try {
			Cursor c = getCursor();
			c.moveToPosition(position);
			return ShoppingListDAO.returnClassInstace(context, c);
		} catch (Exception e) {
			return null;
		}
	}

	public void refreshCursorAdapter() {

		try {
			changeCursor(ShoppingListDAO.selectAll(context));
		} catch (VansException e) {
			e.printStackTrace();
			Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		notifyDataSetChanged();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ShoppingList shoppingList = getItem(cursor.getPosition());

		TextView tvId = (TextView) view.findViewById(R.id.idShoppingList);
		tvId.setText(String.valueOf(shoppingList.getId()));

		ImageView imAllItemsChecked = (ImageView) view.findViewById(R.id.allItemsChecked);
		boolean checked = ItemShoppingListDAO.isAllItemsChecked(context, shoppingList.getId());

		imAllItemsChecked.setImageResource(checked ? R.drawable.btn_check_on_holo_dark : R.drawable.btn_check_off_holo_dark);
		imAllItemsChecked.setVisibility(View.VISIBLE);

		TextView tvName = (TextView) view.findViewById(R.id.nameShoppingList);
		tvName.setText(shoppingList.getName());
		tvName.setPaintFlags(checked ? Paint.STRIKE_THRU_TEXT_FLAG : Paint.ANTI_ALIAS_FLAG);
		tvName.setTypeface(null, checked ? Typeface.ITALIC : Typeface.NORMAL);

		TextView tvDate = (TextView) view.findViewById(R.id.dateShoppingList);
		tvDate.setText(CustomDateFormat.getFormatedDate(shoppingList.getDate()));

		TextView tvTime = (TextView) view.findViewById(R.id.timeShoppingList);
		tvTime.setText(CustomDateFormat.getFormatedTime(shoppingList.getDate()));

	}

	@Override
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		LayoutInflater inflater = (LayoutInflater) arg0.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return inflater.inflate(R.layout.adapter_shopping_list, null);
	}

}