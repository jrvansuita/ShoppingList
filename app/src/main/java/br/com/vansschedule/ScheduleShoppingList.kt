package br.com.vansschedule

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import br.com.activity.R
import br.com.vansformat.CustomDateFormat
import br.com.vansschedule.AlarmNotificationShoppingList.Companion.initAlarm
import java.util.Calendar
import java.util.Date

class ScheduleShoppingList(context: Context, private val idShoppingList: Int) : Dialog(context),
    View.OnClickListener {
    private lateinit var btDisplayTime: TextView
    private lateinit var btDisplayDate: TextView
    private val calendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setTitle(R.string.schedule_notification)
        setContentView(R.layout.schedule_notification)

        btDisplayTime = findViewById<TextView>(R.id.schedule_notificatin_display_time)
        btDisplayDate = findViewById<TextView>(R.id.schedule_notificatin_display_date)
        btDisplayTime.setOnClickListener(this)
        btDisplayDate.setOnClickListener(this)

        setCurrentDateTimeOnView()

        findViewById<View>(R.id.schedule_notificatin_cancel).setOnClickListener(this)
        findViewById<View>(R.id.schedule_notificatin_schedule).setOnClickListener(this)
    }

    private fun setCurrentDateTimeOnView() {
        val c = Calendar.getInstance()
        setDateDisplay(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
        setTimeDisplay(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }

    private fun setDateDisplay(year: Int, month: Int, day: Int) {
        calendar.set(year, month, day)
        btDisplayDate.text =
            context.getString(R.string.date) + " " + CustomDateFormat.getFormatedDate(calendar.time)
    }

    private fun setTimeDisplay(hour: Int, minute: Int) {
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 1)
        btDisplayTime.text = context.getString(R.string.time) + " " + pad(hour) + ":" + pad(minute)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.schedule_notificatin_display_time -> TimePickerDialog(
                context,
                timePickerListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()

            R.id.schedule_notificatin_display_date -> DatePickerDialog(
                context,
                datePickerListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()

            R.id.schedule_notificatin_cancel -> dismiss()
            R.id.schedule_notificatin_schedule -> {
                initAlarm(context, idShoppingList, calendar)
                Toast.makeText(
                    context,
                    context.getString(R.string.new_notification_seted) + " " + CustomDateFormat.getFormatedCompletedDate(
                        Date(calendar.timeInMillis)
                    ),
                    Toast.LENGTH_LONG
                ).show()
                dismiss()
            }
        }
    }

    private val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
        setDateDisplay(year, month, day)
    }

    private val timePickerListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
        setTimeDisplay(hour, minute)
    }

    companion object {
        private fun pad(c: Int) = c.toString().padStart(2, '0')
    }
}
