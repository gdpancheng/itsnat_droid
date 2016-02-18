package org.itsnat.droid.impl.dom;

import android.content.res.Configuration;
import android.os.Build;

import org.itsnat.droid.ItsNatDroidException;
import org.itsnat.droid.impl.dom.values.XMLDOMValues;
import org.itsnat.droid.impl.util.MimeUtil;

import java.util.Locale;

import static org.itsnat.droid.impl.dom.values.XMLDOMValues.TYPE_DRAWABLE;
import static org.itsnat.droid.impl.dom.values.XMLDOMValues.TYPE_LAYOUT;

/**
 * Created by jmarranz on 3/11/14.
 */
public abstract class DOMAttrDynamic extends DOMAttr
{
    protected final String resType;
    protected final String extension; // xml, png...
    protected final String valuesResourceName; // No nulo sólo en el caso de "values" tras el :
    protected final boolean ninePatch;
    protected final String mime;
    protected final String location;
    protected volatile ParsedResource resource;

    public DOMAttrDynamic(String namespaceURI, String name, String value,Configuration configuration)
    {
        super(namespaceURI, name, value);

        // Ej. @assets:drawable/res/drawable/file.png   Path: res/drawable/file.png

        // Ej. @remote:drawable/res/drawable/file.png   Remote Path: res/drawable/file.png
        //     @remote:drawable//res/drawable/file.png  Remote Path: /res/drawable/file.png
        //     @remote:drawable/http://somehost/res/drawable/file.png  Remote Path: http://somehost/res/drawable/file.png
        //     @remote:drawable/ItsNatDroidServletExample?itsnat_doc_name=test_droid_remote_drawable

        // Ej. values:  @assets:dimen/res/values/filename.xml:size
        //              @remote:dimen/res/values/filename.xml:size

        int posType = value.indexOf(':');
        int posPath = value.indexOf('/');
        this.resType = value.substring(posType + 1,posPath); // Ej. "drawable"

        String locationTmp;
        if (XMLDOMValues.isResourceTypeValues(resType))
        {
            int valuesResourcePos = value.lastIndexOf(':'); // Esperamos el de por ej "...filename.xml:size" pero puede devolvernos el de "@assets:dimen..." lo que significa que no existe valuesResourceName lo cual es erróneo
            if (valuesResourcePos > posType) // Correcto, existe un segundo ":" para el valuesResourceName
            {
                locationTmp = value.substring(posPath + 1,valuesResourcePos); // incluye la extension

                this.valuesResourceName = value.substring(valuesResourcePos + 1);
            }
            else // No hay selector ":selector"
            {
                if (TYPE_DRAWABLE.equals(resType) || TYPE_LAYOUT.equals(resType))
                {
                    // En el caso "drawable" podemos tener un acceso a un <drawable> en archivo XML en /res/values o bien directamente acceder al XML en /res/drawable
                    // este es el caso de acceso DIRECTO al XML del drawable
                    // Idem con <item name="..." type="layout">
                    locationTmp = value.substring(posPath + 1);

                    this.valuesResourceName = null;
                }
                else throw new ItsNatDroidException("Bad format of attribute value, expected \"values\" resource ended with \":resname\" : " + value);
            }
        }
        else
        {
            locationTmp = value.substring(posPath + 1);

            this.valuesResourceName = null;
        }

        locationTmp = processLocationSuffixes(locationTmp,configuration);

        this.location = locationTmp;

        int posExt = this.location.lastIndexOf('.');
        if (posExt != -1)
        {
            this.extension = this.location.substring(posExt + 1).toLowerCase(); // xml, png...
        }
        else
        {
            // Por ejemplo:  @remote:drawable/ItsNatDroidServletExample?itsnat_doc_name=test_droid_remote_drawable
            // Suponemos que se genera el XML por ej del drawable
            this.extension = null;
        }


        if (extension != null)
        {
            // http://www.sitepoint.com/web-foundations/mime-types-complete-list/
            String mime = MimeUtil.MIME_BY_EXT.get(extension);
            if (mime == null)
                throw new ItsNatDroidException("Unexpected extension: \"" + extension + "\" Remote resource: " + value);
            this.mime = mime;
            this.ninePatch = MimeUtil.MIME_PNG.equals(mime) && value.endsWith(".9.png");
        }
        else
        {
            this.mime = MimeUtil.MIME_XML;
            this.ninePatch = false;
        }
    }

    public String getResourceType()
    {
        return resType;
    }

    public String getExtension()
    {
        return extension;
    }

    public String getValuesResourceName()
    {
        return valuesResourceName;
    }

    public boolean isNinePatch()
    {
        return ninePatch;
    }

    public String getLocation()
    {
        return location;
    }

    public String getResourceMime()
    {
        return mime;
    }

    public ParsedResource getResource()
    {
        // Es sólo llamado en el hilo UI pero setResource se llama en multihilo
        return resource;
    }

    public void setResource(ParsedResource resource)
    {
        // Es llamado en multihilo en el caso de recurso remoto (por eso es volatile)
        // No pasa nada porque se llame e inmediatamente después se cambie el valor, puede ocurrir que se esté procesando
        // el mismo atributo a la vez por dos hilos, ten en cuenta que el template puede estar cacheado y reutilizado, pero no pasa nada
        // porque el nuevo remoteResource NUNCA es null y es siempre el mismo recurso como mucho actualizado si ha cambiado
        // en el servidor
        this.resource = resource;
    }


