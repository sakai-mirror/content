/**********************************************************************************
 * $URL$
 * $Id$
 ***********************************************************************************
 *
 * Copyright (c) 2007 The Sakai Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 *      http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.content.tool;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sakaiproject.authz.cover.SecurityService;
import org.sakaiproject.component.cover.ComponentManager;
import org.sakaiproject.content.api.ContentCollection;
import org.sakaiproject.content.api.ContentCollectionEdit;
import org.sakaiproject.content.api.ContentEntity;
import org.sakaiproject.content.api.ContentResource;
import org.sakaiproject.content.api.ContentResourceEdit;
import org.sakaiproject.content.api.GroupAwareEdit;
import org.sakaiproject.content.api.GroupAwareEntity;
import org.sakaiproject.content.api.ResourceToolAction;
import org.sakaiproject.content.api.ResourceToolActionPipe;
import org.sakaiproject.content.api.ResourceType;
import org.sakaiproject.content.api.ResourceTypeRegistry;
import org.sakaiproject.content.cover.ContentHostingService;
import org.sakaiproject.content.api.ServiceLevelAction;
import org.sakaiproject.content.api.GroupAwareEntity.AccessMode;
import org.sakaiproject.content.cover.ContentTypeImageService;
import org.sakaiproject.content.tool.ResourcesAction.ContentPermissions;
import org.sakaiproject.entity.api.EntityPropertyNotDefinedException;
import org.sakaiproject.entity.api.EntityPropertyTypeException;
import org.sakaiproject.entity.api.Reference;
import org.sakaiproject.entity.api.ResourceProperties;
import org.sakaiproject.entity.api.ResourcePropertiesEdit;
import org.sakaiproject.entity.cover.EntityManager;
import org.sakaiproject.exception.IdUnusedException;
import org.sakaiproject.exception.InconsistentException;
import org.sakaiproject.exception.PermissionException;
import org.sakaiproject.exception.TypeException;
import org.sakaiproject.site.api.Group;
import org.sakaiproject.site.api.Site;
import org.sakaiproject.site.cover.SiteService;
import org.sakaiproject.time.api.Time;
import org.sakaiproject.time.cover.TimeService;
import org.sakaiproject.tool.cover.ToolManager;
import org.sakaiproject.user.api.User;
import org.sakaiproject.user.cover.UserDirectoryService;
import org.sakaiproject.util.ParameterParser;
import org.sakaiproject.util.ResourceLoader;
import org.sakaiproject.util.Validator;

/**
 * ListItem
 *
 */
public class ListItem
{
	/** Resource bundle using current language locale */
    private static ResourceLoader rb = new ResourceLoader("content");

	/** Resource bundle using current language locale */
    private static ResourceLoader trb = new ResourceLoader("types");

    private static final Log logger = LogFactory.getLog(ListItem.class);
    
