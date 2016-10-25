package br.com.vanswatch;

import java.text.DecimalFormatSymbols;
import java.util.Locale;

import br.com.activity.R;
import br.com.vanslisteners.RightDrawableOnTouchListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.widget.EditText;

public class CustomEditTextWatcher implements TextWatcher {
	private EditText editText;
	private int maxNumbers;

	public CustomEditTextWatcher(EditText editText, int maxNumbers) {
		this.editText = editText;
		this.maxNumbers = maxNumbers;
	}

	@Override
	public void afterTextChanged(Editable s) {
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

		if (s.length() != 0) {
			if (maxNumbers == -1) {
				editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_button_cancel, 0);
				
				editText.setOnTouchListener(new RightDrawableOnTouchListener(editText) {
					@Override
					public boolean onDrawableTouch(final MotionEvent event) {
						editText.setText("");
						return true;
					}
				});
				
			} else {
				/* Samsung keyborad bug */
				s = checkDecimalSeparator(s);

				DecimalFormatSymbols d = DecimalFormatSymbols.getInstance(Locale.getDefault());
				int countDec = countDecimalSeparator(s);

				if ((countDec == 0) && (s.length() == maxNumbers)) {
					editText.setText(s.subSequence(0, s.length() - 1));
					editText.setSelection(s.length() - 1);
				} else {
					if (countDec > 1) {
						count = 0;
						String strValue = "";

						for (int i = 0; i < s.length(); i++) {

							if ((s.charAt(i) != d.getDecimalSeparator())) {
								strValue = strValue + s.charAt(i);

							} else {
								if (count == 0) {
									strValue = strValue + s.charAt(i);
								}
								count++;
							}

						}

						editText.setText(strValue);
						if (count > 1) {
							editText.setSelection(editText.getText().toString().length());
						}
					}
				}
			}
		}
	}

	private CharSequence checkDecimalSeparator(CharSequence s) {
		DecimalFormatSymbols d = DecimalFormatSymbols.getInstance(Locale.getDefault());
		char otherDecimalSeparator = (d.getDecimalSeparator() == ',') ? '.' : ',';

		String posS = String.valueOf(s).replace(otherDecimalSeparator, d.getDecimalSeparator());

		if (!posS.equalsIgnoreCase(String.valueOf(s))) {
			editText.setText(posS);
			editText.setSelection(posS.indexOf(d.getDecimalSeparator()) + 1);
		}

		return posS;
	}

	private int countDecimalSeparator(CharSequence strValue) {
		DecimalFormatSymbols d = DecimalFormatSymbols.getInstance(Locale.getDefault());
		int posStart = String.valueOf(strValue).indexOf(d.getDecimalSeparator());

		if (posStart > -1) {
			int posEnd = String.valueOf(strValue).lastIndexOf(d.getDecimalSeparator());

			return posStart == posEnd ? 1 : 2;
		}
		return 0;
	}
}
