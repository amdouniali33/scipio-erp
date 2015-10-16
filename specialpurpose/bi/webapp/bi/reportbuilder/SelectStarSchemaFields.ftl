<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<form action="<@ofbizUrl>ReportBuilderRenderStarSchemaReport</@ofbizUrl>">
    <input type="hidden" name="starSchemaName" value="${starSchemaName}"/>
    <@table type="data-list" autoAltRows=true cellspacing="0" class="${styles.table!} hover-bar">
      <@thead>    
        <@tr class="header-row">
            <@th>
                ${uiLabelMap.CommonSelect}
            </@th>
            <@th>
                ${uiLabelMap.BusinessIntelligenceFieldName}
            </@th>
            <@th>
                ${uiLabelMap.BusinessIntelligenceFieldDescription}
            </@th>
        </@tr>
      </@thead>        
        <#list starSchemaFields as starSchemaField>
        <@tr valign="middle">
            <@td>
                <input name="selectedFieldName_o_${starSchemaField_index}" value="${starSchemaField.name}" type="hidden"/>
                <input name="_rowSubmit_o_${starSchemaField_index}" value="Y" type="checkbox"/>
            </@td>
            <@td>
                ${starSchemaField.name}
            </@td>
            <@td>
                ${starSchemaField.description?default("")}
            </@td>
        </@tr>
        </#list>
      <@tfoot>
        <@tr>
            <@td colspan="3">
                <input name="submitButton" type="submit" value="${uiLabelMap.BusinessIntelligenceRenderTheReport}"/>
            </@td>
        </@tr>
      </@tfoot>
    </@table>
</form>
