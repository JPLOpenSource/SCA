/**
 * Created Sep 29, 2009.
 * <p>
 * Copyright 2009, by the California Institute of Technology. ALL RIGHTS
 * RESERVED. United States Government Sponsorship acknowledged. Any commercial
 * use must be negotiated with the Office of Technology Transfer at the
 * California Institute of Technology.
 * </p>
 * <p>
 * This software is subject to U.S. export control laws and regulations and has
 * been classified as 4D993. By accepting this software, the user agrees to
 * comply with all applicable U.S. export laws and regulations. User has the
 * responsibility to obtain export licenses, or other export authority as may be
 * required before exporting such information to foreign countries or providing
 * access to foreign persons.
 * </p>
 */
package gov.nasa.jpl.statechart.template;

import static org.junit.Assert.fail;
import gov.nasa.jpl.statechart.UnusedVelocityModel;
import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.UnusedVelocityModel.TransitionNode;
import gov.nasa.jpl.statechart.uml.Behavior;
import gov.nasa.jpl.statechart.uml.NamedElement;
import gov.nasa.jpl.statechart.uml.State;
import gov.nasa.jpl.statechart.uml.StateMachine;
import gov.nasa.jpl.statechart.uml.Transition;

import java.util.List;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class to hold test code against method in UnusedVelocityModel.
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UnusedVelocityModelTest extends AbstractVelocityModelTest<UnusedVelocityModel> {

    @Override
    protected UnusedVelocityModel instantiateVelocityModel (StateMachine machine, TargetLanguageMapper mapper) {
        return new UnusedVelocityModel(machine, mapper);
    }


    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass () throws Exception {
        AbstractVelocityModelTest.setUpBeforeClass();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass () throws Exception {
        AbstractVelocityModelTest.tearDownAfterClass();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp () throws Exception {
        // set up the default test snippet
        prepareUmlSnippet(SNIPPET_COMPLEX_SM);
        numRegionAndStates = 23;  // expediency: hard-coded for Complex SM
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown () throws Exception {
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.template.VelocityModel#getTransitionTree(java.util.Entry, gov.nasa.jpl.statechart.uml.Transition)}.
     * Checks that outputs are equivalent, for every state & outgoing transition
     * combination in this machine, via the model as well as template model.
     * 
     * TODO Better test may require an input that has pseudostate/connectionPointRef on transitions.
     */
    @Test
    public void testGetTransitionTree () {
        int cnt = 0;
        for (Entry<String,State> e
                : velomodel.getStates(velomodel.getStatemachine(), false).entrySet()) {
            State s = e.getValue();
            for (Transition t : s.getOutgoing()) {
                List<TransitionNode<NamedElement>> treeList = velomodel.getTransitionTree(e, t);
                // ignore first and last, placeholder for main transition down and up
                if (treeList.size() > 2) {
                    // make sure we see 3 junction transitions out (down)
                    final int juncTransExpected = 3;
                    List<TransitionNode<NamedElement>> outTrans = Util.newList();
                    for (TransitionNode<NamedElement> tn : treeList) {
                        if (!s.getName().equals(tn.source().getValue().getName())
                                && tn.down()) {
                            outTrans.add(tn);
                        }
                    }
                    Assert.assertEquals("Wrong number of junction transition nodes found!",
                            juncTransExpected,
                            outTrans.size());

                    // ... and that only 2 of 3 has guards while all 3 has actions
                    List<String> guardList = Util.newList();
                    List<String> actionList = Util.newList();
                    for (TransitionNode<NamedElement> tn : outTrans) {
                        if (tn.transition() == null) continue;
                        if (tn.transition().getGuard() != null) {
                            String guardSpec = tn.transition().getGuard().getSpecification().getBody();
                            if (guardSpec.startsWith("Guard")) {
                                // good, found guard!
                                guardList.add(guardSpec);
                            } 
                        }
                        if (tn.transition().getEffect() != null) {
                            Behavior actionSpec = tn.transition().getEffect();
                            for (String action : actionSpec.actionList()) {
                                if (Util.isFunctionCall(action)) {
                                    // found call action!
                                    actionList.add(action);
                                }   
                            }
                        }
                    }
                    System.err.println("Got these guards: " + guardList);
                    Assert.assertEquals("One and only one junction transition can be without a guard condition!",
                            juncTransExpected - 1,
                            guardList.size());
                    System.err.println("Got these actions: " + actionList);
                    Assert.assertEquals("All junction transitions should specify effect actions!",
                            juncTransExpected,
                            actionList.size());

                    // test for simple toString equivalence
                    Assert.assertEquals("Template test: transition target mismatch for " + s.getName() + "!",
                            treeList.toString(),
                            evalTemplate(velocityGetState(s.getName())
                                    + "#foreach( $transition in $model.getOutgoingTransitions($_sFound) )\n"
                                    + "#if( $transition.id().equals(\"" + t.id() + "\") )\n"
                                    + "$model.getTransitionTree($_sFound, $transition)"
                                    + "#end\n"
                                    + "#end"));
                    cnt += (treeList.size() - 2)/2;
                }
            }
        }
        if (cnt > 0) {
            System.err.println("testGetTransitionTree: Tested " + cnt + " pairs of transition nodes.");
        } else {
            fail("testGetTransitionTree: No valid transition node in model to test!");
        }
    }

}
