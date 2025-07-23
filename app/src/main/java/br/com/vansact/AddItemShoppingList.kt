package br.com.vansact

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import br.com.activity.R
import br.com.bean.ItemShoppingList
import br.com.bean.ShoppingList
import br.com.dao.ItemShoppingListDAO
import br.com.dao.ShoppingListDAO
import br.com.vansadapt.ItemShoppingListCursorAdapter
import br.com.vansanalytics.AnalyticsManager
import br.com.vansexception.VansException
import br.com.vansformat.CustomFloatFormat
import br.com.vansintent.CustomIntentOutside
import br.com.vansprefs.UserPreferences
import br.com.vanswatch.CustomEditTextWatcher
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import java.text.DecimalFormatSymbols
import java.util.Locale

class AddItemShoppingList : Activity(), AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener, CompoundButton.OnCheckedChangeListener, View.OnKeyListener,
    View.OnFocusChangeListener, SearchView.OnQueryTextListener {

    private lateinit var adapter: ItemShoppingListCursorAdapter
    private lateinit var shoppingList: ShoppingList
    private lateinit var edDescription: AutoCompleteTextView
    private lateinit var edQuantity: EditText
    private lateinit var edUnitValue: EditText
    private var mSearchView: SearchView? = null
    private var lastQuery: String? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseAnalytics = Firebase.analytics
        setupWindow()
        setContentView(R.layout.activity_add_item_shopping_list)
        applyWindowInsets()
        shoppingList = getShoppingListFromIntent()
        title = shoppingList.name
        setupViews()
        setupListeners()
        setupAdapter()
        configureInputFields()

        AnalyticsManager.getInstance().logAddItemScreenView(this, shoppingList.id)
    }

    override fun onResume() {
        super.onResume()
        edUnitValue.hint = CustomFloatFormat.getMonetaryMaskedValue(this, 0.0)
        edQuantity.hint = CustomFloatFormat.getSimpleFormatedValue(0.0)
        try {
            refreshListView()
        } catch (e: VansException) {
            showToast(e.message)
        }
    }

    private fun setupWindow() {
        // Replace deprecated window decoration code
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            view.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            windowInsets
        }
    }

    private fun getShoppingListFromIntent(): ShoppingList {
        return try {
            val shoppingList = ShoppingListDAO.select(
                this, intent.extras!!.getInt(getString(R.string.id_shopping_list))
            )
            requireNotNull(shoppingList) { "ShoppingList not found for given id" }
        } catch (e: VansException) {
            showToast(e.message)
            throw e
        }
    }

    private fun setupViews() {
        edUnitValue = findViewById(R.id.edUnitValue)
        edQuantity = findViewById(R.id.edQuantity)
        edDescription = findViewById(R.id.edDescription)
    }

    private fun setupListeners() {
        findViewById<ListView>(R.id.lvItemShoppingList).apply {
            onItemClickListener = this@AddItemShoppingList
            onItemLongClickListener = this@AddItemShoppingList
            addHeaderView(
                layoutInflater.inflate(
                    R.layout.header_list_view_item_shopping_list, null
                ), null, false
            )
        }
        edUnitValue.setOnKeyListener(this)
        edUnitValue.addTextChangedListener(CustomEditTextWatcher(edUnitValue, 5))
        edUnitValue.onFocusChangeListener = this

        edQuantity.addTextChangedListener(CustomEditTextWatcher(edQuantity, 4))
        edQuantity.onFocusChangeListener = this

        edDescription.onItemClickListener = this
        edDescription.addTextChangedListener(CustomEditTextWatcher(edDescription, -1))
    }

    private fun setupAdapter() {
        adapter = ItemShoppingListCursorAdapter(this, shoppingList.id)
        findViewById<ListView>(R.id.lvItemShoppingList).adapter = adapter
    }

    private fun configureInputFields() {
        edUnitValue.visibility =
            if (UserPreferences.getShowUnitValue(this)) View.VISIBLE else View.GONE
        edQuantity.visibility =
            if (UserPreferences.getShowQuantity(this)) View.VISIBLE else View.GONE

        when {
            !UserPreferences.getShowQuantity(this) && !UserPreferences.getShowUnitValue(this) -> {
                edDescription.imeOptions = EditorInfo.IME_ACTION_GO
                edDescription.setOnKeyListener(this)
            }

            !UserPreferences.getShowUnitValue(this) -> {
                edQuantity.imeOptions = EditorInfo.IME_ACTION_GO
                edQuantity.setOnKeyListener(this)
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        when (parent.id) {
            R.id.lvItemShoppingList -> handleListItemClick(position - 1)
            else -> handleAutoCompleteClick(parent, position)
        }
    }

    private fun handleListItemClick(position: Int) {
        val item = adapter.getItem(position)
        if (adapter.idSelected != item.id) {
            ActivityCompat.invalidateOptionsMenu(this)
            setViewValues(item.description, item.unitValue, item.quantity)
            adapter.idSelected = item.id
            refreshAdapter()
        }
    }

    private fun handleAutoCompleteClick(parent: AdapterView<*>, position: Int) {
        val value = parent.adapter.getItem(position)
        if (value is String) {
            try {
                val item = ItemShoppingListDAO.findLastInserted(this, value)
                setViewValues(item!!.description, item.unitValue, item.quantity)
            } catch (e: VansException) {
                showToast(e.message)
            }
        }
    }

    override fun onItemLongClick(
        parent: AdapterView<*>, view: View, position: Int, id: Long
    ): Boolean {
        val item = parent.adapter.getItem(position) ?: return true
        val pos = position - 1
        showDeleteConfirmation(pos, adapter.getItem(pos).description)
        return true
    }

    private fun showDeleteConfirmation(position: Int, description: String) {
        AlertDialog.Builder(this).setTitle(R.string.delete_question)
            .setMessage("${getString(R.string.want_delete_item)} '$description'")
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ -> deleteItem(position) }.show()
    }

    private fun deleteItem(position: Int) {
        try {
            ItemShoppingListDAO.delete(this, adapter.getItem(position).id)
            cancelEditing()
        } catch (e: VansException) {
            showToast(e.message)
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        try {
            val item = ItemShoppingListDAO.select(this, buttonView.tag.toString().toInt())
            if (item!!.checked != isChecked) {
                item.checked = isChecked
                ItemShoppingListDAO.update(this, item)
                refreshListView()
            }
        } catch (e: VansException) {
            showToast(e.message)
        }
    }

    override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && edDescription.text.isNotEmpty()) {
            tryInsertOrUpdateItem()
            return true
        }
        return false
    }

    private fun tryInsertOrUpdateItem() {
        try {
            if (isInsertValid()) {
                val description = edDescription.text.toString().trim()
                val unitValue = CustomFloatFormat.parseFloat(edUnitValue.text.toString())
                val quantity = CustomFloatFormat.parseFloat(edQuantity.text.toString())
                if (adapter.idSelected > 0) {
                    val item = ItemShoppingListDAO.select(this, adapter.idSelected)!!
                    item.description = description
                    item.quantity = quantity
                    item.unitValue = unitValue
                    ItemShoppingListDAO.update(this, item)
                } else {
                    ItemShoppingListDAO.insert(
                        this, ItemShoppingList(
                            0, shoppingList.id, description, unitValue, quantity, false
                        )
                    )
                }
                cancelEditing()
            }
        } catch (e: Exception) {
            showToast(e.message)
            cancelEditing()
        }
    }

    private fun cancelEditing() {
        adapter.idSelected = 0
        ActivityCompat.invalidateOptionsMenu(this)
        clearEditTexts()
        refreshListView()
    }

    private fun clearEditTexts() = setViewValues("", 0f, 0f)

    private fun setViewValues(description: String, unitValue: Float, quantity: Float) {
        edDescription.setText(description)
        edDescription.setSelection(description.length)
        edDescription.requestFocus()
        edUnitValue.setText(if (unitValue > 0) CustomFloatFormat.getSimpleFormatedValue(unitValue.toDouble()) else "")
        edQuantity.setText(if (quantity > 0) CustomFloatFormat.getSimpleFormatedValue(quantity.toDouble()) else "")
    }

    private fun isInsertValid(): Boolean {
        val desc = edDescription.text.toString()
        if (desc.isEmpty()) {
            edDescription.requestFocus()
            showToast(getString(R.string.info_desc))
            return false
        }
        if (adapter.idSelected == 0 && isDescriptionDuplicated(desc)) {
            showToast(getString(R.string.item_already_inserted))
            return false
        }
        return true
    }

    private fun refreshListView() {
        refreshAdapter()
        edDescription.setAdapter(ItemShoppingListDAO.selectAutoComplete(this, adapter.descriptions))
        val showFooter =
            adapter.count > 0 && (UserPreferences.getShowQuantity(this) || UserPreferences.getShowUnitValue(
                this
            ))
        findViewById<TextView>(R.id.header_description_collumn).setText(
            if (adapter.count == 0) R.string.no_item_added else R.string.description_title
        )
        findViewById<View>(R.id.header_quantity_collumn).visibility =
            if (showFooter && UserPreferences.getShowQuantity(this)) View.VISIBLE else View.GONE
        findViewById<View>(R.id.header_unit_value_collumn).visibility =
            if (showFooter && UserPreferences.getShowUnitValue(this)) View.VISIBLE else View.GONE
        findViewById<View>(R.id.footer_bar).visibility = if (showFooter) View.VISIBLE else View.GONE
        if (showFooter) {
            findViewById<TextView>(R.id.footer_total_sum).text = adapter.totalValue
            findViewById<TextView>(R.id.footer_total_quant).text = adapter.totalQuant
        }
    }

    private fun isDescriptionDuplicated(value: String): Boolean {
        val trimmed = value.trim()
        return (0 until adapter.count).any {
            adapter.getItem(it).description.equals(
                trimmed, ignoreCase = true
            )
        }
    }

    private fun areAllItemsChecked(): Boolean =
        adapter.count > 0 && (0 until adapter.count).all { adapter.getItem(it).checked }

    private fun deleteAllItems(onlyChecked: Boolean) {
        if (adapter.count > 0) {
            AlertDialog.Builder(this).setTitle(R.string.delete_question)
                .setMessage(getString(if (onlyChecked) R.string.want_delete_all_selected_itens else R.string.want_delete_all_items))
                .setNegativeButton(R.string.no, null).setPositiveButton(R.string.yes) { _, _ ->
                    try {
                        ItemShoppingListDAO.deleteAllList(this, shoppingList.id, onlyChecked)
                        cancelEditing()
                    } catch (e: VansException) {
                        showToast(e.message)
                    }
                }.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.add_item_shopping_list_menu, menu)
        mSearchView = menu.findItem(R.id.search).actionView as? SearchView
        mSearchView?.apply {
            setOnQueryTextListener(this@AddItemShoppingList)
            queryHint = getString(R.string.search)
        }
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.action_cancel).isVisible = adapter.idSelected != 0
        menu.findItem(R.id.action_select_all).title =
            if (areAllItemsChecked()) getString(R.string.desselect_all) else getString(R.string.select_all)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        try {
            when (item.itemId) {
                R.id.action_done -> tryInsertOrUpdateItem()
                R.id.action_cancel -> cancelEditing()
                R.id.action_delete_all -> deleteAllItems(false)
                R.id.action_select_all -> {
                    ItemShoppingListDAO.checkAllItems(this, shoppingList.id, !areAllItemsChecked())
                    cancelEditing()
                }

                R.id.action_delete_selecteds -> deleteAllItems(true)
                R.id.action_share -> {
                    CustomIntentOutside.shareShoppingListText(this, shoppingList.id)
                    cancelEditing()
                }

                R.id.action_settings -> {
                    cancelEditing()
                    startActivity(Intent(this, UserPreferences::class.java))
                }

                R.id.action_barcode_scan -> {
                    CustomIntentOutside.barcodeScanner(this, BARCODE_SCANNER_REQUEST_CODE)
                    cancelEditing()
                }

                else -> return super.onOptionsItemSelected(item)
            }
            return true
        } catch (e: VansException) {
            showToast(e.message)
        }
        return false
    }

    // Replace deprecated Activity with AppCompatActivity methods
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == BARCODE_SCANNER_REQUEST_CODE) {
            data?.getStringExtra("SCAN_RESULT")?.let { barcode ->
                edDescription.setText(barcode)
                edDescription.requestFocus()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            ActivityCompat.invalidateOptionsMenu(this)
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            val decimalSeparator =
                DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator
            if (v.id == R.id.edQuantity && edQuantity.text.isEmpty()) {
                edQuantity.setText("1$decimalSeparator" + "00")
            }
            if (v.id == R.id.edQuantity || v.id == R.id.edUnitValue) {
                val edit = v as EditText
                if (edit.text.isNotEmpty()) {
                    val pos = edit.text.indexOf(decimalSeparator)
                    edit.setSelection(if (pos > 0) pos else edit.length())
                }
            }
        }
    }

    override fun onQueryTextSubmit(query: String) = false

    override fun onQueryTextChange(newText: String): Boolean {
        lastQuery = newText
        refreshAdapter()
        return true
    }

    private fun refreshAdapter() = adapter.refreshCursorAdapter(lastQuery)

    private fun showToast(message: String?) =
        Toast.makeText(this, message ?: "", Toast.LENGTH_LONG).show()

    companion object {
        private const val BARCODE_SCANNER_REQUEST_CODE = 2
    }
}
