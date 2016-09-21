package de.uni_hildesheim.sse.smell.util;

import java.util.List;

import de.uni_hildesheim.sse.model_extender.convert.ListMaxTermConverter;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.Constraint;

public class MaxTermConverterWrapper implements ICnfConverter {

    @Override
    public List<ConstraintSyntaxTree> convertToCnf(ConstraintSyntaxTree tree) throws ConstraintException {
        ListMaxTermConverter converter = new ListMaxTermConverter();
        Constraint tmp = new Constraint(null);
        try {
            tmp.setConsSyntax(tree);
        } catch (CSTSemanticException e) {
            throw new ConstraintException(e);
        }
        converter.convert(tmp);
        
        return converter.getCSTs();
    }

}
