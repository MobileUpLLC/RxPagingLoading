package ru.mobileup.rxplce.sample.loading

import android.os.Bundle
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.visibility
import kotlinx.android.synthetic.main.activity_loading.*
import kotlinx.android.synthetic.main.layout_empty_view.*
import kotlinx.android.synthetic.main.layout_error_view.*
import kotlinx.android.synthetic.main.layout_progress_view.*
import me.dmdev.rxpm.base.PmSupportActivity
import ru.mobileup.rxplce.sample.R

class LoadingSampleActivity : PmSupportActivity<LoadingSamplePm>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
    }

    override fun providePresentationModel(): LoadingSamplePm {
        return LoadingSamplePm.createInstance(DataRepository())
    }

    override fun onBindPresentationModel(pm: LoadingSamplePm) {

        pm.content bindTo { contentView.text = it.text }

        pm.isLoading bindTo progressBar.visibility()

        pm.contentVisible bindTo contentView.visibility()
        pm.emptyViewVisible bindTo emptyView.visibility()
        pm.errorViewVisible bindTo errorView.visibility()

        retryButton.clicks() bindTo pm.retryLoadAction
    }
}