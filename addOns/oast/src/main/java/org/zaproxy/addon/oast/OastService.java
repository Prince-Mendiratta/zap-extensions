/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zaproxy.addon.oast;

import java.util.ArrayList;
import java.util.List;

public abstract class OastService {

    private final List<OastRequestHandler> oastRequestHandlerList = new ArrayList<>();

    public abstract String getName();

    /** Starts the OastService. This method should be called after ZAP has initialised. */
    public abstract void startService();

    public abstract void stopService();

    public void poll() {}

    public void sessionChanged() {}

    public void addOastRequestHandler(OastRequestHandler oastRequestHandler) {
        oastRequestHandlerList.add(oastRequestHandler);
    }

    public void handleOastRequest(OastRequest oastRequest) {
        for (OastRequestHandler handler : oastRequestHandlerList) {
            handler.handle(oastRequest);
        }
    }

    public void clearOastRequestHandlers() {
        oastRequestHandlerList.clear();
    }
}
