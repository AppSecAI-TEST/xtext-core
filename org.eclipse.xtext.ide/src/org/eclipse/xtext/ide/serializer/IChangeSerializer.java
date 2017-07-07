/*******************************************************************************
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ide.serializer;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.ide.serializer.impl.ChangeSerializer;
import org.eclipse.xtext.serializer.diagnostic.ISerializationDiagnostic;
import org.eclipse.xtext.util.IAcceptor;

import com.google.inject.ImplementedBy;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
@ImplementedBy(ChangeSerializer.class)
public interface IChangeSerializer {

	// TODO return TextRegionAccessDiffBuilder
	// TODO handle errors that happened during collecting incoming references
	void beginRecordChanges(Resource resource);

	void endRecordChanges(IAcceptor<IEmfResourceChange> changeAcceptor);

	void setErrorAcceptor(ISerializationDiagnostic.Acceptor acceptor);

	// void setUpdateLocalCrossReferences(boolean value);

	// void setUpdateGlobalCrossReferences(boolean value);
}
