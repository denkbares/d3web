package de.d3web.kernel.psMethods.verbalizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Logger;

import de.d3web.kernel.domainModel.ruleCondition.AbstractCondition;
import de.d3web.kernel.psMethods.xclPattern.XCLModel;
import de.d3web.kernel.psMethods.xclPattern.XCLRelation;
import de.d3web.kernel.psMethods.xclPattern.XCLRelationType;
import de.d3web.kernel.verbalizer.ConditionVerbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager;
import de.d3web.kernel.verbalizer.Verbalizer;
import de.d3web.kernel.verbalizer.VerbalizationManager.RenderingFormat;

/**
 * This class generates a verablisation of a XCLModel object. 
 * 
 * @author kazamatzuri
 * 
 */
public class XclVerbalizer implements Verbalizer {

	@Override
	public Class[] getSupportedClassesForVerbalization() {
		Class[] supportedClasses = { XCLModel.class };
		return supportedClasses;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.d3web.kernel.verbalizer.Verbalizer#getSupportedRenderingTargets()
	 */
	@Override
	public RenderingFormat[] getSupportedRenderingTargets() {
		RenderingFormat[] r = { RenderingFormat.HTML, RenderingFormat.PLAIN_TEXT };
		return r;
	}

	@Override
	/*
	 * @return produces an textual representation of this model
	 */
	public String verbalize(Object o, RenderingFormat targetFormat,
			Map<String, Object> parameter) {
		// no if-s to catch false inputs, as verbalisation manager should be
		// delegating those here
		String verbalisation = "";
		XCLModel model = (XCLModel) o;
		if (targetFormat == RenderingFormat.HTML) {
			verbalisation = verbalizeHTML(model);
		} else if (targetFormat == RenderingFormat.PLAIN_TEXT) {
			verbalisation = verbalizePlainText(model, parameter);
		} else {
			Logger.getLogger("Verbalizer").warning(
					"RenderingTarget" + targetFormat + " is not supported by XclVerbalizer!");
		}
		return verbalisation;
	}
	
	public String verbalizePlainText(XCLModel model, Map<String, Object> parameter) {
		
		ArrayList<XCLRelationType> types = new ArrayList<XCLRelationType>();
        types.add(XCLRelationType.requires);
        types.add(XCLRelationType.sufficiently);
        types.add(XCLRelationType.explains);
        types.add(XCLRelationType.contradicted);
        
		StringBuffer text = new StringBuffer();
		
        ConditionVerbalizer v = new ConditionVerbalizer();
        
		text.append(model.getSolution().getText() + " {\n");
		
        Map<XCLRelationType, Collection<XCLRelation>> relationMap = model.getTypedRelations();
        for (XCLRelationType type : types) {
            Collection<XCLRelation> relationsCol = relationMap.get(type);
            ArrayList<XCLRelation> relations = new ArrayList<XCLRelation>();
            relations.addAll(relationsCol);
            if (type == XCLRelationType.explains) {
            	Collections.sort(relations, 
            			new XCLRelationComparator());
            }
            for (XCLRelation rel : relations) {
                AbstractCondition cond = rel.getConditionedFinding();
                String weight = "";
                if(type == XCLRelationType.explains) {
                	if (rel.getWeight() != XCLRelation.DEFAULT_WEIGHT) {
                		weight = "[" + doubleToString(rel.getWeight()) + "]";   
                	}
                } else if (type == XCLRelationType.contradicted) {
                	weight = "[--]";
                } else if (type == XCLRelationType.requires) {
                	weight = "[!]";
                } else if (type == XCLRelationType.sufficiently) {
                	weight = "[++]";
                }

                text.append("  " + v.verbalize(cond, RenderingFormat.PLAIN_TEXT, parameter)
                        + " " + weight);
                if (text.toString().endsWith(" ")) {
                	text.replace(text.length() -1 , text.length(), "");
                }
                text.append(",\n");
            }
        }
        text.append("}");
        if (!(model.getMinSupport() == XCLModel.defaultMinSupport
        		&& model.getEstablishedThreshold() == XCLModel.defaultEstablishedThreshold
        		&& model.getSuggestedThreshold() == XCLModel.defaultSuggestedThreshold)) {
        	text.append("[ establishedThreshold = " + model.getEstablishedThreshold() + ",\n");
        	text.append("   suggestedThreshold = " + model.getSuggestedThreshold() + ",\n");
        	text.append("   minSupport = " + model.getMinSupport() + "\n ]");
        }
		
		return text.toString();
	}

	/**
	 * @return produces an textual representation of this model
	 */
	public String verbalizeHTML(XCLModel model) {
		String cont = new String();
		String nl = "<br />";
		cont = model.getSolution().toString() + nl;
		if (model.getNecessaryRelations().size() > 0) {
			cont += "necessary relations:" + nl;
			for (XCLRelation current : model.getNecessaryRelations()) {
				cont += VerbalizationManager.getInstance().verbalize(
						current.getConditionedFinding(), RenderingFormat.HTML)
						+ nl;
			}
		}
		if (model.getSufficientRelations().size() > 0) {
			cont += nl + "sufficient relations:" + nl;
			for (XCLRelation current : model.getSufficientRelations()) {
				cont += VerbalizationManager.getInstance().verbalize(
						current.getConditionedFinding(), RenderingFormat.HTML)
						+ nl;
			}
		}
		if (model.getContradictingRelations().size() > 0) {
			cont += nl + "contradicting relations:" + nl;
			for (XCLRelation current : model.getContradictingRelations()) {
				cont += VerbalizationManager.getInstance().verbalize(
						current.getConditionedFinding(), RenderingFormat.HTML)
						+ nl;
			}
		}

		cont += nl + "explains:" + nl;
		for (XCLRelation current : model.getRelations()) {
			cont += VerbalizationManager.getInstance().verbalize(
					current.getConditionedFinding(), RenderingFormat.HTML)
					+ (current.getWeight() == XCLRelation.DEFAULT_WEIGHT ? nl
							: " weighted: " + current.getWeight() + nl);
		}
		cont += nl;
		if (model.getSuggestedThreshold() != XCLModel.defaultSuggestedThreshold)
			cont += " non standard suggested threshold: "
					+ model.getSuggestedThreshold() + nl;
		if (model.getMinSupport() != XCLModel.defaultMinSupport)
			cont += " non standard minimum support: " + model.getMinSupport()
					+ nl;
		if (model.getEstablishedThreshold() != XCLModel.defaultEstablishedThreshold)
			cont += " non standard establihshed threshold: "
					+ model.getEstablishedThreshold() + nl;
		return cont;
	}
	
	private class XCLRelationComparator implements Comparator<XCLRelation> {

		public int compare(XCLRelation r1, XCLRelation r2) {
			return - Double.compare(r1.getWeight(), r2.getWeight());
        }
		
	}
	
	private String doubleToString(double d) {
		String s = Double.toString(d);
		if (s.endsWith(".0")) {
			s = s.substring(0, s.length() - 2);
		}
		return s;
	}
}
