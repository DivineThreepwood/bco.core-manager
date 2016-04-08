/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.bco.manager.device.binding.openhab.comm;

/*
 * #%L
 * COMA DeviceManager Binding OpenHAB
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */
import java.util.concurrent.Future;
import org.dc.bco.dal.lib.binding.AbstractDALBinding;
import org.dc.bco.dal.lib.jp.JPHardwareSimulationMode;
import org.dc.bco.manager.device.binding.openhab.execution.OpenHABCommandExecutor;
import org.dc.bco.manager.device.binding.openhab.transform.OpenhabCommandTransformer;
import org.dc.bco.manager.device.core.DeviceManagerController;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.InvocationFailedException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.extension.protobuf.ClosableDataBuilder;
import org.dc.jul.extension.rsb.com.RSBCommunicationService;
import org.dc.jul.extension.rsb.com.RSBRemoteService;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rst.homeautomation.openhab.DALBindingType;
import rst.homeautomation.openhab.DALBindingType.DALBinding;
import rst.homeautomation.openhab.OpenhabCommandType;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand;
import rst.homeautomation.openhab.RSBBindingType;
import rst.homeautomation.openhab.RSBBindingType.RSBBinding;
import rst.homeautomation.state.ActiveDeactiveType;

/**
 * @author thuxohl
 * @author mpohling
 */
public class OpenHABCommunicatorImpl extends AbstractDALBinding implements OpenHABCommunicator {

    public static final String RPC_METHODE_INTERNAL_RECEIVE_UPDATE = "internalReceiveUpdate";
    public static final String RPC_METHODE_EXECUTE_COMMAND = "executeCommand";

    public static final Scope SCOPE_OPENHAB_IN = new Scope("/openhab/in");
    public static final Scope SCOPE_OPENHAB_OUT = new Scope("/openhab/out");

    private static final Logger logger = LoggerFactory.getLogger(OpenHABCommunicatorImpl.class);

    private OpenHABCommandExecutor commandExecutor;

    private RSBRemoteService<RSBBinding> openhabCommandExecutionRemote;
    private RSBCommunicationService<DALBinding, DALBinding.Builder> openhabItemUpdateListener;

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(OpenhabCommandType.OpenhabCommand.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(RSBBindingType.RSBBinding.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(new ProtocolBufferConverter<>(DALBindingType.DALBinding.getDefaultInstance()));
    }

    public OpenHABCommunicatorImpl() throws InstantiationException {
//        try {
//
//
//        } catch (CouldNotPerformException ex) {
//            throw new org.dc.jul.exception.InstantiationException(this, ex);
//        }
    }

    public void init() throws InitializationException, InterruptedException {
        try {
            this.commandExecutor = new OpenHABCommandExecutor(DeviceManagerController.getDeviceManager().getUnitControllerRegistry());

            openhabCommandExecutionRemote = new RSBRemoteService<RSBBinding>() {

                @Override
                public void notifyUpdated(final RSBBinding data) {
                    OpenHABCommunicatorImpl.this.notifyUpdated(data);
                }
            };

            openhabItemUpdateListener = new RSBCommunicationService<DALBinding, DALBinding.Builder>(DALBinding.newBuilder()) {

                @Override
                public void registerMethods(RSBLocalServerInterface server) throws CouldNotPerformException {
                    OpenHABCommunicatorImpl.this.registerMethods(server);
                }
            };
            openhabCommandExecutionRemote.init(SCOPE_OPENHAB_IN);
            openhabItemUpdateListener.init(SCOPE_OPENHAB_OUT);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void activate() throws CouldNotPerformException, InterruptedException {
        try {
            try {
                if (JPService.getProperty(JPHardwareSimulationMode.class).getValue()) {
                    return;
                }
            } catch (JPServiceException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            }

            // Init Openhab connection
            openhabCommandExecutionRemote.activate();
            openhabItemUpdateListener.activate();

            try (ClosableDataBuilder<DALBinding.Builder> dataBuilder = openhabItemUpdateListener.getDataBuilder(this)) {
                dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.ACTIVE));
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not setup dalCommunicationService as active.", ex);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not activate " + OpenHABCommunicator.class.getSimpleName() + "!", ex);
        }
    }

    public final void notifyUpdated(final RSBBinding data) {
        switch (data.getState().getState()) {
            case ACTIVE:
                logger.info("Active dal binding state!");
                break;
            case DEACTIVE:
                logger.info("Deactive dal binding state!");
                break;
            case UNKNOWN:
                logger.info("Unkown dal binding state!");
                break;
        }
    }

    public final void registerMethods(final RSBLocalServerInterface server) {
        try {
            server.addMethod(RPC_METHODE_INTERNAL_RECEIVE_UPDATE, new InternalReceiveUpdateCallback());
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not add methods to local server in [" + getClass().getSimpleName() + "]", ex);
        }
    }

    @Override
    public void internalReceiveUpdate(final OpenhabCommand command) throws CouldNotPerformException {
        try {
            //Ignore commands that are not for the device manager but for example for the scene registry
            commandExecutor.receiveUpdate(command);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Skip item update [" + command.getItem() + " = " + OpenhabCommandTransformer.getCommandData(command) + "]!", ex);
        }
    }

    public class InternalReceiveUpdateCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            try {
                OpenHABCommunicatorImpl.this.internalReceiveUpdate((OpenhabCommand) request.getData());
            } catch (Throwable cause) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new InvocationFailedException(this, OpenHABCommunicatorImpl.this, cause), logger, LogLevel.ERROR);
            }
            return new Event(Void.class);
        }
    }

    @Override
    public Future executeCommand(final OpenhabCommandType.OpenhabCommand command) throws CouldNotPerformException {
        try {

            if (!command.hasItem() || command.getItem().isEmpty()) {
                throw new NotAvailableException("command item");
            }

            if (!command.hasType()) {
                throw new NotAvailableException("command type");
            }

            try {
                if (JPService.getProperty(JPHardwareSimulationMode.class).getValue()) {
                    internalReceiveUpdate(command);
                    return null;
                }
            } catch (JPServiceException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            }

            if (!openhabCommandExecutionRemote.isConnected()) {
                throw new InvalidStateException("Dal openhab binding could not reach openhab server! Please check if openhab is still running!");
            }

            openhabCommandExecutionRemote.callMethod(RPC_METHODE_EXECUTE_COMMAND, command);
            return null; // TODO: mpohling implement future handling.
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not execute " + command + "!", ex);
        }
    }

    public void shutdown() throws InterruptedException {
        openhabCommandExecutionRemote.shutdown();
        openhabItemUpdateListener.shutdown();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[version=" + getClass().getPackage().getImplementationVersion() + "]";
    }
}
