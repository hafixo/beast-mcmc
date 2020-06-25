/*
 * BayesianSkylineGradient.java
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

package dr.evomodel.coalescent.hmc;

import dr.evolution.coalescent.ConstantPopulation;
import dr.evolution.coalescent.DemographicFunction;
import dr.evolution.util.Units;
import dr.evomodel.coalescent.BayesianSkylineLikelihood;
import dr.evomodel.coalescent.OldAbstractCoalescentLikelihood;
import dr.evomodel.tree.TreeModel;
import dr.evomodel.treedatalikelihood.discrete.NodeHeightProxyParameter;
import dr.inference.hmc.GradientWrtParameterProvider;
import dr.inference.hmc.HessianWrtParameterProvider;
import dr.inference.model.Likelihood;
import dr.inference.model.Parameter;
import dr.math.Binomial;
import dr.xml.Reportable;

/**
 * @author Marc A. Suchard
 * @author Xiang Ji
 */
public class BayesianSkylineGradient implements
        GradientWrtParameterProvider, HessianWrtParameterProvider, Reportable {

    private final BayesianSkylineLikelihood likelihood;
    private final WrtParameter wrtParameter;
    private final OldAbstractCoalescentLikelihood.IntervalNodeMapping intervalNodeMapping;

    public BayesianSkylineGradient(BayesianSkylineLikelihood likelihood,
                                   WrtParameter wrtParameter) {
        this.likelihood = likelihood;
        this.wrtParameter = wrtParameter;
        this.intervalNodeMapping = likelihood.getIntervalNodeMapping();
    }

    @Override
    public Likelihood getLikelihood() {
        return likelihood;
    }

    @Override
    public Parameter getParameter() {
        return wrtParameter.getParameter(likelihood);
    }

    @Override
    public int getDimension() {
        return getParameter().getDimension();
    }

    @Override
    public double[] getGradientLogDensity() {
        return wrtParameter.getGradientLogDensity(likelihood);
    }

    @Override
    public double[] getDiagonalHessianLogDensity() {
        return wrtParameter.getDiagonalHessianLogDensity(likelihood);
    }

    @Override
    public double[][] getHessianLogDensity() {
        throw new RuntimeException("Not yet implemented!");
    }

    @Override
    public String getReport() {
        return GradientWrtParameterProvider.getReportAndCheckForError(this, wrtParameter.getParameterLowerBound(), Double.POSITIVE_INFINITY, null);
    }

    public enum WrtParameter {
        NODE_HEIGHT("nodeHeight") {

            Parameter parameter;

            @Override
            Parameter getParameter(BayesianSkylineLikelihood likelihood) {
                if (parameter == null) {
                    TreeModel treeModel = (TreeModel) likelihood.getTree();
                    parameter = new NodeHeightProxyParameter("allInternalNode", treeModel, true);
                }
                return parameter;
            }

            @Override
            double[] getGradientLogDensity(BayesianSkylineLikelihood likelihood) {
                return getGradientWrtNodeHeights(likelihood);
            }

            private double[] getGradientWrtNodeHeights(BayesianSkylineLikelihood likelihood) {
                getWarning(likelihood);

                double[] unsortedGradients = new double[likelihood.getTree().getInternalNodeCount()];
                double[] sortedHeights = new double[likelihood.getTree().getInternalNodeCount()];
                int[] intervalIndices = new int[likelihood.getTree().getInternalNodeCount()];
                int internalNodeIndex = 0;

                likelihood.setupIntervals();

                double currentTime = 0.0;

                int groupIndex=0;
                int[] groupSizes = likelihood.getGroupSizes();
                double[] groupEnds = likelihood.getGroupHeights();

                int subIndex = 0;

                ConstantPopulation cp = new ConstantPopulation(Units.Type.YEARS);

                for (int j = 0; j < likelihood.getIntervalCount(); j++) {

                    final double ps = likelihood.getPopSize(groupIndex, currentTime + (likelihood.getInterval(j)/2.0), groupEnds);
                    cp.setN0(ps);
                    if (likelihood.getIntervalType(j) == OldAbstractCoalescentLikelihood.CoalescentEventType.COALESCENT) {
                        subIndex += 1;
                        if (subIndex >= groupSizes[groupIndex]) {
                            groupIndex += 1;
                            subIndex = 0;
                        }
                    }
                    currentTime += likelihood.getInterval(j);

                    if (likelihood.getIntervalType(j) == OldAbstractCoalescentLikelihood.CoalescentEventType.COALESCENT) {
                        final double intervalGradient = getIntervalGradient(cp, currentTime, likelihood.getLineageCount(j), likelihood.getIntervalType(j));
                        unsortedGradients[internalNodeIndex] = intervalGradient;
                        sortedHeights[internalNodeIndex] = currentTime;
                        intervalIndices[internalNodeIndex] = j + 1;
                        internalNodeIndex++;
                    }
                }
                for (int i = 0; i < likelihood.getTree().getInternalNodeCount() - 1; i++) {
                    unsortedGradients[i] -= getIntervalGradient(cp, sortedHeights[i],
                            likelihood.getLineageCount(intervalIndices[i]), likelihood.getIntervalType(intervalIndices[i]));
                }

                return likelihood.getIntervalNodeMapping().sortByNodeNumbers(unsortedGradients);
            }

            private double getIntervalGradient(DemographicFunction demogFunction,
                                               double timeOfThisCoal, int lineageCount,
                                               OldAbstractCoalescentLikelihood.CoalescentEventType type) {
                final double intensityDeriv = demogFunction.getIntensityGradient(timeOfThisCoal);
                final double kchoose2 = Binomial.choose2(lineageCount);
                final double gradient = -kchoose2 * intensityDeriv;

                return gradient;
            }


            @Override
            double[] getDiagonalHessianLogDensity(BayesianSkylineLikelihood likelihood) {
                throw new RuntimeException("Not yet implemented!");
            }

            @Override
            double getParameterLowerBound() {
                return 0;
            }

            @Override
            public void getWarning(BayesianSkylineLikelihood likelihood) {
                if (likelihood.getType() != BayesianSkylineLikelihood.STEPWISE_TYPE) {
                    throw new RuntimeException("Only implemented for stepwise type of Skyline model.");
                }

            }
        };

        private final String name;

        WrtParameter(String name) {
            this.name = name;
        }

        abstract Parameter getParameter(BayesianSkylineLikelihood likelihood);

        abstract double[] getGradientLogDensity(BayesianSkylineLikelihood likelihood);

        abstract double[] getDiagonalHessianLogDensity(BayesianSkylineLikelihood likelihood);

        abstract double getParameterLowerBound();

        public abstract void getWarning(BayesianSkylineLikelihood likelihood);

        public static WrtParameter factory(String match) {
            for (WrtParameter type : WrtParameter.values()) {
                if (match.equalsIgnoreCase(type.name)) {
                    return type;
                }
            }
            return null;
        }
    }
}