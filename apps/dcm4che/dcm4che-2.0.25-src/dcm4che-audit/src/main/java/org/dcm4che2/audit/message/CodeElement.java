/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See listed authors below.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
 
package org.dcm4che2.audit.message;

/**
 * Coded value.
 * 
 * @author Gunter Zeilinger <gunterze@gmail.com>
 * @version $Revision: 769 $ $Date: 2007-02-26 01:28:19 +0100 (Mon, 26 Feb 2007) $
 * @since Nov 17, 2006
 */
public class CodeElement extends BaseElement {

    private final boolean readonly;

    protected CodeElement(String name, String code) {
        this(name, code, false);
    }
    
    /**
     * Used by derived classes to instantiate <tt>Code</tt> constants. 
     */
    protected CodeElement(String name, String code, String codeSystemName, 
            String displayName) {
        this(name, code, true);
        addAttribute("codeSystemName", codeSystemName, false);
        addAttribute("displayName", displayName, false);
    }

    protected CodeElement(String name, String code, boolean readonly) {
        super(name);
        addAttribute("code", code, false);
        this.readonly = readonly;
    }    
    
    private void checkReadOnly() {
        if (readonly) {
            throw new IllegalStateException("Cannot modify Code constant");
        }        
    }
    
    public final String getCode() {
        return (String) getAttribute("code");
    }

    public final String getCodeSystem() {
        return (String) getAttribute("codeSystem");
    }
        
    /**
     * Sets OID reference.
     * @param codeSystem OID reference
     * @return this <tt>Code</tt> object.
     */
    public final CodeElement setCodeSystem(String codeSystem) {
        checkReadOnly();
        addAttribute("codeSystem", codeSystem, true);
        return this;
    }

    public final String getCodeSystemName() {
        return (String) getAttribute("codeSystemName");
    }
        
    /**
     * Sets name of the coding system.
     * @param codeSystemName name of the coding system
     * @return this <tt>Code</tt> object.
     */
    public final CodeElement setCodeSystemName(String codeSystemName) {
        checkReadOnly();
        addAttribute("codeSystemName", codeSystemName, true);
        return this;
    }

    public final String getDisplayName() {
        return (String) getAttribute("displayName");
    }
            
    /**
     * Sets the value to be used in displays and reports.
     * @param displayName value to be used in displays and reports
     * @return this <tt>Code</tt> object.
     */
    public final CodeElement setDisplayName(String displayName) {
        checkReadOnly();
        addAttribute("displayName", displayName, true);
        return this;
    }

    public final String getOriginalText() {
        return (String) getAttribute("originalText");
    }
            
    /**
     * Sets original Text Input value that was translated to this code.
     * @param originalText value that was translated to this code
     * @return this <tt>Code</tt> object.
     */
    public final CodeElement setOriginalText(String originalText) {
        checkReadOnly();
        addAttribute("originalText", originalText, true);
        return this;
    }    
}
