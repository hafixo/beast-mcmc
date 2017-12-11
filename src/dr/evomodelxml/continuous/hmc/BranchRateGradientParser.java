/*
 * FullyConjugateTreeTipsPotentialDerivativeParser.java
 *
 * Copyright (c) 2002-2017 Alexei Drummond, Andrew Rambaut and Marc Suchard
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

package dr.evomodelxml.continuous.hmc;

import dr.evomodel.branchratemodel.ArbitraryBranchRates;
import dr.evomodel.branchratemodel.BranchRateModel;
import dr.evomodel.branchratemodel.DefaultBranchRateModel;
import dr.evomodel.treedatalikelihood.DataLikelihoodDelegate;
import dr.evomodel.treedatalikelihood.TreeDataLikelihood;
import dr.evomodel.treedatalikelihood.continuous.BranchRateGradient;
import dr.evomodel.treedatalikelihood.continuous.ContinuousDataLikelihoodDelegate;
import dr.evomodelxml.treelikelihood.TreeTraitParserUtilities;
import dr.inference.model.Parameter;
import dr.xml.*;

import static dr.evomodelxml.treelikelihood.TreeTraitParserUtilities.DEFAULT_TRAIT_NAME;

/**
 * @author Marc A. Suchard
 */

public class BranchRateGradientParser extends AbstractXMLObjectParser {

    private static final String NAME = "branchRateGradient";
    private static final String TRAIT_NAME = TreeTraitParserUtilities.TRAIT_NAME;

    @Override
    public String getParserName() {
        return NAME;
    }

    @Override
    public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        String traitName = xo.getAttribute(TRAIT_NAME, DEFAULT_TRAIT_NAME);
        final TreeDataLikelihood treeDataLikelihood = (TreeDataLikelihood) xo.getChild(TreeDataLikelihood.class);
        BranchRateModel branchRateModel = treeDataLikelihood.getBranchRateModel();

        if (branchRateModel instanceof DefaultBranchRateModel || branchRateModel instanceof ArbitraryBranchRates) {

            Parameter branchRates = null;
            if (branchRateModel instanceof ArbitraryBranchRates) {
                branchRates = ((ArbitraryBranchRates) branchRateModel).getRateParameter();
            }

            DataLikelihoodDelegate delegate = treeDataLikelihood.getDataLikelihoodDelegate();
            if (!(delegate instanceof ContinuousDataLikelihoodDelegate)) {
                throw new XMLParseException("May not provide a sequence data likelihood to compute tip trait gradient");
            }
            final ContinuousDataLikelihoodDelegate continuousData = (ContinuousDataLikelihoodDelegate) delegate;

            return new BranchRateGradient(traitName, treeDataLikelihood, continuousData, branchRates);

        } else {
            throw new XMLParseException("Only implemented for an arbitrary rates model");
        }
    }

    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        return rules;
    }

    private final XMLSyntaxRule[] rules = {
//            new XORRule(
//                    new ElementRule(FullyConjugateMultivariateTraitLikelihood.class),
            AttributeRule.newStringRule(TRAIT_NAME),
            new ElementRule(TreeDataLikelihood.class)
//            ),
//            new ElementRule(MASKING,
//                    new XMLSyntaxRule[]{
//                            new ElementRule(Parameter.class)
//                    }, true),
//            new ElementRule(Parameter.class, true),
    };

    @Override
    public String getParserDescription() {
        return null;
    }

    @Override
    public Class getReturnType() {
        return BranchRateGradient.class;
    }
}
