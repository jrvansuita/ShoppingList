package br.com.vansschedule;

import java.util.Calendar;
import java.util.Date;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import br.com.activity.R;
import br.com.vansformat.CustomDateFormat;

public class ScheduleShoppingList extends Dialog implements android.view.View.OnClickListener {

    private TextView btDisplayTime;
    private TextView btDisplayDate;
    private final int idShoppingList;

    Calendar calendar = Calendar.getInstance();

    public ScheduleShoppingList(Context context, final int idShoppingList) {
        super(context);
        this.idShoppingList = idShoppingList;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setCancelable(true);
        setTitle(R.string.schedule_notification);
        setContentView(R.layout.schedule_notification);

        btDisplayTime = findViewById(R.id.schedule_notificatin_display_time);
        btDisplayTime.setOnClickListener(this);

        btDisplayDate = findViewById(R.id.schedule_notificatin_display_date);
        btDisplayDate.setOnClickListener(this);

        setCurrentDateTimeOnView();

        findViewById(R.id.schedule_notificatin_cancel).setOnClickListener(this);
        findViewById(R.id.schedule_notificatin_schedule).setOnClickListener(this);

    }

    public void setCurrentDateTimeOnView() {
        Calendar c = Calendar.getInstance();

        setDateDisplay(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        setTimeDisplay(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE));
    }

    private void setDateDisplay(int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        btDisplayDate.setText(getContext().getString(R.string.date) + " " + CustomDateFormat.getFormatedDate(calendar.getTime()));
    }

    private void setTimeDisplay(int hour, int minute) {

        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 1);

        // set current time into textview
        btDisplayTime.setText(getContext().getString(R.string.time) + " " + pad(hour) + ":" + pad(minute));
    }

    private static String pad(int c) {
        if (c >= 10)
            return String.valueOf(c);
        else
            return "0" + c;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.schedule_notificatin_display_time) {
            new TimePickerDialog(getContext(), timePickerListener, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        } else if (id == R.id.schedule_notificatin_display_date) {
            new DatePickerDialog(getContext(), datePickerListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        } else if (id == R.id.schedule_notificatin_cancel) {
            dismiss();
        } else if (id == R.id.schedule_notificatin_schedule) {
            AlarmNotificationShoppingList.initAlarm(getContext(), idShoppingList, calendar);
            Toast.makeText(getContext(), getContext().getResources().getString(R.string.new_notification_seted) + " " + CustomDateFormat.getFormatedCompletedDate(new Date(calendar.getTimeInMillis())), Toast.LENGTH_LONG).show();
            dismiss();
        }
    }

    private final DatePickerDialog.OnDateSetListener datePickerListener = (view, year, monthOfYear, dayOfMonth) -> setDateDisplay(year, monthOfYear, dayOfMonth);

    private final TimePickerDialog.OnTimeSetListener timePickerListener = (view, selectedHour, selectedMinute) -> setTimeDisplay(selectedHour, selectedMinute);

}