package org.itsnat.droid.impl.xmlinflater.layout.stdalone;

import android.view.View;

import org.itsnat.droid.impl.dom.DOMAttr;
import org.itsnat.droid.impl.xmlinflated.layout.InflatedLayoutStandaloneImpl;
import org.itsnat.droid.impl.xmlinflater.AttrInflaterListeners;
import org.itsnat.droid.impl.xmlinflater.layout.XMLInflaterLayout;

/**
 * Created by jmarranz on 4/11/14.
 */
public class XMLInflaterLayoutStandalone extends XMLInflaterLayout
{
    public XMLInflaterLayoutStandalone(InflatedLayoutStandaloneImpl inflatedXML,int bitmapDensityReference,AttrInflaterListeners attrInflaterListeners)
    {
        super(inflatedXML,bitmapDensityReference,attrInflaterListeners);
    }

    @Override
    public boolean setAttributeInlineEventHandler(View view, DOMAttr attr)
    {
        return false; // No se soportan inline handlers (onclick="...")
    }

    @Override
    public boolean removeAttributeInlineEventHandler(View view, String namespaceURI, String name)
    {
        return false;  // No se soportan inline handlers (onclick="...")
    }
}
