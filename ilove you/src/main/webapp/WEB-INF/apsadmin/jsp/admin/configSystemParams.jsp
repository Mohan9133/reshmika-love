<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="wpsf" uri="/apsadmin-form"  %>
<%@ taglib prefix="wpsa" uri="/apsadmin-core"  %>
<h1 class="panel panel-default title-page">
	<span class="panel-body display-block">
		<a href="<s:url namespace="/do/BaseAdmin" action="settings" />"><s:text name="menu.configure" /></a> / <s:text name="menu.settings.general" /></span>
</h1>

<s:form action="updateSystemParams">
	<s:if test="hasActionMessages()">
		<div class="alert alert-success alert-dismissable fade in">
			<button class="close" data-dismiss="alert"><span class="icon fa fa-times"></span></button>
			<h2 class="h4 margin-none"><s:text name="messages.confirm" /></h2>
			<ul class="margin-base-top">
				<s:iterator value="actionMessages">
					<li><s:property escape="false" /></li>
				</s:iterator>
			</ul>
		</div>
	</s:if>

	<fieldset class="col-xs-12 margin-large-top" id="additional-features"><legend><s:text name="sysconfig.legend.featuresOnDemand" /></legend>

		<div class="form-group">
			<s:set name="paramName" value="'firstTimeMessages'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="menu.accountAdmin.firstTimeMessages" />
			</label>
			<s:set name="paramName" value="'groupsOnDemand'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="menu.accountAdmin.groups" />
			</label>
			<s:set name="paramName" value="'categoriesOnDemand'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="menu.categoryAdmin" />
			</label>
			<s:set name="paramName" value="'contentTypesOnDemand'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="jacms.menu.contentTypeAdmin" />
			</label>
			<s:set name="paramName" value="'contentModelsOnDemand'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="jacms.menu.contentModelAdmin" />
			</label>
			<s:set name="paramName" value="'apisOnDemand'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="menu.apisAdmin" />
			</label>
			<s:set name="paramName" value="'resourceArchivesOnDemand'" />
			<label class="checkbox">
				<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
				&#32;<s:text name="jacms.menu.resourceAdmin" />
			</label>
		</div>

	</fieldset>

	<fieldset class="col-xs-12">
		<legend><s:text name="name.pages" /></legend>
		<%--
		<p class="text-right">
			<span class="label label-default"><s:text name="sysconfig.legend.systemPages" /></span>
		</p>
		--%>

		<div class="form-group">
			<label for="admin-settings-area-homePageCode"><s:text name="sysconfig.homePageCode" /></label>
			<s:set name="paramName" value="'homePageCode'" />
			<s:include value="/WEB-INF/apsadmin/jsp/admin/selectPageParamBlock.jsp" />
		</div>

		<div class="form-group">
			<label for="admin-settings-area-notFoundPageCode"><s:text name="sysconfig.notFoundPageCode" /></label>
			<s:set name="paramName" value="'notFoundPageCode'" />
			<s:include value="/WEB-INF/apsadmin/jsp/admin/selectPageParamBlock.jsp" />
		</div>

		<div class="form-group">
			<label for="admin-settings-area-errorPageCode"><s:text name="sysconfig.errorPageCode" /></label>
			<s:set name="paramName" value="'errorPageCode'" />
			<s:include value="/WEB-INF/apsadmin/jsp/admin/selectPageParamBlock.jsp" />
		</div>

		<div class="form-group">
			<label for="admin-settings-area-loginPageCode"><s:text name="sysconfig.loginPageCode" /></label>
			<s:set name="paramName" value="'loginPageCode'" />
			<s:include value="/WEB-INF/apsadmin/jsp/admin/selectPageParamBlock.jsp" />
		</div>
		
		<div class="form-group">
			<s:set name="paramName" value="'baseUrl'" />
			<label class="display-block"><s:text name="sysconfig.baseURL" /></label>
			<div class="btn-group" data-toggle="buttons">
				<label class="btn btn-default <s:if test="systemParams['baseUrl'] == 'relative'"> active</s:if>">
					<input type="radio" class="radiocheck" id="admin-settings-area-urlStyle-classic" name="baseUrl" value="relative" <s:if test="systemParams['baseUrl'] == 'relative'">checked="checked"</s:if> />
					<s:text name="baseURL.relative" />
				</label>
				<label class="btn btn-default <s:if test="%{systemParams['baseUrl'] == null || systemParams['baseUrl'] == 'request'}"> active</s:if>">
					<input type="radio" class="radiocheck" id="admin-settings-area-urlStyle-breadcrumbs" name="baseUrl" value="request" <s:if test="%{systemParams['baseUrl'] == null || systemParams['baseUrl'] == 'request'}">checked="checked"</s:if> />
					<s:text name="baseURL.request" />
				</label>
				<label class="btn btn-default <s:if test="systemParams['baseUrl'] == 'static'"> active</s:if>">
					<input type="radio" class="radiocheck" id="admin-settings-area-urlStyle-breadcrumbs" name="baseUrl" value="static" <s:if test="systemParams['baseUrl'] == 'static'">checked="checked"</s:if> />
					<s:text name="baseURL.static" />
				</label>
				<wpsf:hidden name="%{#paramName + externalParamMarker}" value="true"/>
			</div>
		</div>
		
		<div class="form-group">
			<label class="display-block"><s:text name="sysconfig.baseURL.contextName" /></label>
			<div class="btn-group" data-toggle="buttons">
				<s:set name="paramName" value="'baseUrlContext'" />
				<s:include value="/WEB-INF/apsadmin/jsp/admin/booleanParamBlock.jsp" />
				<wpsf:hidden name="%{#paramName + externalParamMarker}" value="true"/>
			</div>
		</div>
		
		<div class="form-group">
			<label class="display-block"><s:text name="sysconfig.useJsessionId" /></label>
			<div class="btn-group" data-toggle="buttons">
				<s:set name="paramName" value="'useJsessionId'" />
				<s:include value="/WEB-INF/apsadmin/jsp/admin/booleanParamBlock.jsp" />
				<%-- hidden parameter to delete --%>
				<wpsf:hidden name="%{#paramName + externalParamMarker}" value="true"/>
			</div>
		</div>
		
		<div class="form-group">
			<s:set name="paramName" value="'urlStyle'" />
			<label class="display-block"><s:text name="sysconfig.URLstyle" /></label>
			<div class="btn-group" data-toggle="buttons">
				<label class="btn btn-default <s:if test="systemParams['urlStyle'] == 'classic'"> active</s:if>">
					<input type="radio" class="radiocheck" id="admin-settings-area-urlStyle-classic" name="urlStyle" value="classic" <s:if test="systemParams['urlStyle'] == 'classic'">checked="checked"</s:if> />
					<s:text name="URLstyle.classic" />
				</label>
				<label class="btn btn-default <s:if test="systemParams['urlStyle'] == 'breadcrumbs'"> active</s:if>">
					<input type="radio" class="radiocheck" id="admin-settings-area-urlStyle-breadcrumbs" name="urlStyle" value="breadcrumbs" <s:if test="systemParams['urlStyle'] == 'breadcrumbs'">checked="checked"</s:if> />
					<s:text name="URLstyle.breadcrumbs" />
				</label>
			</div>
		</div>

		<div class="form-group">
			<label class="display-block"><s:text name="sysconfig.lang.browser" /></label>
			<div class="btn-group" data-toggle="buttons">
				<s:set name="paramName" value="'startLangFromBrowser'" />
				<s:include value="/WEB-INF/apsadmin/jsp/admin/booleanParamBlock.jsp" />
			</div>
		</div>
	</fieldset>

	<fieldset class="col-xs-12 margin-large-top"><legend><s:text name="sysconfig.legend.privacyModule" /></legend>
		<div class="form-group">
			<s:set name="paramName" value="'extendedPrivacyModuleEnabled'" />
			<div class="checkbox-inline">
				<label>
					<s:include value="/WEB-INF/apsadmin/jsp/admin/simpleCheckboxParamBlock.jsp" />
					&#32;<s:text name="label.active" />
				</label>
			</div>
		</div>

		<div class="form-group">
			<label for="admin-settings-area-maxMonthsSinceLastAccess"><s:text name="sysconfig.maxMonthsSinceLastAccess" /></label>
			<s:set name="paramName" value="'maxMonthsSinceLastAccess'" />
			<s:include value="/WEB-INF/apsadmin/jsp/admin/textParamBlock.jsp" />
		</div>

		<div class="form-group">
			<label for="admin-settings-area-maxMonthsSinceLastPasswordChange"><s:text name="sysconfig.maxMonthsSinceLastPasswordChange" /></label>
			<s:set name="paramName" value="'maxMonthsSinceLastPasswordChange'" />
			<s:include value="/WEB-INF/apsadmin/jsp/admin/textParamBlock.jsp" />
		</div>
	</fieldset>

	<fieldset class="col-xs-12 margin-large-top"><legend><s:text name="sysconfig.legend.misc" /></legend>
		<div class="form-group">
		<label class="display-block"><s:text name="sysconfig.chooseYourEditor" /></label>
			<div class="btn-group" data-toggle="buttons">
				<label class="btn btn-default <s:if test="systemParams['hypertextEditor'] == 'none'"> active</s:if>">
					<input type="radio" id="admin-settings-area-hypertextEditor_none" name="hypertextEditor" value="none" <s:if test="systemParams['hypertextEditor'] == 'none'">checked="checked"</s:if> />&#32;
					<s:text name="label.none" />
				</label>
				<label class="btn btn-default <s:if test="systemParams['hypertextEditor'] == 'fckeditor'"> active</s:if>">
					<input type="radio" id="admin-settings-area-hypertextEditor_fckeditor" name="hypertextEditor" value="fckeditor" <s:if test="systemParams['hypertextEditor'] == 'fckeditor'">checked="checked"</s:if> />&#32;
					<s:text name="name.editor.ckeditor" />
				</label>
				<%--
				<label class="btn btn-default <s:if test="systemParams['hypertextEditor'] == 'hoofed'"> active</s:if>">
					<input type="radio" id="admin-settings-area-hypertextEditor_hoofed" name="hypertextEditor" value="hoofed" <s:if test="systemParams['hypertextEditor'] == 'hoofed'">checked="checked"</s:if> />&#32;
					<s:text name="name.editor.hoofed" />
				</label>
				--%>
			</div>
		</div>

		<div class="form-group">
			<label class="display-block"><s:text name="sysconfig.chooseYourPagesTreeStyle" /></label>
			<div class="btn-group" data-toggle="buttons">
				<s:set name="paramName" value="'treeStyle_page'" />
				<s:include value="/WEB-INF/apsadmin/jsp/admin/treeStyleParamBlock.jsp" />
			</div>
		</div>

		<div class="form-group">
			<label class="display-block"><s:text name="sysconfig.chooseYourCategoriesTreeStyle" /></label>
			<div class="btn-group" data-toggle="buttons">
				<s:set name="paramName" value="'treeStyle_category'" />
				<s:include value="/WEB-INF/apsadmin/jsp/admin/treeStyleParamBlock.jsp" />
			</div>
		</div>

	</fieldset>

	<wpsa:hookPoint key="core.configSystemParams" objectName="hookPointElements_core_configSystemParams">
		<s:iterator value="#hookPointElements_core_configSystemParams" var="hookPointElement">
			<wpsa:include value="%{#hookPointElement.filePath}"></wpsa:include>
		</s:iterator>
	</wpsa:hookPoint>

	<div class="form-horizontal">
		<div class="form-group">
			<div class="col-xs-12 col-sm-4 col-md-3 margin-small-vertical">
				<wpsf:submit type="button" cssClass="btn btn-primary btn-block">
					<span class="icon fa fa-floppy-o"></span>&#32;
					<s:text name="label.save" />
				</wpsf:submit>
			</div>
		</div>
	</div>

</s:form>