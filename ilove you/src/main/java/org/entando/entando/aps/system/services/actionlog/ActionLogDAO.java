/*
*
* Copyright 2014 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
* This file is part of Entando software.
* Entando is a free software;
* You can redistribute it and/or modify it
* under the terms of the GNU General Public License (GPL) as published by the Free Software Foundation; version 2.
* 
* See the file License for the specific language governing permissions   
* and limitations under the License
* 
* 
* 
* Copyright 2014 Entando S.r.l. (http://www.entando.com) All rights reserved.
*
*/
package org.entando.entando.aps.system.services.actionlog;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.entando.entando.aps.system.services.actionlog.model.ActionLogRecord;
import org.entando.entando.aps.system.services.actionlog.model.ActivityStreamInfo;
import org.entando.entando.aps.system.services.actionlog.model.ActivityStreamLikeInfo;
import org.entando.entando.aps.system.services.actionlog.model.ActivityStreamLikeInfos;
import org.entando.entando.aps.system.services.actionlog.model.IActionLogRecordSearchBean;

import com.agiletec.aps.system.common.AbstractSearcherDAO;
import com.agiletec.aps.system.common.FieldSearchFilter;
import com.agiletec.aps.system.services.group.Group;

/**
 * @author E.Santoboni
 */
public class ActionLogDAO extends AbstractSearcherDAO implements IActionLogDAO {

	@Override
	public void addActionRecord(ActionLogRecord actionRecord) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(ADD_ACTION_RECORD);
			stat.setInt(1, actionRecord.getId());
			stat.setString(2, actionRecord.getUsername());
			Timestamp timestamp = new Timestamp(actionRecord.getActionDate().getTime());
			stat.setTimestamp(3, timestamp);
			stat.setString(4, actionRecord.getNamespace());
			stat.setString(5, actionRecord.getActionName());
			stat.setString(6, actionRecord.getParameters());
			ActivityStreamInfo asi = actionRecord.getActivityStreamInfo();
			if (null != asi) {
				stat.setString(7, ActivityStreamInfoDOM.marshalInfo(asi));
			} else {
				stat.setNull(7, Types.VARCHAR);
			}
			stat.executeUpdate();
			this.addLogRecordRelations(actionRecord.getId(), asi, conn);
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error on insert actionlogger record", "addActionRecord");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}

