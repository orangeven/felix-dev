/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.felix.upnp.basedriver.export;



import java.util.Dictionary;
import java.util.Enumeration;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.StateVariable;

import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPStateVariable;

import org.apache.felix.upnp.basedriver.Activator;
import org.apache.felix.upnp.basedriver.util.Converter;

/* 
* @author <a href="mailto:dev@felix.apache.org">Felix Project Team</a>
*/
public class ExporterUPnPEventListener implements UPnPEventListener {

	private Device d;
	
	public ExporterUPnPEventListener(Device d){
		this.d=d;
	}
		
	/**
	 * @see org.osgi.service.upnp.UPnPEventListener#notifyUPnPEvent(java.lang.String, java.lang.String, java.util.Dictionary)
	 */
	public void notifyUPnPEvent(String deviceId, String serviceId,Dictionary events) {
        Device dAux = null;
        if(d.getUDN().equals(deviceId)){
            dAux=d;
        }else{
            dAux= d.getDevice(deviceId);
        }
        Service s = dAux.getService(serviceId);
		// fix 2/9/2004 francesco 
		Enumeration e = events.keys();
		while (e.hasMoreElements()) {
            StateVariable sv;
            String dataType;
            String name;
            //TODO Keep for compatibility? The OSGi compendium R4 pag. 257 requires pair containg <UPnPStateVariable,Object value> instead of <String name,Object value>
            Object key = e.nextElement();
            if(key instanceof String){
                name=(String) key;
                sv=s.getStateVariable(name);
                dataType=sv.getDataType();
            }else if(key instanceof UPnPStateVariable){
                UPnPStateVariable variable = (UPnPStateVariable) key;
                name=variable.getName();
                dataType=variable.getUPnPDataType();
                sv=s.getStateVariable(name);
            }else{
                Activator.logger.ERROR(deviceId + " notified the change in the StateVariable of " 
                                       + serviceId + " but the key Java type contained in the Dictiories was " 
                                       + key.getClass().getName() + " instead of " + UPnPStateVariable.class.getName()
                                       + " as specified by OSGi Compendium Release 4 pag. 257");
                continue;
            }
            
			try {
				sv.setValue(Converter.toString(events.get(key),dataType));
			} catch (Exception ignored) {
                Activator.logger.ERROR("UPnP Base Driver Exporter: error converting datatype while sending event, exception message follows:"+ignored.getMessage());
			}
		}
	}
}
