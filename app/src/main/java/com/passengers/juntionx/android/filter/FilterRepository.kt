package com.passengers.juntionx.android.filter

import io.reactivex.subjects.BehaviorSubject

object FilterRepository {
    val filterSubject = BehaviorSubject.createDefault<Filter>(Filter())
}

class Filter(
    val withPredict: Boolean? = null,
    val canDeposit: Boolean? = null,
    val huf: Boolean? = null,
    val eur: Boolean? = null
) {
    val isFilled: Boolean = withPredict == true ||canDeposit == true ||huf == true ||eur == true 
}