package br.com.vansact;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import br.com.activity.R;
import br.com.bean.ItemShoppingList;
import br.com.bean.ShoppingList;
import br.com.dao.ItemShoppingListDAO;
import br.com.dao.ShoppingListDAO;
import br.com.vansadapt.ItemShoppingListCursorAdapter;
import br.com.vansexception.VansException;
import br.com.vansformat.CustomFloatFormat;
import br.com.vansintent.CustomIntentOutside;
import br.com.vansprefs.UserPreferences;
import br.com.vanswatch.CustomEditTextWatcher;

public class AddItemShoppingList extends Activity implements OnItemClickListener, OnItemLongClickListener, OnCheckedChangeListener, OnKeyListener, OnFocusChangeListener, SearchView.OnQueryTextListener {
	private ItemShoppingListCursorAdapter adapter;
	private ShoppingList shoppingList;
	private static final int BARCODE_SCANNER_REQUEST_CODE = 2;
	private View headerView;
	private ListView lvItensShoppingList;
	private AutoCompleteTextView edDescription;
	private EditText edQuantity;
	private EditText edUnitValue;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_item_shopping_list);

		try {
			shoppingList = ShoppingListDAO.select(this, getIntent().getExtras().getInt((getString(R.string.id_shopping_list))));
		} catch (VansException e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}

		this.setTitle(shoppingList.getName());

		lvItensShoppingList = (ListView) findViewById(R.id.lvItemShoppingList);
		lvItensShoppingList.setOnItemClickListener(this);
		lvItensShoppingList.setOnItemLongClickListener(this);

		headerView = (View) getLayoutInflater().inflate(R.layout.header_list_view_item_shopping_list, null);
		lvItensShoppingList.addHeaderView(headerView, null, false);

		adapter = new ItemShoppingListCursorAdapter(this, shoppingList.getId());
		lvItensShoppingList.setAdapter(adapter);

		edUnitValue = (EditText) findViewById(R.id.edUnitValue);
		edUnitValue.setVisibility(UserPreferences.getShowUnitValue(this) ? View.VISIBLE : View.GONE);
		edUnitValue.setOnKeyListener(this);
		edUnitValue.addTextChangedListener(new CustomEditTextWatcher(edUnitValue, 5));
		edUnitValue.setOnFocusChangeListener(this);

		edQuantity = (EditText) findViewById(R.id.edQuantity);
		edQuantity.addTextChangedListener(new CustomEditTextWatcher(edQuantity, 4));
		edQuantity.setVisibility(UserPreferences.getShowQuantity(this) ? View.VISIBLE : View.GONE);
		edQuantity.setOnFocusChangeListener(this);

		edDescription = (AutoCompleteTextView) findViewById(R.id.edDescription);
		edDescription.setOnItemClickListener(this);
		edDescription.addTextChangedListener(new CustomEditTextWatcher(edDescription, -1));

		if ((!UserPreferences.getShowQuantity(this)) && (!UserPreferences.getShowUnitValue(this))) {
			edDescription.setImeOptions(EditorInfo.IME_ACTION_GO);
			edDescription.setOnKeyListener(this);
		} else if (!UserPreferences.getShowUnitValue(this)) {
			edQuantity.setImeOptions(EditorInfo.IME_ACTION_GO);
			edQuantity.setOnKeyListener(this);
		}

	}

	@Override
	protected void onResume() {
		edUnitValue.setHint(CustomFloatFormat.getMonetaryMaskedValue(this, 0));
		edQuantity.setHint(CustomFloatFormat.getSimpleFormatedValue(0));

		try {
			refreshListView();
		} catch (VansException e) {
			e.printStackTrace();
			Toast.makeText(AddItemShoppingList.this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		// cancelEditing();
		super.onResume();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		/* ListView lvItensShoppingList - R.id.lvItemShoppingList */

		if (arg0.getId() == findViewById(R.id.lvItemShoppingList).getId()) {
			int position = arg2 - 1;
			if (adapter.getIdSelected() != adapter.getItem(position).getId()) {
				// Call the methodo onCreateOptionsMenu again
				ActivityCompat.invalidateOptionsMenu(this);

				setViewValues(adapter.getItem(position).getDescription(), adapter.getItem(position).getUnitValue(), adapter.getItem(position).getQuantity());
				adapter.setIdSelected(adapter.getItem(position).getId());
				refreshAdapter();
			}
		} else
		/* AutoCompleteTextView - R.id.edDescription */
		if (arg0.getAdapter().getItem(arg2) instanceof String) {
			ItemShoppingList itemShoppingList;
			try {
				itemShoppingList = ItemShoppingListDAO.findLastInserted(this, (String) arg0.getAdapter().getItem(arg2));
				setViewValues(itemShoppingList.getDescription(), itemShoppingList.getUnitValue(), itemShoppingList.getQuantity());
			} catch (VansException e) {
				e.printStackTrace();
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}

		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
		try {
			if (arg0.getAdapter().getItem(arg2) != null) {
				final int position = arg2 - 1;
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setTitle(R.string.delete_question);
				adb.setMessage(getString(R.string.want_delete_item) + " '" + adapter.getItem(position).getDescription() + "'?");
				adb.setNegativeButton(R.string.no, null);
				adb.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							ItemShoppingListDAO.delete(AddItemShoppingList.this, adapter.getItem(position).getId());
							cancelEditing();
						} catch (VansException e) {
							e.printStackTrace();
							Toast.makeText(AddItemShoppingList.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}

					}
				});

				adb.show();
			}
		} catch (Exception e) {
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		return true;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		try {
			ItemShoppingList itemListaCompras = ItemShoppingListDAO.select(this, Integer.parseInt(buttonView.getTag().toString()));

			if (itemListaCompras.isChecked() != isChecked) {
				itemListaCompras.setChecked(isChecked);
				ItemShoppingListDAO.update(this, itemListaCompras);
				refreshListView();
			}
		} catch (VansException e) {
			e.printStackTrace();
			Toast.makeText(AddItemShoppingList.this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_ENTER) && (!edDescription.getText().toString().isEmpty())) {
			try {
				botaoInserirItem();
			} catch (VansException e) {
				e.printStackTrace();
				Toast.makeText(AddItemShoppingList.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
			return true;
		}

		return false;
	}

	private void botaoInserirItem() throws VansException {
		if (isInsertOk(edDescription)) {
			try {
				String description = edDescription.getText().toString().trim();
				Float unitValue = CustomFloatFormat.parseFloat(edUnitValue.getText().toString());
				Float quantity = CustomFloatFormat.parseFloat(edQuantity.getText().toString());

				if (adapter.getIdSelected() > 0) {
					ItemShoppingList itemListaCompras = ItemShoppingListDAO.select(this, adapter.getIdSelected());
					itemListaCompras.setDescription(description);
					itemListaCompras.setQuantity(quantity);
					itemListaCompras.setUnitValue(unitValue);
					ItemShoppingListDAO.update(this, itemListaCompras);
				} else {
					ItemShoppingListDAO.insert(this, new ItemShoppingList(0, shoppingList.getId(), description, unitValue, quantity, false));
				}

			} catch (Exception e) {
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

			} finally {
				cancelEditing();
			}
		}
	}

	private void cancelEditing() throws VansException {
		adapter.setIdSelected(0);
		// Call the methodo onCreateOptionsMenu again
		ActivityCompat.invalidateOptionsMenu(this);
		clearEditTexts();
		refreshListView();
	}

	private void clearEditTexts() {
		setViewValues("", 0, 0);
	}

	private void setViewValues(String description, float unitValue, float quantity) {
		edDescription.setText(description);
		edDescription.setSelection(description.length());
		edDescription.requestFocus();

		edUnitValue.setText(unitValue > 0 ? CustomFloatFormat.getSimpleFormatedValue(unitValue) : "");

		edQuantity.setText(quantity > 0 ? CustomFloatFormat.getSimpleFormatedValue(quantity) : "");

	}

	private boolean isInsertOk(EditText edDescricao) {

		if (edDescricao.getText().toString().isEmpty()) {
			edDescricao.requestFocus();
			Toast.makeText(this, getString(R.string.info_desc), Toast.LENGTH_LONG).show();
			return false;
		}

		if ((adapter.getIdSelected() == 0) && (descriptionAlreadySetted(edDescricao.getText().toString()))) {
			Toast.makeText(this, getString(R.string.item_already_inserted), Toast.LENGTH_LONG).show();
			// clearEditTexts();
			return false;
		}

		return true;
	}

	private void refreshListView() throws VansException {
		refreshAdapter();
		edDescription.setAdapter(ItemShoppingListDAO.selectAutoComplete(this, adapter.getDescriptions()));

		boolean showFooter = (adapter.getCount() > 0) && (UserPreferences.getShowQuantity(this) || UserPreferences.getShowUnitValue(this));

		((TextView) findViewById(R.id.header_description_collumn)).setText(R.string.description_title);

		 findViewById(R.id.header_quantity_collumn).setVisibility(showFooter && UserPreferences.getShowQuantity(this) ? View.VISIBLE : View.GONE);
		findViewById(R.id.header_unit_value_collumn).setVisibility(showFooter && UserPreferences.getShowUnitValue(this) ? View.VISIBLE : View.GONE);

		View footerBar = findViewById(R.id.footer_bar);
		footerBar.setVisibility(showFooter ? View.VISIBLE : View.GONE);

		if (showFooter) {
			TextView totalSum = (TextView) findViewById(R.id.footer_total_sum);
			totalSum.setText(adapter.getTotalValue());

			TextView totalQuant = (TextView) findViewById(R.id.footer_total_quant);
			totalQuant.setText(adapter.getTotalQuant());
		} else {
			if (adapter.getCount() == 0) {
				((TextView) findViewById(R.id.header_description_collumn)).setText(R.string.no_item_added);
			}
		}
	}

	private boolean descriptionAlreadySetted(String value) {
		for (int i = 0; i < adapter.getCount(); i++) {
			if (adapter.getItem(i).getDescription().equalsIgnoreCase(value.trim())) {
				return true;
			}
		}

		return false;
	}

	private boolean allItensAlreadyChecked() {
		boolean allChecked = adapter.getCount() > 0;
		for (int i = 0; i < adapter.getCount(); i++) {
			allChecked = allChecked && adapter.getItem(i).isChecked();
		}

		return allChecked;
	}

	private void deleteAll(final boolean onlyCheckeds) {
		if (adapter.getCount() > 0) {
			AlertDialog.Builder adb = new AlertDialog.Builder(this);
			adb.setTitle(R.string.delete_question);
			adb.setMessage(getString(onlyCheckeds ? R.string.want_delete_all_selected_itens : R.string.want_delete_all_items));
			adb.setNegativeButton(R.string.no, null);
			adb.setPositiveButton(R.string.yes, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					try {
						ItemShoppingListDAO.deleteAllLista(AddItemShoppingList.this, shoppingList.getId(), onlyCheckeds);
						cancelEditing();
					} catch (VansException e) {
						e.printStackTrace();
						Toast.makeText(AddItemShoppingList.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
				}
			});

			adb.show();
		}
	}

	private SearchView mSearchView;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.add_item_shopping_list_menu, menu);

		MenuItem searchItem = menu.findItem(R.id.search);
		mSearchView = (SearchView) searchItem.getActionView();
		mSearchView.setOnQueryTextListener(this);
		mSearchView.setQueryHint("Pesquisar");

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.action_cancel).setVisible(adapter.getIdSelected() != 0);
		menu.findItem(R.id.action_select_all).setTitle(allItensAlreadyChecked() ? getString(R.string.desselect_all) : getString(R.string.select_all));
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
			case R.id.action_done:
				botaoInserirItem();
				return true;

			case R.id.action_cancel:
				cancelEditing();
				return true;

			case R.id.action_delete_all:
				deleteAll(false);
				return true;

			case R.id.action_select_all:
				ItemShoppingListDAO.checkAllItens(this, shoppingList.getId(), !allItensAlreadyChecked());
				cancelEditing();
				return true;

			case R.id.action_delete_selecteds:
				deleteAll(true);
				return true;

			case R.id.action_share:
				CustomIntentOutside.shareShoppingListText(this, shoppingList.getId());
				cancelEditing();
				return true;
				
			case R.id.action_settings:
				cancelEditing();
				startActivity(new Intent(this, UserPreferences.class));
				return true;
				

			case R.id.action_barcode_scan:
				CustomIntentOutside.barcodeScanner(this, BARCODE_SCANNER_REQUEST_CODE);
				cancelEditing();
				return true;

			default:
				return super.onOptionsItemSelected(item);
			}
		} catch (VansException e) {
			e.printStackTrace();
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
		return false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case BARCODE_SCANNER_REQUEST_CODE:
				edDescription.setText(data.getStringExtra("SCAN_RESULT"));
				edDescription.requestFocus();
				break;
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			ActivityCompat.invalidateOptionsMenu(this);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		if (hasFocus) {
			char decimalSeparator = DecimalFormatSymbols.getInstance(Locale.getDefault()).getDecimalSeparator();

			if ((v.getId() == R.id.edQuantity) && edQuantity.getText().toString().isEmpty()) {
				edQuantity.setText("1" + decimalSeparator + "00");
			}

			if ((v.getId() == R.id.edQuantity) || (v.getId() == R.id.edUnitValue)) {
				EditText edit = (EditText) v;

				if (!edit.getText().toString().isEmpty()) {
					int pos = edit.getText().toString().indexOf(decimalSeparator);

					if (pos > 0) {
						edit.setSelection(pos);
					} else {
						edit.setSelection(edit.length());
					}
				}
			}
		}
	}

	@Override
	public boolean onQueryTextSubmit(String s) {
		return false;
	}

	private String last;

	@Override
	public boolean onQueryTextChange(String s) {
			last = s;
			refreshAdapter();
		return true;
	}

	private void refreshAdapter(){
		adapter.refreshCursorAdapter(last);
	}

}
