package org.itsnat.droid.impl.xmlinflater.layout.attr;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;

import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.impl.ItsNatDroidImpl;
import org.itsnat.droid.impl.dom.DOMAttr;
import org.itsnat.droid.impl.dom.XMLDOM;
import org.itsnat.droid.impl.dom.layout.DOMInclude;
import org.itsnat.droid.impl.domparser.XMLDOMRegistry;
import org.itsnat.droid.impl.domparser.layout.XMLDOMLayoutParser;
import org.itsnat.droid.impl.util.MapSmart;
import org.itsnat.droid.impl.xmlinflater.XMLInflateRegistry;
import org.itsnat.droid.impl.xmlinflater.XMLInflater;
import org.itsnat.droid.impl.xmlinflater.layout.AttrLayoutContext;
import org.itsnat.droid.impl.xmlinflater.layout.PendingViewCreateProcess;
import org.itsnat.droid.impl.xmlinflater.layout.classtree.ClassDescViewBased;
import org.itsnat.droid.impl.xmlinflater.shared.attr.AttrDesc;

/**
 * Created by jmarranz on 30/04/14.
 */
public class AttrDesc_Include_layout extends AttrDesc<ClassDescViewBased,View,AttrLayoutContext>
{
    public AttrDesc_Include_layout(ClassDescViewBased parent)
    {
        super(parent,"layout");
    }

    @Override
    public void setAttribute(final View view,final DOMAttr attr,final AttrLayoutContext attrCtx)
    {
        Runnable task = new Runnable()
        {
            @Override
            public void run()
            {
                ViewGroup viewParent = (ViewGroup)view.getParent();
                int countBefore = viewParent.getChildCount();

                viewParent.removeViewAt(countBefore - 1); // Eliminamos el falso View auxiliar que substituye al <include>

                countBefore = viewParent.getChildCount();

                Context ctx = attrCtx.getContext();

                XMLInflater xmlInflater = attrCtx.getXMLInflater();
                ItsNatDroidImpl itsNatDroid = xmlInflater.getInflatedXML().getItsNatDroidImpl();
                XMLInflateRegistry xmlInflateRegistry = itsNatDroid.getXMLInflateRegistry();

                View resView = xmlInflateRegistry.getLayout(attr, ctx, xmlInflater,viewParent);
                if (resView != viewParent) throw new ItsNatDroidException("Unexpected"); // Es así, ten en cuenta que el layout incluido puede ser un <merge> con varios views

                // Test (eliminar en el futuro):
                int countAfter = viewParent.getChildCount();
                View[] childList = new View[countAfter - countBefore];
                int j = 0;
                for(int i = countBefore; i < countAfter; i++)
                {
                    childList[j] = viewParent.getChildAt(i);
                    j++;
                }

            }
        };
        PendingViewCreateProcess pendingViewCreateProcess = attrCtx.getPendingViewCreateProcess();
        if (pendingViewCreateProcess != null)
            pendingViewCreateProcess.addPendingPostAddViewTask(task);
        else
            task.run();
    }

    @Override
    public void removeAttribute(View view, AttrLayoutContext attrCtx)
    {
        setToRemoveAttribute(view, "@null", attrCtx);
    }

/*
    private View[] inflateInclude(DOMInclude domElemInc,ViewGroup viewParent,XMLDOM xmlDOMParent)
    {
        int countBefore = viewParent.getChildCount();

        ItsNatDroidImpl itsNatDroid = getInflatedLayoutImpl().getItsNatDroidImpl();
        XMLInflateRegistry xmlInflateRegistry = itsNatDroid.getXMLInflateRegistry();
        XMLDOMRegistry xmlDOMRegistry = itsNatDroid.getXMLDOMRegistry();
        AssetManager assetManager = getContext().getResources().getAssets();

        String itsNatServerVersion = null;
        boolean remotePageOrFrag = false;
        boolean loadingRemotePage = false;
        XMLDOMLayoutParser xmlDOMLayoutParser = XMLDOMLayoutParser.createXMLDOMLayoutParser(itsNatServerVersion, remotePageOrFrag, loadingRemotePage, xmlDOMRegistry, assetManager);
        DOMAttr attr = xmlDOMLayoutParser.createDOMAttr(domElemInc, null, "layout", domElemInc.getLayout(), xmlDOMParent);

        View resView = xmlInflateRegistry.getLayout(attr, ctx, this,viewParent);
        if (resView != viewParent) throw new ItsNatDroidException("Unexpected"); // Es así, ten en cuenta que el layout incluido puede ser un <merge> con varios views
        int countAfter = viewParent.getChildCount();
        View[] childList = new View[countAfter - countBefore];
        int j = 0;
        for(int i = countBefore; i < countAfter; i++)
        {
            childList[j] = viewParent.getChildAt(i);
            j++;
        }

        return childList;
    }
    */
}