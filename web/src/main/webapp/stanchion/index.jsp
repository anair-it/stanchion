<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<!DOCTYPE html>
<html id="ng-app" ng-app="stanchion">
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=Edge"/>
	    <meta name="apple-mobile-web-app-capable" content="yes">
	    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">

		<title>${fn:replace(pageContext.request.contextPath, '/', '')}</title>
	
		
        <link rel="stylesheet" href="release/css/vendor.min.css?v=1490317677412"/>
        <link rel="stylesheet" href="release/css/app.min.css?v=1490317677412"/>
         

	

     
	
	</head>

	
	<body ng-controller="StanchionController" ng-cloak>
		<div app-header name="${fn:replace(pageContext.request.contextPath, '/', '')}"></div>
		
		<div ng-include="'template/menu.tmpl.html'"></div>
		
		<div id="app-app-container">
			<div id="alertPanel" alert ng-repeat="alert in alerts" type="{{alert.type}}" class="center-block" close="closeAlert($index)" ng-cloak>
	        	<span ng-bind-html="alert.msg"></span>
	        </div>
	        <div ui-view>
	        </div>
		</div>
		
		
        <script src="release/js/vendor.min.js?v=1490317677412"></script>
        <script src="release/js/app.min.js?v=1490317677412"></script>        
	
	
	
		
		<script>
			stanchion.constant('CONTEXT_PATH', '${pageContext.request.contextPath}');
		</script>
		
		
	</body>
</html>