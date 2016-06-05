package org.dc.bco.manager.device.core;

/*
 * #%L
 * COMA DeviceManager Core
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.dc.bco.dal.lib.layer.service.ServiceFactory;
import org.dc.bco.manager.device.lib.Device;
import org.dc.jul.exception.InstantiationException;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;

/**
 *
 * @author mpohling
 */
public class DeviceFactoryImpl extends AbstractDeviceFactory {

    private final ServiceFactory serviceFactory;

    public DeviceFactoryImpl(final ServiceFactory serviceFactory) throws InstantiationException {
        assert serviceFactory != null;
        this.serviceFactory = serviceFactory;
    }

    @Override
    public Device newInstance(final DeviceConfig config) throws InstantiationException, InterruptedException {
        return newInstance(config, serviceFactory);
    }
}
