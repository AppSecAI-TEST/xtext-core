/*******************************************************************************
 * Copyright (c) 2008 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.xtext.parser;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parsetree.LeafNode;
import org.eclipse.xtext.tests.AbstractGeneratorTest;
import org.eclipse.xtext.util.EmfStructureComparator;

/**
 * @author Jan K�hnlein - Initial contribution and API
 * 
 */
public abstract class AbstractPartialParserTest extends AbstractGeneratorTest {

	protected static final boolean DEBUG = false;
	protected EmfStructureComparator comparator;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		comparator = new EmfStructureComparator() {
			protected boolean isRelevantChild(EObject container, EObject child) {
				return !(child instanceof LeafNode) || !((LeafNode) child).isHidden();
			}
		};
	}

}
