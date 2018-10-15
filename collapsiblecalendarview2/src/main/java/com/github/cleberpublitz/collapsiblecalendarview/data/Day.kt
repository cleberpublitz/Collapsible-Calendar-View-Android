package com.github.cleberpublitz.collapsiblecalendarview.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by shrikanthravi on 06/03/18.
 */

@Parcelize
data class Day(
        val year: Int,
        val month: Int,
        val day: Int
) : Parcelable {
    override fun describeContents(): Int = 0
}
