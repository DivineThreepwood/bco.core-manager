package org.dc.bco.manager.location.core;

import org.dc.bco.dal.lib.layer.service.ServiceFactory;
import org.dc.bco.dal.lib.layer.unit.AbstractUnitCollectionController;
import org.dc.bco.manager.location.lib.Location;
import org.dc.bco.registry.device.lib.DeviceRegistry;
import org.dc.bco.registry.location.lib.LocationRegistry;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import rst.spatial.LocationConfigType.LocationConfig;
import rst.spatial.LocationDataType.LocationData;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class LocationController extends AbstractUnitCollectionController<LocationData, LocationData.Builder, LocationConfig> implements Location {

    public LocationController(final LocationConfig config) throws InstantiationException {
        super(LocationData.newBuilder());
        this.config = config;
    }

    @Override
    public LocationConfig updateConfig(final LocationConfig config) {
        this.config = config;
        return config;
    }

    @Override
    public String getLabel() throws NotAvailableException {
        return config.getLabel();
    }

    @Override
    public ServiceFactory getServiceFactory() throws NotAvailableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LocationRegistry getLocationRegistry() throws NotAvailableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DeviceRegistry getDeviceRegistry() throws NotAvailableException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
