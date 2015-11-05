/*
 * Copyright (C) 2015 Mikhail Sapozhnikov
 *
 * This file is part of scriba-android.
 *
 * scriba-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * scriba-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with scriba-android. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.scribacrm.scriba;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Checkable;
import android.util.Log;

public class CheckableLinearLayout extends LinearLayout
                                   implements Checkable {

    private int _checkId = 0;

    public CheckableLinearLayout(Context context) {
        this(context, null);
    }

    public CheckableLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray arr = context.obtainStyledAttributes(attrs,
            R.styleable.CheckableLinearLayout, 0, 0);

        _checkId = arr.getResourceId(R.styleable.CheckableLinearLayout_checkId, 0);
        arr.recycle();
    }

    // Checkable implementation
    @Override
    public boolean isChecked() {
        Checkable checkable = (Checkable)findViewById(_checkId);
        return checkable.isChecked();
    }

    @Override
    public void setChecked(boolean checked) {
        Checkable checkable = (Checkable)findViewById(_checkId);
        checkable.setChecked(checked);
    }

    @Override
    public void toggle() {
        Checkable checkable = (Checkable)findViewById(_checkId);
        checkable.toggle();
    }
}
