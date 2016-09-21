package de.uni_hildesheim.sse.smell.util;

import java.util.List;

import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;

public interface ICnfConverter {

    /**
     * Converts a given CST to CNF.
     * 
     * @param tree The formula that is converted to CNF.
     *      It can only contain OR, AND, NOT and the boolean constants TRUE and FALSE.
     * 
     * @return A list of clauses, that contain disjunctions over literals.
     *      Literals may be negated.
     *      The input tree is equisatisfiable to a conjunction over all clauses.
     *      
     * @throws ConstraintException If the constraint is not parseable.
     */
    public List<ConstraintSyntaxTree> convertToCnf(ConstraintSyntaxTree tree) throws ConstraintException;
    
}
