package com.passengers.juntionx.android.ui

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.passengers.juntionx.android.R
import com.passengers.juntionx.android.network.ATMApiProvider
import com.passengers.juntionx.android.network.model.ATMIntent
import com.passengers.juntionx.android.network.model.AtmOutputData
import com.passengers.juntionx.android.network.model.AtmWithDistance
import com.passengers.juntionx.android.network.model.LoadLevel
import com.passengers.juntionx.android.user.UserRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


class AtmItemViewHolder(
    val userRepository: UserRepository,
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

        when (atmOutputData.loadLevel) {
            LoadLevel.EMPTY -> loadLevelImgView?.setImageResource(R.drawable.ic_load_level_0)
            LoadLevel.LEVEL_1 -> loadLevelImgView?.setImageResource(R.drawable.ic_load_level_1)
            LoadLevel.LEVEL_2 -> loadLevelImgView?.setImageResource(R.drawable.ic_load_level_2)
            LoadLevel.LEVEL_3 -> loadLevelImgView?.setImageResource(R.drawable.ic_load_level_3)
            LoadLevel.LEVEL_4 -> loadLevelImgView?.setImageResource(R.drawable.ic_load_level_4)
        }
        distanceView?.text = "${atmOutputData.realDistanceInMeters?.toInt()} meters"
        locationValueView?.text = "${atm.city}, ${atm.zipCD}, ${atm.address}"

        getDirectionButtonView?.setOnClickListener {
            itemView.context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?daddr=${atm.geoX},${atm.geoY}")
                )
            )
            if (atmOutputData.realDistanceInMeters != null) {
                ATMApiProvider.get().postATMIntent(
                    ATMIntent(
                        userId = userRepository.getUserId(),
                        atmId = atm.id,
                        realDistanceToAtmInMeters = atmOutputData.realDistanceInMeters,
                        averageHistoricalWaitingTime = atmOutputData.averageHistoricalWaitingTime!!,
                        realtimeWaitingTime = atmOutputData.realtimeWaitingTime!!
                    )
                ).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .onErrorComplete()
                    .subscribe()
            }
        }
    }
}