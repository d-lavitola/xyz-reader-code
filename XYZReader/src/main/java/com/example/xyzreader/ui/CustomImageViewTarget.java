package com.example.xyzreader.ui;

import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.request.target.ImageViewTarget;

public class CustomImageViewTarget extends ImageViewTarget {


    public CustomImageViewTarget(ImageView view) {
        super(view);
    }

    @Override
    protected void setResource(@Nullable Object resource) {

    }
}
