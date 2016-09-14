/*
 * ContinuousDataLikelihoodDelegate.java
 *
 * Copyright (c) 2002-2016 Alexei Drummond, Andrew Rambaut and Marc Suchard
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

package dr.evomodel.treedatalikelihood;

/**
 * ContinuousDataLikelihoodDelegate
 *
 * A DataLikelihoodDelegate for continuous traits
 *
 * @author Andrew Rambaut
 * @author Marc Suchard
 * @author Philippe Lemey
 * @version $Id$
 */

import beagle.*;
import dr.evolution.alignment.PatternList;
import dr.evolution.alignment.UncertainSiteList;
import dr.evolution.datatype.DataType;
import dr.evolution.tree.MultivariateTraitTree;
import dr.evolution.tree.Tree;
import dr.evolution.util.TaxonList;
import dr.evomodel.branchmodel.BranchModel;
import dr.evomodel.branchratemodel.BranchRateModel;
import dr.evomodel.continuous.IntegratedMultivariateTraitLikelihood;
import dr.evomodel.continuous.MultivariateDiffusionModel;
import dr.evomodel.siteratemodel.SiteRateModel;
import dr.evomodel.tipstatesmodel.TipStatesModel;
import dr.evomodel.treedatalikelihood.*;
import dr.evomodel.treedatalikelihood.continuous.ContinuousTraitDataModel;
import dr.evomodel.treelikelihood.PartialsRescalingScheme;
import dr.inference.model.*;
import dr.util.Citable;
import dr.util.Citation;
import dr.util.CommonCitations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ContinuousDataLikelihoodDelegate extends AbstractModel implements DataLikelihoodDelegate, Citable {

//                                              (String traitName,
//                                               MultivariateTraitTree treeModel,
//                                               MultivariateDiffusionModel diffusionModel,
//                                               CompoundParameter traitParameter,
//                                               Parameter deltaParameter,
//                                               List<Integer> missingIndices,
//                                               boolean cacheBranches,
//                                               boolean scaleByTime,
//                                               boolean useTreeLength,
//                                               BranchRateModel rateModel,
//                                               List<BranchRateModel> driftModels,
//                                               List<BranchRateModel> optimalValues,
//                                               BranchRateModel strengthOfSelection,
//                                               Model samplingDensity,
//                                               boolean reportAsMultivariate,
//                                               boolean reciprocalRates)

    public ContinuousDataLikelihoodDelegate(MultivariateTraitTree tree,
                                            MultivariateDiffusionModel diffusionModel,
                                            ContinuousTraitDataModel dataModel,
                                            BranchRateModel rateModel) {

        super("ContinousDataLikelihoodDelegate");
        final Logger logger = Logger.getLogger("dr.evomodel.treedatalikelihood");

        logger.info("Using ContinuousDataLikelihood Delegate");

        this.diffusionModel = diffusionModel;
        this.dataModel = dataModel;

        addModel(diffusionModel);
        addModel(dataModel);

        if (rateModel != null) {
            addModel(rateModel);
        }

//        this.dataType = patternList.getDataType();
//        patternCount = patternList.getPatternCount();
//        stateCount = dataType.getStateCount();
//
//        patternWeights = patternList.getPatternWeights();
//
//        this.branchModel = branchModel;
//        addModel(this.branchModel);
//
//        this.siteRateModel = siteRateModel;
//        addModel(this.siteRateModel);
//
//        this.categoryCount = this.siteRateModel.getCategoryCount();
//
        nodeCount = tree.getNodeCount();
        tipCount = tree.getExternalNodeCount();
        internalNodeCount = nodeCount - tipCount;

        branchUpdateIndices = new int[nodeCount];
        branchLengths = new double[nodeCount];
//        scaleBufferIndices = new int[internalNodeCount];
//        storedScaleBufferIndices = new int[internalNodeCount];

        operations = new int[internalNodeCount * ContinuousDiffusionIntegrator.OPERATION_TUPLE_SIZE];

        try {


//            int compactPartialsCount = tipCount;
//            if (useAmbiguities) {
//                // if we are using ambiguities then we don't use tip partials
//                compactPartialsCount = 0;
//            }
//
            // one partials buffer for each tip and two for each internal node (for store restore)
            partialBufferHelper = new BufferIndexHelper(nodeCount, tipCount);
//
//            // one scaling buffer for each internal node plus an extra for the accumulation, then doubled for store/restore
//            scaleBufferHelper = new BufferIndexHelper(getScaleBufferCount(), 0);
//
//            evolutionaryProcessDelegate = new HomogenousSubstitutionModelDelegate(tree, branchModel);
//
//            // Attempt to get the resource order from the System Property
//            if (resourceOrder == null) {
//                resourceOrder = parseSystemPropertyIntegerArray(RESOURCE_ORDER_PROPERTY);
//            }
//            if (preferredOrder == null) {
//                preferredOrder = parseSystemPropertyIntegerArray(PREFERRED_FLAGS_PROPERTY);
//            }
//            if (requiredOrder == null) {
//                requiredOrder = parseSystemPropertyIntegerArray(REQUIRED_FLAGS_PROPERTY);
//            }
//            if (scalingOrder == null) {
//                scalingOrder = parseSystemPropertyStringArray(SCALING_PROPERTY);
//            }
//            if (extraBufferOrder == null) {
//                extraBufferOrder = parseSystemPropertyIntegerArray(EXTRA_BUFFER_COUNT_PROPERTY);
//            }
//
//            // first set the rescaling scheme to use from the parser
//            this.rescalingScheme = rescalingScheme;
//            this.delayRescalingUntilUnderflow = delayRescalingUntilUnderflow;
//
//            int[] resourceList = null;
//            long preferenceFlags = 0;
//            long requirementFlags = 0;
//
//            if (scalingOrder.size() > 0) {
//                this.rescalingScheme = PartialsRescalingScheme.parseFromString(
//                        scalingOrder.get(instanceCount % scalingOrder.size()));
//            }
//
//            if (resourceOrder.size() > 0) {
//                // added the zero on the end so that a CPU is selected if requested resource fails
//                resourceList = new int[]{resourceOrder.get(instanceCount % resourceOrder.size()), 0};
//                if (resourceList[0] > 0) {
//                    preferenceFlags |= BeagleFlag.PROCESSOR_GPU.getMask(); // Add preference weight against CPU
//                }
//            }
//
//            if (preferredOrder.size() > 0) {
//                preferenceFlags = preferredOrder.get(instanceCount % preferredOrder.size());
//            }
//
//            if (requiredOrder.size() > 0) {
//                requirementFlags = requiredOrder.get(instanceCount % requiredOrder.size());
//            }
//
//
//            // Define default behaviour here
//            if (this.rescalingScheme == PartialsRescalingScheme.DEFAULT) {
//                //if GPU: the default is dynamic scaling in BEAST
//                if (resourceList != null && resourceList[0] > 1) {
//                    this.rescalingScheme = DEFAULT_RESCALING_SCHEME;
//                } else { // if CPU: just run as fast as possible
////                    this.rescalingScheme = PartialsRescalingScheme.NONE;
//                    // Dynamic should run as fast as none until first underflow
//                    this.rescalingScheme = DEFAULT_RESCALING_SCHEME;
//                }
//            }
//
//            // to keep behaviour of the delayed scheme (always + delay)...
//            if (this.rescalingScheme == PartialsRescalingScheme.DELAYED) {
//                this.delayRescalingUntilUnderflow = true;
//                this.rescalingScheme = PartialsRescalingScheme.ALWAYS;
//            }
//
//            if (this.rescalingScheme == PartialsRescalingScheme.AUTO) {
//                preferenceFlags |= BeagleFlag.SCALING_AUTO.getMask();
//                useAutoScaling = true;
//            } else {
////                preferenceFlags |= BeagleFlag.SCALING_MANUAL.getMask();
//            }
//
//            String r = System.getProperty(RESCALE_FREQUENCY_PROPERTY);
//            if (r != null) {
//                rescalingFrequency = Integer.parseInt(r);
//                if (rescalingFrequency < 1) {
//                    rescalingFrequency = RESCALE_FREQUENCY;
//                }
//            }
//
//            String d = System.getProperty(DELAY_SCALING_PROPERTY);
//            if (d != null) {
//                this.delayRescalingUntilUnderflow = Boolean.parseBoolean(d);
//            }
//
//
//            if (preferenceFlags == 0 && resourceList == null) { // else determine dataset characteristics
//                if (stateCount == 4 && patternList.getPatternCount() < 10000) // TODO determine good cut-off
//                    preferenceFlags |= BeagleFlag.PROCESSOR_CPU.getMask();
//            }
//
//            boolean forceVectorization = false;
//            String vectorizationString = System.getProperty(FORCE_VECTORIZATION);
//            if (vectorizationString != null) {
//                forceVectorization = true;
//            }
//
//            if (BeagleFlag.VECTOR_SSE.isSet(preferenceFlags) && (stateCount != 4)
//                    && !forceVectorization
//                    ) {
//                // @todo SSE doesn't seem to work for larger state spaces so for now we override the
//                // SSE option.
//                preferenceFlags &= ~BeagleFlag.VECTOR_SSE.getMask();
//                preferenceFlags |= BeagleFlag.VECTOR_NONE.getMask();
//
//                if (stateCount > 4 && this.rescalingScheme == PartialsRescalingScheme.DYNAMIC) {
//                    this.rescalingScheme = PartialsRescalingScheme.DELAYED;
//                }
//            }
//
//            if (!BeagleFlag.PRECISION_SINGLE.isSet(preferenceFlags)) {
//                // if single precision not explicitly set then prefer double
//                preferenceFlags |= BeagleFlag.PRECISION_DOUBLE.getMask();
//            }
//
//            if (evolutionaryProcessDelegate.canReturnComplexDiagonalization()) {
//                requirementFlags |= BeagleFlag.EIGEN_COMPLEX.getMask();
//            }
//
//            beagle = BeagleFactory.loadBeagleInstance(
//                    tipCount,
//                    partialBufferHelper.getBufferCount(),
//                    compactPartialsCount,
//                    stateCount,
//                    patternCount,
//                    evolutionaryProcessDelegate.getEigenBufferCount(),
//                    evolutionaryProcessDelegate.getMatrixBufferCount(),
//                    categoryCount,
//                    scaleBufferHelper.getBufferCount(), // Always allocate; they may become necessary
//                    resourceList,
//                    preferenceFlags,
//                    requirementFlags
//            );
//
//            InstanceDetails instanceDetails = beagle.getDetails();
//            ResourceDetails resourceDetails = null;
//
//            if (instanceDetails != null) {
//                resourceDetails = BeagleFactory.getResourceDetails(instanceDetails.getResourceNumber());
//                if (resourceDetails != null) {
//                    StringBuilder sb = new StringBuilder("  Using BEAGLE resource ");
//                    sb.append(resourceDetails.getNumber()).append(": ");
//                    sb.append(resourceDetails.getName()).append("\n");
//                    if (resourceDetails.getDescription() != null) {
//                        String[] description = resourceDetails.getDescription().split("\\|");
//                        for (String desc : description) {
//                            if (desc.trim().length() > 0) {
//                                sb.append("    ").append(desc.trim()).append("\n");
//                            }
//                        }
//                    }
//                    sb.append("    with instance flags: ").append(instanceDetails.toString());
//                    logger.info(sb.toString());
//                } else {
//                    logger.info("  Error retrieving BEAGLE resource for instance: " + instanceDetails.toString());
//                }
//            } else {
//                logger.info("  No external BEAGLE resources available, or resource list/requirements not met, using Java implementation");
//            }
//
//            if (patternList instanceof UncertainSiteList) {
//                useAmbiguities = true;
//            }
//
//            logger.info("  " + (useAmbiguities ? "Using" : "Ignoring") + " ambiguities in tree likelihood.");
//            logger.info("  With " + patternList.getPatternCount() + " unique site patterns.");
//
//            for (int i = 0; i < tipCount; i++) {
//                // Find the id of tip i in the patternList
//                String id = tree.getTaxonId(i);
//                int index = patternList.getTaxonIndex(id);
//
//                if (index == -1) {
//                    throw new TaxonList.MissingTaxonException("Taxon, " + id + ", in tree, " + tree.getId() +
//                            ", is not found in patternList, " + patternList.getId());
//                } else {
//                    if (useAmbiguities) {
//                        setPartials(beagle, patternList, index, i);
//                    } else {
//                        setStates(beagle, patternList, index, i);
//                    }
//                }
//            }
//
//            beagle.setPatternWeights(patternWeights);
//
//            String rescaleMessage = "  Using rescaling scheme : " + this.rescalingScheme.getText();
//            if (this.rescalingScheme == PartialsRescalingScheme.AUTO &&
//                    resourceDetails != null &&
//                    (resourceDetails.getFlags() & BeagleFlag.SCALING_AUTO.getMask()) == 0) {
//                // If auto scaling in BEAGLE is not supported then do it here
//                this.rescalingScheme = PartialsRescalingScheme.DYNAMIC;
//                rescaleMessage = "  Auto rescaling not supported in BEAGLE, using : " + this.rescalingScheme.getText();
//            }
//            boolean parenthesis = false;
//            if (this.rescalingScheme == PartialsRescalingScheme.DYNAMIC) {
//                rescaleMessage += " (rescaling every " + rescalingFrequency + " evaluations";
//                parenthesis = true;
//            }
//            if (this.delayRescalingUntilUnderflow) {
//                rescaleMessage += (parenthesis ? ", " : "(") + "delay rescaling until first overflow";
//                parenthesis = true;
//            }
//            rescaleMessage += (parenthesis ? ")" : "");
//            logger.info(rescaleMessage);
//
//            if (this.rescalingScheme == PartialsRescalingScheme.DYNAMIC) {
//                everUnderflowed = false; // If false, BEAST does not rescale until first under-/over-flow.
//            }
//
//            updateSubstitutionModel = true;
//            updateSiteModel = true;

        } catch (Exception mte
                //TaxonList.MissingTaxonException mte
        ) {
            throw new RuntimeException(mte.toString());
        }
    }

    @Override
    public TreeDataLikelihood.TraversalType getOptimalTraversalType() {
        return TreeDataLikelihood.TraversalType.POST_ORDER;
    }

    private int getScaleBufferCount() {
        return internalNodeCount + 1;
    }

    /**
     * Sets the partials from a sequence in an alignment.
     *
     * @param beagle        beagle
     * @param patternList   patternList
     * @param sequenceIndex sequenceIndex
     * @param nodeIndex     nodeIndex
     */
//    private final void setPartials(Beagle beagle,
//                                   PatternList patternList,
//                                   int sequenceIndex,
//                                   int nodeIndex) {
//        double[] partials = new double[patternCount * stateCount * categoryCount];
//
//        boolean[] stateSet;
//
//        int v = 0;
//        for (int i = 0; i < patternCount; i++) {
//
//            if (patternList instanceof UncertainSiteList) {
//                ((UncertainSiteList) patternList).fillPartials(sequenceIndex, i, partials, v);
//                v += stateCount;
//                // TODO Add this functionality to SimpleSiteList to avoid if statement here
//            } else {
//
//                int state = patternList.getPatternState(sequenceIndex, i);
//                stateSet = dataType.getStateSet(state);
//
//                for (int j = 0; j < stateCount; j++) {
//                    if (stateSet[j]) {
//                        partials[v] = 1.0;
//                    } else {
//                        partials[v] = 0.0;
//                    }
//                    v++;
//                }
//            }
//        }
//
//        // if there is more than one category then replicate the partials for each
//        int n = patternCount * stateCount;
//        int k = n;
//        for (int i = 1; i < categoryCount; i++) {
//            System.arraycopy(partials, 0, partials, k, n);
//            k += n;
//        }
//
//        beagle.setPartials(nodeIndex, partials);
//    }

    /**
     * Sets the partials from a sequence in an alignment.
     */
//    private final void setPartials(Beagle beagle,
//                                   TipStatesModel tipStatesModel,
//                                   int nodeIndex) {
//        double[] partials = new double[patternCount * stateCount * categoryCount];
//
//        tipStatesModel.getTipPartials(nodeIndex, partials);
//
//        // if there is more than one category then replicate the partials for each
//        int n = patternCount * stateCount;
//        int k = n;
//        for (int i = 1; i < categoryCount; i++) {
//            System.arraycopy(partials, 0, partials, k, n);
//            k += n;
//        }
//
//        beagle.setPartials(nodeIndex, partials);
//    }

    /**
     * Sets the partials from a sequence in an alignment.
     *
     * @param beagle        beagle
     * @param patternList   patternList
     * @param sequenceIndex sequenceIndex
     * @param nodeIndex     nodeIndex
     */
//    private final void setStates(Beagle beagle,
//                                 PatternList patternList,
//                                 int sequenceIndex,
//                                 int nodeIndex) {
//        int i;
//
//        int[] states = new int[patternCount];
//
//        for (i = 0; i < patternCount; i++) {
//
//            states[i] = patternList.getPatternState(sequenceIndex, i);
//        }
//
//        beagle.setTipStates(nodeIndex, states);
//    }


    //    public void setStates(int tipIndex, int[] states) {
//        System.err.println("BTL:setStates");
//        beagle.setTipStates(tipIndex, states);
//        makeDirty();
//    }
//
//    public void getStates(int tipIndex, int[] states) {
//        System.err.println("BTL:getStates");
//        beagle.getTipStates(tipIndex, states);
//    }

    /**
     * Calculate the log likelihood of the current state.
     *
     * @return the log likelihood.
     */
    @Override
    public double calculateLikelihood(List<BranchOperation> branchOperations, List<NodeOperation> nodeOperations, int rootNodeNumber) throws LikelihoodUnderflowException {

//        if (RESCALING_OFF) { // a debugging switch
//            useScaleFactors = false;
//            recomputeScaleFactors = false;
//        }
//
//        int branchUpdateCount = 0;
//        for (BranchOperation op : branchOperations) {
//            branchUpdateIndices[branchUpdateCount] = op.getBranchNumber();
//            branchLengths[branchUpdateCount] = op.getBranchLength();
//            branchUpdateCount ++;
//        }
//
//        if (updateSubstitutionModel) { // TODO More efficient to update only the substitution model that changed, instead of all
//            evolutionaryProcessDelegate.updateSubstitutionModels(beagle, flip);
//
//            // we are currently assuming a no-category model...
//        }
//
//        if (updateSiteModel) {
//            double[] categoryRates = this.siteRateModel.getCategoryRates();
//            beagle.setCategoryRates(categoryRates);
//        }
//
//        if (branchUpdateCount > 0) {
//            evolutionaryProcessDelegate.updateTransitionMatrices(
//                    beagle,
//                    branchUpdateIndices,
//                    branchLengths,
//                    branchUpdateCount,
//                    flip);
//        }
//
//        if (flip) {
//            // Flip all the buffers to be written to first...
//            for (NodeOperation op : nodeOperations) {
//                partialBufferHelper.flipOffset(op.getNodeNumber());
//            }
//        }
//
//        int operationCount = nodeOperations.size();
//        int k = 0;
//        for (NodeOperation op : nodeOperations) {
//            int nodeNum = op.getNodeNumber();
//
//            operations[k] = partialBufferHelper.getOffsetIndex(nodeNum);
//
//            if (useScaleFactors) {
//                // get the index of this scaling buffer
//                int n = nodeNum - tipCount;
//
//                if (recomputeScaleFactors) {
//                    // flip the indicator: can take either n or (internalNodeCount + 1) - n
//                    scaleBufferHelper.flipOffset(n);
//
//                    // store the index
//                    scaleBufferIndices[n] = scaleBufferHelper.getOffsetIndex(n);
//
//                    operations[k + 1] = scaleBufferIndices[n]; // Write new scaleFactor
//                    operations[k + 2] = Beagle.NONE;
//
//                } else {
//                    operations[k + 1] = Beagle.NONE;
//                    operations[k + 2] = scaleBufferIndices[n]; // Read existing scaleFactor
//                }
//
//            } else {
//
//                if (useAutoScaling) {
//                    scaleBufferIndices[nodeNum - tipCount] = partialBufferHelper.getOffsetIndex(nodeNum);
//                }
//                operations[k + 1] = Beagle.NONE; // Not using scaleFactors
//                operations[k + 2] = Beagle.NONE;
//            }
//
//            operations[k + 3] = partialBufferHelper.getOffsetIndex(op.getLeftChild()); // source node 1
//            operations[k + 4] = evolutionaryProcessDelegate.getMatrixIndex(op.getLeftChild()); // source matrix 1
//            operations[k + 5] = partialBufferHelper.getOffsetIndex(op.getRightChild()); // source node 2
//            operations[k + 6] = evolutionaryProcessDelegate.getMatrixIndex(op.getRightChild()); // source matrix 2
//
//            k += Beagle.OPERATION_TUPLE_SIZE;
//        }
//
//        beagle.updatePartials(operations, operationCount, Beagle.NONE);
//
//        int rootIndex = partialBufferHelper.getOffsetIndex(rootNodeNumber);
//
//        double[] categoryWeights = this.siteRateModel.getCategoryProportions();
//
//        // This should probably explicitly be the state frequencies for the root node...
//        double[] frequencies = evolutionaryProcessDelegate.getRootStateFrequencies();
//
//        int cumulateScaleBufferIndex = Beagle.NONE;
//        if (useScaleFactors) {
//
//            if (recomputeScaleFactors) {
//                scaleBufferHelper.flipOffset(internalNodeCount);
//                cumulateScaleBufferIndex = scaleBufferHelper.getOffsetIndex(internalNodeCount);
//                beagle.resetScaleFactors(cumulateScaleBufferIndex);
//                beagle.accumulateScaleFactors(scaleBufferIndices, internalNodeCount, cumulateScaleBufferIndex);
//            } else {
//                cumulateScaleBufferIndex = scaleBufferHelper.getOffsetIndex(internalNodeCount);
//            }
//        } else if (useAutoScaling) {
//            beagle.accumulateScaleFactors(scaleBufferIndices, internalNodeCount, Beagle.NONE);
//        }
//
//        // these could be set only when they change but store/restore would need to be considered
//        beagle.setCategoryWeights(0, categoryWeights);
//        beagle.setStateFrequencies(0, frequencies);
//
//        double[] sumLogLikelihoods = new double[1];
//
//        beagle.calculateRootLogLikelihoods(new int[]{rootIndex}, new int[]{0}, new int[]{0},
//                new int[]{cumulateScaleBufferIndex}, 1, sumLogLikelihoods);
//
//        double logL = sumLogLikelihoods[0];
//
//        if (Double.isNaN(logL) || Double.isInfinite(logL)) {
//            everUnderflowed = true;
//            // turn off double buffer flipping so the next call overwrites the
//            // underflowed buffers. Flip will be turned on again in storeState for
//            // next step
//            flip = false;
//            throw new LikelihoodUnderflowException();
//        }
//
//        updateSubstitutionModel = false;
//        updateSiteModel = false;
//        //********************************************************************
//
//        // If these are needed...
//        //if (patternLogLikelihoods == null) {
//        //    patternLogLikelihoods = new double[patternCount];
//        //}
//        //beagle.getSiteLogLikelihoods(patternLogLikelihoods);
//
//        return logL;
        return 0.0;
    }

//    public void getPartials(int number, double[] partials) {
//        int cumulativeBufferIndex = Beagle.NONE;
//        /* No need to rescale partials */
//        beagle.getPartials(partialBufferHelper.getOffsetIndex(number), cumulativeBufferIndex, partials);
//    }

//    private void setPartials(int number, double[] partials) {
//        beagle.setPartials(partialBufferHelper.getOffsetIndex(number), partials);
//    }

    @Override
    public void makeDirty() {
        updateSiteModel = true;
        updateSubstitutionModel = true;
    }

    @Override
    protected void handleModelChangedEvent(Model model, Object object, int index) {
//        if (model == siteRateModel) {
//            updateSiteModel = true;
//        } else
        if (model == diffusionModel) {
            updateSubstitutionModel = true;
        }

        // Tell TreeDataLikelihood to update all nodes
        fireModelChanged();
    }

    @Override
    protected void handleVariableChangedEvent(Variable variable, int index, Parameter.ChangeType type) {
        // Do nothing
    }

    /**
     * Stores the additional state other than model components
     */
    @Override
    public void storeState() {
        partialBufferHelper.storeState();
//        evolutionaryProcessDelegate.storeState();
//
//        if (useScaleFactors || useAutoScaling) { // Only store when actually used
//            scaleBufferHelper.storeState();
//            System.arraycopy(scaleBufferIndices, 0, storedScaleBufferIndices, 0, scaleBufferIndices.length);
////             storedRescalingCount = rescalingCount;
//        }

        // turn on double buffering flipping (may have been turned off to enable a rescale)
        flip = true;
    }

    /**
     * Restore the additional stored state
     */
    @Override
    public void restoreState() {
        updateSiteModel = true; // this is required to upload the categoryRates to BEAGLE after the restore

        partialBufferHelper.restoreState();
//        evolutionaryProcessDelegate.restoreState();
//
//        if (useScaleFactors || useAutoScaling) {
//            scaleBufferHelper.restoreState();
//            int[] tmp = storedScaleBufferIndices;
//            storedScaleBufferIndices = scaleBufferIndices;
//            scaleBufferIndices = tmp;
////            rescalingCount = storedRescalingCount;
//        }

    }

    @Override
    protected void acceptState() {
    }

    // **************************************************************
    // INSTANCE CITABLE
    // **************************************************************

    @Override
    public Citation.Category getCategory() {
        return Citation.Category.TRAIT_MODELS;
    }

    @Override
    public String getDescription() {
        return "TODO";
    }

    @Override
    public List<Citation> getCitations() {
        return Collections.singletonList(CommonCitations.LEMEY_2010_PHYLOGEOGRAPHY);
    }

    // **************************************************************
    // INSTANCE VARIABLES
    // **************************************************************

    private final int nodeCount;
    private final int tipCount;
    private final int internalNodeCount;

    private final int[] branchUpdateIndices;
    private final double[] branchLengths;

    private int[] scaleBufferIndices;
    private int[] storedScaleBufferIndices;

    private final int[] operations;

    private boolean flip = true;
    private final BufferIndexHelper partialBufferHelper;
//    private final BufferIndexHelper scaleBufferHelper;

//    private PartialsRescalingScheme rescalingScheme;
//    private int rescalingFrequency = RESCALE_FREQUENCY;
//    private boolean delayRescalingUntilUnderflow = true;

    private boolean useScaleFactors = false;
    private boolean useAutoScaling = false;

//    private boolean recomputeScaleFactors = false;
//    private boolean everUnderflowed = false;
//    private int rescalingCount = 0;
//    private int rescalingCountInner = 0;

    /**
     * the patternList
     */
//    private final DataType dataType;

    /**
     * the pattern weights
     */
//    private final double[] patternWeights;

    /**
     * the number of patterns
     */
//    private final int patternCount;

    /**
     * the number of states in the data
     */
//    private final int stateCount;

    /**
     * the branch-site model for these sites
     */

    private final MultivariateDiffusionModel diffusionModel;

    private final ContinuousTraitDataModel dataModel;

    /**
     * A delegate to handle substitution models on branches
     */
//    private final EvolutionaryProcessDelegate evolutionaryProcessDelegate;

    /**
     * the site model for these sites
     */
//    private final SiteRateModel siteRateModel;

    /**
     * the pattern likelihoods
     */
//    private double[] patternLogLikelihoods = null;

    /**
     * the number of rate categories
     */
//    private final int categoryCount;

    /**
     * an array used to transfer tip partials
     */
//    private double[] tipPartials;

    /**
     * an array used to transfer tip states
     */
//    private int[] tipStates;

    /**
     * the BEAGLE library instance
     */
    private ContinuousDiffusionIntegrator cdi; // TODO Make final

    /**
     * Flag to specify that the substitution model has changed
     */
    private boolean updateSubstitutionModel;

    /**
     * Flag to specify that the site model has changed
     */
    private boolean updateSiteModel;
}
