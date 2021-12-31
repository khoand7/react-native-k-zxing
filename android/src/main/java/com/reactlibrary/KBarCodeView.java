package com.reactlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class KBarCodeView extends FrameLayout {


    public KBarCodeView(@NonNull Context context) {
        super(context);
    }

    public KBarCodeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public KBarCodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public KBarCodeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
