package br.com.bean;

import android.content.Context;
import br.com.activity.R;

public class ItemShoppingList {
	private int id;
	private int idShoppingList;
	private String description;
	private float unitValue;
	private float quantity;

	private boolean checked;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdShoppingList() {
		return idShoppingList;
	}

	public void setIdShoppingList(int idShoppingList) {
		this.idShoppingList = idShoppingList;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float getUnitValue() {
		return unitValue;
	}

	public void setUnitValue(float unitValue) {
		this.unitValue = unitValue;
	}

	public float getQuantity() {
		return quantity;
	}


	public float getTotal() {
		return quantity * unitValue;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public ItemShoppingList(Context context) {
		this(0, 0, context.getString(R.string.no_description), 0, 0, false);
	}

	public ItemShoppingList(int id, int idShoppingList, String description, float unitValue, float quantity, boolean checked) {
		setId(id);
		setIdShoppingList(idShoppingList);
		setDescription(description);
		setUnitValue(unitValue);
		setQuantity(quantity);
		setChecked(checked);
	}

}
