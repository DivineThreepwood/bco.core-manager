/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.manager.device.core;

import com.google.protobuf.GeneratedMessage;
import org.dc.bco.dal.lib.layer.unit.AbstractUnitCollectionController;
import static org.dc.bco.dal.lib.layer.unit.AbstractUnitController.TYPE_FILED_ID;
import static org.dc.bco.dal.lib.layer.unit.AbstractUnitController.TYPE_FILED_LABEL;
import org.dc.bco.dal.lib.layer.unit.Unit;
import org.dc.bco.manager.device.lib.DeviceController;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.CouldNotTransformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import rst.homeautomation.device.DeviceConfigType.DeviceConfig;

/**
 *
 * @author Divine Threepwood
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractDeviceController<M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractUnitCollectionController<M, MB, DeviceConfig> implements DeviceController {

    public final static String DEVICE_TYPE_FILED_CONFIG = "config";

    public AbstractDeviceController(final MB builder) throws InstantiationException, CouldNotTransformException {
        super(builder);
//        try {
//        } catch (CouldNotPerformException ex) {
//            throw new InstantiationException(RSBCommunicationService.class, ex);
//        }
    }

    @Override
    public void init(final DeviceConfig config) throws InitializationException, InterruptedException {

        try {
            if (config == null) {
                throw new NotAvailableException("config");
            }

            if (!config.hasId()) {
                throw new NotAvailableException("config.id");
            }

            if (config.getId().isEmpty()) {
                throw new NotAvailableException("Field config.id is empty!");
            }

            if (!config.hasLabel()) {
                throw new NotAvailableException("config.label");
            }

            if (config.getLabel().isEmpty()) {
                throw new NotAvailableException("Field config.label is emty!");
            }

            super.init(config);

            try {
                registerUnits(config.getUnitConfigList());

                for (Unit unit : getUnits()) {
                    DeviceManagerController.getDeviceManager().getUnitControllerRegistry().register(unit);
                }
            } catch (CouldNotPerformException ex) {
                throw new InstantiationException(this, ex);
            }

        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

//    @Override
//    public DeviceConfig updateConfig(final DeviceConfig config) throws CouldNotPerformException {
////        setField(DEVICE_TYPE_FILED_CONFIG, config);
//        return super.updateConfig(config);
//    }

    @Override
    public DeviceConfig updateConfig(final DeviceConfig config) throws CouldNotPerformException {
        setField(TYPE_FILED_ID, config.getId());
        setField(TYPE_FILED_LABEL, config.getLabel());
        return super.updateConfig(config);
    }

    @Override
    public final String getId() throws NotAvailableException {
        try {
            DeviceConfig tmpConfig = getConfig();
            if (!tmpConfig.hasId()) {
                throw new NotAvailableException("unitconfig.id");
            }

            if (tmpConfig.getId().isEmpty()) {
                throw new InvalidStateException("unitconfig.id is empty");
            }

            return tmpConfig.getId();
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("id", ex);
        }
    }

    @Override
    public String getLabel() throws NotAvailableException {
        try {
            DeviceConfig tmpConfig = getConfig();
            if (!tmpConfig.hasId()) {
                throw new NotAvailableException("unitconfig.label");
            }

            if (tmpConfig.getId().isEmpty()) {
                throw new InvalidStateException("unitconfig.label is empty");
            }
            return getConfig().getLabel();
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("label", ex);
        }
    }
}
