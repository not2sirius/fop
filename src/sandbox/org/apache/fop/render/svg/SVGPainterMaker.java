/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.render.svg;

import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.render.intermediate.AbstractIFPainterMaker;
import org.apache.fop.render.intermediate.IFPainter;
import org.apache.fop.render.intermediate.IFPainterConfigurator;

/**
 * Painter factory for SVG output.
 */
public class SVGPainterMaker extends AbstractIFPainterMaker {

    private static final String[] MIMES = new String[] {SVGConstants.MIME_TYPE};

    /** {@inheritDoc} */
    public IFPainter makePainter(FOUserAgent ua) {
        return new SVGPainter();
    }

    /** {@inheritDoc} */
    public boolean needsOutputStream() {
        return true;
    }

    /** {@inheritDoc} */
    public String[] getSupportedMimeTypes() {
        return MIMES;
    }

    /** {@inheritDoc} */
    public IFPainterConfigurator getConfigurator(FOUserAgent userAgent) {
        // TODO Auto-generated method stub
        return null;
    }

}
