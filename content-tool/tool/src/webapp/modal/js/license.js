/*
 * SimpleModal Basic Modal Dialog
 * http://www.ericmmartin.com/projects/simplemodal/
 * http://code.google.com/p/simplemodal/
 *
 * Copyright (c) 2008 Eric Martin - http://ericmmartin.com
 *
 * Licensed under the MIT license:
 *   http://www.opensource.org/licenses/mit-license.php
 *
 * Revision: $Id: basic.js 164 2008-12-02 23:31:54Z emartin24 $
 *
 */
$(document).ready(function(){
    $('input#set-license-button, #setLicenseAll').click(function(x){
        x.preventDefault();
        $('#basicModalContent').modal({
            zIndex: 2001,
            position: ["5%", "25%"]
        });
        
        if ($('#licenseHolder').text() !== '') {
            var prevLicense = ($('#licenseHolder').text()).split('-s')[0];
            $('tr#' + prevLicense).addClass("selectedLic")
            $('tr#' + prevLicense).children('td.icon').children('input').attr('checked', 'checked')
        switch (prevLicense) {
            case ('attr'):
                $('#comm_y').attr('checked', 'checked');
                $('#der_y').attr('checked', 'checked');
                $('#comm_y').parent('li').addClass("highlight");
                $('#der_y').parent('li').addClass("highlight");
                break;
            case ('attr_share'):
                $('#comm_y').attr('checked', 'checked');
                $('#der_s').attr('checked', 'checked');
                $('#comm_y').parent('li').addClass("highlight");
                $('#der_s').parent('li').addClass("highlight");
                break;
            case ('attr_noder'):
                $('#comm_y').attr('checked', 'checked');
                $('#der_n').attr('checked', 'checked');
                $('#comm_y').parent('li').addClass("highlight");
                $('#der_n').parent('li').addClass("highlight");
                break;
            case ('attr_nocom'):
                $('#comm_n').attr('checked', 'checked');
                $('#der_y').attr('checked', 'checked');
                $('#comm_n').parent('li').addClass("highlight");
                $('#der_y').parent('li').addClass("highlight");
                break;
            case ('attr_nocom_share'):
                $('#comm_n').attr('checked', 'checked');
                $('#der_s').attr('checked', 'checked');
                $('#comm_n').parent('li').addClass("highlight");
                $('#der_s').parent('li').addClass("highlight");
                break;
            case ('attr_nocom_noder'):
                $('#comm_n').attr('checked', 'checked');
                $('#der_n').attr('checked', 'checked');
                $('#comm_n').parent('li').addClass("highlight");
                $('#der_n').parent('li').addClass("highlight");
              
                break;
                
        }
        };
            });
    $("#helpmelink a").toggle(function(){
        //		$('#simplemodal-container').css('width','600px');
        $('#helper').show('medium');
        $('#licenseInfo').hide();
        $('#helpercont, #helpmelink').addClass('helpercont');
    }, function(){
        $('#helper').hide('fast');
        //		$('#licenseInfo').show();		
        //		$('#simplemodal-container').css('width','300px');
        $('#helpercont,#helpmelink ').removeClass('helpercont');
    });
    $('.icon, .label').click(function(){
        $('.icon').parent('tr').removeClass('selectedLic');
        $(this).parent('tr').addClass('selectedLic');
        $(this).parent('tr').children('td').children('input').attr('checked', 'checked');
        $('#setLicense').attr('disabled', '');
        var whatLicense = $(this).parent('tr').attr('id');
        $('#licenseInfo ul  li').hide();
        $('#licenseInfo').show();
        $('#help-' + whatLicense).show();
		$('#helper input').parent('li').removeClass("highlight");        
        switch (whatLicense) {
            case ('attr'):
                $('#comm_y').attr('checked', 'checked');
                $('#der_y').attr('checked', 'checked');
                $('#comm_y').parent('li').addClass("highlight");
                $('#der_y').parent('li').addClass("highlight");
                break;
            case ('attr_share'):
                $('#comm_y').attr('checked', 'checked');
                $('#der_s').attr('checked', 'checked');
                $('#comm_y').parent('li').addClass("highlight");
                $('#der_s').parent('li').addClass("highlight");
                break;
            case ('attr_noder'):
                $('#comm_y').attr('checked', 'checked');
                $('#der_n').attr('checked', 'checked');
                $('#comm_y').parent('li').addClass("highlight");
                $('#der_n').parent('li').addClass("highlight");
                break;
            case ('attr_nocom'):
                $('#comm_n').attr('checked', 'checked');
                $('#der_y').attr('checked', 'checked');
                $('#comm_n').parent('li').addClass("highlight");
                $('#der_y').parent('li').addClass("highlight");
                break;
            case ('attr_nocom_share'):
                $('#comm_n').attr('checked', 'checked');
                $('#der_s').attr('checked', 'checked');
                $('#comm_n').parent('li').addClass("highlight");
                $('#der_s').parent('li').addClass("highlight");
                break;
            case ('attr_nocom_noder'):
                $('#comm_n').attr('checked', 'checked');
                $('#der_n').attr('checked', 'checked');
                $('#comm_n').parent('li').addClass("highlight");
                $('#der_n').parent('li').addClass("highlight");
              
                break;
                
        }
    });
    $('#helper input').click(function(){
        $('.icon').parent('tr').removeClass('selectedLic');
        var setting = ""
		$('#helper input').parent('li').removeClass("highlight");
		$(this).parent('li').addClass("highlight");			
        $("#helper :checked").each(function(){
            setting = setting + $(this).attr('id');
            $('#setLicense').attr('disabled', '')
        });
        var targetSetting
        
        switch (setting) {
            case ('comm_yder_y'):
                targetSetting = 'attr';
                break;
            case ('comm_yder_s'):
                targetSetting = 'attr_share';
                break;
            case ('comm_yder_n'):
                targetSetting = 'attr_noder';
                break;
            case ('comm_nder_y'):
                targetSetting = 'attr_nocom';
                break
            case ('comm_nder_s'):
                targetSetting = 'attr_nocom_share';
                break
            case ('comm_nder_n'):
                targetSetting = 'attr_nocom_noder';
                break
        }
        $('#' + targetSetting).children('td').children('input').attr('checked', 'checked');
        $('#' + targetSetting).addClass('selectedLic');
        
    });
    //clicking on "Done" in license helper modal
    $('#setLicense').click(function(){
        //what license radio is checked in the license helper
        $('#licenseIcons :checked').each(function(){
        
            //'that' is set when a single item is bneing specified
            if (typeof(that) != "undefined") {
                $(that).val($(this).attr('id')); //set the value of that to id of checked license radio 
                if ($(that).parent('p').children('img.licenseIcon')) {
                    var iconUrl = ($(this).attr('id')).split('-s')[0];
                    $(that).parent('p').children('img.licenseIcon').attr("src", "/sakai-content-tool/images/" + iconUrl + ".png")
                }
                delete that; //undefine the variable
            }
            /*
             otherwise more than one item is being set
             this happens in 2 contexts:
             1) setting licenses on list items
             2 ) setting licenses on all items when creating multiple items
             */
            else {
                $('#licenseHolder').text($(this).attr('id')); // 
                $('.setLicenseVal').each(function(){
                    var iconUrl = $('#licenseHolder').text().split('-s')[0];
                    $(this).parent('p').children('img.licenseIcon').attr("src", "/sakai-content-tool/images/" + iconUrl + ".png")
                    $(this).val($('#licenseHolder').text());
                });
            }
        });
        
    });
    
});
