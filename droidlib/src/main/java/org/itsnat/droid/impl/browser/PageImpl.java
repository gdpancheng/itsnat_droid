package org.itsnat.droid.impl.browser;

import android.content.Context;

import org.apache.http.params.HttpParams;
import org.itsnat.droid.HttpRequestResult;
import org.itsnat.droid.ItsNatDoc;
import org.itsnat.droid.ItsNatDroidBrowser;
import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.ItsNatDroidScriptException;
import org.itsnat.droid.ItsNatSession;
import org.itsnat.droid.OnEventErrorListener;
import org.itsnat.droid.OnHttpRequestErrorListener;
import org.itsnat.droid.OnServerStateLostListener;
import org.itsnat.droid.Page;
import org.itsnat.droid.PageRequest;
import org.itsnat.droid.UserData;
import org.itsnat.droid.impl.ItsNatDroidImpl;
import org.itsnat.droid.impl.browser.serveritsnat.ItsNatDocImpl;
import org.itsnat.droid.impl.browser.serveritsnat.ItsNatSessionImpl;
import org.itsnat.droid.impl.dom.layout.XMLDOMLayout;
import org.itsnat.droid.impl.util.UserDataImpl;
import org.itsnat.droid.impl.xmlinflated.layout.InflatedLayoutPageImpl;
import org.itsnat.droid.impl.xmlinflater.layout.InflateLayoutRequestImpl;
import org.itsnat.droid.impl.xmlinflater.layout.page.InflateLayoutRequestPageImpl;
import org.itsnat.droid.impl.xmlinflater.layout.page.XMLInflaterLayoutPage;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;

/**
 * Created by jmarranz on 4/06/14.
 */
public class PageImpl implements Page
{
    protected final PageRequestResult pageReqResult;
    protected final PageRequestImpl pageRequestCloned; // Nos interesa únicamente para el reload, es un clone del original por lo que podemos tomar datos del mismo sin miedo a cambiarse
    protected final String itsNatServerVersion;  // Si es null es que la página NO ha sido servida por ItsNat
    protected final int bitmapDensityReference;
    protected final XMLInflaterLayoutPage xmlInflaterLayoutPage;
    protected final String uniqueIdForInterpreter;
    protected final Interpreter interp;
    protected ItsNatDocImpl itsNatDoc = new ItsNatDocImpl(this);
    protected ItsNatSessionImpl itsNatSession;
    protected String clientId;
    protected OnEventErrorListener eventErrorListener;
    protected OnServerStateLostListener stateLostListener;
    protected OnHttpRequestErrorListener httpReqErrorListener;
    protected UserDataImpl userData;
    protected boolean dispose;

    public PageImpl(PageRequestImpl pageRequestToClone,PageRequestResult pageReqResult)
    {
        this.pageRequestCloned = pageRequestToClone.clone(); // De esta manera conocemos como se ha creado pero podemos reutilizar el PageRequestImpl original
        this.pageReqResult = pageReqResult;

        HttpRequestResultOKImpl httpReqResult = pageReqResult.getHttpRequestResultOKImpl();
        this.itsNatServerVersion = httpReqResult.getItsNatServerVersion();

        Integer bitmapDensityReference = httpReqResult.getBitmapDensityReference(); // Sólo está definida en el caso de ItsNat server, por eso se puede pasar por el usuario también a través del PageRequest
        this.bitmapDensityReference = bitmapDensityReference != null ? bitmapDensityReference : pageRequestCloned.getBitmapDensityReference();

        ItsNatDroidImpl itsNatDroid = pageRequestCloned.getItsNatDroidBrowserImpl().getItsNatDroidImpl();
        ItsNatDroidBrowserImpl browser = pageRequestCloned.getItsNatDroidBrowserImpl();

        this.uniqueIdForInterpreter = browser.getUniqueIdGenerator().generateId("i"); // i = interpreter
        this.interp = new Interpreter(new StringReader(""), System.out, System.err, false, new NameSpace(browser.getInterpreter().getNameSpace(), uniqueIdForInterpreter)); // El StringReader está copiado del código fuente de beanshell2 https://code.google.com/p/beanshell2/source/browse/branches/v2.1/src/bsh/Interpreter.java

        // Definimos pronto el itsNatDoc para que los layout incluidos tengan algún soporte de scripting de ItsNatDoc por ejemplo toast, eval, alert etc
        try
        {
            interp.set("itsNatDoc", itsNatDoc);
        }
        catch (EvalError ex)
        {
            throw new ItsNatDroidException(ex);
        }
        catch (Exception ex)
        {
            throw new ItsNatDroidException(ex);
        }

        StringBuilder methods = new StringBuilder();
        methods.append("alert(data){itsNatDoc.alert(data);}");
        methods.append("toast(value,duration){itsNatDoc.toast(value,duration);}");
        methods.append("toast(value){itsNatDoc.toast(value);}");
        methods.append("eval(code){itsNatDoc.eval(code);}");
        itsNatDoc.eval(methods.toString());


        InflateLayoutRequestPageImpl inflateLayoutRequest = new InflateLayoutRequestPageImpl(itsNatDroid,this);


        XMLDOMLayout domLayout = pageReqResult.getXMLDOMLayout();

        String[] loadScriptArr = new String[1];
        List<String> scriptList = new LinkedList<String>();
        this.xmlInflaterLayoutPage = (XMLInflaterLayoutPage) inflateLayoutRequest.inflateLayout(domLayout,null, loadScriptArr, scriptList, this);


        String loadScript = loadScriptArr[0];

//long start = System.currentTimeMillis();


        if (!scriptList.isEmpty())
        {
            for (String code : scriptList)
            {
                itsNatDoc.eval(code);
            }
        }

        if (loadScript != null) // El caso null es cuando se devuelve un layout sin script (layout sin eventos)
            itsNatDoc.eval(loadScript);


//long end = System.currentTimeMillis();
//System.out.println("LAPSE" + (end - start));

        if (getId() != null && itsNatDoc.isEventsEnabled()) // Es página generada por ItsNat y tiene al menos scripting enabled
            itsNatDoc.sendLoadEvent();
        else dispose(); // En el servidor
    }

