/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.coma.scm.lib;

import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Activatable;
import rst.homeautomation.control.scene.SceneConfigType;

/**
 *
 * @author mpohling
 */
public interface Scene extends Activatable {
    public SceneConfigType.SceneConfig getConfig() throws NotAvailableException;
}
