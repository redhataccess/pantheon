package com.redhat.pantheon.upgrade;

import com.google.common.collect.Lists;
import com.redhat.pantheon.sling.ServiceResourceResolverProvider;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.testing.mock.sling.junit5.SlingContext;
import org.apache.sling.testing.mock.sling.junit5.SlingContextExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SlingContextExtension.class})
class UpgradeRunnerTest {

    SlingContext sc = new SlingContext();

    @Mock
    UpgradeProvider upgradeProvider;

    @Mock
    ResourceResolverFactory resolverFactory;

    @Test
    void allUpgradesAreInvokedAndStored() throws Exception {
        // Given
        List<Upgrade> mockUpgrades = Lists.newArrayList(
                randomUpgrade(),
                randomUpgrade(),
                randomUpgrade()
        );
        when(resolverFactory.getServiceResourceResolver(any())).thenReturn(sc.resourceResolver());
        when(upgradeProvider.getUpgrades()).thenReturn(mockUpgrades);

        // When
        UpgradeRunner runner = new UpgradeRunner(
                new ServiceResourceResolverProvider(resolverFactory),
                upgradeProvider);
        runner.runUpgrades();

        // Then
        verify(mockUpgrades.get(0), times(1)).run(eq(sc.resourceResolver()), any());
        verify(mockUpgrades.get(1), times(1)).run(eq(sc.resourceResolver()), any());
        verify(mockUpgrades.get(2), times(1)).run(eq(sc.resourceResolver()), any());
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(0).getId()));
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(1).getId()));
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(2).getId()));
    }

    @Test
    void onlyNewUpgradesAreRan() throws Exception {
        // Given
        sc.build()
                .resource("/conf/pantheon/upgrades/upgrade-1")
                .commit();
        List<Upgrade> mockUpgrades = Lists.newArrayList(
                upgradeWithId("upgrade-1"),
                upgradeWithId("upgrade-2"),
                upgradeWithId("upgrade-3")
        );
        when(resolverFactory.getServiceResourceResolver(any())).thenReturn(sc.resourceResolver());
        when(upgradeProvider.getUpgrades()).thenReturn(mockUpgrades);

        // When
        UpgradeRunner runner = new UpgradeRunner(
                new ServiceResourceResolverProvider(resolverFactory),
                upgradeProvider);
        runner.runUpgrades();

        // Then
        verify(mockUpgrades.get(0), times(0)).run(eq(sc.resourceResolver()), any());
        verify(mockUpgrades.get(1), times(1)).run(eq(sc.resourceResolver()), any());
        verify(mockUpgrades.get(2), times(1)).run(eq(sc.resourceResolver()), any());
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(0).getId()));
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(1).getId()));
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(2).getId()));
    }

    @Test
    void errorWhenRunningSingleUpgrade() throws Exception {
        // Given
        List<Upgrade> mockUpgrades = Lists.newArrayList(
                upgradeWithId("upgrade-1"),
                randomErrorUpgrade(),
                upgradeWithId("upgrade-3")
        );
        when(resolverFactory.getServiceResourceResolver(any())).thenReturn(sc.resourceResolver());
        when(upgradeProvider.getUpgrades()).thenReturn(mockUpgrades);

        // When
        UpgradeRunner runner = new UpgradeRunner(
                new ServiceResourceResolverProvider(resolverFactory),
                upgradeProvider);
        runner.runUpgrades();

        // Then
        verify(mockUpgrades.get(0), times(1)).run(eq(sc.resourceResolver()), any());
        verify(mockUpgrades.get(1), times(1)).run(eq(sc.resourceResolver()), any());
        verify(mockUpgrades.get(2), times(0)).run(eq(sc.resourceResolver()), any());
        assertNotNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(0).getId()));
        assertNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(1).getId()));
        assertNull(sc.resourceResolver().getResource("/conf/pantheon/upgrades/" + mockUpgrades.get(2).getId()));
    }

    /**
     * Mock an upgrade with a random id and no real functionality
     */
    private Upgrade randomUpgrade() {
        Upgrade mock = mock(Upgrade.class);
        when(mock.getId()).thenReturn(UUID.randomUUID().toString());
        return mock;
    }

    /**
     * Mock an upgrade with a given id and no real functionality
     */
    private Upgrade upgradeWithId(final String id) {
        Upgrade mock = mock(Upgrade.class);
        when(mock.getId()).thenReturn(id);
        return mock;
    }

    /**
     * Mock an upgrade with a random id and functionality that throws an exception
     */
    private Upgrade randomErrorUpgrade() throws Exception {
        Upgrade mock = mock(Upgrade.class);
        when(mock.getId()).thenReturn(UUID.randomUUID().toString());
        doThrow(Exception.class).when(mock).run(any(), any());
        return mock;
    }
}
