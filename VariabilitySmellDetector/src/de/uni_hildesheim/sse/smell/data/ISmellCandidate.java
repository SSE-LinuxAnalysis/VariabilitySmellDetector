package de.uni_hildesheim.sse.smell.data;

import de.uni_hildesheim.sse.smell.filter.AbstractSatSolverFilter;
import de.uni_hildesheim.sse.smell.filter.old_permanent_parent.SmellDetector;

/**
 * A possible smell. This can be passed to the {@link SmellDetector} to find
 * actual smells.
 */
public interface ISmellCandidate extends IDataElement {

    /**
     * @return  A condition that can be combined with the VarModel to find smells.
     *      If <code>VarModel AND thisCondition</code> is <code>true</code>, then we
     *       assume a smell.
     *      <br />
     *      Note that the actual formula used is <code>VarModel AND
     *      NOT(smellExtract)</code>. Thus the condition returned by this must
     *      already contain the NOT. 
     */
    public String getVarModelCondition();
    
    /**
     * This method is called when the {@link SmellDetector} detects that this
     * smell candidate is an actual smell.
     * @param solver The solver to get the solution from.
     * @return A smell.
     */
    public ISmell getSmell(AbstractSatSolverFilter solver);
    
}
