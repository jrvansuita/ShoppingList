package br.com.vansdialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;

import br.com.activity.R;
import br.com.bean.ItemShoppingList;
import br.com.bean.ShoppingList;
import br.com.dao.ItemShoppingListDAO;
import br.com.dao.ShoppingListDAO;
import br.com.vansadapt.OptionAdapter;
import br.com.vansexception.VansException;
import br.com.vansintent.CustomIntentOutside;
import br.com.vansschedule.ScheduleShoppingList;
import br.com.vansxmlhandler.ShoppingListXmlExporter;

public class CustomDialogShoppingListOptions extends Dialog implements OnItemClickListener, android.content.DialogInterface.OnDismissListener {

    private int idShoppingList;
    private Context context;

    public CustomDialogShoppingListOptions(Context context, int idShoppingList) {
        super(context);

        setCancelable(true);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        String[] options = { context.getString(R.string.rename), context.getString(R.string.duplicate), context.getString(R.string.delete), context.getString(R.string.schedule), context.getString(R.string.share), context.getString(R.string.share_via_text) };

        OptionAdapter adapter = new OptionAdapter(context, options);
        ListView lv = new ListView(context);

        lv.setAdapter(adapter);
        lv.setLayoutParams(new ListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        lv.setOnItemClickListener(this);

        this.context = context;
        this.idShoppingList = idShoppingList;
        setContentView(lv);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        try {
            if (arg0.getAdapter().getItem(arg2).toString().equalsIgnoreCase(context.getString(R.string.delete))) {
                optionDelete();
            }

            if (arg0.getAdapter().getItem(arg2).toString().equalsIgnoreCase(context.getString(R.string.duplicate))) {
                optionDuplicate();
            }

            if (arg0.getAdapter().getItem(arg2).toString().equalsIgnoreCase(context.getString(R.string.rename))) {
                optionRename();
            }

            if (arg0.getAdapter().getItem(arg2).toString().equalsIgnoreCase(context.getString(R.string.schedule))) {

                ScheduleShoppingList d = new ScheduleShoppingList(context, idShoppingList);
                d.setOnDismissListener(this);
                d.show();

            }

            if (arg0.getAdapter().getItem(arg2).toString().equalsIgnoreCase(context.getString(R.string.share))) {
                optionShare();
            }

            if (arg0.getAdapter().getItem(arg2).toString().equalsIgnoreCase(context.getString(R.string.share_via_text))) {
                optionShareText();
            }
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    private void optionDuplicate() throws VansException {
        ShoppingList shop = ShoppingListDAO.select(context, idShoppingList);
        shop.setId(0);

        ShoppingList newShop = ShoppingListDAO.insert(context, shop);

        Cursor c = ItemShoppingListDAO.selectAll(context,null,  idShoppingList);

        if (c != null) {
            while (c.moveToNext()) {
                ItemShoppingList item = ItemShoppingListDAO.select(context, c.getInt(c.getColumnIndex(ItemShoppingListDAO.FIELD_ID)));
                item.setId(0);
                item.setIdShoppingList(newShop.getId());
                ItemShoppingListDAO.insert(context, item);
            }

            c.close();
        }

        dismiss();

    }

    private void optionDelete() throws VansException {
        ShoppingListDAO.delete(context, idShoppingList);
        dismiss();
    }

    private void optionRename() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getString(R.string.save));
        builder.setMessage(context.getString(R.string.title_rename));
        final EditText edName = new EditText(context);
        builder.setView(edName);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int whichButton) {
                try {
                    ShoppingList shoppingList = ShoppingListDAO.select(context, idShoppingList);
                    shoppingList.setName(context, edName.getText().toString());
                    ShoppingListDAO.update(context, shoppingList);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                dismiss();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.setOnDismissListener(this);
        dialog.show();
    }

    private void optionShareText() throws VansException {
        CustomIntentOutside.shareShoppingListText(context, idShoppingList);
    }

    private void optionShare() throws IOException, VansException {
        ShoppingList shoppingList = ShoppingListDAO.select(context, idShoppingList);
        String title = context.getString(R.string.share_title) + " " + shoppingList.getName();

        ShoppingListXmlExporter shoppingListXmlExporter = new ShoppingListXmlExporter(context);
        CustomIntentOutside.shareShoppingListFile(context, title, shoppingListXmlExporter.export(idShoppingList));
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        this.dismiss();
    }

}
