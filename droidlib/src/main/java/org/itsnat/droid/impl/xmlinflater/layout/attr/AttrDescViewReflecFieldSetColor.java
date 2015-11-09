package org.itsnat.droid.impl.xmlinflater.layout.attr;

import android.content.Context;
import android.view.View;

import org.itsnat.droid.impl.dom.DOMAttr;
import org.itsnat.droid.impl.xmlinflater.layout.AttrLayoutContext;
import org.itsnat.droid.impl.xmlinflater.layout.XMLInflaterLayout;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;

/**
 * Created by jmarranz on 1/05/14.
 */
public class AttrDescViewReflecFieldSetColor extends AttrDescViewReflecFieldSet
{
    protected String defaultValue;

    public AttrDescViewReflecFieldSetColor(ClassDescViewBased parent, String name, String fieldName, String defaultValue)
    {
        super(parent,name,fieldName);
        this.defaultValue = defaultValue;
    }

    public void setAttribute(View view, DOMAttr attr, AttrLayoutContext attrCtx)
    {
        int convertedValue = getColor(attr.getValue(),attrCtx.getContext());

        setField(view,convertedValue);
    }

    public void removeAttribute(View view, XMLInflaterLayout xmlInflaterLayout, Context ctx)
    {
        if (defaultValue != null)
            setToRemoveAttribute(view, defaultValue, xmlInflaterLayout, ctx);
    }
}
