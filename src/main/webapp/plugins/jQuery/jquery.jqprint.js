/*
* JiaQi
* creatTime:2019-07-03
* phone:17633766096
**/
(function($) {
    var opt;

    $.fn.jqprint = function (options) {
        //使用extend方法合并默认值到该类中默认值，并不修改默认对象，而是产生新对象
        opt = $.extend({}, $.fn.jqprint.defaults, options);
        //判断调用该方法的对象是不是jquery实例对象 是就使用该对象 不是就封装成jquery对象
        var $element = (this instanceof jQuery) ? this : $(this);
        //判断是否是opera浏览器  $.browser.opera自带判断
        if (opt.operaSupport && $.browser.opera) 
        { 
            var tab = window.open("","jqPrint-preview");
            tab.document.open();

            var doc = tab.document;
        }
        else 
        {
            var $iframe = $("<iframe  />");
        
            if (!opt.debug)
            {
                $iframe.css({ position: "absolute", width: "0px", height: "0px", left: "-600px", top: "-600px" });
            }
            //将iframe添加至body中
            $iframe.appendTo("body");
            //获取iframe对象
            var doc = $iframe[0].contentWindow.document;
        }
        
        if (opt.importCSS)
        {
            if ($("link[media=print]").length > 0) 
            {
                $("link[media=print]").each( function() {
                    doc.write("<link type='text/css' rel='stylesheet' href='" + $(this).attr("href") + "' media='print' />");
                });
            }
            else
            {
                $("link").each( function() {
                    doc.write("<link type='text/css' rel='stylesheet' href='" + $(this).attr("href") + "' />");
                });
            }
        }

        //调用打印方法
        if (opt.printContainer)
        {
            doc.write($element.outer());
        }
        else
        {
            $element.each( function() { doc.write($(this).html()); });
        }
        
        doc.close();
        
        (opt.operaSupport && $.browser.opera ? tab : $iframe[0].contentWindow).focus();
        setTimeout( function() {
                (opt.operaSupport && $.browser.opera ? tab : $iframe[0].contentWindow).print();
                if (tab)
                {
                    tab.close();
                }
            }, 1000);
    }
    
    $.fn.jqprint.defaults = {
		debug: false,//如果是true则可以显示iframe查看效果（iframe默认高和宽都很小，可以再源码中调大），默认是false
		importCSS: true, //true表示引进原来的页面的css，默认是true。（如果是true，先会找$("link[media=print]")，若没有会去找$("link")中的css文件）
		printContainer: true,//表示如果原来选择的对象必须被纳入打印（注意：设置为false可能会打破你的CSS规则）。
		operaSupport: true//表示如果插件也必须支持歌opera浏览器，在这种情况下，它提供了建立一个临时的打印选项卡。默认是true
	};

    jQuery.fn.outer = function() {
      return $($('<div></div>').html(this.clone())).html();
    }

})(jQuery);