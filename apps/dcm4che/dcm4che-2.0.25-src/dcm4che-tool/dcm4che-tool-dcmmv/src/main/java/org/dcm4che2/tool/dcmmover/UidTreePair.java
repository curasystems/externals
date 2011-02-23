package org.dcm4che2.tool.dcmmover;

import java.util.ArrayList;
import java.util.List;

/**
 * Used during object anonymization to track old and new uid values.
 * 
 * @author gpotter (gcac96@gmail.com)
 * @version $Revision$
 */
class UidTreePair {
    private String oldUid;

    private String newUid;

    private UidTreePair parent;

    private List<UidTreePair> children;

    public UidTreePair(UidTreePair parent, String oldUid, String newUid) {
        this.parent = parent;
        this.oldUid = oldUid;
        this.newUid = newUid;
    }

    public UidTreePair getParent() {
        return parent;
    }

    public List<UidTreePair> getChildren() {
        if (null == children) {
            children = new ArrayList<UidTreePair>();
        }
        return children;
    }

    public String getNewUid() {
        return newUid;
    }

    public String getOldUid() {
        return oldUid;
    }

    public void addChild(UidTreePair child) {
        if (null == children) {
            children = new ArrayList<UidTreePair>();
        }
        children.add(child);
    }
}
