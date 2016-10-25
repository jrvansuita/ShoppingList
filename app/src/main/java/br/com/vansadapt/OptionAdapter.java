package br.com.vansadapt;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import br.com.activity.R;

public class OptionAdapter extends ArrayAdapter<String> {

	private String[] optionList;
	private Context context;

	public OptionAdapter(Context context, String[] optionList) {
		super(context, R.layout.adapter_options);
		this.optionList = optionList;
		this.context = context;



	}




	@Override
	public String getItem(int position) {
		return optionList[position];
	}

	@Override
	public int getCount() {
		return optionList.length;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.adapter_options, null);
		}

		TextView tvOptionName = (TextView) convertView.findViewById(R.id.tvOptionName);
		tvOptionName.setText(String.valueOf(optionList[position]));

		return convertView;
	}

}