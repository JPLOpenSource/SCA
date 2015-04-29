/**
 * Created Aug 11, 2009.
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
package gov.nasa.jpl.statechart.uml;

import gov.nasa.jpl.statechart.Util;
import gov.nasa.jpl.statechart.autocode.UMLSnippetTestHarness;
import gov.nasa.jpl.statechart.input.identifiers.UMLLabel;
import gov.nasa.jpl.statechart.input.magicdraw.MagicDrawReader;
import gov.nasa.jpl.statechart.model.UMLModelGroup;

import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test class to test the UML set of classes for expected implementation.
 * The testing strategy is to parse UML snippets, currently taken from
 * MagicDraw UML XMI files, and check to make sure that internal-model objects
 * are present as expected.
 * <p>
 * UML snippets are parsed with {@link MagicDrawReader}, separately JUnit-tested.
 * Each snippet of UML is composed with a header and footer into a well-formed
 * UML XMI document.
 * </p>
 * <p>
 * Copyright &copy; 2009 Jet Propulsion Lab / California Institute of Technology
 * </p>
 * @author Shang-Wen Cheng <scheng@jpl.nasa.gov>
 *
 */
public class UMLStateMachineFullTest extends UMLSnippetTestHarness {
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass () throws Exception {
        staticInit();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass () throws Exception {
        staticDispose();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp () throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown () throws Exception {
        // make sure that we clear out the static stuff, incl. xmi2uml map
        UMLModelGroup.clearStaticMaps();
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLStateMachine#UMLStateMachine(org.w3c.dom.Node)}.
     * Accounts for existence of elements within a state machine by simple
     * counting.  The simple state machine should package:<ul>
     * <li> one machine named "Test1"
     * <li> one signal, (tested in {@link #testUMLClass()}) named "Ev1"
     * <li> one region, (tested in {@link #testGetRegion()}) of
     * public visibility, within which there are 2 states,
     * 1 pseudostate that is an initial state (implicit), and 3 transitions.
     * </ul> 
     */
    @Test
    public void testUMLStateMachine () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        // check state machine
        List<StateMachine> machines = Util.filter(model.getOwnedMember(), StateMachine.class);
        Assert.assertEquals("Incorrect number of state machines found!", 1, machines.size());
        for (StateMachine machine : machines) {
            // state machine name
            Assert.assertEquals("State machine name not properly set!", "Test1", machine.getName());
            // one signal
            Assert.assertEquals("Incorrect signal count!", 1,
                    Util.filter(machine.getOwnedMember(), Signal.class).size());
            // one region, but none as owned member
            Assert.assertEquals("Incorrect region count!", 1, machine.getRegion().size());
            Assert.assertEquals("Region found in owned member!", 0,
                    Util.filter(machine.getOwnedMember(), Region.class).size());
        }

        // check the complex penultimate sample (3 machines)
        model = loadUmlSnippet(SNIPPET_PENULTIMATE_SM);
        machines = Util.filter(model.getOwnedMember(), StateMachine.class);
        Assert.assertEquals("Not all state machines found!", 3, machines.size());
        // count signal events globally for model:
        Assert.assertEquals("Incorrect signal event count for model " + model.getName() + "!",
                11,
                Util.filter(model.getOwnedMember(), SignalEvent.class).size());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLStateMachine#getRegion()},
     * but also exercises member methods in {@link UMLRegion}.
     * Test that the machine's only region has the right items.
     */
    @Test
    public void testGetRegion () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        // region in state machine
        Region r = machine.getRegion().iterator().next();
        Assert.assertSame("Visibility not properly set!", VisibilityKind.PUBLIC, r.getVisibility());
        // 2 states, 1 initial pseudostate, and 3 transitions in region
        List<State> states = Util.filter(r.getSubvertex(), State.class);
        Assert.assertEquals("Incorrect state count!", 2, states.size());
        List<Pseudostate> pseudos = Util.filter(r.getSubvertex(), Pseudostate.class);
        Assert.assertEquals("Incorrect pseudostate count!", 1, pseudos.size());
        Assert.assertEquals("Incorrect transition count!", 3, r.getTransition().size());
        // but none as owned element!
        Assert.assertEquals("Items found in owned member!", 0, r.getOwnedMember().size());
        // make sure region's parent state is nobody, and parent state machine is as expected
        Assert.assertNull("Region should NOT have parent state!", r.getState());
        Assert.assertSame("Region's parent state machine not right!", machine, r.getStatemachine());
    }

//    /**
//     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLStateMachine#getConnectionPoint()}.
//     */
//    @Test
//    public void testGetConnectionPoint () {
//        fail("Not yet implemented");
//    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLStateMachine#ancestor(gov.nasa.jpl.statechart.uml.State, gov.nasa.jpl.statechart.uml.State)}.
     * We use 3 states to verify ancestor relationship: S1, S2, S2.1, where<ul>
     * <li> ancestor(S2, S2.1) == true
     * <li> ancestor(S1, S1) == true
     * <li> ancestor(S1, S2.1) == false
     * <li> ancestor(S2.1, S2) == false
     * </ul>
     */
    @Test
    public void testAncestor () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_HIERARCHICAL_SM));
        Region r = machine.getRegion().iterator().next();
        State s1 = null, s2 = null, s2c1 = null;
        // get first level states
        for (State s : Util.filter(r.getSubvertex(), State.class)) {
            if (s.getName().equals("T2S1")) {
                s1 = s;
            } else if (s.getName().equals("T2S2")) {
                s2 = s;
                // drill down to get sub-level state
                r = s2.getRegion().iterator().next();
                for (State subS : Util.filter(r.getSubvertex(), State.class)) {
                    if (subS.getName().equals("T2S21")) {
                        s2c1 = subS;
                    }
                }
            }
        }
        Assert.assertTrue("T2S2 is NOT ancestor to T2S21!", machine.ancestor(s2, s2c1));
        Assert.assertTrue("T2S1 is NOT ancestor to T2S1!", machine.ancestor(s1, s1));
        Assert.assertFalse("T2S1 is ancestor to T2S21!", machine.ancestor(s1, s2c1));
        Assert.assertFalse("T2S21 is ancestor to T2S2!", machine.ancestor(s2c1, s2));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLBehavior#UMLBehavior(org.w3c.dom.Node)}.
     */
    @Test
    public void testUMLBehavior () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        StateMachine machine = getFirstStateMachine(model);
        Assert.assertNull("Currently, behavior context is set to null!", machine.getContext());
        System.err.println("Look into why UMLBehavior implementation is incomplete.");
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLBehavior#isReentrant()}.
     */
    @Test
    public void testIsReentrant () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        Assert.assertFalse("By default, behavior is NOT reentrant!", machine.isReentrant());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLBehavior#getContext()}
     * and {@link gov.nasa.jpl.statechart.uml.UMLBehavior#setContext(gov.nasa.jpl.statechart.uml.BehavioredClassifier)}.
     */
    @Test
    public void testBehaviorContext () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        BehavioredClassifier bogusContext = new UMLBehavioredClassifier(((UMLElement) machine).getNode(), null);
        machine.setContext(bogusContext);
        Assert.assertSame("Behavior context not properly set!", bogusContext, machine.getContext());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLClass#UMLClass(org.w3c.dom.Node)}.
     * After a UMLClass is instantiated, signals should have been added to owned set.
     */
    @Test
    public void testUMLClass () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        // signals in state machine
        List<Signal> signals = Util.filter(machine.getOwnedMember(), Signal.class);
        Assert.assertEquals("Incorrect signal count!", 1, signals.size());
        Assert.assertEquals("Signal name not properly set!", "Ev1", signals.get(0).getName());
        // also, nested classifiers should have signals as well
        Assert.assertEquals("Nested classifiers not correctly populated!", 1, ((UMLClass) machine).nestedClassifier.size());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamespace#UMLNamespace(org.w3c.dom.Node)}.
     */
    @Test
    public void testUMLNamespace () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        Assert.assertTrue("Owned members not populated!", ((UMLNamespace) machine).ownedMember.size() > 0);
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamespace#getOwnedMember()}.
     * For simple state machine model, should have 3 members, one of which is
     * the state machine and the other 2 are signal events.
     */
    @Test
    public void testGetOwnedMember () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Incorrect number of owned members!", 3, model.getOwnedMember().size());
        Assert.assertEquals("Incorrect number of state machines!", 1,
                Util.filter(model.getOwnedMember(), StateMachine.class).size());
        Assert.assertEquals("Incorrect number of signal events!", 2,
                Util.filter(model.getOwnedMember(), SignalEvent.class).size());

        model = loadUmlSnippet(SNIPPET_TOPLEVEL_SIGNAL);
        Assert.assertEquals("Incorrect number of top-level signals!", 1,
                Util.filter(model.getOwnedMember(), Signal.class).size());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamespace#getNamesOfMember(gov.nasa.jpl.statechart.uml.NamedElement)}.
     */
    @Test
    public void testGetNamesOfMember () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        Set<String> nameSet = machine.getNamesOfMember(machine);
        Assert.assertTrue("No names found to test!", nameSet.size() > 0);
        for (String name : nameSet) {
            Assert.assertTrue("Name doesn't end with Test1 in some namespace!", name.endsWith("Test1"));
        }
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#UMLNamedElement(org.w3c.dom.Node)}.
     * After a UMLNamedElement is instantiated, the visibility should be set.
     */
    @Test
    public void testUMLNamedElement () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertNotNull("Visibility not set?", ((UMLNamedElement) model).getVisibility());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#getName()}.
     */
    @Test
    public void testGetName () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Model name not correct!", "Data", model.getName());
        StateMachine machine = getFirstStateMachine(model);
        Assert.assertEquals("Machine name not correct!", "Test1", machine.getName());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#getQualifiedName()}.
     */
    @Test
    public void testGetQualifiedName () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Model qualified name not correct!", "Data", model.getQualifiedName());
        StateMachine machine = getFirstStateMachine(model);
        Assert.assertEquals("Machine qualified name not correct!", "Data::Test1", machine.getQualifiedName());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#getNamespace()}.
     * Make sure that a model and its containing state machine are in the same
     * namespace.
     */
    @Test
    public void testGetNamespace () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        StateMachine machine = getFirstStateMachine(model);
        Assert.assertSame("Machine's parent UML namespace should be the model",
                model, ((UMLNamedElement) machine).getNamespace());
        Assert.assertNull("Model has no parent UML namespace!",
                ((UMLNamedElement) model).getNamespace());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#allNamespaces()}.
     * There should be one and only one namespace for model.
     */
    @Test
    public void testAllNamespaces () {
        StateMachine machine = getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        Assert.assertEquals("Only one namespace should be found for machine!", 1, machine.allNamespaces().size());
        Signal signal = Util.filter(machine.getOwnedMember(), Signal.class).get(0);
        Assert.assertEquals("Signal should have two namespaces!", 2, signal.allNamespaces().size());
    }

//    /**
//     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#isDistinguishableFrom(gov.nasa.jpl.statechart.uml.NamedElement, gov.nasa.jpl.statechart.uml.Namespace)}.
//     */
//    @Test
//    public void testIsDistinguishableFrom () {
//        fail("Not yet implemented");
//    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLNamedElement#separator()}.
     */
    @Test
    public void testSeparator () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Separator changed?!", "::", model.separator());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#UMLElement(org.w3c.dom.Node)}.
     * After a UMLElement is instantiated, the ID and domElement should both be set.
     */
    @Test
    public void testUMLElement () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertNotNull("ID not set?", ((UMLElement) model).id);
        Assert.assertNotNull("DOM element not set?", ((UMLElement) model).domElement);
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#id()}.
     */
    @Test
    public void testId () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Model ID incorrectly set!", "eee_1045467100313_135436_1", model.id());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#getNode()}.
     */
    @Test
    public void testGetNode () {
        UMLStateMachine machine = (UMLStateMachine)getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        Assert.assertNotNull("DOM node not set?!", machine.getNode());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#allOwnedElements()}.
     */
    @Test
    public void testAllOwnedElements () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Model should have zero owned elements!", 0, model.allOwnedElements().size());
        StateMachine machine = getFirstStateMachine(model);
        Assert.assertEquals("Machien should have zero owned elements!", 0, machine.allOwnedElements().size());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#getDOMElement()}.
     */
    @Test
    public void testGetDOMElement () {
        UMLStateMachine machine = (UMLStateMachine)getFirstStateMachine(loadUmlSnippet(SNIPPET_SIMPLE_SM));
        Assert.assertNotNull("DOM element not set?!", machine.getDOMElement());
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#getAttribute(org.w3c.dom.Node, java.lang.String)}.
     * Simply test getting the name and visibility of the top model node.
     */
    @Test
    public void testGetAttributeNodeString () {
        UMLModel model = (UMLModel) loadUmlSnippet(SNIPPET_SIMPLE_SM);
        Assert.assertEquals("Name attribute of model incorrect!", "Data", model.getAttribute(model.getNode(), "name"));
        Assert.assertEquals("Visibility attribute of model incorrect!", "public", model.getAttribute(model.getNode(), "visibility"));
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#getAttribute(org.w3c.dom.Node, gov.nasa.jpl.statechart.input.identifiers.UMLLabel)}.
     * Get the attribute labled by signal for a SignalEvent node.
     */
    @Test
    public void testGetAttributeNodeUMLLabel () {
        UMLModel model = (UMLModel) loadUmlSnippet(SNIPPET_SIMPLE_SM);
        List<SignalEvent> events = Util.filter(model.getOwnedMember(), SignalEvent.class);
        Assert.assertTrue("No signal events found?!", events.size() > 0);
        int eventsWithSignal = 0;
        for (SignalEvent e : events) {
            if (e.getSignal() != null) {
                ++eventsWithSignal;
                Assert.assertEquals("Signal attribute of SignalEvent incorrect!",
                        "_12_5_9120299_1181847090283_973661_298",
                        model.getAttribute(((UMLSignalEvent) e).getNode(), UMLLabel.KEY_SIGNAL));
            }
        }
        Assert.assertTrue("No signal events with a signal to test!", eventsWithSignal > 0);
    }

    /**
     * Test method for {@link gov.nasa.jpl.statechart.uml.UMLElement#getParentAs(java.lang.Class)}
     * and for {@link gov.nasa.jpl.statechart.uml.UMLElement#getParentAs(org.w3c.dom.Node, java.lang.Class)},
     * which is implicitly tested.
     * Obtain a state within the state machine and make sure that the parent
     * is as expected all the way to the root.
     */
    @Test
    public void testGetParentAsClassOfT () {
        Model model = loadUmlSnippet(SNIPPET_SIMPLE_SM);
        StateMachine machine = getFirstStateMachine(model);
        Region region = machine.getRegion().iterator().next();
        State state = Util.filter(region.getSubvertex(), State.class).get(0);
        Assert.assertSame("State's parent is NOT region!", region,
                ((UMLElement) state).getParentAs(Region.class));
        Assert.assertSame("Region's parent is NOT machine!", machine,
                ((UMLElement) region).getParentAs(StateMachine.class));
        Assert.assertSame("StateMachine's parent is NOT model!", model,
                ((UMLElement) machine).getParentAs(Model.class));
    }

}
