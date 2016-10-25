package br.com.vansadapt;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import br.com.activity.R;
import br.com.bean.ItemShoppingList;
import br.com.dao.ItemShoppingListDAO;
import br.com.vansexception.VansException;
import br.com.vansformat.CustomFloatFormat;
import br.com.vansprefs.UserPreferences;

public class ItemShoppingListCursorAdapter extends CursorAdapter {

    private Context context;
    private int idSelected;
    private int idShoppingList;
    private final int INVALID_INDEX = 0;

    public ItemShoppingListCursorAdapter(Context context, int idShoppingList) {
        super(context, null, 0);
        this.context = context;
        setIdSelected(INVALID_INDEX);
        this.idShoppingList = idShoppingList;
    }

    @Override
    public ItemShoppingList getItem(int position) {
        try {
            Cursor c = getCursor();
            c.moveToPosition(position);
            return ItemShoppingListDAO.returnClassInstace(context, c);
        } catch (Exception e) {
            return null;
        }
    }

    public int getIdSelected() {
        return idSelected;
    }

    public void setIdSelected(int value) {
        idSelected = value;
    }

    public boolean isSelected() {
        return idSelected > INVALID_INDEX;
    }

    public void refreshCursorAdapter(String filter) {
        try {
            changeCursor(ItemShoppingListDAO.selectAll(context, filter, idShoppingList));
        } catch (VansException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        notifyDataSetChanged();
    }

    public String getTotalValue() {
        float result = 0;

        boolean useMonetaryMask = UserPreferences.getShowUnitValue(context);

        Cursor c = getCursor();
        for (int i = 0; i < c.getCount(); i++) {
            c.moveToPosition(i);

            ItemShoppingList itemShoppingList = getItem(c.getPosition());

            if (UserPreferences.getShowQuantity(context) && UserPreferences.getShowUnitValue(context)) {
                result = result + (itemShoppingList.getUnitValue() * itemShoppingList.getQuantity());

            } else if (UserPreferences.getShowQuantity(context)) {
                result = result + itemShoppingList.getQuantity();

            } else if (UserPreferences.getShowUnitValue(context)) {
                result = result + itemShoppingList.getUnitValue();
            }
        }

        return useMonetaryMask ? CustomFloatFormat.getMonetaryMaskedValue(context, result) : CustomFloatFormat.getSimpleFormatedValue(result);
    }

    public String getTotalQuant() {
        return context.getString(R.string.quant_items, getCount());
    }

    public String[] getDescriptions() {
        Cursor c = getCursor();
        if (c != null) {
            String[] descriptions = new String[c.getCount()];

            for (int i = 0; i < c.getCount(); i++) {
                c.moveToPosition(i);
                ItemShoppingList itemShoppingList = getItem(c.getPosition());

                descriptions[c.getPosition()] = itemShoppingList.getDescription();
            }

            return descriptions;
        }

        return new String[]{};
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ItemShoppingList itemShoppingList = getItem(cursor.getPosition());

        TextView tvId = (TextView) view.findViewById(R.id.idItemShoppingList);
        tvId.setText(String.valueOf(itemShoppingList.getId()));

        TextView tvDescription = (TextView) view.findViewById(R.id.descriptionItemShoppingList);
        tvDescription.setText(itemShoppingList.getDescription());


        if (UserPreferences.getShowCheckBox(context)) {
            tvDescription.setPaintFlags(itemShoppingList.isChecked() ? Paint.STRIKE_THRU_TEXT_FLAG : Paint.ANTI_ALIAS_FLAG);
            tvDescription.setTypeface(null, itemShoppingList.isChecked() ? Typeface.ITALIC : Typeface.NORMAL);
        }

        int leftPadding = UserPreferences.getShowCheckBox(context) ? tvDescription.getPaddingLeft() : 15;
        tvDescription.setPadding(leftPadding, tvDescription.getPaddingTop(), tvDescription.getPaddingRight(), tvDescription.getPaddingBottom());

        CheckBox cbChecked = (CheckBox) view.findViewById(R.id.checkedItemShoppingList);
        cbChecked.setOnCheckedChangeListener(null);
        cbChecked.setTag(itemShoppingList.getId());
        cbChecked.setChecked(itemShoppingList.isChecked());
        cbChecked.setClickable(!isSelected());
        cbChecked.setVisibility(UserPreferences.getShowCheckBox(context) ? View.VISIBLE : View.GONE);

        if (!isSelected()) {
            cbChecked.setOnCheckedChangeListener((OnCheckedChangeListener) context);
        }

        TextView tvQuantity = (TextView) view.findViewById(R.id.qtItemShoppingList);
        tvQuantity.setVisibility(UserPreferences.getShowQuantity(context) ? View.VISIBLE : View.GONE);
        tvQuantity.setText(CustomFloatFormat.getSimpleFormatedValue(itemShoppingList.getQuantity()));

        TextView tvUnitValue = (TextView) view.findViewById(R.id.unitValueItemShoppingList);
        tvUnitValue.setVisibility(UserPreferences.getShowUnitValue(context) ? View.VISIBLE : View.GONE);
        tvUnitValue.setText(CustomFloatFormat.getMonetaryMaskedValue(context, itemShoppingList.getUnitValue()));

        TextView tvTotal = (TextView) view.findViewById(R.id.totalItemShoppingList);
        tvTotal.setVisibility(itemShoppingList.getTotal() != 0 ? View.VISIBLE : View.GONE);
        tvTotal.setText(CustomFloatFormat.getMonetaryMaskedValue(context, itemShoppingList.getTotal()));

        view.setBackgroundColor(((isSelected()) && getIdSelected() == itemShoppingList.getId()) ? context.getResources().getColor(R.color.gray_inactive) : Color.TRANSPARENT);

    }

    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        LayoutInflater inflater = (LayoutInflater) arg0.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.adapter_item_shopping_list, null);
    }

}