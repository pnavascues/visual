/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other
 * contributors as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.infinispan.visualizer.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

import org.infinispan.visualizer.internal.VisualizerRemoteCacheManager;
import org.infinispan.visualizer.poller.PollerManager;
import org.infinispan.visualizer.poller.infinispan.JdgJmxCacheEntriesPollerManager;
import org.infinispan.visualizer.poller.infinispan.JdgJmxCacheNamesPollerManager;
import org.infinispan.visualizer.poller.jmx.JmxCacheEntriesPollerManager;
import org.infinispan.visualizer.poller.jmx.JmxCacheNamesPollerManager;
import org.infinispan.visualizer.rest.CacheNameInfo;

/**
 * This class uses CDI to alias Java EE resources, such as the persistence context, to CDI beans
 * <p>
 * <p> Example injection on a managed bean field: </p>
 * <p>
 * <pre>
 * &#064;Inject
 * private EntityManager em;
 * </pre>
 *
 * @author <a href="mailto:rtsang@redhat.com">Ray Tsang</a>
 */
public class Resources {
   private String refreshRate = System.getProperty("infinispan.visualizer.refreshRate", "2000");
   private String jmxUsername = System.getProperty("infinispan.visualizer.jmxUser", "admin");
   private String jmxPassword = System.getProperty("infinispan.visualizer.jmxPass", "jboss");
   //JMX port offset is calculated by the formula: HotRod port - jmxHotrodPortOffset
   //e.g. 11222 - 1232 = 9990
   private int jmxHotrodPortOffset = Integer.parseInt(System.getProperty("infinispan.visualizer.jmxPortOffset", "1232"));
   private String nodeColorAsString = System.getProperty("infinispan.visualizer.nodeColor");

   @Produces
   @Default
   @ApplicationScoped
   public VisualizerRemoteCacheManager cacheManager() {
      VisualizerRemoteCacheManager cm = new VisualizerRemoteCacheManager();
      cm.start();
      return cm;
   }

   @Produces
   @Default
   public JmxCacheEntriesPollerManager cacheEntriesPollerManager(VisualizerRemoteCacheManager cacheManager) {
      JmxCacheEntriesPollerManager manager = new JdgJmxCacheEntriesPollerManager(cacheManager());
      manager.setJmxUsername(jmxUsername);
      manager.setJmxPassword(jmxPassword);
      manager.setJmxHotrodPortOffset(jmxHotrodPortOffset);
      manager.setRefreshRate(Long.valueOf(refreshRate));

      if (nodeColorAsString != null)
         manager.setMultiColor(false, Integer.parseInt(nodeColorAsString));
      else
         manager.setMultiColor(true, null);

      return manager;
   }

   @Produces
   @Default
   @ApplicationScoped
   public PollerManager<CacheNameInfo> cacheNamesPoller(VisualizerRemoteCacheManager cacheManager) {
      JmxCacheNamesPollerManager manager = new JdgJmxCacheNamesPollerManager(cacheManager);
      manager.setJmxUsername(jmxUsername);
      manager.setJmxPassword(jmxPassword);
      manager.setJmxHotrodPortOffset(jmxHotrodPortOffset);
      manager.setRefreshRate(Long.valueOf(refreshRate));

      manager.init();

      return manager;
   }

   public void destroyPollerManager(@Disposes PollerManager<CacheNameInfo> pollerManager) {
      pollerManager.destroy();
   }

   public void destroyCacheManager(@Disposes VisualizerRemoteCacheManager cacheManager) {
      cacheManager.stop();
   }
}
