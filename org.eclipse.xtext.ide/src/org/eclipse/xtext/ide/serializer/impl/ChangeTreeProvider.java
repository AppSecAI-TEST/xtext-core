/*******************************************************************************
 * Copyright (c) 2017 TypeFox GmbH (http://www.typefox.io) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.xtext.ide.serializer.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ResourceChange;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * @author Moritz Eysholdt - Initial contribution and API
 */
public class ChangeTreeProvider {

	public class EObjectChange {
		private final EList<FeatureChange> changes;
		private final Set<EObjectChange> children = Sets.newLinkedHashSet();
		private final EObject eObject;

		public EObjectChange(EObject eObject) {
			this(eObject, ECollections.emptyEList());
		}

		public EObjectChange(EObject eObject, EList<FeatureChange> changes) {
			super();
			Preconditions.checkNotNull(eObject);
			this.eObject = eObject;
			this.changes = changes;
		}

		public EList<FeatureChange> getChanges() {
			return changes == null ? ECollections.emptyEList() : changes;
		}

		public Set<EObjectChange> getChildren() {
			return ImmutableSet.copyOf(children);
		}

		public EObject getEObject() {
			return eObject;
		}

	}

	public static class ResourceRecording {
		private Set<EObject> removed;
		private final Resource resource;
		private ResourceChange resourceChange;
		private final ResourceSetRecording resourceSetRecording;

		private final Set<EObjectChange> roots = Sets.newLinkedHashSet();

		public ResourceRecording(ResourceSetRecording rsRecording, Resource resource) {
			this(rsRecording, resource, null);
		}

		public ResourceRecording(ResourceSetRecording rsRecording, Resource resource, ResourceChange resourceChange) {
			super();
			this.resourceSetRecording = rsRecording;
			this.resource = resource;
			this.resourceChange = resourceChange;
		}

		public List<EObjectChange> getAllEObjectRecordings() {
			LinkedList<EObjectChange> todo = new LinkedList<>();
			todo.addAll(roots);
			List<EObjectChange> result = Lists.newArrayList();
			while (!todo.isEmpty()) {
				EObjectChange next = todo.pop();
				result.add(next);
				todo.addAll(next.getChildren());
			}
			return result;
		}

		public Resource getResource() {
			return resource;
		}

		public ResourceChange getResourceChange() {
			return resourceChange;
		}

		public ResourceSetRecording getResourceSetRecording() {
			return resourceSetRecording;
		}

		public List<EObjectChange> getRootEObjectRecordings() {
			return ImmutableList.copyOf(roots);
		}

	}

	public static class ResourceSetRecording {

		private Map<EObject, EObjectChange> objects;
		private List<ResourceRecording> resources = Lists.newArrayList();
		private final ResourceSet resourceSet;

		public ResourceSetRecording(ResourceSet resourceSet) {
			super();
			this.resourceSet = resourceSet;
		}

		public Map<EObject, EObjectChange> getAllEObjectRecordings() {
			return objects;
		}

		public ResourceRecording getRecordedResource(Resource resource) {
			for (ResourceRecording r : resources) {
				if (r.resource == resource) {
					return r;
				}
			}
			return null;
		}

		public List<ResourceRecording> getRecordedResources() {
			return resources;
		}

		public ResourceSet getResourceSet() {
			return resourceSet;
		}

	}

	public ResourceSetRecording createChangeTree(ResourceSet resourceSet, ChangeDescription desc) {
		ResourceSetRecording rs = new ResourceSetRecording(resourceSet);
		Map<Resource, ResourceRecording> result = Maps.newLinkedHashMap();
		EMap<EObject, EList<FeatureChange>> recorded = desc.getObjectChanges();
		Set<EObject> unconsumed = Sets.newHashSet(recorded.keySet());
		Map<EObject, EObjectChange> objectChanges = Maps.newLinkedHashMap();
		for (Entry<EObject, EList<FeatureChange>> e : desc.getObjectChanges().entrySet()) {
			EList<FeatureChange> featureChange = e.getValue();
			EObject object = e.getKey();
			objectChanges.put(object, new EObjectChange(object, featureChange));

		}
		for (EObjectChange change : Lists.newArrayList(objectChanges.values())) {
			EObjectChange current = change;
			while (true) {
				EObject container = current.eObject.eContainer();
				if (container != null) {
					EObjectChange next = objectChanges.get(container);
					if (next == null) {
						next = new EObjectChange(container);
						next.children.add(current);
						objectChanges.put(container, next);
					} else {
						if (!next.children.add(current)) {
							break;
						}
					}
					current = next;
				} else {
					Resource resource = current.eObject.eResource();
					ResourceRecording resourceChange = result.get(resource);
					if (resourceChange == null) {
						resourceChange = new ResourceRecording(rs, resource);
						result.put(resource, resourceChange);
						// TODO: check that only recorded resources contain
						// modification
					}
					resourceChange.roots.add(current);
					break;
				}
			}
		}
		rs.resources = ImmutableList.copyOf(result.values());
		rs.objects = ImmutableMap.copyOf(objectChanges);
		return rs;
	}

}
