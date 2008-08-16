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

package org.apache.fop.prototype.breaking.layout;

import java.util.Collection;
import java.util.List;

import org.apache.fop.prototype.breaking.Alternative;
import org.apache.fop.prototype.knuth.KnuthElement;

public interface Layout {

    LayoutClass getLayoutClass();

    Layout getPrevious();

    void setPrevious(Layout previous);

    /** Returns the demerits represented by this layout plus all of its predecessors. */
    double getDemerits();

    void setDemerits(double demerits);

    ProgressInfo getProgress();

    Collection<Alternative> getAlternatives();

    List<KnuthElement> getElements();

    void addElement(KnuthElement e);

    int getDifference();

    int getIPD(int bpd); // TODO block-specific

    int getDimension(); // TODO package-private

    Layout clone(Collection<Alternative> alternatives);

    void setPage(); // TODO page-specific

    boolean isPage();
}