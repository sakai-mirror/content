/**********************************************************************************
 * $URL:  $
 * $Id:  $
 ***********************************************************************************
 *
 * Copyright (c) 2003, 2004, 2005, 2006 The Sakai Foundation.
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

var popups = new Array();
var locks = new Array();
var addIds = new Array();
var removeIds = new Array();


function openPopup(name, url, title, height) 
{ 
	var params = "height=" + height + ",width=800,scrollbars=yes,menubar=no,toolbar=no,location=no,status=no";
	parent.popups[name] = window.open(url,title,params);
	parent.popups[name].focus();
	return false;
}
function closePopup(name)
{
	if(window.opener && window.opener.parent && window.opener.parent.popups && window.opener.parent.popups[name])
	{
		window.opener.parent.popups[name].close();
	}
}
function setPopupHeight(name)
{
	var frame = window.opener.parent.popups[name];

	if (frame)
	{
		// reset the scroll
		frame.window.scrollTo(0,0);

		var height; 
		
		var scrollH = document.body.scrollHeight;
		var offsetH = document.body.offsetHeight;
		var clientH = document.body.clientHeight;
		var innerDocScrollH = null;

		if (typeof(frame.contentDocument) != 'undefined' || typeof(frame.contentWindow) != 'undefined')
		{
			// very special way to get the height from IE on Windows!
			// note that the above special way of testing for undefined variables is necessary for older browsers
			// (IE 5.5 Mac) to not choke on the undefined variables.
 			var innerDoc = (frame.contentDocument) ? frame.contentDocument : frame.contentWindow.document;
			innerDocScrollH = (innerDoc != null) ? innerDoc.body.scrollHeight : null;
		}

		if (document.all && innerDocScrollH != null)
		{
			// IE on Windows only
			height = innerDocScrollH;
		}
		else
		{
			// every other browser!
			height = offsetH;
		}

		// here we fudge to get a little bigger
		var newHeight = height + 100;
		if(newHeight > 1000)
		{
			newHeight = 1000;
		}
		frame.resizeTo(800, newHeight);
	}

}
function submitform(id)
{
	var theForm = document.getElementById(id);
	if(theForm && theForm.onsubmit)
	{
		theForm.onsubmit();
	}
	if(theForm && theForm.submit)
	{
		theForm.submit();
	}
}
function addCitation(citationId)
{
	addIds.push(citationId);
	sendMessage('add');
	
	var removeLabel = document.getElementById('removeLabel').value;
	var span = document.getElementById("save_" + citationId);
	var span_str = "<a href=\"#\" onclick=\"removeCitation('" + citationId + "');\">" + removeLabel + "\n</a>\n";
	span.innerHTML = span_str;
}
function removeCitation(citationId)
{
	removeIds.push(citationId);
	sendMessage('remove');
	
	var addLabel = document.getElementById('addLabel').value;
	var span = document.getElementById("save_" + citationId);
	var span_str = "<a href=\"#\" onclick=\"addCitation('" + citationId + "');\">" + addLabel + "\n</a>\n";
	span.innerHTML = span_str;
}
function sendMessage(msg)
{
	if(addIds.length > 0 || removeIds.length > 0)
	{
		var scratch = window.opener.parent.locks.pop();
		if(scratch)
		{
			if(msg == 'add')
			{
				addCitations(scratch);
			}
			else if(msg == 'remove')
			{
				removeCitations(scratch);
			}
		}
		else
		{
			window.setTimeout('sendMessage("' + msg + '");', 250);
		}
	}
}
function addCitations(scratch)
{
	var collectionId = document.getElementById('collectionId').value;
	var actionUrl = document.getElementById('actionUrl').value;
	
	var form_str = "<form name=\"addForm\" id=\"addForm\" method=\"post\" action=\"" + actionUrl + "&sakai_action=doAdd\">\n";
	
	var citationId = addIds.pop();
	while(citationId)
	{
		form_str += "<input type=\"hidden\" name=\"citationId\" id=\"citationId\" value=\"" + citationId + "\" />\n";
		citationId = addIds.pop();
	}
	
	form_str += "<input type=\"hidden\" name=\"collectionId\" id=\"collectionId\" value=\"" + collectionId + "\" />\n";
	form_str += "</form>\n";
	scratch.innerHTML = form_str;
	window.opener.submitform("addForm");	
}
function removeCitations(scratch)
{
	var collectionId = document.getElementById('collectionId').value;
	var actionUrl = document.getElementById('actionUrl').value;
	
	var form_str = "<form name=\"removeForm\" id=\"removeForm\" method=\"post\" action=\"" + actionUrl + "&sakai_action=doRemove\">\n";
	
	var citationId = removeIds.pop();
	while(citationId)
	{
		form_str += "<input type=\"hidden\" name=\"citationId\" id=\"citationId\" value=\"" + citationId + "\" />\n";
		citationId = removeIds.pop();
	}
	
	form_str += "<input type=\"hidden\" name=\"collectionId\" id=\"collectionId\" value=\"" + collectionId + "\" />\n";
	form_str += "</form>\n";
	scratch.innerHTML = form_str;
	window.opener.submitform("removeForm");	
}
function closePopupAndSubmitFormFromOpener(form_id)
{
	var form_obj = document.getElementById(form_id);
	var form_str = "<form name=\"" + form_id + "\" id=\"" + form_id + "\" method=\"post\" action=\"" + form_obj.action + "\">\n";
	for(var i = 0; i < form_obj.elements.length; i++)
	{
		if(form_obj.elements[i].value && form_obj.elements[i].type != "button" && form_obj.elements[i].type != "cancel" && form_obj.elements[i].type != "submit")
		{
			form_str += "<input type=\"hidden\" name=\"" + form_obj.elements[i].name + "\" value=\"" + form_obj.elements[i].value + "\" />\n";
		}
	}
	form_str += "</form>\n";

	window.opener.submitFormAndClosePopup(form_str, form_id, window);
}
function submitFormAndClosePopup(form_str, form_id, popup)
{
	var scratch_div = document.getElementById("scratch_space");
	scratch_div.innerHTML = form_str;
	popup.close();
	var form_obj = document.getElementById(form_id);
	form_obj.submit();
}
function checkinWithOpener(name)
{
	if(window.opener && window.opener.parent && window.opener.parent.popups && window.opener.parent.popups)
	{
		setTimeout("checkinWithOpener(\"" + name + "\");", 60000); 
	}
	else
	{
		window.close();
		//window.focus();
		//document.getElementById("hiddenMessage").style.display = "block";
	}
}


