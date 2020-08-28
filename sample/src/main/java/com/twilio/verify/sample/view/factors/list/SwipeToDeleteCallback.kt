/*
 * Copyright (c) 2020, Twilio Inc.
 */
package com.twilio.verify.sample.view.factors.list

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder

class SwipeToDeleteCallback(
  private val delete: (Int) -> Unit,
  private val icon: Drawable,
  private val background: ColorDrawable = ColorDrawable(Color.RED)
) : SimpleCallback(
  0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
) {
  override fun onMove(
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    target: ViewHolder
  ): Boolean {
    // used for up and down movements
    return false
  }

  override fun onSwiped(
    viewHolder: ViewHolder,
    direction: Int
  ) {
    val position = viewHolder.adapterPosition
    delete(position)
  }

  override fun onChildDraw(
    c: Canvas,
    recyclerView: RecyclerView,
    viewHolder: ViewHolder,
    dX: Float,
    dY: Float,
    actionState: Int,
    isCurrentlyActive: Boolean
  ) {
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    val itemView: View = viewHolder.itemView
    val backgroundCornerOffset = 20 // so background is behind the rounded corners of itemView
    val iconMargin: Int = (itemView.height - icon.intrinsicHeight) / 2
    val iconTop: Int = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
    val iconBottom = iconTop + icon.intrinsicHeight
    when {
      dX > 0 -> { // Swiping to the right
        val iconLeft: Int = itemView.left + iconMargin + icon.intrinsicWidth
        val iconRight: Int = itemView.left + iconMargin
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        background.setBounds(
          itemView.left, itemView.top,
          itemView.left + dX.toInt() + backgroundCornerOffset, itemView.bottom
        )
      }
      dX < 0 -> { // Swiping to the left
        val iconLeft: Int = itemView.right - iconMargin - icon.intrinsicWidth
        val iconRight: Int = itemView.right - iconMargin
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
        background.setBounds(
          itemView.right + dX.toInt() - backgroundCornerOffset,
          itemView.top, itemView.right, itemView.bottom
        )
      }
      else -> { // view is unSwiped
        background.setBounds(0, 0, 0, 0)
      }
    }
    background.draw(c)
    icon.draw(c)
  }
}
