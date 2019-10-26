package com.passengers.juntionx.android.ui

import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.passengers.juntionx.android.R
import com.passengers.juntionx.android.network.model.AtmOutputData
import com.passengers.juntionx.android.network.model.AtmWithDistance


class AtmItemViewHolder(
    val itemView: View
) {

    private var atmInfoView: ConstraintLayout? = null
    private var topAnchorButtonView: View? = null
    private var closeView: ImageView? = null
    private var atmTypeImgView: ImageView? = null
    private var atmTitleConstView: TextView? = null
    private var atmTitleView: TextView? = null
    private var loadLevelImgView: ImageView? = null
    private var dotView: View? = null
    private var distanceView: TextView? = null
    private var dividerView: View? = null
    private var locationTitleView: TextView? = null
    private var locationValueView: TextView? = null
    private var infoButtonView: Button? = null
    private var getDirectionButtonView: Button? = null

    init {
        onBindViews(itemView)
    }

    protected fun onBindViews(view: View) {
        atmInfoView = view.findViewById(R.id.atm_info)
        topAnchorButtonView = view.findViewById(R.id.top_anchor_button)
        closeView = view.findViewById(R.id.close)
        atmTypeImgView = view.findViewById(R.id.atm_type_img)
        atmTitleConstView = view.findViewById(R.id.atm_title_const)
        atmTitleView = view.findViewById(R.id.atm_title)
        loadLevelImgView = view.findViewById(R.id.load_level_img)
        dotView = view.findViewById(R.id.dot)
        distanceView = view.findViewById(R.id.distance)
        dividerView = view.findViewById(R.id.divider)
        locationTitleView = view.findViewById(R.id.location_title)
        locationValueView = view.findViewById(R.id.location_value)
        infoButtonView = view.findViewById(R.id.info_button)
        getDirectionButtonView = view.findViewById(R.id.get_direction_button)
    }

    fun bind(atmOutputData: AtmOutputData) {
        val atm = atmOutputData.atm
        if (atmOutputData.atm.canDeposit) {
            atmTitleView?.text = "Withdrawal & Deposite"
            atmTypeImgView?.setImageResource(R.drawable.ic_atm_type_withdrawal_and_dep)
        } else {
            atmTitleView?.text = "Withdrawal"
            atmTypeImgView?.setImageResource(R.drawable.ic_atm_type_withdrawal)
        }
        distanceView?.text = "${atmOutputData.distanceInMeters?.toInt()} meters"
        locationValueView?.text = "${atm.city}, ${atm.zipCD}, ${atm.address}"
    }
}