function openCopyrightWindow(theURL,winName,winSettings) 
{ 
  window.open(theURL,winName,winSettings);
  return false;
}

function adjustCount(caller, key)
{
	var counter = document.getElementById(key + "-count");
	var button = document.getElementById(key + "-button");
	
	if(caller && caller.checked && caller.checked == true)
	{
		counter.value = parseInt(counter.value) + 1;
	}
	else
	{
		counter.value = parseInt(counter.value) - 1;
	}

	if(button)
	{
		if(counter.value > 0)
		{
			button.disabled = false;
			button.className='enabled';
		}
		else
		{
			button.disabled = true;
			button.className='disabled';
		}
	}
}

function disableLinks()
{
	if(document.getElementsByName)
	{
		var enabledLinks = document.getElementsByName("enabledActionLinks");
		var disabledLinks = document.getElementsByName("disabledActionLinks");
		if(enabledLinks)
		{
			for(var i = 0; i < enabledLinks.length; i++)
			{
				//enabledLinks[i].innerHTML = disabledLinks[i].innerHTML;
				enabledLinks[i].style.display="none";
				disabledLinks[i].style.display="block";
			}
		}			
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
function enableActionMenus()
{
	jQuery('.portletBody').click(function(e) { 
		if ( e.target.className !='menuOpen' &&e.target.className !='dropdn'  ){
			$('.makeMenuChild').hide();
		}
		else
		{
			if( e.target.className =='dropdn' ){
				targetId=$(e.target).parent('li').attr('id');
				$('.makeMenuChild').hide();
				$('#menu-' + targetId).show();
			} else {
				targetId=e.target.id;
				$('.makeMenuChild').hide();
				$('#menu-' + targetId).show();
			}
		}
	});
}

/*
 utilities
 */
 var utils = utils || {};
/*
 initialize a jQuery-UI dialog
 */
utils.startDialog = function (dialogTarget){
    $("#" + dialogTarget).dialog({
        close: function(event, ui){
            utils.resizeFrame('shrink');
        },
        autoOpen: false,
        modal: true,
        height: 410,
        maxHeight: 450,
        width: 500,
        draggable: true,
        closeOnEscape: true
    });
}
/*
 position, open a jQuery-UI dialog, adjust the parent iframe size if any
 */
utils.endDialog = function(ev, dialogTarget){
    var frame;
    if (top.location !== self.location) {
        frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        var clientH = document.body.clientHeight + 450;
        $(frame).height(clientH);
    }
	var position = $(ev.target).parents('tr').position();
	$('.makeMenuChild').hide();
    
    $("#" + dialogTarget).dialog('option', 'position', [100, position.top + 10]);
    $("#" + dialogTarget).dialog("open");
};
/*
 resize the iframe based on the contained document height.
 used after DOM operations that add or substract to the doc height
 */
utils.resizeFrame = function(updown){
    var clientH;
    if (top.location !== self.location) {
        var frame = parent.document.getElementById(window.name);
    }
    if (frame) {
        if (updown === 'shrink') {
            clientH = document.body.clientHeight;
        }
        else {
            clientH = document.body.clientHeight + 50;
        }
        $(frame).height(clientH);
    }
    else {
        // throw( "resizeFrame did not get the frame (using name=" + window.name + ")" );
    }
};

