package br.com.vansact;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import br.com.activity.R;
import br.com.bean.ShoppingList;
import br.com.dao.ShoppingListDAO;
import br.com.vansadapt.ShoppingListCursorAdapter;
import br.com.vansdialog.CustomDialogShoppingListOptions;
import br.com.vansexception.VansException;
import br.com.vansintent.CustomIntentOutside;
import br.com.vansprefs.UserPreferences;

public class MainApp extends Activity implements OnItemClickListener, OnItemLongClickListener, OnDismissListener, OnClickListener {
    private ShoppingListCursorAdapter adapter;
    private ListView lvShoppingList;
    private View headerView;


    //private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().setNavigationBarColor(android.graphics.Color.TRANSPARENT);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        setContentView(R.layout.activity_main_app);

        View rootView = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemInsets.left,
                    systemInsets.top,
                    systemInsets.right,
                    systemInsets.bottom
            );
            return WindowInsetsCompat.CONSUMED;
        });


        adapter = new ShoppingListCursorAdapter(this);
        lvShoppingList = (ListView) findViewById(R.id.lvShoppingList);

        headerView = getLayoutInflater().inflate(R.layout.adapter_shopping_list, null);
        ((TextView) headerView.findViewById(R.id.nameShoppingList)).setText(R.string.no_list_added);
        (headerView.findViewById(R.id.view_date_shopping_list)).setVisibility(View.INVISIBLE);
        headerView.setBackgroundResource(R.drawable.custom_blanck_backgroud);
        headerView.setOnClickListener(this);

        ((LinearLayout) findViewById(R.id.activity_main_app_id)).addView(headerView);

        lvShoppingList.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        lvShoppingList.setOnItemClickListener(this);
        lvShoppingList.setOnItemLongClickListener(this);

        refreshListView();
        super.onResume();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add:
                addNew();
                return true;

            case R.id.action_delete_all:
                deleteAll();
                return true;

            case R.id.action_update:
                CustomIntentOutside.UpdateApp(this);
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, UserPreferences.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteAll() {
        if (adapter.getCount() > 0) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(R.string.delete_question);
            adb.setMessage(getString(R.string.want_delete_all_list));
            adb.setNegativeButton(R.string.no, null);
            adb.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        ShoppingListDAO.deleteAll(MainApp.this);
                        refreshListView();
                    } catch (VansException e) {
                        Toast.makeText(MainApp.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            });

            adb.show();
        }
    }

    private void addNew() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.save));
        alert.setMessage(getString(R.string.title_new));

        final EditText edName = new EditText(this);
        alert.setView(edName);

        alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ShoppingList shoppingList = new ShoppingList(MainApp.this);
                shoppingList.setName(MainApp.this, edName.getText().toString());

                try {
                    startActivity(new Intent(MainApp.this, AddItemShoppingList.class).putExtra(getString(R.string.id_shopping_list), ShoppingListDAO.insert(MainApp.this, shoppingList).getId()));
                } catch (VansException e) {
                    Toast.makeText(MainApp.this, e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        });

        alert.setNegativeButton(android.R.string.cancel, null);
        alert.show();
    }

    private void refreshListView() {
        adapter.refreshCursorAdapter();
        headerView.setVisibility(adapter.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        lvShoppingList.setOnItemClickListener(null);
        startActivity(new Intent(this, AddItemShoppingList.class).putExtra(getString(R.string.id_shopping_list), adapter.getItem(arg2).getId()));

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
        if (adapter.getItem(arg2) != null) {
            CustomDialogShoppingListOptions c = new CustomDialogShoppingListOptions(this, adapter.getItem(arg2).getId());
            c.setOnDismissListener(this);
            c.show();
        }

        return true;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        refreshListView();
    }

    @Override
    public void onClick(View v) {
        if (adapter.getCount() == 0) {
            addNew();

        }
    }

}
