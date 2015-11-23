package org.itsnat.droid.impl.xmlinflater.shared.attr;

import android.content.Context;

import org.itsnat.droid.impl.dom.DOMAttr;
import org.itsnat.droid.impl.xmlinflater.AttrContext;
import org.itsnat.droid.impl.xmlinflater.ClassDesc;

/**
 * Aunque la entrada de datos sea una dimensión float con sufijo y todo, el método que define el valor sólo admite un entero
 * Created by jmarranz on 30/04/14.
 */
public class AttrDescReflecMethodDimensionIntRound<TclassDesc extends ClassDesc,TattrTarget,TattrContext extends AttrContext>
        extends AttrDescReflecMethodDimensionIntBase<TclassDesc,TattrTarget,TattrContext>
{
    public AttrDescReflecMethodDimensionIntRound(TclassDesc parent, String name, String methodName, Float defaultValue)
    {
        super(parent,name,methodName,defaultValue);
    }

    public AttrDescReflecMethodDimensionIntRound(TclassDesc parent, String name, Float defaultValue)
    {
        super(parent,name,defaultValue);
    }

    @Override
    public int getDimensionIntAbstract(DOMAttr attr, Context ctx)
    {
        return getDimensionIntRound(attr.getValue(), ctx);
    }
}