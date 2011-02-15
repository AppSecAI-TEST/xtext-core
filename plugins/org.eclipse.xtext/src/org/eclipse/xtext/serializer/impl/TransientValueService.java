/*******************************************************************************
 * Copyright (c) 2011 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.serializer.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.serializer.ITransientValueService;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class TransientValueService implements ITransientValueService {

	protected boolean defaultValueIsSerializeable(EStructuralFeature feature) {
		// TODO: this needs a generic implementation
		if (feature instanceof EAttribute) {
			return feature.getEType() == EcorePackage.eINSTANCE.getEInt() || feature.getEType() instanceof EEnum;
		}
		return false;
	}

	protected boolean isContainerReferenceInSameResource(EObject owner, EStructuralFeature feature) {
		if (feature instanceof EReference && ((EReference) feature).isContainer()) {
			Resource ownerResource = ((InternalEObject) owner).eDirectResource();
			// if eDirectResource is set, owner is a root element, so its container 
			// must be in another resource 
			return (ownerResource == null);
		}
		return false;
	}

	public ListTransient isListTransient(EObject semanitcObject, EStructuralFeature feature) {
		if (feature.isTransient() || isContainerReferenceInSameResource(semanitcObject, feature))
			return ListTransient.YES;
		else
			return ListTransient.NO;
	}

	public boolean isValueInListTransient(EObject semanitcObject, int index, EStructuralFeature feature) {
		return false;
	}

	public ValueTransient isValueTransient(EObject semanitcObject, EStructuralFeature feature) {
		if (feature.isTransient() || !semanitcObject.eIsSet(feature)
				|| isContainerReferenceInSameResource(semanitcObject, feature)) {
			if (defaultValueIsSerializeable(feature))
				return ValueTransient.PREFERABLY;
			else
				return ValueTransient.YES;
		} else
			return ValueTransient.NO;
	}
}
