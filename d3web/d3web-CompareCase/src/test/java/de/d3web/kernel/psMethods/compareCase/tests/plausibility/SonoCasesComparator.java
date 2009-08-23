package de.d3web.kernel.psMethods.compareCase.tests.plausibility;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import de.d3web.caserepository.CaseObject;
import de.d3web.caserepository.sax.CaseObjectListCreator;
import de.d3web.kernel.domainModel.KnowledgeBase;
import de.d3web.kernel.psMethods.compareCase.comparators.CaseComparator;
import de.d3web.kernel.psMethods.compareCase.comparators.CompareMode;
import de.d3web.persistence.xml.PersistenceManager;

/**
 * @author bruemmer
 */
public class SonoCasesComparator {

	public static void main(String[] args) {
		try {
			PersistenceManager pm = PersistenceManager.getInstance();
			KnowledgeBase kb = pm.load(new URL(args[0]));

			CaseObjectListCreator colc = new CaseObjectListCreator();

			List cases = colc.createCaseObjectList(new File(args[1]), kb);

			long start = System.currentTimeMillis();

			Iterator iter0 = cases.iterator();
			while (iter0.hasNext()) {

				double maxSim = 0;
				double minSim = 1.1;
				CaseObject case0 = (CaseObject) iter0.next();

				Iterator iter = cases.iterator();
				while (iter.hasNext()) {
					CaseObject cobj = (CaseObject) iter.next();

					CompareMode cMode = CompareMode.BOTH_FILL_UNKNOWN;
					cMode.setIsIgnoreMutualUnknowns(true);

					double sim =
						CaseComparator.calculateSimilarityBetweenCases(
							cMode,
							case0,
							cobj);

					if (!case0.getId().equals(cobj.getId())) {

						if (sim > maxSim) {
							maxSim = sim;
						}
						if (sim < minSim) {
							minSim = sim;
						}
					} else if(sim != 1) {
						System.err.println("sim != 1");
					} else {
						System.out.println("sim = 1. OK.");
					}
				}

				System.out.println(
					case0.getId()
						+ ": maxSim="
						+ ((int) (maxSim * 100))
						+ "%\t minSim="
						+ ((int) (minSim * 100))
						+ "%");
			}

			System.out.println(
				"Whole comparison took "
					+ (System.currentTimeMillis() - start) / 1000
					+ " seconds");

		} catch (Exception e) {
			System.err.println(
				"usage: java SonoCasesComparator <kb-jar> <cases.xml>");
		}
	}

}