    public ItsNatDroidBrowserImpl getItsNatDroidBrowserImpl()
    {
        return pageRequestCloned.getItsNatDroidBrowserImpl();
    }

    public PageRequestResult getPageRequestResult()
    {
        return pageReqResult;
    }

    public PageRequestImpl getPageRequestClonedImpl()
    {
        return pageRequestCloned;
    }

    @Override
    public ItsNatDroidBrowser getItsNatDroidBrowser()
    {
        return getItsNatDroidBrowserImpl();
    }

    @Override
    public HttpParams getHttpParams()
    {
        return getPageRequestClonedImpl().getHttpParams();
    }

    @Override
    public HttpRequestResult getHttpRequestResult()
    {
        return getHttpRequestResultOKImpl();
    }

    public HttpRequestResultOKImpl getHttpRequestResultOKImpl()
    {
        return pageReqResult.getHttpRequestResultOKImpl();
    }

    public int getBitmapDensityReference()
    {
        return bitmapDensityReference;
    }

    @Override
    public String getURL()
    {
        return pageRequestCloned.getURL();
    }

    public String getURLBase()
    {
        return pageRequestCloned.getURLBase();
    }

    public String getItsNatServerVersion()
    {
        return itsNatServerVersion;
    }

    public InflatedLayoutPageImpl getInflatedLayoutPageImpl()
    {
        return xmlInflaterLayoutPage.getInflatedLayoutPageImpl();
    }

    public XMLInflaterLayoutPage getXMLInflaterLayoutPage()
    {
        return xmlInflaterLayoutPage;
    }

    public Interpreter getInterpreter()
    {
        return interp;
    }

    public void setSessionIdAndClientId(String stdSessionId,String sessionToken,String sessionId,String clientId)
    {
        this.clientId = clientId;
        this.itsNatSession = getItsNatDroidBrowserImpl().getItsNatSession(stdSessionId,sessionToken,sessionId);
        itsNatSession.registerPage(this);
    }

    public ItsNatSession getItsNatSession()
    {
        return itsNatSession;
    }

    public ItsNatSessionImpl getItsNatSessionImpl()
    {
        return itsNatSession;
    }

    public String getId()
    {
        return clientId;
    }

    public Context getContext()
    {
        return pageRequestCloned.getContext();
    }

    public UserData getUserData()
    {
        if (userData == null) this.userData = new UserDataImpl();
        return userData;
    }

    public ItsNatDoc getItsNatDoc()
    {
        return getItsNatDocImpl();
    }

    public ItsNatDocImpl getItsNatDocImpl()
    {
        return itsNatDoc;
    }

    public OnEventErrorListener getOnEventErrorListener()
    {
        return eventErrorListener;
    }

    @Override
    public void setOnEventErrorListener(OnEventErrorListener listener)
    {
        this.eventErrorListener = listener;
    }

    public OnServerStateLostListener getOnServerStateLostListener()
    {
        return stateLostListener;
    }

    public void setOnServerStateLostListener(OnServerStateLostListener listener)
    {
        this.stateLostListener = listener;
    }

    public OnHttpRequestErrorListener getOnHttpRequestErrorListener()
    {
        return httpReqErrorListener;
    }

    public void setOnHttpRequestErrorListener(OnHttpRequestErrorListener listener)
    {
        this.httpReqErrorListener = listener;
    }

    public PageRequest reusePageRequest()
    {
        return pageRequestCloned.clone();
    }

    public void dispose()
    {
        if (dispose) return;
        this.dispose = true;
        if (getId() != null && itsNatDoc.isEventsEnabled())
            itsNatDoc.sendUnloadEvent();
        if (itsNatSession != null) // itsNatSession es null cuando la página no contiene script de inicialización
        {
            itsNatSession.disposePage(this);
            getItsNatDroidBrowserImpl().disposeSessionIfEmpty(itsNatSession);
        }
    }


}
