package com.passengers.juntionx.android.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.Switch
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.passengers.juntionx.android.R
import com.passengers.juntionx.android.filter.Filter
import com.passengers.juntionx.android.filter.FilterRepository

class FilterFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_filter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<Toolbar>(R.id.toobar)

        val filter = FilterRepository.filterSubject.value

        val withPredictionSwitch = view.findViewById<Switch>(R.id.prediction)
        val canDepositSwitch = view.findViewById<Switch>(R.id.can_deposit)
        val hufCheckBox = view.findViewById<CheckBox>(R.id.huf)
        val eurCheckBox = view.findViewById<CheckBox>(R.id.eur)

        toolbar.title = "ATM filter"
        toolbar.setNavigationIcon(R.drawable.ic_toolbar_close)
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }
        toolbar.inflateMenu(R.menu.filter)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_clear) {
                withPredictionSwitch.isChecked = false
                canDepositSwitch.isChecked = false
                hufCheckBox.isChecked = false
                eurCheckBox.isChecked = false
                return@setOnMenuItemClickListener true
            }
            false
        }

        withPredictionSwitch.isChecked = filter?.dontProposeBestAtm == true
        canDepositSwitch.isChecked = filter?.canDeposit == true
        hufCheckBox.isChecked = filter?.huf == true
        eurCheckBox.isChecked = filter?.eur == true

        view.findViewById<Button>(R.id.apply).setOnClickListener {
            FilterRepository.filterSubject.onNext(
                Filter(
                    withPredictionSwitch.isChecked,
                    canDepositSwitch.isChecked,
                    hufCheckBox.isChecked,
                    eurCheckBox.isChecked
                )
            )
            activity?.onBackPressed()
        }
    }
}