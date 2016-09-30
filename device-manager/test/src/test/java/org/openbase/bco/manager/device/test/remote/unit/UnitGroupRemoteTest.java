package org.openbase.bco.manager.device.test.remote.unit;

/*
 * #%L
 * COMA DeviceManager Test
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.bco.dal.lib.jp.JPHardwareSimulationMode;
import org.openbase.bco.dal.lib.layer.service.operation.PowerStateOperationService;
import org.openbase.bco.dal.lib.layer.unit.Unit;
import org.openbase.bco.dal.remote.unit.UnitGroupRemote;
import org.openbase.bco.manager.device.core.DeviceManagerLauncher;
import org.openbase.bco.registry.mock.MockRegistryHolder;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.slf4j.LoggerFactory;
import rst.homeautomation.service.ServiceConfigType.ServiceConfig;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServicePattern;
import rst.homeautomation.service.ServiceTemplateType.ServiceTemplate.ServiceType;
import rst.homeautomation.state.BrightnessStateType.BrightnessState;
import rst.homeautomation.state.PowerStateType.PowerState;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.homeautomation.unit.UnitGroupConfigType.UnitGroupConfig;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class UnitGroupRemoteTest {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(UnitGroupRemoteTest.class);

    private static DeviceManagerLauncher deviceManagerLauncher;
    private static UnitGroupRemote unitGroupRemote;
    private static final List<Unit> units = new ArrayList<>();

    public UnitGroupRemoteTest() {
    }

    @BeforeClass
    public static void setUpClass() throws InitializationException, InvalidStateException, InstantiationException, CouldNotPerformException, JPServiceException, InterruptedException {
        JPService.registerProperty(JPHardwareSimulationMode.class, true);
        MockRegistryHolder.newMockRegistry();

        deviceManagerLauncher = new DeviceManagerLauncher();
        deviceManagerLauncher.launch();
        deviceManagerLauncher.getDeviceManager().waitForInit(30, TimeUnit.SECONDS);

        unitGroupRemote = new UnitGroupRemote();
        ServiceTemplate powerStateOperationService = ServiceTemplate.newBuilder().setType(ServiceType.POWER_STATE_SERVICE).setPattern(ServicePattern.OPERATION).build();
        ServiceTemplate powerStateProviderService = ServiceTemplate.newBuilder().setType(ServiceType.POWER_STATE_SERVICE).setPattern(ServicePattern.PROVIDER).build();
        UnitGroupConfig.Builder unitGroupConfig = UnitGroupConfig.newBuilder().addServiceTemplate(powerStateOperationService).addServiceTemplate(powerStateProviderService);
        UnitConfig.Builder unitConfig = UnitConfig.newBuilder().setUnitGroupConfig(unitGroupConfig).setLabel("testGroup");

        for (Unit unit : deviceManagerLauncher.getDeviceManager().getUnitControllerRegistry().getEntries()) {
            if (allServiceTemplatesImplemented(unitGroupConfig, unit.getConfig().getServiceConfigList())) {
                units.add(unit);
                unitGroupConfig.addMemberId(unit.getConfig().getId());
            }
        }
        logger.info("Unit group [" + unitGroupConfig.build() + "]");
        unitGroupRemote.init(unitConfig.build());
        unitGroupRemote.activate();
        Thread.sleep(3000);
    }

    private static boolean allServiceTemplatesImplemented(UnitGroupConfig.Builder unitGroup, List<ServiceConfig> serviceConfigList) {
        List<ServiceTemplate> fromUnit = new ArrayList<>();
        serviceConfigList.stream().forEach((serviceConfig) -> {
            fromUnit.add(serviceConfig.getServiceTemplate());
        });
        return unitGroup.getServiceTemplateList().stream().noneMatch((serviceTemplate) -> (!fromUnit.contains(serviceTemplate)));
    }

    @AfterClass
    public static void tearDownClass() throws CouldNotPerformException, InterruptedException {
        if (deviceManagerLauncher != null) {
            deviceManagerLauncher.shutdown();
        }
        if (unitGroupRemote != null) {
            unitGroupRemote.shutdown();
        }
        MockRegistryHolder.shutdownMockRegistry();
    }

    @Before
    public void setUp() throws InitializationException, InvalidStateException {

    }

    @After
    public void tearDown() throws CouldNotPerformException {

    }

    /**
     * Test of setPowerState method, of class UnitGroupRemote.
     *
     * @throws java.lang.Exception
     */
    @Test(timeout = 10000)
    public void testSetPowerState() throws Exception {
        System.out.println("setPowerState");
        PowerState state = PowerState.newBuilder().setValue(PowerState.State.ON).build();
        unitGroupRemote.setPowerState(state).get();

        for (Unit unit : units) {
            assertEquals("Power state of unit [" + unit.getConfig().getId() + "] has not been set on!", state.getValue(), ((PowerStateOperationService) unit).getPowerState().getValue());
        }

        state = PowerState.newBuilder().setValue(PowerState.State.OFF).build();
        unitGroupRemote.setPowerState(state).get();
        for (Unit unit : units) {
            assertEquals("Power state of unit [" + unit.getConfig().getId() + "] has not been set on!", state.getValue(), ((PowerStateOperationService) unit).getPowerState().getValue());
        }
    }

    /**
     * Test of getPowerState method, of class UnitGroupRemote.
     *
     * @throws java.lang.Exception
     */
    @Test(timeout = 10000)
    public void testGetPowerState() throws Exception {
        System.out.println("getPowerState");
        PowerState state = PowerState.newBuilder().setValue(PowerState.State.OFF).build();
        unitGroupRemote.setPowerState(state).get();
        assertEquals("Power state has not been set in time or the return value from the getter is different!", state.getValue(), unitGroupRemote.getPowerState().getValue());
    }

    /**
     * Test of setBrightness method, of class UnitGroupRemote.
     *
     * @throws java.lang.Exception
     */
    @Test(timeout = 10000)
    public void testSetBrightness() throws Exception {
        System.out.println("setBrightness");
        Double brightness = 75d;
        BrightnessState brightnessState = BrightnessState.newBuilder().setBrightness(brightness).setBrightnessDataUnit(BrightnessState.DataUnit.PERCENT).build();
        try {
            unitGroupRemote.setBrightnessState(brightnessState).get();
            fail("Brighntess service has been used even though the group config is only defined for power service");
        } catch (CouldNotPerformException ex) {
        }
    }
}
