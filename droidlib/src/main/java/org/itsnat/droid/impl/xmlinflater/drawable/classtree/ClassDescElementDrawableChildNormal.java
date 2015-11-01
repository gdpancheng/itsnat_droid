package org.itsnat.droid.impl.xmlinflater.drawable.classtree;

import org.itsnat.droid.impl.xmlinflated.drawable.ElementDrawableChildDrawableBridge;
import org.itsnat.droid.impl.xmlinflated.drawable.ElementDrawableChildNormal;
import org.itsnat.droid.impl.xmlinflater.drawable.ClassDescDrawableMgr;

/**
 * Created by jmarranz on 10/11/14.
 */
public abstract class ClassDescElementDrawableChildNormal<TelementDrawable extends ElementDrawableChildNormal> extends ClassDescElementDrawableChild<ElementDrawableChildNormal>
{
    public ClassDescElementDrawableChildNormal(ClassDescDrawableMgr classMgr,String elemName)
    {
        super(classMgr,elemName);
    }

    public ClassDescElementDrawableChildNormal(ClassDescDrawableMgr classMgr,String elemName,ClassDescDrawable parentClass)
    {
        super(classMgr,elemName,parentClass);
    }
}