	private void addLogRecordRelations(int recordId, ActivityStreamInfo asi, Connection conn) {
		if (asi == null) {
			return;
		}
		List<String> groups = asi.getGroups();
		if (null == groups || groups.isEmpty()) {
			return;
		}
		Set<String> codes = new HashSet<String>(groups);
		Iterator<String> iterator = codes.iterator();
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement(ADD_LOG_RECORD_RELATION);
			while (iterator.hasNext()) {
				String groupCode = iterator.next();
				stat.setInt(1, recordId);
				stat.setString(2, groupCode);
				stat.addBatch();
				stat.clearParameters();
			}
			stat.executeBatch();
		} catch (BatchUpdateException e) {
			processDaoException(e.getNextException(), "Error adding relations for record - " + recordId, "addLogRecordRelations");
		} catch (Throwable t) {
			processDaoException(t, "Error adding relations for record - " + recordId, "addLogRecordRelations");
		} finally {
			closeDaoResources(null, stat);
		}
	}

	@Override
	public List<Integer> getActionRecords(IActionLogRecordSearchBean searchBean) {
		FieldSearchFilter[] filters = this.createFilters(searchBean);
		return this.getActionRecords(filters);
	}

	@Override
	public List<Integer> getActionRecords(FieldSearchFilter[] filters) {
		List<Integer> actionRecords = new ArrayList<Integer>();
		try {
			List<String> ids = super.searchId(filters);
			if (null != ids) {
				for (int i = 0; i < ids.size(); i++) {
					String id = ids.get(i);
					actionRecords.add(Integer.parseInt(id));
				}
			}
		} catch (Throwable t) {
			processDaoException(t, "Error loading actionlogger records", "getActionRecords");
		}
		return actionRecords;
	}

	@Override
	public List<Integer> getActivityStream(List<String> userGroupCodes) {
		Connection conn = null;
		List<Integer> idList = new ArrayList<Integer>();
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			conn = this.getConnection();
			FieldSearchFilter filter1 = new FieldSearchFilter("actiondate");
			filter1.setOrder(FieldSearchFilter.DESC_ORDER);
			FieldSearchFilter filter2 = new FieldSearchFilter("activitystreaminfo");
			FieldSearchFilter[] filters = {filter1, filter2};
			List<String> groupCodes = (null != userGroupCodes && userGroupCodes.contains(Group.ADMINS_GROUP_NAME)) ? null : userGroupCodes;
			stat = this.buildStatement(filters, groupCodes, conn);
			result = stat.executeQuery();
			while (result.next()) {
				idList.add(result.getInt(1));
			}
		} catch (Throwable t) {
			processDaoException(t, "Error while loading activity stream records", "getActivityStream");
		} finally {
			closeDaoResources(result, stat, conn);
		}
		return idList;
	}

	private PreparedStatement buildStatement(FieldSearchFilter[] filters, Collection<String> groupCodes, Connection conn) {
		String query = this.createQueryString(filters, groupCodes);
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement(query);
			int index = 0;
			index = this.addMetadataFieldFilterStatementBlock(filters, index, stat);
			index = this.addGroupStatementBlock(groupCodes, index, stat);
		} catch (Throwable t) {
			processDaoException(t, "Error while creating the statement", "buildStatement");
		}
		return stat;
	}

	protected String createQueryString(FieldSearchFilter[] filters, Collection<String> groupCodes) {
		StringBuffer query = this.createBaseQueryBlock(filters, false);
		this.appendJoinTableRefQueryBlock(query, groupCodes);
		boolean hasAppendWhereClause = this.appendMetadataFieldFilterQueryBlocks(filters, query, false);
		if (null != groupCodes && !groupCodes.isEmpty()) {
			hasAppendWhereClause = this.verifyWhereClauseAppend(query, hasAppendWhereClause);
			query.append(" ( ");
			int size = groupCodes.size();
			for (int i=0; i<size; i++) {
				if (i!=0) query.append("OR ");
				query.append("actionlogrelations.refgroup = ? ");
			}
			query.append(") ");
		}
		boolean ordered = appendOrderQueryBlocks(filters, query, false);
		return query.toString();
	}

	private void appendJoinTableRefQueryBlock(StringBuffer query, Collection<String> groupCodes) {
		if (null == groupCodes || groupCodes.isEmpty()) {
			return;
		}
		String masterTableName = this.getMasterTableName();
		String masterTableIdFieldName = this.getMasterTableIdFieldName();
			query.append("INNER JOIN ");
			query.append("actionlogrelations").append(" ON ")
				.append(masterTableName).append(".").append(masterTableIdFieldName).append(" = ")
				.append("actionlogrelations").append(".").append("recordid").append(" ");
	}

	protected int addGroupStatementBlock(Collection<String> groupCodes, int index, PreparedStatement stat) throws Throwable {
		if (null != groupCodes && !groupCodes.isEmpty()) {
			Iterator<String> groupIter = groupCodes.iterator();
			while (groupIter.hasNext()) {
				String groupName = groupIter.next();
				stat.setString(++index, groupName);
			}
		}
		return index;
	}

	protected FieldSearchFilter[] createFilters(IActionLogRecordSearchBean searchBean) {
		FieldSearchFilter[] filters = new FieldSearchFilter[0];
		if (null != searchBean) {
			String username = searchBean.getUsername();
			if (null != username && username.trim().length() > 0) {
				FieldSearchFilter filter = new FieldSearchFilter("username", this.extractSearchValues(username), true);
				filters = super.addFilter(filters, filter);
			}
			String namespace = searchBean.getNamespace();
			if (null != namespace && namespace.trim().length() > 0) {
				FieldSearchFilter filter = new FieldSearchFilter("namespace", this.extractSearchValues(namespace), true);
				filters = super.addFilter(filters, filter);
			}
			String actionName = searchBean.getActionName();
			if (null != actionName && actionName.trim().length() > 0) {
				FieldSearchFilter filter = new FieldSearchFilter("actionname", this.extractSearchValues(actionName), true);
				filters = super.addFilter(filters, filter);
			}
			String parameters = searchBean.getParams();
			if (null != parameters && parameters.trim().length() > 0) {
				FieldSearchFilter filter = new FieldSearchFilter("parameters", this.extractSearchValues(parameters), true);
				filters = super.addFilter(filters, filter);
			}
			Date start = searchBean.getStart();
			Date end = searchBean.getEnd();
			if (null != start || null != end) {
				Timestamp tsStart = (null != start) ? new Timestamp(start.getTime()) : null;
				Timestamp tsEnd = (null != end) ? new Timestamp(end.getTime()) : null;
				FieldSearchFilter filter = new FieldSearchFilter("actiondate", tsStart, tsEnd);
				filters = super.addFilter(filters, filter);
			}
		}
		return filters;
	}

	protected List<String> extractSearchValues(String text) {
		String[] titleSplit = text.trim().split(" ");
		return (List<String>) Arrays.asList(titleSplit);
	}

	@Override
	public ActionLogRecord getActionRecord(int id) {
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet res = null;
		ActionLogRecord actionRecord = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(GET_ACTION_RECORD);
			stat.setInt(1, id);
			res = stat.executeQuery();
			if (res.next()) {
				actionRecord = new ActionLogRecord();
				actionRecord.setId(id);
				Timestamp timestamp = res.getTimestamp("actiondate");
				actionRecord.setActionDate(new Date(timestamp.getTime()));
				actionRecord.setActionName(res.getString("actionname"));
				actionRecord.setNamespace(res.getString("namespace"));
				actionRecord.setParameters(res.getString("parameters"));
				actionRecord.setUsername(res.getString("username"));
				String asiXml = res.getString("activitystreaminfo");
				if (null != asiXml && asiXml.trim().length() > 0) {
					ActivityStreamInfo asi = ActivityStreamInfoDOM.unmarshalInfo(asiXml);
					actionRecord.setActivityStreamInfo(asi);
				}
			}
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error loading actionlogger record with id: " + id, "getActionRecord");
		} finally {
			closeDaoResources(res, stat, conn);
		}
		return actionRecord;
	}

	@Override
	public void deleteActionRecord(int id) {
		Connection conn = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			this.deleteActionRecord(id, conn);
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error on delete record: " + id , "deleteActionRecord");
		} finally {
			closeConnection(conn);
		}
	}

	private void deleteActionRecord(int id, Connection conn) {
		try {
			this.deleteRecord(id, conn, DELETE_LOG_LIKE_RECORDS);
			this.deleteRecord(id, conn, DELETE_LOG_RECORD_RELATIONS);
			this.deleteRecord(id, conn, DELETE_LOG_RECORD);
		} catch (Throwable t) {
			processDaoException(t, "Error on delete record: " + id , "deleteActionRecord");
		}
	}

	@Override
	public void editActionLikeRecord(int id, String username, boolean add) {
		if (add) {
			this.addActionLikeRecord(id, username);
		} else {
			this.deleteActionLikeRecord(id, username);
		}
	}

	@Override
	public void addActionLikeRecord(int id, String username) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(ADD_ACTION_LIKE_RECORD);
			stat.setInt(1, id);
			stat.setString(2, username);
			Timestamp timestamp = new Timestamp(new Date().getTime());
			stat.setTimestamp(3, timestamp);
			stat.executeUpdate();
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error on insert actionlogger like record", "addActionLikeRecord");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}

	@Override
	public void deleteActionLikeRecord(int id, String username) {
		Connection conn = null;
		PreparedStatement stat = null;
		try {
			conn = this.getConnection();
			conn.setAutoCommit(false);
			stat = conn.prepareStatement(DELETE_LOG_LIKE_RECORD);
			stat.setInt(1, id);
			stat.setString(2, username);
			stat.executeUpdate();
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error on delete like record: " + id , "deleteActionLikeRecord");
		} finally {
			closeDaoResources(null, stat, conn);
		}
	}

	public void deleteRecord(int id, Connection conn, String query) {
		PreparedStatement stat = null;
		try {
			stat = conn.prepareStatement(query);
			stat.setInt(1, id);
			stat.executeUpdate();
		} catch (Throwable t) {
			processDaoException(t, "Error on delete record: " + id , "deleteRecord");
		} finally {
			closeDaoResources(null, stat);
		}
	}

	@Override
	public List<ActivityStreamLikeInfo> getActionLikeRecords(int id) {
		List<ActivityStreamLikeInfo> infos = new ActivityStreamLikeInfos();
		Connection conn = null;
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			conn = this.getConnection();
			stat = conn.prepareStatement(GET_ACTION_LIKE_RECORDS);
			stat.setInt(1, id);
			result = stat.executeQuery();
			while (result.next()) {
				ActivityStreamLikeInfo asli = new ActivityStreamLikeInfo();
				asli.setUsername(result.getString(1));
				infos.add(asli);
			}
		} catch (Throwable t) {
			processDaoException(t, "Error while loading activity stream like records", "getActionLikeRecords");
		} finally {
			closeDaoResources(result, stat, conn);
		}
		return infos;
	}

	@Override
	protected String getMasterTableName() {
		return "actionlogrecords";
	}

	@Override
	protected String getMasterTableIdFieldName() {
		return "id";
	}

	@Override
	protected String getTableFieldName(String metadataFieldKey) {
		return metadataFieldKey;
	}

	@Override
	protected boolean isForceCaseInsensitiveLikeSearch() {
		return true;
	}

	@Override
	public synchronized void cleanOldActivityStreamLogs(int maxActivitySizeByGroup) {
		Connection conn = null;
		try {
			Set<Integer> recordsToDelete = new HashSet<Integer>();
			conn = this.getConnection();
			conn.setAutoCommit(false);
			Map<String, Integer> occurrences = this.getOccurrences(maxActivitySizeByGroup, conn);
			Iterator<String> groupIter = occurrences.keySet().iterator();
			while (groupIter.hasNext()) {
				String groupName = groupIter.next();
				this.extractRecordToDelete(groupName, maxActivitySizeByGroup, recordsToDelete, conn);
			}
			Iterator<Integer> idIter = recordsToDelete.iterator();
			while (idIter.hasNext()) {
				Integer id = idIter.next();
				this.deleteActionRecord(id, conn);
			}
			conn.commit();
		} catch (Throwable t) {
			this.executeRollback(conn);
			processDaoException(t, "Error cleaning old Stream logs" , "cleanOldActivityStreamLogs");
		} finally {
			closeConnection(conn);
		}
	}

	private Map<String, Integer> getOccurrences(Integer maxActivitySizeByGroup, Connection conn) {
		Map<String, Integer> occurrences = new HashMap<String, Integer>();
		Statement stat = null;
		ResultSet res = null;
		try {
			stat = conn.createStatement();
			res = stat.executeQuery(GET_GROUP_OCCURRENCES);
			while (res.next()) {
				String groupName = res.getString(1);
				Integer integer = res.getInt(2);
				if (null != maxActivitySizeByGroup && integer > maxActivitySizeByGroup) {
					occurrences.put(groupName, integer);
				}
			}
		} catch (Throwable t) {
			processDaoException(t, "Error loading actionlogger occurrences", "getOccurrences");
		} finally {
			closeDaoResources(res, stat);
		}
		return occurrences;
	}

	private void extractRecordToDelete(String groupName,
			Integer maxActivitySizeByGroup, Set<Integer> recordIds, Connection conn) {
		PreparedStatement stat = null;
		ResultSet result = null;
		try {
			List<Integer> idList = new ArrayList<Integer>();
			conn = this.getConnection();
			FieldSearchFilter filter1 = new FieldSearchFilter("actiondate");
			filter1.setOrder(FieldSearchFilter.Order.DESC);
			FieldSearchFilter filter2 = new FieldSearchFilter("activitystreaminfo");
			FieldSearchFilter[] filters = {filter1, filter2};
			List<String> groupCodes = new ArrayList<String>();
			groupCodes.add(groupName);
			stat = this.buildStatement(filters, groupCodes, conn);
			result = stat.executeQuery();
			while (result.next()) {
				Integer id = result.getInt(1);
				if (!idList.contains(id)) {
					idList.add(id);
				}
			}
			if (idList.size() > maxActivitySizeByGroup) {
				for (int i = maxActivitySizeByGroup; i < idList.size(); i++) {
					Integer id = idList.get(i);
					recordIds.add(id);
				}
			}
		} catch (Throwable t) {
			processDaoException(t, "Error while loading activity stream records to delete : group " + groupName, "extractRecordToDelete");
		} finally {
			closeDaoResources(result, stat);
		}
	}

	private static final String ADD_ACTION_RECORD =
			"INSERT INTO actionlogrecords ( id, username, actiondate, namespace, actionname, parameters, activitystreaminfo) " +
			"VALUES ( ? , ? , ? , ? , ? , ? , ? )";

	private static final String ADD_ACTION_LIKE_RECORD =
			"INSERT INTO actionloglikerecords ( recordid, username, likedate) VALUES ( ? , ? , ? )";

	private static final String GET_ACTION_RECORD =
			"SELECT username, actiondate, namespace, actionname, parameters, activitystreaminfo FROM actionlogrecords WHERE id = ?";

	private static final String DELETE_LOG_RECORD =
			"DELETE from actionlogrecords where id = ?";

	private static final String DELETE_LOG_RECORD_RELATIONS =
			"DELETE from actionlogrelations where recordid = ?";

	private final String ADD_LOG_RECORD_RELATION =
			"INSERT INTO actionlogrelations (recordid, refgroup) VALUES ( ? , ? )";

	private static final String DELETE_LOG_LIKE_RECORDS =
			"DELETE from actionloglikerecords where recordid = ? ";

	private static final String DELETE_LOG_LIKE_RECORD =
			DELETE_LOG_LIKE_RECORDS + "AND username = ? ";

	private static final String GET_ACTION_LIKE_RECORDS =
			"SELECT username from actionloglikerecords where recordid = ? ";

	private static final String GET_GROUP_OCCURRENCES =
			"SELECT refgroup, count(refgroup) FROM actionlogrelations GROUP BY refgroup";

}