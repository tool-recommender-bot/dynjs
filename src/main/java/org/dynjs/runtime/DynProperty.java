/**
 *  Copyright 2012 Douglas Campos, and individual contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dynjs.runtime;

import java.util.HashMap;
import java.util.Map;

public class DynProperty {

    private final Map<String, Object> attributes = new HashMap<>();
    public boolean configurable = false;
    
    public DynProperty() {
        setValue( DynThreadContext.UNDEFINED );
    }

    public DynProperty setAttribute(String key, Object value) {
        this.attributes.put(key, value);
        return this;
    }

    public Object getAttribute(String key) {
        if (this.attributes.containsKey(key)) {
            return this.attributes.get(key);
        } else {
            return DynThreadContext.UNDEFINED;
        }
    }
    
    /** Retrieve the value of the property.
     * 
     * @return
     */
    public Object getValue() {
        Object value = this.attributes.get(  "value"  );
        if ( value == null ) {
            return DynThreadContext.UNDEFINED;
        }
        return value;
    }
    
    /** Set the value of the property.
     * 
     * @param value
     */
    public void setValue(Object value) {
        this.attributes.put(  "value", value );
    }
}
