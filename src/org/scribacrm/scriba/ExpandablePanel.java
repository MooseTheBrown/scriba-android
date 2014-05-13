/* 
 * Copyright (C) 2014 Mikhail Sapozhnikov
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
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;

public class ExpandablePanel extends LinearLayout {

    private final int _handleId;
    private final int _contentId;

    private View _handle;
    private View _content;

    private boolean _expanded = false;
    private int _collapsedHeight = 0;
    private int _contentHeight = 0;
    private int _animationDuration = 0;

    private OnExpandListener _listener;

    public ExpandablePanel(Context context) {
        this(context, null);
    }

    public ExpandablePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        _listener = new DefaultOnExpandListener();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ExpandablePanel, 0, 0);

        // How high the content should be in "collapsed" state
        _collapsedHeight = (int) a.getDimension(R.styleable.ExpandablePanel_collapsedHeight, 0.0f);

        // How long the animation should take
        _animationDuration = a.getInteger(R.styleable.ExpandablePanel_animationDuration, 500);

        int handleId = a.getResourceId(R.styleable.ExpandablePanel_handle, 0);
        if (handleId == 0) {
            throw new IllegalArgumentException(
                "The handle attribute is required and must refer "
                    + "to a valid child.");
        }

        int contentId = a.getResourceId(R.styleable.ExpandablePanel_content, 0);
        if (contentId == 0) {
            throw new IllegalArgumentException("The content attribute is required and must refer to a valid child.");
        }

        _handleId = handleId;
        _contentId = contentId;

        a.recycle();
    }

    public void setOnExpandListener(OnExpandListener listener) {
        _listener = listener; 
    }

    public void setCollapsedHeight(int collapsedHeight) {
        _collapsedHeight = collapsedHeight;
    }

    public void setAnimationDuration(int animationDuration) {
        _animationDuration = animationDuration;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        _handle = findViewById(_handleId);
        if (_handle == null) {
            throw new IllegalArgumentException(
                "The handle attribute is must refer to an"
                    + " existing child.");
        }

        _content = findViewById(_contentId);
        if (_content == null) {
            throw new IllegalArgumentException(
                "The content attribute must refer to an"
                    + " existing child.");
        }

        android.view.ViewGroup.LayoutParams lp = _content.getLayoutParams();
        lp.height = _collapsedHeight;
        _content.setLayoutParams(lp);

        _handle.setOnClickListener(new PanelToggler());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // First, measure how high content wants to be
        _content.measure(widthMeasureSpec, MeasureSpec.UNSPECIFIED);
        _contentHeight = _content.getMeasuredHeight();

        if (_contentHeight < _collapsedHeight) {
            _handle.setVisibility(View.GONE);
        } else {
            _handle.setVisibility(View.VISIBLE);
        }

        // Then let the usual thing happen
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private class PanelToggler implements OnClickListener {
        public void onClick(View v) {
            Animation a;
            if (_expanded) {
                a = new ExpandAnimation(_contentHeight, _collapsedHeight);
                _listener.onCollapse(_handle, _content);
            } else {
                a = new ExpandAnimation(_collapsedHeight, _contentHeight);
                _listener.onExpand(_handle, _content);
            }
            a.setDuration(_animationDuration);
            _content.startAnimation(a);
            _expanded = !_expanded;
        }
    }

    private class ExpandAnimation extends Animation {
        private final int mStartHeight;
        private final int mDeltaHeight;

        public ExpandAnimation(int startHeight, int endHeight) {
            mStartHeight = startHeight;
            mDeltaHeight = endHeight - startHeight;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            android.view.ViewGroup.LayoutParams lp = _content.getLayoutParams();
            lp.height = (int) (mStartHeight + mDeltaHeight * interpolatedTime);
            _content.setLayoutParams(lp);
        }

        @Override
        public boolean willChangeBounds() {
            return true;
        }
    }

    public interface OnExpandListener {
        public void onExpand(View handle, View content); 
        public void onCollapse(View handle, View content);
    }

    private class DefaultOnExpandListener implements OnExpandListener {
        public void onCollapse(View handle, View content) {}
        public void onExpand(View handle, View content) {}
    }
}
