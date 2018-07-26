package com.github.praveen2gemini

import android.content.Context
import android.content.res.TypedArray
import android.os.Build
import android.support.annotation.IdRes
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.autofill.AutofillManager
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout

import java.util.ArrayList
import java.util.HashSet

/**
 * @author Praveen on 26/07/18.
 */
class CheckBoxGroup : LinearLayout {


    private val mCheckedIds = ArrayList<Int>()
    // tracks children checkbox buttons checked state
    private lateinit var mChildOnCheckedChangeListener: CompoundButton.OnCheckedChangeListener
    private lateinit var mPassThroughListener: PassThroughHierarchyChangeListener
    private var mOnCheckedChangeListener: CheckBoxGroup.OnCheckedChangeListener? = null

    /**
     *
     * Returns the identifier of the selected checkbox button in this group.
     * Upon empty selection, the returned value is -1.
     *
     * @return the unique id of the selected checkbox button in this group
     * @attr ref android.R.styleable#checkboxGroup_checkedButton
     * @see .check
     * @see .clearCheck
     */
    val checkedCheckboxButtonId: ArrayList<Int>
        @IdRes
        get() = ArrayList(HashSet(mCheckedIds))

    constructor(context: Context) : super(context) {
        orientation = LinearLayout.VERTICAL
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        orientation = LinearLayout.VERTICAL
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {

        // checkboxGroup is important by default, unless app developer overrode attribute.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (importantForAutofill == View.IMPORTANT_FOR_AUTOFILL_AUTO) {
                importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES
            }
        }
        orientation = LinearLayout.VERTICAL
        init()
    }


    private fun init() {
        mChildOnCheckedChangeListener = CheckedStateTracker()
        mPassThroughListener = PassThroughHierarchyChangeListener()
        super.setOnHierarchyChangeListener(mPassThroughListener)
    }

    /**
     *
     * Sets the selection to the checkbox button whose identifier is passed in
     * parameter. Using -1 as the selection identifier clears the selection;
     * such an operation is equivalent to invoking
     *
     * @param id the unique id of the checkbox button to select in this group
     */
    fun check(@IdRes id: Int) {
        val isChecked = mCheckedIds.contains(id)
        if (id != -1) {
            setCheckedStateForView(id, isChecked)
        }
        setCheckedId(id, isChecked)
    }


    private fun setCheckedId(id: Int, isChecked: Boolean) {
        if (mCheckedIds.contains(id) && !isChecked) {
            mCheckedIds.remove(id)
        } else {
            mCheckedIds.add(id)
        }
        mOnCheckedChangeListener?.onCheckedChanged(this, id, isChecked)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val afm = context.getSystemService(AutofillManager::class.java)
            afm?.notifyValueChanged(this)
        }
    }

    private fun setCheckedStateForView(viewId: Int, checked: Boolean) {
        val checkedView = findViewById<View>(viewId)
        if (checkedView != null && checkedView is CheckBox) {
            checkedView.isChecked = checked
        }
    }

    override fun generateLayoutParams(attrs: AttributeSet): LayoutParams {
        return CheckBoxGroup.LayoutParams(context, attrs)
    }

    override fun setOnHierarchyChangeListener(listener: ViewGroup.OnHierarchyChangeListener) {
        mPassThroughListener.mOnHierarchyChangeListener = listener
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

        for (mCheckedId in mCheckedIds) {
            // checks the appropriate checkbox button as requested in the XML file
            if (mCheckedId <= 0) continue
            setCheckedStateForView(mCheckedId, true)
            setCheckedId(mCheckedId, true)

        }
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        if (child is CheckBox) {
            if (child.isChecked) {
                setCheckedId(child.id, true)
            }
        }

        super.addView(child, index, params)
    }

    /**
     *
     * Clears the selection. When the selection is cleared, no checkbox button
     * in this group is selected
     * null.
     */
    fun clearCheck() {
        check(-1)
    }

    /**
     *
     * Register a callback to be invoked when the checked checkbox button
     * changes in this group.
     *
     * @param listener the callback to call on checked state change
     */
    fun setOnCheckedChangeListener(listener: OnCheckedChangeListener) {
        mOnCheckedChangeListener = listener
    }

    /**
     *
     * Interface definition for a callback to be invoked when the checked
     * checkbox button changed in this group.
     */
    interface OnCheckedChangeListener {
        /**
         *
         * Called when the checked checkbox button has changed. When the
         * selection is cleared, checkedId is -1.
         *
         * @param group     the group in which the checked checkbox button has changed
         * @param checkedId the unique identifier of the newly checked checkbox button
         */
        fun onCheckedChanged(group: CheckBoxGroup, @IdRes checkedId: Int, isChecked: Boolean)
    }

    /**
     *
     * This set of layout parameters defaults the width and the height of
     * the children to [.WRAP_CONTENT] when they are not specified in the
     * XML file. Otherwise, this class ussed the value read from the XML file.
     *
     *
     * for a list of all child view attributes that this class supports.
     */
    class LayoutParams : LinearLayout.LayoutParams {
        
        constructor(c: Context, attrs: AttributeSet) : super(c, attrs)

        constructor(w: Int, h: Int) : super(w, h)


        constructor(w: Int, h: Int, initWeight: Float) : super(w, h, initWeight)

        constructor(p: ViewGroup.LayoutParams) : super(p)

        constructor(source: ViewGroup.MarginLayoutParams) : super(source)

        /**
         *
         * Fixes the child's width to
         * [android.view.ViewGroup.LayoutParams.WRAP_CONTENT] and the child's
         * height to  [android.view.ViewGroup.LayoutParams.WRAP_CONTENT]
         * when not specified in the XML file.
         *
         * @param a          the styled attributes set
         * @param widthAttr  the width attribute to fetch
         * @param heightAttr the height attribute to fetch
         */
        override fun setBaseAttributes(a: TypedArray,
                                       widthAttr: Int, heightAttr: Int) {

            width = if (a.hasValue(widthAttr)) {
                a.getLayoutDimension(widthAttr, "layout_width")
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }

            height = if (a.hasValue(heightAttr)) {
                a.getLayoutDimension(heightAttr, "layout_height")
            } else {
                ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
    }

    private inner class CheckedStateTracker : CompoundButton.OnCheckedChangeListener {
        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            val id = buttonView.id
            setCheckedId(id, isChecked)
        }
    }

    /**
     *
     * A pass-through listener acts upon the events and dispatches them
     * to another listener. This allows the table layout to set its own internal
     * hierarchy change listener without preventing the user to setup his.
     */
    private inner class PassThroughHierarchyChangeListener : ViewGroup.OnHierarchyChangeListener {
        internal var mOnHierarchyChangeListener: ViewGroup.OnHierarchyChangeListener? = null
        
        override fun onChildViewAdded(parent: View, child: View) {
            if (parent === this@CheckBoxGroup && child is CheckBox) {
                var id = child.getId()
                // generates an id if it's missing
                if (id == View.NO_ID) {
                    id = View.generateViewId()
                    child.setId(id)
                }
                child.setOnCheckedChangeListener(
                        mChildOnCheckedChangeListener)
            }

            mOnHierarchyChangeListener?.onChildViewAdded(parent, child)
        }
        
        override fun onChildViewRemoved(parent: View, child: View) {
            if (parent === this@CheckBoxGroup && child is CheckBox) {
                child.setOnCheckedChangeListener(null)
            }

            mOnHierarchyChangeListener?.onChildViewRemoved(parent, child)
        }
    }
}
