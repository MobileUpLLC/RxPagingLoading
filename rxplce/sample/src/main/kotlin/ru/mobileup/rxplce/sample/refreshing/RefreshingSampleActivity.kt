package ru.mobileup.rxplce.sample.refreshing

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import com.jakewharton.rxbinding3.swiperefreshlayout.refreshes
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import kotlinx.android.synthetic.main.activity_refreshing.*
import kotlinx.android.synthetic.main.layout_empty_view.*
import kotlinx.android.synthetic.main.layout_error_view.*
import kotlinx.android.synthetic.main.layout_progress_view.*
import me.dmdev.rxpm.base.PmSupportActivity
import ru.mobileup.rxplce.sample.R

class RefreshingSampleActivity : PmSupportActivity<RefreshingSamplePm>() {

    val randomNumbersRepository = RandomNumbersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_refreshing)

        updateButton.setOnClickListener {
            randomNumbersRepository.updateNumbers(
                randomNumbersRepository.generateRandomNumbers()
            )
        }

        setupSettings()
    }

    override fun providePresentationModel(): RefreshingSamplePm {
        return RefreshingSamplePm.createInstance(randomNumbersRepository)
    }

    override fun onBindPresentationModel(pm: RefreshingSamplePm) {

        pm.content bindTo {
            val sb = StringBuilder("Random numbers:\n\n")
            contentView.text = it.joinTo(buffer = sb, separator = "\n").toString()
        }

        pm.isLoading bindTo progressBar.visibility()
        pm.refreshEnabled bindTo swipeRefreshLayout::setEnabled
        pm.isRefreshing bindTo swipeRefreshLayout::setRefreshing

        pm.contentVisible bindTo contentView.visibility()
        pm.emptyViewVisible bindTo emptyView.visibility()
        pm.errorViewVisible bindTo errorView.visibility()

        pm.errorDialog bindTo { message, _ ->
            AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("ok", null)
                .create()
        }

        swipeRefreshLayout.refreshes() bindTo pm.refreshAction
        retryButton.clicks() bindTo pm.retryLoadAction
    }

    private fun setupSettings() {

        val spinner = toolbar.menu.findItem(R.id.spinner).actionView as Spinner
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.repository_settings, android.R.layout.simple_spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> randomNumbersRepository.mode = RandomNumbersRepository.Mode.NORMAL
                    1 -> randomNumbersRepository.mode = RandomNumbersRepository.Mode.ERROR
                    2 -> randomNumbersRepository.mode = RandomNumbersRepository.Mode.EMPTY_DATA
                    3 -> randomNumbersRepository.mode = RandomNumbersRepository.Mode.RANDOM_ERROR
                }
            }

        }
    }
}