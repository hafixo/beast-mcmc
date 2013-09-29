/*
 * MarginalLikelihoodEstimationComponentFactory.java
 *
 * Copyright (C) 2002-2013 Alexei Drummond, Andrew Rambaut and Marc Suchard
 *
 * This file is part of BEAST.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership and licensing.
 *
 * BEAST is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 *  BEAST is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BEAST; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
 * Boston, MA  02110-1301  USA
 */

package dr.app.beauti.components.marginalLikelihoodEstimation;

import dr.app.beauti.components.ComponentFactory;
import dr.app.beauti.components.sequenceerror.SequenceErrorModelComponentGenerator;
import dr.app.beauti.components.sequenceerror.SequenceErrorModelComponentOptions;
import dr.app.beauti.generator.ComponentGenerator;
import dr.app.beauti.options.BeautiOptions;
import dr.app.beauti.options.ComponentOptions;

/**
 * @author Andrew Rambaut
 * @version $Id$
 */
public class MarginalLikelihoodEstimationComponentFactory implements ComponentFactory {

    private MarginalLikelihoodEstimationComponentFactory() {
        // singleton pattern - private constructor
    }

    public ComponentGenerator getGenerator(final BeautiOptions beautiOptions) {
        if (generator == null) {
            generator = new MarginalLikelihoodEstimationGenerator(beautiOptions);
        }
        return generator;
    }

    public ComponentOptions getOptions(final BeautiOptions beautiOptions) {
        if (options == null) {
            options = new MarginalLikelihoodEstimationOptions();
        }
        return options;
    }

    private MarginalLikelihoodEstimationGenerator generator = null;
    private MarginalLikelihoodEstimationOptions options = null;

    public static ComponentFactory INSTANCE = new MarginalLikelihoodEstimationComponentFactory();
}
