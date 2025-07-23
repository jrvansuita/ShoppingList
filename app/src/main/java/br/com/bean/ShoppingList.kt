package br.com.bean;

import java.util.Date;

import android.content.Context;
import br.com.activity.R;

public class ShoppingList {
	private int id;
	private String name;
	private Date date;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(Context context, String name) {
		this.name = (name.isEmpty() ? context.getString(R.string.untitled) : name);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
	
	public ShoppingList(Context context) {
		this(context, 0, context.getString(R.string.untitled), new Date());
	}

	public ShoppingList(Context context, int id, String name, Date date) {
		setId(id);
		setName(context, name);
		setDate(date);
	}	
}
