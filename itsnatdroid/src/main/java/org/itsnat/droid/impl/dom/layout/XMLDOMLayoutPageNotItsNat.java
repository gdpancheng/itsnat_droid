package org.itsnat.droid.impl.dom.layout;

/**
 * Created by jmarranz on 19/01/2016.
 */
public class XMLDOMLayoutPageNotItsNat extends XMLDOMLayoutPage
{
    public XMLDOMLayoutPageNotItsNat()
    {
    }

    @Override
    protected XMLDOMLayout createXMLDOMLayout()
    {
        return new XMLDOMLayoutPageNotItsNat();
    }
}