    private String processLocationSuffixes(String location,Configuration configuration)
    {
        // http://developer.android.com/guide/topics/resources/providing-resources.html (el orden de la tabla 2 es el orden de los sufijos en el caso de múltiples sufijos)
        // http://developer.android.com/guide/topics/resources/localization.html

        String suffix = "}";

        int posToSearchMore = 0;

        {
            // No soportamos MCC y MNC filtros, aportan muy poco valor
        }

        if (!location.contains("{")) // Todos los filtros empiezan de la misma manera, evitamos así buscar a lo tonto
            return location;

        {
            // Soportamos la existencia de sufijo de lenguaje
            // Ej {lg-es}
            String prefix = "{lg-";
            int posStart = location.indexOf(prefix,posToSearchMore);
            if (posStart != -1)
            {
                int posEnd = location.indexOf(suffix, posStart);
                if (posEnd == -1) throw new ItsNatDroidException("Unfinished prefix: " + prefix);

                String lang = location.substring(posStart + prefix.length(), posEnd);
                String currentLang = configuration.locale.getLanguage();
                try
                {
                    if (currentLang.equals(lang))
                    {
                        location = location.substring(0, posStart) + "-" + lang + location.substring(posEnd + 1);
                    }
                    else
                    {
                        // Quitamos el sufijo pues no se usa (versiones inferiores al version especificado)
                        location = location.substring(0, posStart) + location.substring(posEnd + 1);
                    }
                }
                catch (Exception ex)
                {
                    throw new ItsNatDroidException("Bad language suffix: " + lang);
                }

                posToSearchMore = posStart; // recuerda que se ha cambiado la cadena
            }
        }

        if (!location.contains("{")) // Todos los filtros empiezan de la misma manera, evitamos así buscar a lo tonto
            return location;

        {
            // Soportamos la existencia de sufijo de región
            // Ej {rg-rES}
            String prefix = "{rg-";
            int posStart = location.indexOf(prefix,posToSearchMore);
            if (posStart != -1)
            {
                int posEnd = location.indexOf(suffix, posStart);
                if (posEnd == -1) throw new ItsNatDroidException("Unfinished prefix: " + prefix);

                String region = location.substring(posStart + prefix.length() + 1 /* el +1 es la r de rES */, posEnd);
                String currentRegion = configuration.locale.getCountry();
                try
                {
                    if (currentRegion.equals(region))
                    {
                        location = location.substring(0, posStart) + "-r" + region + location.substring(posEnd + 1);
                    }
                    else
                    {
                        // Quitamos el sufijo pues no se usa (versiones inferiores al version especificado)
                        location = location.substring(0, posStart) + location.substring(posEnd + 1);
                    }
                }
                catch (Exception ex)
                {
                    throw new ItsNatDroidException("Bad language suffix: " + region);
                }

                posToSearchMore = posStart; // recuerda que se ha cambiado la cadena
            }
        }


        {
            // Layout Direction es level 17 no lo soportamos todavía
        }

        if (!location.contains("{")) // Todos los filtros empiezan de la misma manera, evitamos así buscar a lo tonto
            return location;

        {
            // Soportamos la existencia de sufijo smallestWidth
            // Ej {sw-sw720dp}
            String prefix = "{sw-";
            int posStart = location.indexOf(prefix,posToSearchMore);
            if (posStart != -1)
            {
                int posEnd = location.indexOf(suffix, posStart);
                if (posEnd == -1) throw new ItsNatDroidException("Unfinished prefix: " + prefix);

                String smallestScreenWidthDpStr  = location.substring(posStart + prefix.length() + 2, posEnd - 2); // El +2 es para quitar el "sw" y el -2 es para quitar el "dp" y que smallestScreenWidthDpStr sea un entero
                try
                {
                    int smallestScreenWidthDp = Integer.parseInt(smallestScreenWidthDpStr);
                    if (configuration.smallestScreenWidthDp >= smallestScreenWidthDp)
                    {
                        location = location.substring(0, posStart) + "-sw" + smallestScreenWidthDp + "dp" + location.substring(posEnd + 1);
                    }
                    else
                    {
                        // Quitamos el sufijo pues no se usa (versiones inferiores al version especificado)
                        location = location.substring(0, posStart) + location.substring(posEnd + 1);
                    }
                }
                catch (Exception ex)
                {
                    throw new ItsNatDroidException("Bad platform version suffix: " + smallestScreenWidthDpStr);
                }

                posToSearchMore = posStart; // recuerda que se ha cambiado la cadena
            }
        }

        if (!location.contains("{")) // Todos los filtros empiezan de la misma manera, evitamos así buscar a lo tonto
            return location;

        {
            // Soportamos la existencia de sufijo de versión de la plataforma
            // Ej {v-v21}
            String prefix = "{v-";
            int posStart = location.indexOf(prefix,posToSearchMore);
            if (posStart != -1)
            {
                int posEnd = location.indexOf(suffix, posStart);
                if (posEnd == -1) throw new ItsNatDroidException("Unfinished prefix: " + prefix);

                String versionStr = location.substring(posStart + prefix.length() + 1 /* el +1 es para quitar el v */, posEnd);
                try
                {
                    int version = Integer.parseInt(versionStr);
                    if (Build.VERSION.SDK_INT >= version)
                    {
                        location = location.substring(0, posStart) + "-v" + version + location.substring(posEnd + 1);
                    }
                    else
                    {
                        // Quitamos el sufijo pues no se usa (versiones inferiores al version especificado)
                        location = location.substring(0, posStart) + location.substring(posEnd + 1);
                    }
                }
                catch (Exception ex)
                {
                    throw new ItsNatDroidException("Bad platform version suffix: " + versionStr);
                }

                posToSearchMore = posStart; // recuerda que se ha cambiado la cadena
            }
        }

        return location;
    }

}
