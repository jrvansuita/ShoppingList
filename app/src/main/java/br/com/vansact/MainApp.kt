package br.com.vansact

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import br.com.activity.R
import br.com.bean.ShoppingList
import br.com.dao.ShoppingListDAO
import br.com.vansadapt.ShoppingListCursorAdapter
import br.com.vansads.AdsManager
import br.com.vansanalytics.AnalyticsManager
import br.com.vansdialog.CustomDialogShoppingListOptions
import br.com.vansexception.VansException
import br.com.vansintent.CustomIntentOutside
import br.com.vansprefs.UserPreferences
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics

class MainApp : Activity(), AdapterView.OnItemClickListener,
    AdapterView.OnItemLongClickListener,
    DialogInterface.OnDismissListener, View.OnClickListener {

    private lateinit var adapter: ShoppingListCursorAdapter
    private lateinit var lvShoppingList: ListView
    private lateinit var headerView: View
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindow()

        setContentView(R.layout.activity_main_app)
        applyWindowInsets()

        firebaseAnalytics = Firebase.analytics

        adapter = ShoppingListCursorAdapter(this)
        lvShoppingList = findViewById(R.id.lvShoppingList)
        headerView = layoutInflater.inflate(R.layout.adapter_shopping_list, null).apply {
            findViewById<TextView>(R.id.nameShoppingList).setText(R.string.no_list_added)
            findViewById<View>(R.id.view_date_shopping_list).visibility = View.INVISIBLE
            setBackgroundResource(R.drawable.custom_blanck_backgroud)
            setOnClickListener(this@MainApp)
        }
        findViewById<LinearLayout>(R.id.activity_main_app_id).addView(headerView)
        lvShoppingList.adapter = adapter

        AnalyticsManager.getInstance().logMainScreenView(this)

        AdsManager.loadAdBanner(findViewById(R.id.ads_holder))
    }

    private fun setupWindow() {
        // Replace deprecated window decoration code
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
    }

    override fun onResume() {
        super.onResume()
        lvShoppingList.onItemClickListener = this
        lvShoppingList.onItemLongClickListener = this
        refreshListView()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_app_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_add -> {
            addNew(); true
        }

        R.id.action_delete_all -> {
            confirmDeleteAll(); true
        }

        R.id.action_update -> {
            CustomIntentOutside.UpdateApp(this); true
        }

        R.id.action_settings -> {
            startActivity(Intent(this, UserPreferences::class.java)); true
        }

        else -> super.onOptionsItemSelected(item)
    }

    private fun confirmDeleteAll() {
        if (adapter.count == 0) return
        AlertDialog.Builder(this).setTitle(R.string.delete_question)
            .setMessage(getString(R.string.want_delete_all_list))
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { _, _ -> deleteAllLists() }.show()
    }

    private fun deleteAllLists() {
        try {
            ShoppingListDAO.deleteAll(this)
            refreshListView()
        } catch (e: VansException) {
            showToast(e.message)
        }
    }

    private fun addNew() {
        val edName = EditText(this)
        AlertDialog.Builder(this).setTitle(getString(R.string.save))
            .setMessage(getString(R.string.title_new)).setView(edName)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val shoppingList = ShoppingList(this).apply { name = edName.text.toString() }
                try {
                    val id =
                        ShoppingListDAO.insert(this, shoppingList)?.id ?: return@setPositiveButton
                    startActivity(
                        Intent(
                            this,
                            AddItemShoppingList::class.java
                        ).putExtra(getString(R.string.id_shopping_list), id)
                    )
                } catch (e: VansException) {
                    showToast(e.message)
                }
            }.setNegativeButton(android.R.string.cancel, null).show()
    }

    private fun refreshListView() {
        adapter.refreshCursorAdapter()
        headerView.visibility = if (adapter.count == 0) View.VISIBLE else View.GONE
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        startActivity(
            Intent(
                this,
                AddItemShoppingList::class.java
            ).putExtra(getString(R.string.id_shopping_list), adapter.getItem(position).id)
        )
    }

    override fun onItemLongClick(
        parent: AdapterView<*>?, view: View?, position: Int, id: Long
    ): Boolean {
        CustomDialogShoppingListOptions(this, adapter.getItem(position).id).apply {
            setOnDismissListener(this@MainApp)
            show()
        }
        return true
    }

    override fun onDismiss(dialog: DialogInterface?) = refreshListView()

    override fun onClick(v: View?) {
        if (adapter.count == 0) addNew()
    }

    private fun applyWindowInsets() {
        val rootView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sys.left, sys.top, sys.right, sys.bottom)
            WindowInsetsCompat.CONSUMED
        }
    }

    private fun showToast(msg: String?) = Toast.makeText(this, msg ?: "", Toast.LENGTH_LONG).show()
}
