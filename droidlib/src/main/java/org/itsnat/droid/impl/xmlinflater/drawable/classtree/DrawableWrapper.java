package org.itsnat.droid.impl.xmlinflater.drawable.classtree;

import android.graphics.drawable.Drawable;

/**
 * Created by Jose on 15/10/2015.
 */
public class DrawableWrapper extends DrawableOrElementDrawableWrapper<Drawable>
{
    protected Drawable drawable;

    public DrawableWrapper(Drawable drawable)
    {
        this.drawable = drawable;
    }

    @Override
    public Drawable getDrawable() {
        return drawable;
    }

    @Override
    public Drawable getInstanceToSetAttributes()
    {
        return getDrawable();
    }
}