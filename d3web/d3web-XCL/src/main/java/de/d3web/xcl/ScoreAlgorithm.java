package de.d3web.xcl;

import java.util.Collection;

import de.d3web.core.inference.PropagationEntry;
import de.d3web.core.session.Session;

public interface ScoreAlgorithm {

	/**
	 * Creates and returns an empty InferenceTrace capable to be used with this
	 * scoring algorithm.
	 * 
	 * @return the empty inference trace
	 */
	InferenceTrace createInferenceTrace(XCLModel xclModel);

	/**
	 * Updates a given XCLModel with the given PropagationEntries.
	 * 
	 * @param xclModel the XCLModel to be updated
	 * @param entries the propagation entries relevant for the model update
	 * @param session the current case
	 */
	void update(XCLModel xclModel, Collection<PropagationEntry> entries, Session session);

	/**
	 * Recalculates the states of the XCLModels of the case. The updated
	 * XCLModels since the last call to this method are delivered as a hint. It
	 * might me necessary to refresh the states of other models as well
	 * (depending on the ScoreAlgorithm used).
	 * 
	 * @param updatedModels the models updated before this call
	 * @param session the current case
	 */
	void refreshStates(Collection<XCLModel> updatedModels, Session session);

	/**
	 * Returns the established threshold of the specified model
	 * 
	 * @created 29.06.2010
	 * @param model XCL Model
	 * @return established threshold
	 */
	double getEstablishedThreshold(XCLModel model);

	/**
	 * Returns the minimal support of the specified model
	 * 
	 * @created 29.06.2010
	 * @param model XCL Model
	 * @return minimal support
	 */
	double getMinSupport(XCLModel model);

	/**
	 * Returns the suggested threshold of the specified model
	 * 
	 * @created 29.06.2010
	 * @param model XCL Model
	 * @return suggested threshold
	 */
	double getSuggestedThreshold(XCLModel model);
}
