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