    protected static Comparator DEFAULT_COMPARATOR = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_DISPLAY_NAME, true);
    protected static final Comparator PRIORITY_SORT_COMPARATOR = ContentHostingService.newContentHostingComparator(ResourceProperties.PROP_CONTENT_PRIORITY, true);

	/** A long representing the number of milliseconds in one week.  Used for date calculations */
	protected static final long ONE_WEEK = 1000L * 60L * 60L * 24L * 7L;

	public static final String DOT = "_";

	/**
	 * @param entity
	 * @param parent
	 * @param registry
	 * @param expandAll
	 * @param expandedFolders
	 * @param items_to_be_moved
	 * @param items_to_be_copied
	 * @param depth
	 * @param userSelectedSort
	 * @param preventPublicDisplay
	 * @return
	 */
	public static ListItem getListItem(ContentEntity entity, ListItem parent, ResourceTypeRegistry registry, boolean expandAll, Set<String> expandedFolders, List<String> items_to_be_moved, List<String> items_to_be_copied, int depth, Comparator userSelectedSort, boolean preventPublicDisplay)
	{
		ListItem item = null;
		boolean isCollection = entity.isCollection();
		
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		
		boolean isAvailabilityEnabled = contentService.isAvailabilityEnabled();
        
        Reference ref = EntityManager.newReference(entity.getReference());

        if(entity == null)
        {
        	item = new ListItem("");
        }
        else
        {
        	item = new ListItem(entity);
        }
        item.setPubviewPossible(! preventPublicDisplay);
        item.setDepth(depth);
        
        /*
         * calculate permissions for this entity.  If its access mode is 
         * GROUPED, we need to calculate permissions based on current user's 
         * role in group. Otherwise, we inherit from containing collection
         * and check to see if additional permissions are set on this entity
         * that were't set on containing collection...
         */
        if(GroupAwareEntity.AccessMode.INHERITED == entity.getAccess())
        {
        	// permissions are same as parent or site
        	if(parent == null)
        	{
        		// permissions are same as site
        		item.setPermissions(ResourcesAction.getPermissions(entity.getId(), null));
        	}
        	else
        	{
        		// permissions are same as parent
        		item.setPermissions(ResourcesAction.getPermissions(entity.getId(), parent.getPermissions()));
        	}
        }
        else if(GroupAwareEntity.AccessMode.GROUPED == entity.getAccess())
        {
        	// permissions are determined by group(s)
        	item.setPermissions(ResourcesAction.getPermissions(entity.getId(), null));
        }

        if(isCollection)
        {
        	ContentCollection collection = (ContentCollection) entity;
        	
        	if(item.isTooBig)
        	{
        		// do nothing
        	}
			else if(expandAll)
        	{
        		expandedFolders.add(entity.getId());
        	}

			if(expandedFolders.contains(entity.getId()))
			{
				item.setExpanded(true);

		       	List<ContentEntity> children = collection.getMemberResources();
		       	
				Comparator comparator = null;
				if(userSelectedSort != null)
				{
					comparator = userSelectedSort;
				}
				else
				{
					boolean hasCustomSort = false;
					try
					{
						hasCustomSort = collection.getProperties().getBooleanProperty(ResourceProperties.PROP_HAS_CUSTOM_SORT);
					}
					catch(Exception e)
					{
						// ignore -- let value be false
					}
					if(hasCustomSort)
					{
						comparator = PRIORITY_SORT_COMPARATOR;
					}
					else
					{
						comparator = DEFAULT_COMPARATOR;
					}
				}
				
				Collections.sort(children, comparator);

	        	Iterator<ContentEntity> childIt = children.iterator();
	        	while(childIt.hasNext())
	        	{
	        		ContentEntity childEntity = childIt.next();
	        		if(childEntity.getAccess() == AccessMode.GROUPED)
	        		{
	        			if(childEntity.isCollection())
	        			{
	        				if(! contentService.allowGetCollection(childEntity.getId()))
	        				{
		        				continue;
	        				}
	        			}
	        			else
	        			{
	        				if(!contentService.allowGetResource(childEntity.getId()))
	        				{
	        					continue;
	        				}
	        			}
	        		}
	        		
					if(isAvailabilityEnabled && ! contentService.isAvailable(childEntity.getId()))
					{
						continue;
					}

	        		ListItem child = getListItem(childEntity, item, registry, expandAll, expandedFolders, items_to_be_moved, items_to_be_copied, depth + 1, userSelectedSort, preventPublicDisplay);
	        		if(items_to_be_copied != null && items_to_be_copied.contains(child.id))
	        		{
	        			child.setSelectedForCopy(true);
	        		}
	        		if(items_to_be_moved != null && items_to_be_moved.contains(child.id))
	        		{
	        			child.setSelectedForMove(true);
	        		}
	        		item.addMember(child);
	        	}
			}
			
			item.setAddActions(ResourcesAction.getAddActions(entity, item.getPermissions(), registry));
			//this.members = coll.getMembers();
			item.setIconLocation( ContentTypeImageService.getContentTypeImage("folder"));
        }
        
		item.setOtherActions(ResourcesAction.getActions(entity, item.getPermissions(), registry));
		
		item.setPasteActions(ResourcesAction.getPasteActions(entity, item.getPermissions(), registry,items_to_be_moved, items_to_be_copied));
		
		return item;
	}

	protected boolean selectedForMove = false;

	protected boolean selectedForCopy = false;
	
	protected String name;
	protected String id;
	protected List<ResourceToolAction> addActions;
	protected List<ResourceToolAction> otherActions;
	protected List<ResourceToolAction> pasteActions;
	protected String otherActionsLabel;
	protected List<ListItem> members;
	protected Set<ContentPermissions> permissions;
	protected boolean selected;
	protected boolean collection;
	protected String hoverText;
	protected String accessUrl;
	protected String iconLocation;
	protected String mimetype;
	protected String resourceType;
	protected ResourceType resourceTypeDef = null;
	protected boolean isEmpty = true;
	protected boolean isExpanded = false;
	protected boolean isSortable = false;
	protected boolean isTooBig = false;
	protected String size = "";
	protected String sizzle = "";
	protected String createdBy;
	protected String createdTime;
	protected String modifiedBy;
	protected String modifiedTime;
	protected int depth;

	protected Map<String, ResourceToolAction> multipleItemActions = new HashMap<String, ResourceToolAction>();

	protected boolean canSelect = true;

	/** 
	 * Access settings
	 * Access mode can be "grouped" or "inherited". Inherited access mode
	 * can be "site" or "grouped". Site access implies that the site's
	 * permissions apply, possibly with changes due to custom folder 
	 * permissions in the hierarchy.   
	 */
	protected ContentEntity entity;
	protected AccessMode accessMode;
	protected AccessMode inheritedAccessMode;
	protected Collection<Group> groups = new Vector<Group>();
	protected Collection<Group> inheritedGroups = new Vector<Group>();
	protected Collection<Group> possibleGroups = new Vector<Group>();
	protected Collection<Group> allowedRemoveGroups = new Vector<Group>();
	protected Collection<Group> allowedAddGroups = new Vector<Group>();
	protected Map<String,Group> siteGroupsMap = new HashMap<String, Group>();

	protected boolean isPubviewPossible;
	protected boolean isPubviewInherited = false;
	protected boolean isPubview = false;

	protected boolean hidden;
	protected boolean isAvailable;
	protected boolean useReleaseDate;
	protected Time releaseDate;
	protected boolean useRetractDate;

	protected Time retractDate;
	
	protected String description;
	protected String copyrightInfo;
	protected String copyrightStatus;
	protected boolean copyrightAlert;

	protected ListItem parent;

	protected String containingCollectionId;

	protected boolean isUserSite = false;
	protected boolean isDropbox = false;

	protected boolean isSiteCollection = false;
	protected boolean hasQuota = false;
	protected boolean canSetQuota = false;
	protected String quota;

	protected boolean nameIsMissing = false;


	
	/**
	 * @param entity
	 */
	public ListItem(ContentEntity entity)
	{
		this.entity = entity;
		if(entity == null)
		{
			return;
		}
		String refstr = entity.getReference();
		Reference ref = EntityManager.newReference(refstr);
		String contextId = ref.getContext();
		boolean isUserSite = false;
		if(contextId != null)
		{
			isUserSite = SiteService.isUserSite(contextId);
		}
		setUserSite(isUserSite);
		
		this.isSiteCollection = this.siteCollection(refstr);

		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		if(entity.getContainingCollection() == null)
		{
			this.containingCollectionId = null;
		}
		else
		{
			this.containingCollectionId = entity.getContainingCollection().getId();
		}
		if(this.id != null)
		{
			this.isDropbox = contentService.isInDropbox(id);
		}
		else if(this.containingCollectionId != null)
		{
			this.isDropbox = contentService.isInDropbox(this.containingCollectionId);
		}
		ResourceProperties props = entity.getProperties();
		this.accessUrl = entity.getUrl();
		this.collection = entity.isCollection();
		this.id = entity.getId();
		this.name = props.getProperty(ResourceProperties.PROP_DISPLAY_NAME);
		this.description = props.getProperty(ResourceProperties.PROP_DESCRIPTION);
		
		this.permissions = new TreeSet<ContentPermissions>();
		this.selected = false;
		
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		this.resourceType = entity.getResourceType();
		ResourceType typeDef = registry.getType(resourceType);
		this.hoverText = this.name;
		if(typeDef != null)
		{
			this.hoverText = typeDef.getLocalizedHoverText(entity);
			this.iconLocation = typeDef.getIconLocation(this.entity);
			String[] args = { typeDef.getLabel() };
			this.otherActionsLabel = trb.getFormattedMessage("action.other", args);
		}

		if(this.collection)
		{
			ContentCollection collection = (ContentCollection) entity;
        	int collection_size = collection.getMemberCount();
        	if(collection_size == 1)
        	{
        		setSize(rb.getString("size.item"));
        	}
        	else
        	{
	        	String[] args = { Integer.toString(collection_size) };
	        	setSize(rb.getFormattedMessage("size.items", args));
        	}
			setIsEmpty(collection_size < 1);
			setSortable(contentService.isSortByPriorityEnabled() && collection_size > 1 && collection_size < ResourcesAction.EXPANDABLE_FOLDER_SIZE_LIMIT);
			if(collection_size > ResourcesAction.EXPANDABLE_FOLDER_SIZE_LIMIT)
			{
				setIsTooBig(true);
			}
			// setup for quota - ADMIN only, site-root collection only
			if (SecurityService.isSuperUser())
			{
				String siteCollectionId = ContentHostingService.getSiteCollection(contextId);
				if(siteCollectionId.equals(entity.getId()))
				{
					setCanSetQuota(true);
					try
					{
						// Getting the quota as a long validates the property
						long quota = props.getLongProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
						setHasQuota(true);
						setQuota(Long.toString(quota));
					}
					catch (Exception any)
					{
					}
				}
			}
		}
		else 
		{
			ContentResource resource = (ContentResource) entity;
			this.mimetype = resource.getContentType();
			if(this.mimetype == null)
			{
				this.mimetype = props.getProperty(ResourceProperties.PROP_CONTENT_TYPE);
			}
			if(this.mimetype == null)
			{
				
			}
			else if(this.iconLocation == null)
			{
				this.iconLocation = ContentTypeImageService.getContentTypeImage(this.mimetype);
			}
			String size = "";
			String sizzle = "";
			if(props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH) != null)
			{
				long size_long = 0;
                try
                {
	                size_long = props.getLongProperty(ResourceProperties.PROP_CONTENT_LENGTH);
                }
                catch (EntityPropertyNotDefinedException e)
                {
	                // TODO Auto-generated catch block
	                logger.warn("EntityPropertyNotDefinedException for size of " + this.id);
                }
                catch (EntityPropertyTypeException e)
                {
	                size = props.getProperty(ResourceProperties.PROP_CONTENT_LENGTH);
                }
				NumberFormat formatter = NumberFormat.getInstance(rb.getLocale());
				formatter.setMaximumFractionDigits(1);
				if(size_long > 700000000L)
				{
					String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)) };
					size = rb.getFormattedMessage("size.gb", args);
					String[] argyles = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)), formatter.format(size_long) };
					sizzle = rb.getFormattedMessage("size.gbytes", argyles);
				}
				else if(size_long > 700000L)
				{
					String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L)) };
					size = rb.getFormattedMessage("size.mb", args);
					String[] argyles = { formatter.format(1.0 * size_long / (1024L * 1024L)), formatter.format(size_long) };
					sizzle = rb.getFormattedMessage("size.mbytes", argyles);
				}
				else if(size_long > 700L)
				{
					String[] args = { formatter.format(1.0 * size_long / 1024L) };
					size = rb.getFormattedMessage("size.kb", args);
					String[] argyles = { formatter.format(1.0 * size_long / 1024L), formatter.format(size_long) };
					sizzle = rb.getFormattedMessage("size.kbytes", argyles);
				}
				else 
				{
					String[] args = { formatter.format(size_long) };
					size = rb.getFormattedMessage("size.bytes", args);
					sizzle = rb.getFormattedMessage("size.bytes", args);
				}
			}
			setSize(size);
			setSizzle(sizzle);
			
			this.copyrightStatus = props.getProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
			this.copyrightInfo = props.getProperty(ResourceProperties.PROP_COPYRIGHT);
			try 
			{
				this.copyrightAlert = props.getBooleanProperty(ResourceProperties.PROP_COPYRIGHT_ALERT);
			} 
			catch (EntityPropertyNotDefinedException e) 
			{
				this.copyrightAlert = false;
			} 
			catch (EntityPropertyTypeException e) 
			{
				this.copyrightAlert = false;
			}
		}
		
		User creator = ResourcesAction.getUserProperty(props, ResourceProperties.PROP_CREATOR);
		if(creator != null)
		{
			String createdBy = creator.getDisplayName();
			setCreatedBy(createdBy);
		}
		User modifier = ResourcesAction.getUserProperty(props, ResourceProperties.PROP_MODIFIED_BY);
		if(modifier != null)
		{
			String modifiedBy = modifier.getDisplayName();
			setModifiedBy(modifiedBy);
		}
		// setCreatedBy(props.getProperty(ResourceProperties.PROP_CREATOR));
		this.setModifiedTime(props.getPropertyFormatted(ResourceProperties.PROP_MODIFIED_DATE));
		this.setCreatedTime(props.getPropertyFormatted(ResourceProperties.PROP_CREATION_DATE));
		
		Site site = null;
		Collection<Group> site_groups = new Vector<Group>();
		
		try 
		{
			site = SiteService.getSite(ToolManager.getCurrentPlacement().getContext());
		} 
		catch (IdUnusedException e) 
		{
			logger.warn("resourcesAction.newEditItems() IdUnusedException ", e);
		}
		if(site != null)
		{
			for(Group gr : (Collection<Group>) site.getGroups())
			{
				if(gr.getId().equals(site.getId()))
				{
					// ignore
				}
				else
				{
					site_groups.add(gr);
				}
			}
		}
				
		setSiteGroups(site_groups);
		
		this.accessMode = entity.getAccess();
		this.inheritedAccessMode = entity.getInheritedAccess();
		//this.effectiveAccess = entity.getInheritedAccess();
		this.groups.clear();
		this.groups.addAll(entity.getGroupObjects());
		this.inheritedGroups.clear();
		this.inheritedGroups.addAll(entity.getInheritedGroupObjects());
		
		if(this.inheritedAccessMode == AccessMode.GROUPED)
		{
			setPossibleGroups(this.inheritedGroups);
		}
		else 
		{
			setPossibleGroups(site_groups);
		}
        
		Collection<Group> groupsWithRemovePermission = null;
		if(AccessMode.GROUPED == this.accessMode)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(id);
			Collection<Group> more = contentService.getGroupsWithRemovePermission(ref.getContainer());
			if(more != null && ! more.isEmpty())
			{
				groupsWithRemovePermission.addAll(more);
			}
		}
		else if(AccessMode.GROUPED == this.inheritedAccessMode)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(ref.getContainer());
		}
		else if(contentService.getSiteCollection(ref.getContext()) != null)
		{
			groupsWithRemovePermission = contentService.getGroupsWithRemovePermission(contentService.getSiteCollection(ref.getContext()));
		}
		this.allowedRemoveGroups.clear();
		if(groupsWithRemovePermission != null)
		{
			this.allowedRemoveGroups.addAll(groupsWithRemovePermission);
		}
		
		Collection<Group> groupsWithAddPermission = null;
		if(AccessMode.GROUPED == this.accessMode)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(id);
			Collection<Group> more = contentService.getGroupsWithAddPermission(ref.getContainer());
			if(more != null && ! more.isEmpty())
			{
				groupsWithAddPermission.addAll(more);
			}
		}
		else if(AccessMode.GROUPED == this.inheritedAccessMode)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(ref.getContainer());
		}
		else if(contentService.getSiteCollection(ref.getContext()) != null)
		{
			groupsWithAddPermission = contentService.getGroupsWithAddPermission(contentService.getSiteCollection(ref.getContext()));
		}
		this.allowedAddGroups.clear();
		if(groupsWithAddPermission != null)
		{
			this.allowedAddGroups.addAll(groupsWithAddPermission);
		}

        this.isPubviewInherited = contentService.isInheritingPubView(id);
		if (!this.isPubviewInherited) 
		{
			this.isPubview = contentService.isPubView(id);
		}
		
		this.hidden = entity.isHidden();
		Time releaseDate = entity.getReleaseDate();
		if(releaseDate == null)
		{
			this.useReleaseDate = false;
			this.releaseDate = TimeService.newTime();
		}
		else
		{
			this.useReleaseDate = true;
			this.releaseDate = releaseDate;
		}
		Time retractDate = entity.getRetractDate();
		if(retractDate == null)
		{
			this.useRetractDate = false;
		}
		else
		{
			this.useRetractDate = true;
			this.retractDate = retractDate;
		}
		this.isAvailable = entity.isAvailable();

	}

	private void setSizzle(String sizzle) 
	{
		this.sizzle = sizzle;
	}

	public void setQuota(String quota) 
	{
		this.quota = quota;
		
	}

	public void setHasQuota(boolean hasQuota) 
	{
		this.hasQuota = hasQuota;
	}

	public void setCanSetQuota(boolean canSetQuota) 
	{
		this.canSetQuota = canSetQuota;
	}

	public void setUserSite(boolean isUserSite) 
	{
		this.isUserSite = isUserSite;
	}

	public ListItem(ResourceToolActionPipe pipe, ListItem parent, Time defaultRetractTime)
	{
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		this.entity = null;
		this.containingCollectionId = parent.getId();
		ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
		this.resourceType = pipe.getAction().getTypeId();
		this.resourceTypeDef = registry.getType(resourceType);
		this.hoverText = this.name;
		if(resourceTypeDef != null)
		{
			this.hoverText = resourceTypeDef.getLocalizedHoverText(null);
			this.iconLocation = resourceTypeDef.getIconLocation(this.entity);
			String[] args = { resourceTypeDef.getLabel() };
			this.otherActionsLabel = trb.getFormattedMessage("action.other", args);
			this.name = trb.getFormattedMessage("create.unknown", args);
		}

		this.collection = ResourceType.TYPE_FOLDER.equals(resourceType);
		this.id = "";
		this.parent = parent;
		this.permissions = parent.getPermissions();
		this.selected = false;
		

		if(this.collection)
		{
        	int collection_size = 0;
        	String[] args = { Integer.toString(0) };
	        setSize(rb.getFormattedMessage("size.items", args));
 			setIsEmpty(true);
			setSortable(false);
			setIsTooBig(false);
		}
		else 
		{
			this.mimetype = pipe.getMimeType();
			if(this.mimetype == null)
			{
				
			}
			else if(this.iconLocation == null)
			{
				this.iconLocation = ContentTypeImageService.getContentTypeImage(this.mimetype);
			}
			String size = "";
			if(pipe.getContent() != null)
			{
				long size_long = pipe.getContent().length;
				NumberFormat formatter = NumberFormat.getInstance(rb.getLocale());
				formatter.setMaximumFractionDigits(1);
				if(size_long > 700000000L)
				{
					String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L * 1024L)) };
					size = rb.getFormattedMessage("size.gb", args);
				}
				else if(size_long > 700000L)
				{
					String[] args = { formatter.format(1.0 * size_long / (1024L * 1024L)) };
					size = rb.getFormattedMessage("size.mb", args);
				}
				else if(size_long > 700L)
				{
					String[] args = { formatter.format(1.0 * size_long / 1024L) };
					size = rb.getFormattedMessage("size.kb", args);
				}
				else 
				{
					String[] args = { formatter.format(size_long) };
					size = rb.getFormattedMessage("size.bytes", args);
				}
			}
			setSize(size);

		}
		
		if(this.id != null)
		{
			this.isDropbox = contentService.isInDropbox(id);
		}
		else if(this.containingCollectionId != null)
		{
			this.isDropbox = contentService.isInDropbox(this.containingCollectionId);
		}
		else
		{
			this.isDropbox = parent.isDropbox;
		}
		
		Time now = TimeService.newTime();
		User creator = UserDirectoryService.getCurrentUser();
		if(creator != null)
		{
			String createdBy = creator.getDisplayName();
			setCreatedBy(createdBy);
			setModifiedBy(createdBy);
		}
		// setCreatedBy(props.getProperty(ResourceProperties.PROP_CREATOR));
		this.setModifiedTime(now.getDisplay());
		this.setCreatedTime(now.getDisplay());
		
		this.setSiteGroups(parent.siteGroupsMap.values());
		this.accessMode = AccessMode.INHERITED;
		this.inheritedAccessMode = parent.getEffectiveAccess();
		//this.effectiveAccess = parent.getEffectiveAccess();
		this.groups.clear();
		//this.groups.addAll();
		this.inheritedGroups.clear();
		if(parent.getAccessMode() == AccessMode.GROUPED)
		{
			this.inheritedGroups.addAll(parent.getGroups());
			this.setPossibleGroups(parent.getGroups());
		}
		else
		{
			this.inheritedGroups.addAll(parent.getInheritedGroups());
			this.setPossibleGroups(parent.getPossibleGroups());

		}
        
		this.allowedRemoveGroups = new Vector(parent.allowedRemoveGroups);		
		this.allowedAddGroups = new Vector(parent.allowedAddGroups);
		
		this.isPubviewPossible = parent.isPubviewPossible;
        this.isPubviewInherited = parent.isPubviewInherited || parent.isPubview;
        if(this.isPubviewInherited)
        {
        	this.isPubview = false;
        }
        else
        {
        	this.isPubview = contentService.isPubView(id);
        }
		
		this.hidden = false;
		this.useReleaseDate = false;
		Time releaseDate = TimeService.newTime();
		this.useRetractDate = false;
		Time retractDate = TimeService.newTime(defaultRetractTime.getTime());
		this.isAvailable = parent.isAvailable();

	}

	/**
	 * @param entityId
	 */
	public ListItem(String entityId)
	{
		this.id = entityId;
		this.containingCollectionId = ContentHostingService.getContainingCollectionId(entityId);
		
		String refstr = ContentHostingService.getReference(id);
		this.isSiteCollection = this.siteCollection(refstr);

	}

	/**
     * @param child
     */
    public void addMember(ListItem member)
    {
        if(this.members == null)
        {
        	this.members = new Vector<ListItem>();
        }
        this.members.add(member);
    }

	/**
     * @param action
     */
    public void addMultipleItemAction(ResourceToolAction action)
    {
	    this.multipleItemActions.put(action.getId(), action);
    }
	
	/**
	 * @param permission
	 */
	public void addPermission(ContentPermissions permission)
	{
		if(this.permissions == null)
		{
			this.permissions = new TreeSet<ContentPermissions>();
		}
		this.permissions.add(permission);
	}
	
	/**
     * @param group
     * @return
     */
    public boolean allowedRemove(Group group)
    {
    	boolean allowed = false;
    	
    	for(Group gr : this.allowedRemoveGroups)
    	{
    		if(gr.getId().equals(group.getId()))
    		{
    			allowed = true;
    			break;
    		}
    	}
    	
    	return allowed;
    }

	public boolean canRead()
	{
		return isPermitted(ContentPermissions.READ);
	}

	/**
     * @return
     */
    public boolean canSelect()
    {
    	return canSelect;
    }

	protected void captureAccess(ParameterParser params, String index) 
	{
		String access_mode = params.getString("access_mode" + index);
		SortedSet groups = new TreeSet();
		
		if(access_mode == null || AccessMode.GROUPED.toString().equals(access_mode))
		{
			// we inherit more than one group and must check whether group access changes at this item
			String[] access_groups = params.getStrings("access_groups" + index);
			
			SortedSet<String> new_groups = new TreeSet<String>();
			if(access_groups != null)
			{
				new_groups.addAll(Arrays.asList(access_groups));
			}
			SortedSet<String> new_group_refs = convertToRefs(new_groups);
			
			Collection inh_grps = getInheritedGroupRefs();
			boolean groups_are_inherited = (new_group_refs.size() == inh_grps.size()) && inh_grps.containsAll(new_group_refs);
			
			if(groups_are_inherited)
			{
				new_groups.clear();
				setGroupsById(new_groups);
				setAccessMode(AccessMode.INHERITED);
			}
			else
			{
				setGroupsById(new_groups);
				setAccessMode(AccessMode.GROUPED);
			}
			
			setPubview(false);
		}
		else if(ResourcesAction.PUBLIC_ACCESS.equals(access_mode))
		{
			if(! isPubviewInherited())
			{
				setPubview(true);
				setAccessMode(AccessMode.INHERITED);
			}
		}
		else if(AccessMode.INHERITED.toString().equals(access_mode))
		{
			setAccessMode(AccessMode.INHERITED);
			this.groups.clear();
			setPubview(false);
		}
	}

	protected void captureAvailability(ParameterParser params, String index) 
	{
		// availability
		this.hidden = params.getBoolean("hidden" + index);
		boolean use_start_date = params.getBoolean("use_start_date" + index);
		boolean use_end_date = params.getBoolean("use_end_date" + index);
		
		this.useReleaseDate = use_start_date;
		if(use_start_date)
		{
			int begin_year = params.getInt("release_year" + index);
			int begin_month = params.getInt("release_month" + index);
			int begin_day = params.getInt("release_day" + index);
			int begin_hour = params.getInt("release_hour" + index);
			int begin_min = params.getInt("release_min" + index);
			String release_ampm = params.getString("release_ampm" + index);
			if("pm".equals(release_ampm))
			{
				begin_hour += 12;
			}
			else if(begin_hour == 12)
			{
				begin_hour = 0;
			}
			this.releaseDate = TimeService.newTimeLocal(begin_year, begin_month, begin_day, begin_hour, begin_min, 0, 0);
		}
		
		this.useRetractDate = use_end_date;
		if(use_end_date)
		{
			int end_year = params.getInt("retract_year" + index);
			int end_month = params.getInt("retract_month" + index);
			int end_day = params.getInt("retract_day" + index);
			int end_hour = params.getInt("retract_hour" + index);
			int end_min = params.getInt("retract_min" + index);
			String retract_ampm = params.getString("retract_ampm" + index);
			if("pm".equals(retract_ampm))
			{
				end_hour += 12;
			}
			else if(end_hour == 12)
			{
				end_hour = 0;
			}
			this.retractDate = TimeService.newTimeLocal(end_year, end_month, end_day, end_hour, end_min, 0, 0);
		}
		
	}
	
	protected void captureCopyright(ParameterParser params, String index) 
	{
		// rights
		String copyright = params.getString("copyright" + index);
		if(copyright == null || copyright.trim().length() == 0)
		{
			// do nothing -- there must be no copyright dialog
		}
		else
		{
			this.copyrightInfo = copyright;
			
			String newcopyright = params.getString("newcopyright" + index);
			
			if(newcopyright == null || newcopyright.trim().length() == 0)
			{
				this.copyrightStatus = null;
			}
			else
			{
				this.copyrightStatus = newcopyright;
			}
			
			boolean copyrightAlert = params.getBoolean("copyrightAlert" + index);
			this.copyrightAlert = copyrightAlert;
		}
	}

	protected void captureDescription(ParameterParser params, String index) 
	{
		// description
		String description = params.getString("description" + index);
		this.setDescription(description);
	}

	public void captureProperties(ParameterParser params, String index) 
	{
		captureDisplayName(params, index);
		captureDescription(params, index);
		captureCopyright(params, index);
		captureAccess(params, index);
		captureAvailability(params, index);
		if(this.canSetQuota)
		{
			captureQuota(params, index);
		}
		if(! isUrl() && ! isCollection() && this.mimetype != null)
		{
			captureMimetypeChange(params, index);
		}
	}

	protected void captureMimetypeChange(ParameterParser params, String index) 
	{
		String mimeCategory = params.getString("mime_category" + index);
		if(mimeCategory != null)
		{
			String mimeSubtype = params.getString("mime_subtype" + index);
			if(mimeSubtype != null)
			{
				String mimeType = mimeCategory.trim() + "/" + mimeSubtype.trim();
				if(mimeType.equals(this.mimetype))
				{
					
				}
				else
				{
					this.mimetype = mimeType;
				}
			}
		}
		
	}

	protected void captureQuota(ParameterParser params, String index) 
	{
		String setQuota = params.getString("setQuota" + index);
		if(setQuota != null)
		{
			this.hasQuota = params.getBoolean("hasQuota" + index);
			if(this.hasQuota)
			{
				String quota = params.getString("quota" + index);
				if(quota != null && quota.trim().matches("^\\d+$"))
				{
					this.quota = quota.trim();
				}
			}
			else
			{
				this.quota = null;
			}		
		}
	}

	protected void captureDisplayName(ParameterParser params, String index) 
	{
		String displayName = params.getString("displayName" + index);
		if(displayName == null)
		{
			String[] delimiters = {"/", "\\"};
			displayName = this.id;
			if(displayName != null)
			{
				for(int i = 0; i < delimiters.length; i++)
				{
					if(displayName.lastIndexOf(delimiters[i]) >= 0)
					{
						displayName = displayName.substring(displayName.lastIndexOf(delimiters[i]) + 1);
					}
				}
			}
		}
		this.setName(displayName);
	}

	/**
     * @param item
     * @return
     */
    public List<ListItem> convert2list()
    {
    	List<ListItem> list = new Vector<ListItem>();
    	Stack<ListItem> processStack = new Stack<ListItem>();
    	
    	processStack.push(this);
    	while(! processStack.empty())
    	{
    		ListItem parent = processStack.pop();
    		list.add(parent);
    		List<ListItem> children = parent.getMembers();
    		if(children != null)
    		{
    			for(int i = children.size() - 1; i >= 0; i--)
    			{
    				ListItem child = children.get(i);
    				processStack.push(child);
    			}
    		}
    	}
    	
	    return list;
	    
    }	// convert2list
	
	/**
     * @param new_groups
     * @return
     */
	public SortedSet<String> convertToRefs(Collection<String> groupIds) 
	{
		SortedSet<String> groupRefs = new TreeSet<String>();
		for(String groupId : groupIds)
		{
			Group group = (Group) this.siteGroupsMap.get(groupId);
			if(group != null)
			{
				groupRefs.add(group.getReference());
			}
		}
		return groupRefs;

	}
	
	public String getLongAccessLabel()
	{
		String label = "";
		
		if(isSiteOnly())
		{
			checkParent();
			//Site has *NO* groups and public-view is *NOT* enabled on the server 
			if(this.parent != null)
			{
				label = trb.getFormattedMessage("access.site.nochoice", new String[]{parent.getName()});
			}
		}
		else if(isGroupInherited())
		{
			//Grouped access is inherited
			label = getMultiGroupLabel();
		}
		else if(isPubviewInherited())
		{
			checkParent();
			if(parent != null)
			{
				label = trb.getFormattedMessage("access.public.nochoice", new String[]{parent.getName()});
			}
		}
		else if(isCollection())
		{
			label = trb.getString("edit.who.fldr");
		}
		else
		{
			label = trb.getString("edit.who");
		}
		
		return label;
	}

	/**
     * 
     */
    protected void checkParent()
    {
	    // Public access inherited from parent 
	    if(parent == null)
	    {
	    	if(this.containingCollectionId == null)
	    	{
	    		this.containingCollectionId = ContentHostingService.getContainingCollectionId(this.id);
	    	}
	    	try
	        {
	            parent = new ListItem(ContentHostingService.getCollection(this.containingCollectionId));
	        }
	        catch (IdUnusedException e)
	        {
	            // TODO Auto-generated catch block
	            logger.warn("IdUnusedException ", e);
	        }
	        catch (TypeException e)
	        {
	            // TODO Auto-generated catch block
	            logger.warn("TypeException ", e);
	        }
	        catch (PermissionException e)
	        {
	            // TODO Auto-generated catch block
	            logger.warn("PermissionException ", e);
	        }
	    }
    }
	
	/**
     * @return the accessMode
     */
    public AccessMode getAccessMode()
    {
    	return accessMode;
    }
	
	/**
	 * @return the accessUrl
	 */
	public String getAccessUrl()
	{
		return this.accessUrl;
	}
	
	/**
     * @return the addActions
     */
    public List<ResourceToolAction> getAddActions()
    {
    	return addActions;
    }
	
	private Collection<Group> getAllowedRemoveGroupRefs() 
	{
		// TODO Auto-generated method stub
		return new TreeSet<Group>(this.allowedAddGroups);
	}
	
	/**
     * @return the createdBy
     */
    public String getCreatedBy()
    {
    	if(createdBy == null)
    	{
    		createdBy = "";
    	}
    	return createdBy;
    }
    
	/**
	 * @return
	 */
	public List<ListItem> getCollectionPath()
	{
		LinkedList<ListItem> path = new LinkedList<ListItem>();
		org.sakaiproject.content.api.ContentHostingService contentService = ContentHostingService.getInstance();
		
		ContentCollection containingCollection = null;
		ContentEntity entity = this.getEntity();
		if(entity == null)
		{
			try 
			{
				containingCollection = contentService.getCollection(this.containingCollectionId);

			} 
			catch (IdUnusedException e) 
			{
				logger.warn("IdUnusedException " + e);
			} 
			catch (TypeException e) 
			{
				logger.warn("TypeException " + e);
			} 
			catch (PermissionException e) 
			{
				logger.warn("PermissionException " + e);
			}
			
		}
		else
		{
			containingCollection = entity.getContainingCollection();
		}
		
		ListItem previousItem = null;
		while(containingCollection != null && ! contentService.isRootCollection(containingCollection.getId()))
		{
			if(previousItem != null)
			{
				path.addFirst(previousItem);
			}
			previousItem = new ListItem(containingCollection);
			containingCollection = containingCollection.getContainingCollection();
		}
//		if(containingCollection != null)
//		{
//			path.addFirst(new ListItem(containingCollection));
//		}
		
		return path;
	}
	
	/**
     * @return the depth
     */
    public int getDepth()
    {
    	return depth;
    }
	
	/**
	 * @return
	 */
	public String getDescription() 
	{
		return description;
	}
	
	/**
     * @return the effectiveAccess
     */
    public AccessMode getEffectiveAccess()
    {
    	AccessMode access = this.accessMode;
    	
		if(AccessMode.INHERITED == access)
		{
			access = this.inheritedAccessMode;
		}

		return access;
	}
    
    /**
     * @return
     */
    public Collection<Group> getEffectiveGroups()
    {
    	Collection<Group> groups = new Vector<Group>();
    	
    	
    	
    	return groups;
    }
	
	/**
     * @return
     */
    public ContentEntity getEntity()
    {
	    return this.entity;
    }
    
    public String[] getGroupNameArray(boolean includeItemName)
    {
    	Collection<Group> groups = this.groups;
    	if(AccessMode.INHERITED == this.accessMode)
    	{
    		groups = this.inheritedGroups;
    	}
    	
    	int size = groups.size();
		if(includeItemName)
		{
			size += 1;
		}
    	String[] names = new String[size];
    	
    	int index = 0;
    	if(includeItemName)
    	{
    		names[index] = this.name;
    		index++;
    	}
    	for(Group group : groups)
    	{
    		names[index] = group.getTitle();
    		index++;
    	}
    	
    	return names;
    }
    
    public String getEffectiveAccessLabel()
    {
		String label = rb.getString("access.site");
		
		if(this.isPubviewInherited || this.isPubview)
		{
			label = rb.getString("access.public");
		}
		else if(AccessMode.GROUPED == this.getEffectiveAccess())
		{
			label = rb.getString("access.group");
		}

		return label;

    }
    
    public String getGroupNamesAsString()
    {
    	String names = "";
    	String[] groups = getGroupNameArray(false);
    	for(int i = 0; i < groups.length; i++)
    	{
    		if(i > 0 && i < groups.length)
    		{
    			names += ", ";
    		}
    		names += groups[i];
    	}
    	return names;
    }
    
    public String getEffectiveGroupsLabel()
    {
		String label = rb.getString("access.site1");
		
		if(this.isPubviewInherited())
		{
			label = rb.getString("access.public1");
		}
		else if(this.isPubview())
		{
			label = rb.getString("access.public1");
		}
		else if(this.isDropbox)
		{
			label = rb.getString("access.dropbox1");
		}
		else if(AccessMode.GROUPED == getEffectiveAccess())
		{
			int size = getNumberOfGroups();
			label = (String) rb.getFormattedMessage("access.group1",  new Object[]{getGroupNamesAsString()});
		}

		return label;
    }
	
	private int getNumberOfGroups()
    {
		int size = 0;
    	
    	if(AccessMode.INHERITED == this.accessMode)
    	{
    		size = this.inheritedGroups.size();
    	}
    	else
    	{
    		size = this.groups.size();
    	}
    	
    	return size;
    }
	
	public String getMultiGroupLabel()
	{
		int size = getNumberOfGroups();
		String label = "";
		if(size > 9)
		{
			label = trb.getFormattedMessage("access.groupsX",  getGroupNameArray(true));
		}
		else
		{
			label = trb.getFormattedMessage("access.groups" + size,  getGroupNameArray(true));
		}
		return label;
	}

	/**
     * @return
     */
    public Collection<String> getGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.groups)
    	{
    		refs.add(group.getReference());
    	}
    	return refs;
    }
	
	/**
     * @return the groups
     */
    public Collection<Group> getGroups()
    {
    	return new Vector<Group>(groups);
    }
	
	/**
	 * @return the hoverText
	 */
	public String getHoverText()
	{
		return this.hoverText;
	}

	/**
	 * @return the iconLocation
	 */
	public String getIconLocation()
	{
		return this.iconLocation;
	}

	/**
	 * @return
	 */
	public String getId() 
	{
		return id;
	}

	/**
     * @return
     */
    public Collection<String> getInheritedGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.inheritedGroups)
    	{
    		refs.add(group.getReference());
    	}
    	return refs;
    }

	/**
     * @return the inheritedGroups
     */
    public Collection<Group> getInheritedGroups()
    {
    	return new Vector<Group>(inheritedGroups);
    }

	public List<ListItem> getMembers() 
	{
		return members;
	}

	/**
	 * @return the mimetype
	 */
	public String getMimetype()
	{
		return this.mimetype;
	}
	
	/**
     * @return the modifiedTime
     */
    public String getModifiedTime()
    {
    	return modifiedTime;
    }

	public ResourceToolAction getMultipleItemAction(String key)
    {
    	return this.multipleItemActions.get(key);
    }

	/**
     * @return
     */
    public Map<String, ResourceToolAction> getMultipleItemActions()
    {
	    return this.multipleItemActions;
    }

	public String getName() 
	{
		return name;
	}

	/**
     * @return the otherActions
     */
    public List<ResourceToolAction> getOtherActions()
    {
    	return otherActions;
    }

	/**
     * @return the otherActionsLabel
     */
    public String getOtherActionsLabel()
    {
    	return otherActionsLabel;
    }

	/**
	 * @return the permissions
	 */
	public Set<ContentPermissions> getPermissions()
	{
		return this.permissions;
	}

	/**
     * @return
     */
    public Collection<String> getPossibleGroupRefs()
    {
    	SortedSet<String> refs = new TreeSet<String>();
    	for(Group group : this.possibleGroups)
    	{
    		refs.add(group.getReference());
    	}
    	return refs;
    }

	/**
     * @return the possibleGroups
     */
    public Collection<Group> getPossibleGroups()
    {
    	return new Vector<Group>(possibleGroups);
    }

	/**
     * @return the releaseDate
     */
    public Time getReleaseDate()
    {
    	if(this.releaseDate == null)
    	{
    		this.releaseDate = TimeService.newTime();
    	}
    	return releaseDate;
    }

	/**
	 * @return
	 */
	public ResourceType getResourceTypeDef() 
	{
		if(resourceTypeDef == null)
		{
			if(resourceType == null)
			{
				resourceType = ResourceType.TYPE_UPLOAD;
			}
			ResourceTypeRegistry registry = (ResourceTypeRegistry) ComponentManager.get("org.sakaiproject.content.api.ResourceTypeRegistry");
			resourceTypeDef = registry.getType(this.resourceType);
		}
		return resourceTypeDef;
	}

	/**
     * @return the retractDate
     */
    public Time getRetractDate()
    {
    	if(this.retractDate == null)
    	{
    		this.retractDate = TimeService.newTime(TimeService.newTime().getTime() + ONE_WEEK);
    	}
    	return retractDate;
    }

	/**
     * @return the size
     */
    public String getSize()
    {
    	return size;
    }
    
    public String getTarget()
    {
    	return Validator.getResourceTarget(this.mimetype);
    }

	public boolean hasMultipleItemAction(String key)
    {
    	return this.multipleItemActions.containsKey(key);
    }

	public boolean hasMultipleItemActions()
    {
    	return ! this.multipleItemActions.isEmpty();
    }

	/**
     * @return the isAvailable
     */
    public boolean isAvailable()
    {
    	return isAvailable;
    }

	/**
	 * @return the collection
	 */
	public boolean isCollection()
	{
		return this.collection;
	}

	public boolean isEmpty()
	{
		return this.isEmpty;
	}
    
    /**
     * @return the isExpanded
     */
    public boolean isExpanded()
    {
    	return isExpanded;
    }
    
	public boolean isGroupInherited()
    {
    	return AccessMode.GROUPED == this.inheritedAccessMode;
    }

	/**
     * @return
     */
    public boolean isGroupPossible()
    {
    	return this.allowedAddGroups != null && ! this.allowedAddGroups.isEmpty();
    }

	/**
     * @return the hidden
     */
    public boolean isHidden()
    {
    	return hidden;
    }
    
    /**
     * @param group
     * @return
     */
    public boolean isLocal(Group group)
    {
    	boolean isLocal = false;
    	
    	for(Group gr : this.groups)
    	{
    		if(gr.getId().equals(group.getId()))
    		{
    			isLocal = true;
    			break;
    		}
    	}
    	
    	return isLocal;
    }
    
    /**
	 * @param permission
	 * @return
	 */
	public boolean isPermitted(ContentPermissions permission)
	{
		if(this.permissions == null)
		{
			this.permissions = new TreeSet<ContentPermissions>();
		}
		return this.permissions.contains(permission);
	}
    
    /**
     * @param group
     * @return
     */
    public boolean isPossible(Group group)
    {
    	boolean isPossible = false;
    	
    	Collection<Group> groupsToCheck = this.possibleGroups;
    	if(AccessMode.GROUPED == this.inheritedAccessMode)
    	{
    		groupsToCheck = this.inheritedGroups;
    	}
    	
    	for(Group gr : groupsToCheck)
    	{
    		if(gr.getId().equals(group.getId()))
    		{
    			isPossible = true;
    			break;
    		}
    	}
    	
    	return isPossible;
    }
    
	/**
     * @return the isPubview
     */
    public boolean isPubview()
    {
    	return isPubview;
    }

	/**
     * @return the isPubviewInherited
     */
    public boolean isPubviewInherited()
    {
    	return isPubviewInherited;
    }

	/**
     * @return
     */
    public boolean isPubviewPossible()
    {
    	return isPubviewPossible;
    }

	public boolean isSelected() 
	{
		return selected;
	}
    
    /**
	  * Does this entity inherit grouped access mode with a single group that has access?
	  * @return true if this entity inherits grouped access mode with a single group that has access, and false otherwise.
	  */
	 public boolean isSingleGroupInherited()
	 {
		 //Collection groups = getInheritedGroups();
		 return // AccessMode.INHERITED.toString().equals(this.m_access) && 
		 AccessMode.GROUPED == this.inheritedAccessMode && 
		 this.inheritedGroups != null && 
		 this.inheritedGroups.size() == 1; 
		 // && this.m_oldInheritedGroups != null 
		 // && this.m_oldInheritedGroups.size() == 1;
	 }

    /**
	  * Is this entity's access restricted to the site (not pubview) and are there no groups defined for the site?
	  * @return
	  */
	 public boolean isSiteOnly()
	 {
		 boolean isSiteOnly = false;
		 isSiteOnly = !isGroupPossible() && !isPubviewPossible();
		 return isSiteOnly;
	 }

	/**
	  * @return
	  */
	 public boolean isSitePossible()
	 {
		 return !this.isPubviewInherited && !isGroupInherited() && !isSingleGroupInherited();
	 }

	public boolean isTooBig()
	{
		return this.isTooBig;
	}
	
	public boolean isUrl()
	{
		return this.resourceType != null && this.resourceType.equals(ResourceType.TYPE_URL);
	}

	/**
     * @param accessMode the accessMode to set
     */
    public void setAccessMode(AccessMode accessMode)
    {
    	this.accessMode = accessMode;
    }

	/**
	 * @param accessUrl the accessUrl to set
	 */
	public void setAccessUrl(String accessUrl)
	{
		this.accessUrl = accessUrl;
	}
    
	 /**
     * @param addActions the addActions to set
     */
    public void setAddActions(List<ResourceToolAction> addActions)
    {
    	for(ResourceToolAction action : addActions)
    	{
    		if(action instanceof ServiceLevelAction && ((ServiceLevelAction) action).isMultipleItemAction())
    		{
    			this.multipleItemActions.put(action.getId(), action);
    		}
    	}
    	this.addActions = addActions;
    }

	 /**
     * @param isAvailable the isAvailable to set
     */
    public void setAvailable(boolean isAvailable)
    {
    	this.isAvailable = isAvailable;
    }

	 /**
     * @param canSelect
     */
    public void setCanSelect(boolean canSelect)
    {
	    this.canSelect  = canSelect;
    }

    /**
	 * @param collection the collection to set
	 */
	public void setCollection(boolean collection)
	{
		this.collection = collection;
	}

	/**
     * @param createdBy the createdBy to set
     */
    public void setCreatedBy(String createdBy)
    {
    	this.createdBy = createdBy;
    }

	private void setCreatedTime(String createdTime) 
	{
		this.createdTime = createdTime;
	}

	/**
     * @param depth the depth to set
     */
    public void setDepth(int depth)
    {
    	this.depth = depth;
    }

	/**
	 * @param description
	 */
	public void setDescription(String description) 
	{
		this.description = description;
	}
    
    /**
     * @param isExpanded the isExpanded to set
     */
    public void setExpanded(boolean isExpanded)
    {
    	this.isExpanded = isExpanded;
    }
    
    /**
     * @param groups the groups to set
     */
    public void setGroups(Collection<Group> groups)
    {
    	this.groups.clear();
    	this.groups.addAll(groups);
    }
    
    /**
     * @param new_groups
     */
    public void setGroupsById(Collection<String> groupIds)
    {
    	this.groups.clear();
    	if(groupIds != null && ! groupIds.isEmpty())
    	{
	    	for(String groupId : groupIds)
	    	{
	    		Group group = this.siteGroupsMap.get(groupId);
	    		this.groups.add(group);
	     	}
    	}
    }
    
    /**
     * @param hidden the hidden to set
     */
    public void setHidden(boolean hidden)
    {
    	this.hidden = hidden;
    }

	/**
	 * @param hover
	 */
	public void setHoverText(String hover)
	{
		this.hoverText = hover;
	}

	/**
	 * @param iconLocation the iconLocation to set
	 */
	public void setIconLocation(String iconLocation)
	{
		this.iconLocation = iconLocation;
	}

	/**
	 * @param id
	 */
	public void setId(String id) 
	{
		this.id = id;
	}

	/**
     * @param inheritedGroups the inheritedGroups to set
     */
    public void setInheritedGroups(Collection<Group> inheritedGroups)
    {
    	this.inheritedGroups.clear();
    	this.inheritedGroups.addAll(inheritedGroups);
    }

	/**
     * @param isEmpty
     */
    public void setIsEmpty(boolean isEmpty)
    {
        this.isEmpty = isEmpty;
    }

	/**
     * @param b
     */
    public void setIsTooBig(boolean isTooBig)
    {
        this.isTooBig = isTooBig;
    }

	public void setMembers(List<ListItem> members) 
	{
		if(this.members == null)
		{
			this.members = new Vector<ListItem>();
		}
		this.members.clear();
		this.members.addAll(members);
	}

	/**
	 * @param mimetype the mimetype to set
	 */
	public void setMimetype(String mimetype)
	{
		this.mimetype = mimetype;
	}

	private void setModifiedBy(String modifiedBy) 
	{
		this.modifiedBy = modifiedBy;
	}

	/**
     * @param modifiedTime the modifiedTime to set
     */
    public void setModifiedTime(String modifiedTime)
    {
    	this.modifiedTime = modifiedTime;
    }

	public void setName(String name) 
	{
		this.name = name;
	}

	/**
     * @param otherActions the otherActions to set
     */
    public void setOtherActions(List<ResourceToolAction> otherActions)
    {
    	for(ResourceToolAction action : otherActions)
    	{
    		if(action instanceof ServiceLevelAction && ((ServiceLevelAction) action).isMultipleItemAction())
    		{
    			this.multipleItemActions.put(action.getId(), action);
    		}
    	}
		if(this.otherActions == null)
		{
			this.otherActions = new ArrayList<ResourceToolAction>();
		}
		this.otherActions.clear();
	    if(otherActions != null)
    	{
    		this.otherActions.addAll(otherActions);
    	}
    }

	/**
     * @param otherActionsLabel the otherActionsLabel to set
     */
    public void setOtherActionsLabel(String otherActionsLabel)
    {
    	this.otherActionsLabel = otherActionsLabel;
    }

	protected void setPasteActions(List<ResourceToolAction> pasteActions)
    {
		if(this.pasteActions == null)
		{
			this.pasteActions = new ArrayList<ResourceToolAction>();
		}
		this.pasteActions.clear();
	    if(pasteActions != null)
    	{
    		this.pasteActions.addAll(pasteActions);
    	}
    }
	
	/**
	 * @param permissions the permissions to set
	 */
	public void setPermissions(Collection<ContentPermissions> permissions)
	{
		if(this.permissions == null)
		{
			this.permissions = new TreeSet<ContentPermissions>();
		}
		
		this.permissions.clear();
		this.permissions.addAll(permissions);
	}

	/**
     * @param possibleGroups the possibleGroups to set
     */
    public void setPossibleGroups(Collection<Group> possibleGroups)
    {
    	this.possibleGroups.clear();
    	this.possibleGroups.addAll(possibleGroups);
    	// TODO remove site itself?
    }
    
    public void setSiteGroups(Collection<Group> siteGroups)
    {
    	this.siteGroupsMap.clear();
        for(Group group : siteGroups)
        {
        	this.siteGroupsMap.put(group.getId(), group);
        }
    }

	/**
     * @param isPubview the isPubview to set
     */
    public void setPubview(boolean isPubview)
    {
    	this.isPubview = isPubview;
    }

	/**
     * @param isPubviewInherited the isPubviewInherited to set
     */
    public void setPubviewInherited(boolean isPubviewInherited)
    {
    	this.isPubviewInherited = isPubviewInherited;
    }

	/**
     * @param isPubviewPossible the isPubviewPossible to set
     */
    public void setPubviewPossible(boolean isPubviewPossible)
    {
    	this.isPubviewPossible = isPubviewPossible;
    }

	/**
     * @param releaseDate the releaseDate to set
     */
    public void setReleaseDate(Time releaseDate)
    {
    	this.releaseDate = releaseDate;
    }

	/**
	 * @param resourceTypeDef
	 */
	public void setResourceTypeDef(ResourceType resourceTypeDef) 
	{
		this.resourceTypeDef = resourceTypeDef;
		
		// make sure typeDef and typeId are consistent
		if(resourceTypeDef != null)
		{
			this.resourceType = resourceTypeDef.getId();
		}
	}

	/**
     * @param retractDate the retractDate to set
     */
    public void setRetractDate(Time retractDate)
    {
    	this.retractDate = retractDate;
    }

	public void setSelected(boolean selected) 
	{
		this.selected = selected;
	}

	public void setSelectedForCopy(boolean selectedForCopy)
    {
	    this.selectedForCopy = selectedForCopy;
    }

	public void setSelectedForMove(boolean selectedForMove)
    {
	    this.selectedForMove = selectedForMove;
    }

	/**
     * @param string
     */
    public void setSize(String size)
    {
        this.size = size;
    }

	/**
     * @param isSortable
     */
    public void setSortable(boolean isSortable)
    {
        this.isSortable  = isSortable;
    }

	/**
     * @param useReleaseDate the useReleaseDate to set
     */
    public void setUseReleaseDate(boolean useReleaseDate)
    {
    	this.useReleaseDate = useReleaseDate;
    }

	/**
     * @param useRetractDate the useRetractDate to set
     */
    public void setUseRetractDate(boolean useRetractDate)
    {
    	this.useRetractDate = useRetractDate;
    }

	/**
     * @return the useReleaseDate
     */
    public boolean useReleaseDate()
    {
    	return useReleaseDate;
    }

	/**
     * @return the useRetractDate
     */
    public boolean useRetractDate()
    {
    	return useRetractDate;
    }

	public String getCopyrightInfo() 
	{
		return copyrightInfo;
	}

	public void setCopyrightInfo(String copyright) 
	{
		this.copyrightInfo = copyright;
	}

	public boolean hasCopyrightAlert() 
	{
		return copyrightAlert;
	}

	public void setCopyrightAlert(boolean copyrightAlert) 
	{
		this.copyrightAlert = copyrightAlert;
	}

	public void updateContentCollectionEdit(ContentCollectionEdit edit) 
	{
		ResourcePropertiesEdit props = edit.getPropertiesEdit();
		setDisplayNameOnEntity(props);
		setDescriptionOnEntity(props);
		//setCopyrightOnEntity(props);
		setAccessOnEntity(edit);
		setAvailabilityOnEntity(edit);
		setQuotaOnEntity(props);
	}
	
	protected void setQuotaOnEntity(ResourcePropertiesEdit props) 
	{
		if(this.canSetQuota)
		{
			if(SecurityService.isSuperUser())
			{
				if(this.hasQuota)
				{
					if(this.quota != null && this.quota.trim().matches("^\\d+$"))
					{
						props.addProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA, this.quota.trim());
					}
				}
				else
				{
					props.removeProperty(ResourceProperties.PROP_COLLECTION_BODY_QUOTA);
				}
			}
		}
	}

	protected void setAvailabilityOnEntity(GroupAwareEdit edit)
	{
		edit.setAvailability(hidden, releaseDate, retractDate);
	}

	protected void setCopyrightOnEntity(ResourcePropertiesEdit props) 
	{
		if(copyrightInfo == null || copyrightInfo.trim().length() == 0)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT_CHOICE);
		}
		else
		{
			props.addProperty (ResourceProperties.PROP_COPYRIGHT_CHOICE, copyrightInfo);
		}
		if(copyrightStatus == null || copyrightStatus.trim().length() == 0)
		{
			props.removeProperty(ResourceProperties.PROP_COPYRIGHT);
		}
		else
		{
			props.addProperty (ResourceProperties.PROP_COPYRIGHT, copyrightStatus);
		}
		if (copyrightAlert)
		{
			props.addProperty (ResourceProperties.PROP_COPYRIGHT_ALERT, Boolean.TRUE.toString());
		}
		else
		{
			props.removeProperty (ResourceProperties.PROP_COPYRIGHT_ALERT);
		}
		
	}

	protected void setAccessOnEntity(GroupAwareEdit edit) 
	{
		try 
		{
			if(this.accessMode == AccessMode.GROUPED && this.groups != null && ! this.groups.isEmpty())
			{
					edit.setGroupAccess(groups);
			}
			else if(this.isPubview && ! this.isPubviewInherited)
			{
				edit.setPublicAccess();
			}
			else if(edit.getAccess() == AccessMode.GROUPED)
			{
				edit.clearGroupAccess();
			}
		} 
		catch (InconsistentException e) 
		{
			logger.warn("InconsistentException " + e);
		} 
		catch (PermissionException e) 
		{
			logger.warn("PermissionException " + e);
		}
	}

	protected void setDescriptionOnEntity(ResourcePropertiesEdit props) 
	{
		if(this.description != null)
		{
			props.addProperty(ResourceProperties.PROP_DESCRIPTION, this.description);
		}
	}

	public void updateContentResourceEdit(ContentResourceEdit edit) 
	{
		ResourcePropertiesEdit props = edit.getPropertiesEdit();
		setDisplayNameOnEntity(props);
		setDescriptionOnEntity(props);
		setCopyrightOnEntity(props);
		setAccessOnEntity(edit);
		setAvailabilityOnEntity(edit);
		if(! isUrl() && ! isCollection() && this.mimetype != null)
		{
			setMimetypeOnEntity(edit, props);
		}
	}

	protected void setMimetypeOnEntity(ContentResourceEdit edit, ResourcePropertiesEdit props) 
	{
		if(this.mimetype != null)
		{
			props.addProperty(ResourceProperties.PROP_CONTENT_TYPE, this.mimetype);
			edit.setContentType(this.mimetype);
		}
	}

	protected void setDisplayNameOnEntity(ResourcePropertiesEdit props) 
	{
		if(this.name != null)
		{
			props.addProperty(ResourceProperties.PROP_DISPLAY_NAME, this.name);
		}
	}

	public String getCopyrightStatus() 
	{
		return copyrightStatus;
	}

	public void setCopyrightStatus(String copyrightStatus) 
	{
		this.copyrightStatus = copyrightStatus;
	}

	public boolean isUserSite() 
	{
		return isUserSite;
	}

	public boolean canSetQuota() 
	{
		return canSetQuota;
	}

	public boolean hasQuota() 
	{
		return hasQuota;
	}

	public String getQuota() 
	{
		return quota;
	}

	public boolean isSiteCollection() 
	{
		return isSiteCollection;
	}

	public void setSiteCollection(boolean isSiteCollection) 
	{
		this.isSiteCollection = isSiteCollection;
	}

	public String getResourceType() 
	{
		return resourceType;
	}

	public void setResourceType(String resourceType) 
	{
		this.resourceType = resourceType;
	}

	public String getCreatedTime() 
	{
		return createdTime;
	}

	public String getModifiedBy() 
	{
		return modifiedBy;
	}

	 public String getMimeCategory()
	 {
		 if(this.mimetype == null || this.mimetype.equals(""))
		 {
			 return "";
		 }
		 int index = this.mimetype.indexOf("/");
		 if(index < 0)
		 {
			 return this.mimetype;
		 }
		 return this.mimetype.substring(0, index);
	 }

	 public String getMimeSubtype()
	 {
		 if(this.mimetype == null || this.mimetype.equals(""))
		 {
			 return "";
		 }
		 int index = this.mimetype.indexOf("/");
		 if(index < 0 || index + 1 == this.mimetype.length())
		 {
			 return "";
		 }
		 return this.mimetype.substring(index + 1);
	 }

	public String getSizzle() 
	{
		return sizzle;
	}

	public boolean isDropbox() 
	{
		return isDropbox;
	}

	public void setDropbox(boolean isDropbox) 
	{
		this.isDropbox = isDropbox;
	}

	public List<ResourceToolAction> getPasteActions()
    {
    	return pasteActions;
    }

	public AccessMode getInheritedAccessMode()
    {
    	return inheritedAccessMode;
    }

	public void setInheritedAccessMode(AccessMode inheritedAccessMode)
    {
    	this.inheritedAccessMode = inheritedAccessMode;
    }

	protected boolean siteCollection(String refStr)
	{
		boolean site = false;
		
		Reference ref = EntityManager.newReference(refStr);
		String context = ref.getContext();
		String siteCollection = ContentHostingService.getSiteCollection(context);
		if(ref.getId().equals(siteCollection))
		{
			site = true;
		}
		
		return site;
	}

	public ListItem getParent()
    {
		checkParent();
	    return this.parent;
    }

	public void setNameIsMissing(boolean nameIsMissing)
    {
	    this.nameIsMissing = nameIsMissing;
    }

	public boolean isNameMissing()
    {
    	return nameIsMissing;
    }

	public boolean isSelectedForCopy()
    {
    	return selectedForCopy;
    }

	public boolean isSelectedForMove()
    {
    	return selectedForMove;
    }
	
}

